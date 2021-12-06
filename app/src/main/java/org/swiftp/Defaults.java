package org.swiftp;

import android.content.Context;
import android.os.Environment;

import com.google.android.gms.cast.Cast;
import com.sugarsnooper.filetransfer.ConnectToPC.FTP.FTPServerService;

public class Defaults {
    public static final int REMOTE_PROXY_PORT = 2222;
    public static final String SESSION_ENCODING = "UTF-8";
    public static final int SO_TIMEOUT_MS = 30000;
    public static final String STRING_ENCODING = "UTF-8";
    public static final String chrootDir = Util.getSdDirectory();
    protected static int consoleLogLevel = 4;
    protected static int dataChunkSize = Cast.MAX_MESSAGE_LENGTH;
    public static final boolean do_mediascanner_notify = true;
    protected static int inputBufferSize = 256;
    public static int portNumber = 2121;
    public static final boolean release = true;
    protected static int serverLogScrollBack = 10;
    protected static int settingsMode = Context.MODE_PRIVATE;
    protected static String settingsName = FTPServerService.WAKE_LOCK_TAG;
    public static final int tcpConnectionBacklog = 5;
    protected static int uiLogLevel = 4;

    public static int getPortNumber() {
        return portNumber;
    }

    public static void setPortNumber(int portNumber2) {
        portNumber = portNumber2;
    }

    public static String getSettingsName() {
        return settingsName;
    }

    public static void setSettingsName(String settingsName2) {
        settingsName = settingsName2;
    }

    public static int getSettingsMode() {
        return settingsMode;
    }

    public static void setSettingsMode(int settingsMode2) {
        settingsMode = settingsMode2;
    }

    public static void setServerLogScrollBack(int serverLogScrollBack2) {
        serverLogScrollBack = serverLogScrollBack2;
    }

    public static int getUiLogLevel() {
        return uiLogLevel;
    }

    public static void setUiLogLevel(int uiLogLevel2) {
        uiLogLevel = uiLogLevel2;
    }

    public static int getInputBufferSize() {
        return inputBufferSize;
    }

    public static void setInputBufferSize(int inputBufferSize2) {
        inputBufferSize = inputBufferSize2;
    }

    public static int getDataChunkSize() {
        return dataChunkSize;
    }

    public static void setDataChunkSize(int dataChunkSize2) {
        dataChunkSize = dataChunkSize2;
    }

    public static int getServerLogScrollBack() {
        return serverLogScrollBack;
    }

    public static void setLogScrollBack(int serverLogScrollBack2) {
        serverLogScrollBack = serverLogScrollBack2;
    }

    public static int getConsoleLogLevel() {
        return consoleLogLevel;
    }

    public static void setConsoleLogLevel(int consoleLogLevel2) {
        consoleLogLevel = consoleLogLevel2;
    }
    public static class Util{

        public static String getSdDirectory() {
            return Environment.getExternalStorageDirectory().getPath();
        }
    }
}
