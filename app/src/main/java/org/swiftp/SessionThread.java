package org.swiftp;

import com.sugarsnooper.filetransfer.ConnectToPC.FTP.FTPServerService;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

public class SessionThread extends Thread {
    public static int MAX_AUTH_FAILS = 3;
    static int numNulls = 0;
    protected Account account = new Account();
    int authFails = 0;
    protected boolean authenticated = false;
    protected boolean binaryMode = false;
    protected ByteBuffer buffer = ByteBuffer.allocate(Defaults.getInputBufferSize());
    protected Socket cmdSocket;
    OutputStream dataOutputStream = null;
    protected Socket dataSocket = null;
    protected DataSocketFactory dataSocketFactory;
    protected String encoding = "UTF-8";
    protected MyLog myLog = new MyLog(getClass().getName());
    protected boolean pasvMode = false;
    protected File renameFrom = null;
    private boolean sendWelcomeBanner;
    protected boolean shouldExit = false;
    protected Source source;
    protected File workingDir = Globals.getChrootDir();

    public enum Source {
        LOCAL,
        PROXY
    }

    public boolean sendViaDataSocket(String string) {
        try {
            byte[] bytes = string.getBytes(this.encoding);
            this.myLog.d("Using data connection encoding: " + this.encoding);
            return sendViaDataSocket(bytes, bytes.length);
        } catch (UnsupportedEncodingException e) {
            this.myLog.l(6, "Unsupported encoding for data socket send");
            return false;
        }
    }

    public boolean sendViaDataSocket(byte[] bytes, int len) {
        return sendViaDataSocket(bytes, 0, len);
    }

    public boolean sendViaDataSocket(byte[] bytes, int start, int len) {
        if (this.dataOutputStream == null) {
            this.myLog.l(4, "Can't send via null dataOutputStream");
            return false;
        } else if (len == 0) {
            return true;
        } else {
            try {
                this.dataOutputStream.write(bytes, start, len);
                this.dataSocketFactory.reportTraffic((long) len);
                return true;
            } catch (IOException e) {
                this.myLog.l(4, "Couldn't write output stream for data socket");
                this.myLog.l(4, e.toString());
                return false;
            }
        }
    }

    public int receiveFromDataSocket(byte[] buf) {
        int bytesRead;
        if (this.dataSocket == null) {
            this.myLog.l(4, "Can't receive from null dataSocket");
            return -2;
        } else if (!this.dataSocket.isConnected()) {
            this.myLog.l(4, "Can't receive from unconnected socket");
            return -2;
        } else {
            try {
                InputStream in = this.dataSocket.getInputStream();
                do {
                    bytesRead = in.read(buf, 0, buf.length);
                } while (bytesRead == 0);
                if (bytesRead == -1) {
                    return -1;
                }
                this.dataSocketFactory.reportTraffic((long) bytesRead);
                return bytesRead;
            } catch (IOException e) {
                this.myLog.l(4, "Error reading data socket");
                return 0;
            }
        }
    }

    public int onPasv() {
        return this.dataSocketFactory.onPasv();
    }

    public boolean onPort(InetAddress dest, int port) {
        return this.dataSocketFactory.onPort(dest, port);
    }

    public InetAddress getDataSocketPasvIp() {
        return this.cmdSocket.getLocalAddress();
    }

    public boolean startUsingDataSocket() {
        try {
            this.dataSocket = this.dataSocketFactory.onTransfer();
            if (this.dataSocket == null) {
                this.myLog.l(4, "dataSocketFactory.onTransfer() returned null");
                return false;
            }
            this.dataOutputStream = this.dataSocket.getOutputStream();
            return true;
        } catch (IOException e) {
            this.myLog.l(4, "IOException getting OutputStream for data socket");
            this.dataSocket = null;
            return false;
        }
    }

    public void quit() {
        this.myLog.d("SessionThread told to quit");
        closeSocket();
    }

    public void closeDataSocket() {
        this.myLog.l(3, "Closing data socket");
        if (this.dataOutputStream != null) {
            try {
                this.dataOutputStream.close();
            } catch (IOException e) {
            }
            this.dataOutputStream = null;
        }
        if (this.dataSocket != null) {
            try {
                this.dataSocket.close();
            } catch (IOException e2) {
            }
        }
        this.dataSocket = null;
    }

