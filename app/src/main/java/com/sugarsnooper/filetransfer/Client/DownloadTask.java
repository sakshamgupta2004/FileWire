package com.sugarsnooper.filetransfer.Client;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.sugarsnooper.filetransfer.UnzipUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class DownloadTask extends AsyncTask<String, Integer, String> {

    private final String[] sUrl;
    private final boolean isFolder;
    private long lastTime = 0;
    private final PowerManager pm;
    private long total = 0L;
    private PowerManager.WakeLock mWakeLock;
    AsynResponse asynResponse;
    public boolean hasCompleted = false;

    public interface AsynResponse {
        void processFinish(String out, long total);
        void progressupdate(long progress);
    }

    public DownloadTask(Context context, boolean isFolder, AsynResponse asynResponse, String... sUrl) {
        this.asynResponse = asynResponse;
        this.pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        this.sUrl = sUrl;
        this.isFolder = isFolder;
    }

    @Override
    protected String doInBackground(String... strings) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {

            File yourFile = new File(sUrl[1]);
            URL url = new URL(sUrl[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }
            input = connection.getInputStream();
            if (!this.isFolder) {
                output = new FileOutputStream(yourFile, false);
                byte[] data = new byte[4096 * 100];

                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    long timeNow = new Date().getTime();
                    if (timeNow - lastTime > 210) {
                        lastTime = timeNow;
                        asynResponse.progressupdate(total);
                    }
                    output.write(data, 0, count);
                }
            }
            else {
                new File(sUrl[1]).mkdirs();
                total = new UnzipUtility().unzipWhileDownloading(input, sUrl[1], total, asynResponse);
            }

        } catch (Exception e) {
            Log.e ("Exception", e.toString());
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }

        return null;
    }

    @SuppressLint("WakelockTimeout")
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (pm != null) {
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (pm != null) {
            mWakeLock.release();
        }
        Log.e("Size", String.valueOf(total));
        asynResponse.processFinish(result, total);
        hasCompleted = true;
    }
}
