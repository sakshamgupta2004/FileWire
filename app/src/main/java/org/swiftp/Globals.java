package org.swiftp;

import android.content.Context;
import java.io.File;

public class Globals {
    public static boolean isUserNameCorrect = false;
    private static File chrootDir = new File(Defaults.chrootDir);
    private static Context context;
    private static String lastError;
    private static ProxyConnector proxyConnector = null;
    private static String username = null;
    private static String password = null;

    public static ProxyConnector getProxyConnector() {
        if (proxyConnector == null || proxyConnector.isAlive()) {
            return proxyConnector;
        }
        return null;
    }

    public static void setProxyConnector(ProxyConnector proxyConnector2) {
        proxyConnector = proxyConnector2;
    }


    public static File getChrootDir() {
        return chrootDir;
    }

    public static void setChrootDir(File chrootDir2) {
        if (chrootDir2.isDirectory()) {
            chrootDir = chrootDir2;
        }
    }

    public static String getLastError() {
        return lastError;
    }

    public static void setLastError(String lastError2) {
        lastError = lastError2;
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context2) {
        if (context2 != null) {
            context = context2;
        }
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username2) {
        username = username2;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        Globals.password = password;
    }
}
