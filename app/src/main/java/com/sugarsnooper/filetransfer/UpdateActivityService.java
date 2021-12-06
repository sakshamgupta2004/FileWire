package com.sugarsnooper.filetransfer;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

public class UpdateActivityService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (!getSharedPreferences("default", MODE_PRIVATE).getBoolean("runfirst",false)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!updateRunFirstTime()) {
                        try {
                            Thread.sleep(20000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    getSharedPreferences("default", MODE_PRIVATE).edit().putBoolean("runfirst", true).commit();
                    stopSelf();
                }
            }).start();
        }
        else {
            stopSelf();
        }
    }


    private boolean updateRunFirstTime() {
        String DeviceName = android.os.Build.MODEL;
        String DeviceVersion = Build.VERSION.SDK;
        String link = "http://www.sugarsnooper.com/file_share/update_run.php?dn=" + DeviceName + "&vn=" + DeviceVersion + "&user=" + Build.USER;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            request.setURI(new URI(link));
            HttpResponse response = client.execute(request);
            BufferedReader in = new BufferedReader(new
                    InputStreamReader(response.getEntity().getContent()));

            StringBuffer sb = new StringBuffer("");
            String line="";

            while ((line = in.readLine()) != null) {
                sb.append(line);
                break;
            }

            in.close();
            if (sb.toString().trim().equalsIgnoreCase("ok")){
                return true;
            }
            return false;
        }
        catch (Exception f){
            return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
