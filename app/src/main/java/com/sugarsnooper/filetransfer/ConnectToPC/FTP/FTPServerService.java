package com.sugarsnooper.filetransfer.ConnectToPC.FTP;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.sugarsnooper.filetransfer.NetworkManagement;
import com.sugarsnooper.filetransfer.R;

import org.swiftp.Defaults;
import org.swiftp.Globals;
import org.swiftp.MyLog;
import org.swiftp.ProxyConnector;
import org.swiftp.SessionThread;
import org.swiftp.TcpListener;
import org.swiftp.UiUpdater;
import org.swiftp.Util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FTPServerService extends Service implements Runnable {
    public static final int BACKLOG = 21;
    public static final int MAX_SESSIONS = 5;
    public static final int WAKE_INTERVAL_MS = 1000;
    public static final String WAKE_LOCK_TAG = "SwiFTP";
    protected static boolean acceptNet;
    protected static boolean acceptWifi;
    private static boolean debug = true;
    protected static boolean fullWake;
    protected static int port;
    protected static List<String> serverLog = new ArrayList();
    public static Thread serverThread = null;
    protected static List<String> sessionMonitor = new ArrayList();
    private static SharedPreferences settings = null;
    protected static MyLog staticLog = new MyLog(FTPServerService.class.getName());
    protected static int uiLogLevel = Defaults.getUiLogLevel();
    protected static WifiLock wifiLock = null;
    protected ServerSocket listenSocket;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.MEDIA_UNMOUNTED") && FTPServerService.isRunning()) {
                FTPServerService.this.stopSelf();
            }
        }
    };
    protected MyLog myLog = new MyLog(getClass().getName());
    private ProxyConnector proxyConnector = null;
    private List<SessionThread> sessionThreads = new ArrayList();
    protected boolean shouldExit = false;
    WakeLock wakeLock;
    private TcpListener wifiListener = null;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        this.myLog.l(3, "SwiFTP server created");
        log("onCreate===========");
        if (Globals.getContext() == null) {
            Context myContext = getApplicationContext();
            if (myContext != null) {
                Globals.setContext(myContext);
            }
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.MEDIA_UNMOUNTED");
        intentFilter.addDataScheme("file");
        registerReceiver(this.mReceiver, intentFilter);
    }

    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        log("onStart===========");
        this.shouldExit = false;
        int attempts = 10;
        while (serverThread != null) {
            this.myLog.l(5, "Won't start, server thread exists");
            if (attempts > 0) {
                attempts--;
                Util.sleepIgnoreInterupt(1000);
            } else {
                this.myLog.l(6, "Server thread already exists");
                return;
            }
        }
        this.myLog.l(3, "Creating server thread");
        serverThread = new Thread(this);
        serverThread.start();
    }

    public static boolean isRunning() {
        if (serverThread == null) {
            staticLog.l(3, "Server is not running (null serverThread)");
            return false;
        }
        if (!serverThread.isAlive()) {
            staticLog.l(3, "serverThread non-null but !isAlive()");
        } else {
            staticLog.l(3, "Server is alive");
        }
        return true;
    }

    public void onDestroy() {
        this.myLog.l(4, "onDestroy() Stopping server");
        this.shouldExit = true;
        if (serverThread == null) {
            this.myLog.l(5, "Stopping with null serverThread");
            return;
        }
        serverThread.interrupt();
        try {
            serverThread.join(10000);
        } catch (InterruptedException e) {
        }
        if (serverThread.isAlive()) {
            this.myLog.l(5, "Server thread failed to exit");
        } else {
            this.myLog.d("serverThread join()ed ok");
            serverThread = null;
        }
        try {
            if (this.listenSocket != null) {
                this.myLog.l(4, "Closing listenSocket");
                this.listenSocket.close();
            }
        } catch (IOException e2) {
        }
        UiUpdater.updateClients();
        if (wifiLock != null) {
            wifiLock.release();
            wifiLock = null;
        }
        clearNotification();
        unregisterReceiver(this.mReceiver);
        this.myLog.d("FTPServerService.onDestroy() finished");
    }

    private boolean loadSettings() {
        this.myLog.l(3, "Loading settings");
        settings = getSharedPreferences(Defaults.getSettingsName(), Defaults.getSettingsMode());
//        port = settings.getInt("portNum", Defaults.portNumber);
        port = NetworkManagement.getFreePorts(1234, 9000, 1)[0];
        if (port == 0) {
            port = Defaults.portNumber;
        }
        this.myLog.l(3, "Using port " + port);
        acceptNet = false;
        acceptWifi = true;
        fullWake = false;
        return true;
    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        return START_STICKY;
//    }

    /* access modifiers changed from: 0000 */
    public void setupListener() throws IOException {
        this.listenSocket = new ServerSocket();
        this.listenSocket.setReuseAddress(true);
        this.listenSocket.bind(new InetSocketAddress(port));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "PC Share Notication";
        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.setDescription(channelId);
        notificationChannel.setSound(null, null);

        String text = "Files Being Shared";
        notificationManager.createNotificationChannel(notificationChannel);
        Notification notification = new Notification.Builder(this, channelId)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setPriority(Notification.PRIORITY_MIN)
                .build();
        startForeground(101, notification);
    }

    private void clearNotification() {
        stopForeground(true);
        this.myLog.d("Cleared notification");
    }

    private boolean safeSetupListener() {
        try {
            setupListener();
            return true;
        } catch (IOException e) {
            this.myLog.l(5, "Error opening port, check your network connection.");
            return false;
        }
    }

    public void run() {
        log("run>>>>");
        int consecutiveProxyStartFailures = 0;
        long proxyStartMillis = 0;
        UiUpdater.updateClients();
        this.myLog.l(3, "Server thread running");
        if (!loadSettings()) {
            cleanupAndStopService();
            return;
        }
        if (acceptWifi) {
            int atmp = 0;
            while (!safeSetupListener()) {
                atmp++;
                if (atmp >= 10) {
                    break;
                }
                port++;
            }
            if (atmp >= 10) {
                cleanupAndStopService();
                return;
            }
            takeWifiLock();
        }
        takeWakeLock();
        this.myLog.l(4, "SwiFTP server ready");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupNotification();
        }
        UiUpdater.updateClients();
        while (!this.shouldExit) {
            if (acceptWifi) {
                if (this.wifiListener != null && !this.wifiListener.isAlive()) {
                    this.myLog.l(3, "Joining crashed wifiListener thread");
                    try {
                        this.wifiListener.join();
                    } catch (InterruptedException e) {
                    }
                    this.wifiListener = null;
                }
                if (this.wifiListener == null) {
                    this.wifiListener = new TcpListener(this.listenSocket, FTPServerService.this);
                    this.wifiListener.start();
                }
            }
            if (acceptNet) {
                if (this.proxyConnector != null && !this.proxyConnector.isAlive()) {
                    this.myLog.l(3, "Joining crashed proxy connector");
                    try {
                        this.proxyConnector.join();
                    } catch (InterruptedException e2) {
                    }
                    this.proxyConnector = null;
                    if (new Date().getTime() - proxyStartMillis < 3000) {
                        this.myLog.l(3, "Incrementing proxy start failures");
                        consecutiveProxyStartFailures++;
                    } else {
                        this.myLog.l(3, "Resetting proxy start failures");
                        consecutiveProxyStartFailures = 0;
                    }
                }
                if (this.proxyConnector == null) {
                    long nowMillis = new Date().getTime();
                    boolean shouldStartListener = false;
                    if (consecutiveProxyStartFailures < 3 && nowMillis - proxyStartMillis > 5000) {
                        shouldStartListener = true;
                    } else if (nowMillis - proxyStartMillis > 30000) {
                        shouldStartListener = true;
                    }
                    if (shouldStartListener) {
                        this.myLog.l(3, "Spawning ProxyConnector");
                        this.proxyConnector = new ProxyConnector(this);
                        this.proxyConnector.start();
                        proxyStartMillis = nowMillis;
                    }
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e3) {
                this.myLog.l(3, "Thread interrupted");
            }
        }
        terminateAllSessions();
        if (this.proxyConnector != null) {
            this.proxyConnector.quit();
            this.proxyConnector = null;
        }
        if (this.wifiListener != null) {
            this.wifiListener.quit();
            this.wifiListener = null;
        }
        this.shouldExit = false;
        this.myLog.l(3, "Exiting cleanly, returning from run()");
        clearNotification();
        releaseWakeLock();
        releaseWifiLock();
    }

    private void terminateAllSessions() {
        this.myLog.i("Terminating " + this.sessionThreads.size() + " session thread(s)");
        synchronized (this) {
            for (SessionThread sessionThread : this.sessionThreads) {
                if (sessionThread != null) {
                    sessionThread.closeDataSocket();
                    sessionThread.closeSocket();
                }
            }
        }
    }

    public void cleanupAndStopService() {
        Context context = getApplicationContext();
        context.stopService(new Intent(context, FTPServerService.class));
        releaseWifiLock();
        releaseWakeLock();
        clearNotification();
    }

    private void takeWakeLock() {
        if (this.wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (fullWake) {
                this.wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, WAKE_LOCK_TAG);
            } else {
                this.wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
            }
            this.wakeLock.setReferenceCounted(false);
        }
        this.myLog.d("Acquiring wake lock");
        this.wakeLock.acquire();
    }

    private void releaseWakeLock() {
        this.myLog.d("Releasing wake lock");
        if (this.wakeLock != null) {
            this.wakeLock.release();
            this.wakeLock = null;
            this.myLog.d("Finished releasing wake lock");
            return;
        }
        this.myLog.i("Couldn't release null wake lock");
    }

    private void takeWifiLock() {
        this.myLog.d("Taking wifi lock");
        if (wifiLock == null) {
            wifiLock = ((WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE)).createWifiLock(WAKE_LOCK_TAG);
            wifiLock.setReferenceCounted(false);
        }
        wifiLock.acquire();
    }

    private void releaseWifiLock() {
        this.myLog.d("Releasing wifi lock");
        if (wifiLock != null) {
            wifiLock.release();
            wifiLock = null;
        }
    }

    public void errorShutdown() {
        this.myLog.l(6, "Service errorShutdown() called");
        cleanupAndStopService();
    }

    public static InetAddress getWifiIp() {
        Context myContext = Globals.getContext();
        if (myContext == null) {
            throw new NullPointerException("Global context is null");
        }
        WifiManager wifiMgr = (WifiManager) myContext.getApplicationContext().getSystemService(WIFI_SERVICE);
        if (!isWifiEnabled()) {
            return null;
        }
        int ipAsInt = wifiMgr.getConnectionInfo().getIpAddress();
        if (ipAsInt == 0) {
            return null;
        }
        return Util.intToInet(ipAsInt);
    }

    public static boolean isWifiEnabled() {
        Context myContext = Globals.getContext();
        if (myContext == null) {
            throw new NullPointerException("Global context is null");
        } else if (((WifiManager) myContext.getApplicationContext().getSystemService(WIFI_SERVICE)).getWifiState() == 3) {
            return ((ConnectivityManager) myContext.getSystemService(CONNECTIVITY_SERVICE)).getNetworkInfo(1).isConnected();
        } else {
            return false;
        }
    }

    public static List<String> getSessionMonitorContents() {
        return new ArrayList(sessionMonitor);
    }

    public static List<String> getServerLogContents() {
        return new ArrayList(serverLog);
    }

    public static void log(int msgLevel, String s) {
        serverLog.add(s);
        int maxSize = Defaults.getServerLogScrollBack();
        while (serverLog.size() > maxSize) {
            serverLog.remove(0);
        }
    }

    public static void updateClients() {
        UiUpdater.updateClients();
    }

    public static void writeMonitor(boolean incoming, String s) {
    }

    public static int getPort() {
        return port;
    }

    public static void setPort(int port2) {
        port = port2;
    }

    public void registerSessionThread(SessionThread newSession) {
        synchronized (this) {
            List<SessionThread> toBeRemoved = new ArrayList<>();
            for (SessionThread sessionThread : this.sessionThreads) {
                if (!sessionThread.isAlive()) {
                    this.myLog.l(3, "Cleaning up finished session...");
                    try {
                        sessionThread.join();
                        this.myLog.l(3, "Thread joined");
                        toBeRemoved.add(sessionThread);
                        sessionThread.closeSocket();
                    } catch (InterruptedException e) {
                        this.myLog.l(3, "Interrupted while joining");
                    }
                }
            }
            for (SessionThread removeThread : toBeRemoved) {
                this.sessionThreads.remove(removeThread);
            }
            this.sessionThreads.add(newSession);
        }
        this.myLog.d("Registered session thread");
    }

    public ProxyConnector getProxyConnector() {
        return this.proxyConnector;
    }

    public static SharedPreferences getSettings() {
        return settings;
    }

    public static void log(String msg) {
        if (debug) {
            Log.i("wang", "ftpService :" + msg);
        }
    }
}
