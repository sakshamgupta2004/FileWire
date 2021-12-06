package org.swiftp;

import android.content.SharedPreferences.Editor;

import com.sugarsnooper.filetransfer.ConnectToPC.FTP.FTPServerService;

import org.json.JSONException;
import org.json.JSONObject;
import org.swiftp.SessionThread.Source;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ProxyConnector extends Thread {
    public static final int CONNECT_TIMEOUT = 5000;
    public static final String ENCODING = "UTF-8";
    public static final int IN_BUF_SIZE = 2048;
    public static final String PREFERRED_SERVER = "preferred_server";
    public static final int QUEUE_WAIT_MS = 20000;
    public static final int RESPONSE_WAIT_MS = 10000;
    public static final long UPDATE_USAGE_BYTES = 5000000;
    static final String USAGE_PREFS_NAME = "proxy_usage_data";
    private Socket commandSocket = null;
    private FTPServerService ftpServerService;
    private String hostname = null;
    private InputStream inputStream = null;
    private MyLog myLog = new MyLog(getClass().getName());
    private OutputStream out = null;
    private String prefix;
    private String proxyMessage = null;
    private State proxyState = State.DISCONNECTED;
    private long proxyUsage = 0;
    private Queue<Thread> queuedRequestThreads = new LinkedList();
    private JSONObject response = null;
    private Thread responseWaiter = null;

    public enum State {
        CONNECTING,
        CONNECTED,
        FAILED,
        UNREACHABLE,
        DISCONNECTED
    }

    public ProxyConnector(FTPServerService ftpServerService2) {
        this.ftpServerService = ftpServerService2;
        this.proxyUsage = getPersistedProxyUsage();
        setProxyState(State.DISCONNECTED);
        Globals.setProxyConnector(this);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        r14.prefix = r14.response.getString("prefix");
        r14.response = null;
        r14.myLog.l(4, "Got prefix of: " + r14.prefix);
     */
    public void run() {
        int i = 0;
        this.myLog.i("In ProxyConnector.run()");
        setProxyState(State.CONNECTING);
        try {
            String[] candidateProxies = getProxyList();
            int length = candidateProxies.length;
            while (true) {
                if (i >= length) {
                    break;
                }
                this.hostname = candidateProxies[i];
                this.commandSocket = newAuthedSocket(this.hostname, Defaults.REMOTE_PROXY_PORT);
                if (this.commandSocket != null) {
                    this.commandSocket.setSoTimeout(0);
                    this.response = sendRequest(this.commandSocket, makeJsonRequest("start_command_session"));
                    if (this.response != null) {
                        if (this.response.has("prefix")) {
                            break;
                        }
                        this.myLog.l(4, "start_command_session didn't receive a prefix in response");
                    } else {
                        this.myLog.i("Couldn't create proxy command session");
                    }
                }
                i++;
            }
            if (this.commandSocket == null) {
                this.myLog.l(4, "No proxies accepted connection, failing.");
                setProxyState(State.UNREACHABLE);
                return;
            }
            setProxyState(State.CONNECTED);
            preferServer(this.hostname);
            this.inputStream = this.commandSocket.getInputStream();
            this.out = this.commandSocket.getOutputStream();
            byte[] bytes = new byte[IN_BUF_SIZE];
            while (true) {
                this.myLog.d("to proxy read()");
                int numBytes = this.inputStream.read(bytes);
                incrementProxyUsage((long) numBytes);
                this.myLog.d("from proxy read()");
                if (numBytes <= 0) {
                    if (numBytes != 0) {
                        break;
                    }
                    this.myLog.d("Command socket read 0 bytes, looping");
                } else {
                    JSONObject incomingJson = new JSONObject(new String(bytes, "UTF-8"));
                    if (incomingJson.has("action")) {
                        incomingCommand(incomingJson);
                    } else if (this.responseWaiter != null) {
                        if (this.response != null) {
                            this.myLog.l(4, "Overwriting existing cmd session response");
                        }
                        this.response = incomingJson;
                        this.responseWaiter.interrupt();
                    } else {
                        this.myLog.l(4, "Response received but no responseWaiter");
                    }
                }
            }
            this.myLog.l(3, "Command socket end of stream, exiting");
            if (this.proxyState != State.DISCONNECTED) {
                setProxyState(State.FAILED);
            }
            this.myLog.l(4, "ProxyConnector thread quitting cleanly");
            Globals.setProxyConnector(null);
            this.hostname = null;
            this.myLog.d("ProxyConnector.run() returning");
            persistProxyUsage();
        } catch (IOException e) {
            this.myLog.l(4, "IOException in command session: " + e);
            setProxyState(State.FAILED);
        } catch (JSONException e2) {
            this.myLog.l(4, "Commmand socket JSONException: " + e2);
            setProxyState(State.FAILED);
        } catch (Exception e3) {
            this.myLog.l(4, "Other exception in ProxyConnector: " + e3);
            setProxyState(State.FAILED);
        } finally {
            Globals.setProxyConnector(null);
            this.hostname = null;
            this.myLog.d("ProxyConnector.run() returning");
            persistProxyUsage();
        }
    }

    private void preferServer(String hostname2) {
        Editor editor = Globals.getContext().getSharedPreferences(PREFERRED_SERVER, 0).edit();
        editor.putString(PREFERRED_SERVER, hostname2);
        editor.commit();
    }

    private String[] getProxyList() {
        String preferred = Globals.getContext().getSharedPreferences(PREFERRED_SERVER, 0).getString(PREFERRED_SERVER, null);
        List<String> proxyList = Arrays.asList(new String[]{"c1.swiftp.org", "c2.swiftp.org", "c3.swiftp.org", "c4.swiftp.org", "c5.swiftp.org", "c6.swiftp.org", "c7.swiftp.org", "c8.swiftp.org", "c9.swiftp.org"});
        Collections.shuffle(proxyList);
        String[] allProxies = (String[]) proxyList.toArray(new String[0]);
        if (preferred == null) {
            return allProxies;
        }
        return Util.concatStrArrays(new String[]{preferred}, allProxies);
    }

    private boolean checkAndPrintJsonError(JSONObject json) throws JSONException {
        if (!json.has("error_code")) {
            return false;
        }
        StringBuilder s = new StringBuilder("Error in JSON response, code: ");
        s.append(json.getString("error_code"));
        if (json.has("error_string")) {
            s.append(", string: ");
            s.append(json.getString("error_string"));
        }
        this.myLog.l(4, s.toString());
        return true;
    }

    private void incomingCommand(JSONObject json) {
        try {
            String action = json.getString("action");
            if (action.equals("control_connection_waiting")) {
                startControlSession(json.getInt("port"));
            } else if (action.equals("prefer_server")) {
                String host = json.getString("host");
                preferServer(host);
                this.myLog.i("New preferred server: " + host);
            } else if (action.equals("message")) {
                this.proxyMessage = json.getString("text");
                this.myLog.i("Got news from proxy server: \"" + this.proxyMessage + "\"");
                FTPServerService.updateClients();
            } else if (action.equals("noop")) {
                this.myLog.d("Proxy noop");
            } else {
                this.myLog.l(4, "Unsupported incoming action: " + action);
            }
        } catch (JSONException e) {
            this.myLog.l(4, "JSONException in proxy incomingCommand");
        }
    }

    private void startControlSession(int port) {
        this.myLog.d("Starting new proxy FTP control session");
        Socket socket = newAuthedSocket(this.hostname, port);
        if (socket == null) {
            this.myLog.i("startControlSession got null authed socket");
            return;
        }
        SessionThread thread = new SessionThread(socket, new ProxyDataSocketFactory(), Source.PROXY);
        thread.start();
        this.ftpServerService.registerSessionThread(thread);
    }

    private Socket newAuthedSocket(String hostname2, int port) {
        if (hostname2 == null) {
            this.myLog.i("newAuthedSocket can't connect to null host");
            return null;
        }
        JSONObject json = new JSONObject();
        try {
            this.myLog.d("Opening proxy connection to " + hostname2 + ":" + port);
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(hostname2, port), CONNECT_TIMEOUT);
            json.put("android_id", Util.getAndroidId());
            json.put("swiftp_version", Util.getVersion());
            json.put("action", "login");
            OutputStream out2 = socket.getOutputStream();
            InputStream in = socket.getInputStream();
            out2.write(json.toString().getBytes("UTF-8"));
            this.myLog.l(3, "Sent login request");
            byte[] bytes = new byte[IN_BUF_SIZE];
            int numBytes = in.read(bytes);
            if (numBytes == -1) {
                this.myLog.l(4, "Proxy socket closed while waiting for auth response");
                return null;
            } else if (numBytes == 0) {
                this.myLog.l(4, "Short network read waiting for auth, quitting");
                return null;
            } else {
                JSONObject json2 = new JSONObject(new String(bytes, 0, numBytes, "UTF-8"));
                try {
                    if (checkAndPrintJsonError(json2)) {
                        return null;
                    }
                    this.myLog.d("newAuthedSocket successful");
                    return socket;
                } catch (Exception e) {
                    e = e;
                    JSONObject jSONObject = json2;
                    this.myLog.i("Exception during proxy connection or authentication: " + e);
                    return null;
                }
            }
        } catch (Exception e2) {
            Exception e = e2;
            this.myLog.i("Exception during proxy connection or authentication: " + e);
            return null;
        }
    }

    public void quit() {
        setProxyState(State.DISCONNECTED);
        try {
            sendRequest(this.commandSocket, makeJsonRequest("finished"));
            if (this.inputStream != null) {
                this.myLog.d("quit() closing proxy inputStream");
                this.inputStream.close();
            } else {
                this.myLog.d("quit() won't close null inputStream");
            }
            if (this.commandSocket != null) {
                this.myLog.d("quit() closing proxy socket");
                this.commandSocket.close();
            } else {
                this.myLog.d("quit() won't close null socket");
            }
        } catch (IOException e) {
        } catch (JSONException e2) {
        }
        persistProxyUsage();
        Globals.setProxyConnector(null);
    }

    private JSONObject sendCmdSocketRequest(JSONObject json) throws Throwable {
        boolean queued;
        try {
            synchronized (this) {
                if (this.responseWaiter == null) {
                    this.responseWaiter = Thread.currentThread();
                    queued = false;
                    this.myLog.d("sendCmdSocketRequest proceeding without queue");
                } else if (!this.responseWaiter.isAlive()) {
                    this.myLog.l(4, "Won't wait on dead responseWaiter");
                    if (this.queuedRequestThreads.size() == 0) {
                        this.responseWaiter = Thread.currentThread();
                        queued = false;
                    } else {
                        this.queuedRequestThreads.add(Thread.currentThread());
                        ((Thread) this.queuedRequestThreads.remove()).interrupt();
                        queued = true;
                    }
                } else {
                    this.myLog.d("sendCmdSocketRequest queueing thread");
                    this.queuedRequestThreads.add(Thread.currentThread());
                    queued = true;
                }
            }
            if (queued) {
                boolean interrupted = false;
                try {
                    this.myLog.d("Queued cmd session request thread sleeping...");
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    this.myLog.l(3, "Proxy request popped and ready");
                    interrupted = true;
                }
                if (!interrupted) {
                    this.myLog.l(4, "Timed out waiting on proxy queue");
                    return null;
                }
            }
            try {
                this.responseWaiter = Thread.currentThread();
                this.out.write(Util.jsonToByteArray(json));
                boolean interrupted2 = false;
                try {
                    this.myLog.d("Cmd session request sleeping until response");
                    Thread.sleep(10000);
                } catch (InterruptedException e2) {
                    this.myLog.d("Cmd session response received");
                    interrupted2 = true;
                }
                if (!interrupted2) {
                    this.myLog.l(4, "Proxy request timed out");
                    synchronized (this) {
                        if (this.queuedRequestThreads.size() != 0) {
                            ((Thread) this.queuedRequestThreads.remove()).interrupt();
                        }
                    }
                    return null;
                }
                this.myLog.d("Cmd session response was: " + this.response);
                JSONObject jSONObject = this.response;
                synchronized (this) {
                    if (this.queuedRequestThreads.size() != 0) {
                        ((Thread) this.queuedRequestThreads.remove()).interrupt();
                    }
                }
                return jSONObject;
            } catch (IOException e3) {
                this.myLog.l(4, "IOException sending proxy request");
                synchronized (this) {
                    if (this.queuedRequestThreads.size() != 0) {
                        ((Thread) this.queuedRequestThreads.remove()).interrupt();
                    }
                    return null;
                }
            } catch (Throwable th) {
                Throwable th2 = th;
                synchronized (this) {
                    if (this.queuedRequestThreads.size() != 0) {
                        ((Thread) this.queuedRequestThreads.remove()).interrupt();
                    }
                    throw th2;
                }
            }
        } catch (JSONException e4) {
            this.myLog.l(4, "JSONException in sendRequest: " + e4);
            return null;
        }
    }

    public JSONObject sendRequest(InputStream in, OutputStream out2, JSONObject request) throws JSONException {
        try {
            out2.write(Util.jsonToByteArray(request));
            byte[] bytes = new byte[IN_BUF_SIZE];
            if (in.read(bytes) < 1) {
                this.myLog.i("Proxy sendRequest short read on response");
                return null;
            }
            JSONObject response2 = Util.byteArrayToJson(bytes);
            if (response2 == null) {
                this.myLog.i("Null response to sendRequest");
            }
            if (!checkAndPrintJsonError(response2)) {
                return response2;
            }
            this.myLog.i("Error response to sendRequest");
            return null;
        } catch (IOException e) {
            this.myLog.i("IOException in proxy sendRequest: " + e);
            return null;
        }
    }

    public JSONObject sendRequest(Socket socket, JSONObject request) throws JSONException, IOException {
        if (socket != null) {
            return sendRequest(socket.getInputStream(), socket.getOutputStream(), request);
        }
        this.myLog.i("null socket in ProxyConnector.sendRequest()");
        return null;
    }

    public ProxyDataSocketInfo pasvListen() {
        try {
            this.myLog.d("Sending data_pasv_listen to proxy");
            Socket socket = newAuthedSocket(this.hostname, Defaults.REMOTE_PROXY_PORT);
            if (socket == null) {
                this.myLog.i("pasvListen got null socket");
                return null;
            }
            JSONObject response2 = sendRequest(socket, makeJsonRequest("data_pasv_listen"));
            if (response2 != null) {
                return new ProxyDataSocketInfo(socket, response2.getInt("port"));
            }
            return null;
        } catch (JSONException | IOException e) {
            this.myLog.l(4, "JSONException in pasvListen");
            return null;
        }
    }

    public Socket dataPortConnect(InetAddress clientAddr, int clientPort) {
        try {
            this.myLog.d("Sending data_port_connect to proxy");
            Socket socket = newAuthedSocket(this.hostname, Defaults.REMOTE_PROXY_PORT);
            if (socket == null) {
                this.myLog.i("dataPortConnect got null socket");
                return null;
            }
            JSONObject request = makeJsonRequest("data_port_connect");
            request.put("address", clientAddr.getHostAddress());
            request.put("port", clientPort);
            if (sendRequest(socket, request) == null) {
                return null;
            }
            return socket;
        } catch (JSONException | IOException e) {
            this.myLog.i("JSONException in dataPortConnect");
            return null;
        }
    }

    public boolean pasvAccept(Socket socket) {
        try {
            JSONObject response2 = sendRequest(socket, makeJsonRequest("data_pasv_accept"));
            if (response2 == null) {
                return false;
            }
            if (checkAndPrintJsonError(response2)) {
                this.myLog.i("Error response to data_pasv_accept");
                return false;
            }
            this.myLog.d("Proxy data_pasv_accept successful");
            return true;
        } catch (JSONException | IOException e) {
            this.myLog.i("JSONException in pasvAccept: " + e);
            return false;
        }
    }

    public InetAddress getProxyIp() {
        if (!isAlive() || !this.commandSocket.isConnected()) {
            return null;
        }
        return this.commandSocket.getInetAddress();
    }

    private JSONObject makeJsonRequest(String action) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("action", action);
        return json;
    }

    /* access modifiers changed from: 0000 */
    public void persistProxyUsage() {
        if (this.proxyUsage != 0) {
            Editor editor = Globals.getContext().getSharedPreferences(USAGE_PREFS_NAME, 0).edit();
            editor.putLong(USAGE_PREFS_NAME, this.proxyUsage);
            editor.commit();
            this.myLog.d("Persisted proxy usage to preferences");
        }
    }

    /* access modifiers changed from: 0000 */
    public long getPersistedProxyUsage() {
        return Globals.getContext().getSharedPreferences(USAGE_PREFS_NAME, 0).getLong(USAGE_PREFS_NAME, 0);
    }

    public long getProxyUsage() {
        return this.proxyUsage;
    }

    /* access modifiers changed from: 0000 */
    public void incrementProxyUsage(long num) {
        long oldProxyUsage = this.proxyUsage;
        this.proxyUsage += num;
        if (this.proxyUsage % UPDATE_USAGE_BYTES < oldProxyUsage % UPDATE_USAGE_BYTES) {
            FTPServerService.updateClients();
            persistProxyUsage();
        }
    }

    public State getProxyState() {
        return this.proxyState;
    }

    private void setProxyState(State state) {
        this.proxyState = state;
        this.myLog.l(3, "Proxy state changed to " + state, true);
        FTPServerService.updateClients();
    }

    public static String stateToString(State s) {
        return "";
    }

    public String getURL() {
        if (this.proxyState == State.CONNECTED) {
            String username = Globals.getUsername();
            if (username != null) {
                return "ftp://" + this.prefix + "_" + username + "@" + this.hostname;
            }
        }
        return "";
    }

    public String getProxyMessage() {
        return this.proxyMessage;
    }
}