    /* access modifiers changed from: protected */
    public InetAddress getLocalAddress() {
        return this.cmdSocket.getLocalAddress();
    }

    public void run() {
        this.myLog.l(4, "SessionThread started");
        if (this.sendWelcomeBanner) {
            writeString("220 SwiFTP " + Util.getVersion() + " ready\r\n");
        }
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.cmdSocket.getInputStream()), 8192);
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                FTPServerService.writeMonitor(true, line);
                this.myLog.l(3, "Received line from client: " + line);
                FtpCmd.dispatchCommand(this, line);
            }
            this.myLog.i("readLine gave null, quitting");
        } catch (IOException e) {
            this.myLog.l(4, "Connection was dropped");
        }
        closeSocket();
    }

    public static boolean compareLen(byte[] array1, byte[] array2, int len) {
        for (int i = 0; i < len; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        return true;
    }

    public void closeSocket() {
        if (this.cmdSocket != null) {
            try {
                this.cmdSocket.close();
            } catch (IOException e) {
            }
        }
    }

    public void writeBytes(byte[] bytes) {
        try {
            BufferedOutputStream out = new BufferedOutputStream(this.cmdSocket.getOutputStream(), Defaults.dataChunkSize);
            out.write(bytes);
            out.flush();
            this.dataSocketFactory.reportTraffic((long) bytes.length);
        } catch (IOException e) {
            this.myLog.l(4, "Exception writing socket");
            closeSocket();
        }
    }

    public void writeString(String str) {
        byte[] strBytes;
        FTPServerService.writeMonitor(false, str);
        try {
            strBytes = str.getBytes(this.encoding);
        } catch (UnsupportedEncodingException e) {
            this.myLog.e("Unsupported encoding: " + this.encoding);
            strBytes = str.getBytes();
        }
        writeBytes(strBytes);
    }

    /* access modifiers changed from: protected */
    public Socket getSocket() {
        return this.cmdSocket;
    }

    public Account getAccount() {
        return this.account;
    }

    public void setAccount(Account account2) {
        this.account = account2;
    }

    public boolean isPasvMode() {
        return this.pasvMode;
    }

    public SessionThread(Socket socket, DataSocketFactory dataSocketFactory2, Source source2) {
        this.cmdSocket = socket;
        this.source = source2;
        this.dataSocketFactory = dataSocketFactory2;
        if (source2 == Source.LOCAL) {
            this.sendWelcomeBanner = true;
        } else {
            this.sendWelcomeBanner = false;
        }
    }

    public static ByteBuffer stringToBB(String s) {
        return ByteBuffer.wrap(s.getBytes());
    }

    public boolean isBinaryMode() {
        return this.binaryMode;
    }

    public void setBinaryMode(boolean binaryMode2) {
        this.binaryMode = binaryMode2;
    }

    public boolean isAuthenticated() {
        return this.authenticated;
    }

    public void authAttempt(boolean authenticated2) {
        if (authenticated2) {
            this.myLog.l(4, "Authentication complete");
            this.authenticated = true;
            return;
        }
        if (this.source == Source.PROXY) {
            quit();
        } else {
            this.authFails++;
            this.myLog.i("Auth failed: " + this.authFails + "/" + MAX_AUTH_FAILS);
        }
        if (this.authFails > MAX_AUTH_FAILS) {
            this.myLog.i("Too many auth fails, quitting session");
            quit();
        }
    }

    public File getWorkingDir() {
        return this.workingDir;
    }

    public void setWorkingDir(File workingDir2) {
        try {
            this.workingDir = workingDir2.getCanonicalFile().getAbsoluteFile();
        } catch (IOException e) {
            this.myLog.l(4, "SessionThread canonical error");
        }
    }

    public Socket getDataSocket() {
        return this.dataSocket;
    }

    public void setDataSocket(Socket dataSocket2) {
        this.dataSocket = dataSocket2;
    }

    public File getRenameFrom() {
        return this.renameFrom;
    }

    public void setRenameFrom(File renameFrom2) {
        this.renameFrom = renameFrom2;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public void setEncoding(String encoding2) {
        this.encoding = encoding2;
    }
}
