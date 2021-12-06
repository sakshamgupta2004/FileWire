package org.swiftp;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.provider.Settings.Secure;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class Util {
    static MyLog myLog = new MyLog(Util.class.getName());

    private static class MediaScannerNotifier implements MediaScannerConnectionClient {
        private MediaScannerConnection connection;
        private String path;

        public MediaScannerNotifier(Context context, String path2) {
            this.path = path2;
            this.connection = new MediaScannerConnection(context, this);
            this.connection.connect();
        }

        public void onMediaScannerConnected() {
            this.connection.scanFile(this.path, null);
        }

        public void onScanCompleted(String path2, Uri uri) {
            this.connection.disconnect();
        }
    }

    static String getAndroidId() {
        return Secure.getString(Globals.getContext().getContentResolver(), "android_id");
    }

    public static String getVersion() {
        try {
            return Globals.getContext().getPackageManager().getPackageInfo(Globals.getContext().getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            myLog.l(6, "NameNotFoundException looking up SwiFTP version");
            return null;
        }
    }

    public static byte byteOfInt(int value, int which) {
        return (byte) (value >> (which * 8));
    }

    public static String ipToString(int addr, String sep) {
        if (addr <= 0) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        buf.append(byteOfInt(addr, 0)).append(sep).append(byteOfInt(addr, 1)).append(sep).append(byteOfInt(addr, 2)).append(sep).append(byteOfInt(addr, 3));
        myLog.l(3, "ipToString returning: " + buf.toString());
        return buf.toString();
    }

    public static InetAddress intToInet(int value) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[i] = byteOfInt(value, i);
        }
        try {
            return InetAddress.getByAddress(bytes);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public static String ipToString(int addr) {
        if (addr != 0) {
            return ipToString(addr, ".");
        }
        myLog.l(4, "ipToString won't convert value 0");
        return null;
    }

    static byte[] jsonToByteArray(JSONObject json) throws JSONException {
        try {
            return json.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    static JSONObject byteArrayToJson(byte[] bytes) throws JSONException {
        try {
            return new JSONObject(new String(bytes, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static void newFileNotify(String path) {
        myLog.l(3, "Notifying others about new file: " + path);
        new MediaScannerNotifier(Globals.getContext(), path);
    }

    public static void deletedFileNotify(String path) {
        myLog.l(3, "Notifying others about deleted file: " + path);
        new MediaScannerNotifier(Globals.getContext(), path);
    }

    public static String[] concatStrArrays(String[] a1, String[] a2) {
        String[] retArr = new String[(a1.length + a2.length)];
        System.arraycopy(a1, 0, retArr, 0, a1.length);
        System.arraycopy(a2, 0, retArr, a1.length, a2.length);
        return retArr;
    }

    public static void sleepIgnoreInterupt(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }
}
