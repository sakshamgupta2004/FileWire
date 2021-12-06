package org.swiftp;

public class Settings {
    protected static boolean allowOverwrite = false;
    protected static int dataChunkSize = 8192;
    protected static int inputBufferSize = 256;
    protected static int serverLogScrollBack = 10;
    protected static int sessionMonitorScrollBack = 10;
    protected static int uiLogLevel = 4;

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

    public static boolean isAllowOverwrite() {
        return allowOverwrite;
    }

    public static void setAllowOverwrite(boolean allowOverwrite2) {
        allowOverwrite = allowOverwrite2;
    }

    public static int getDataChunkSize() {
        return dataChunkSize;
    }

    public static void setDataChunkSize(int dataChunkSize2) {
        dataChunkSize = dataChunkSize2;
    }

    public static int getSessionMonitorScrollBack() {
        return sessionMonitorScrollBack;
    }

    public static void setSessionMonitorScrollBack(int sessionMonitorScrollBack2) {
        sessionMonitorScrollBack = sessionMonitorScrollBack2;
    }

    public static int getServerLogScrollBack() {
        return serverLogScrollBack;
    }

    public static void setLogScrollBack(int serverLogScrollBack2) {
        serverLogScrollBack = serverLogScrollBack2;
    }
}
