package com.sugarsnooper.filetransfer.Server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.sugarsnooper.filetransfer.NetworkManagement;
import com.sugarsnooper.filetransfer.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

import static com.sugarsnooper.filetransfer.Strings.ImagesLocation;
import static com.sugarsnooper.filetransfer.Strings.no_hardware;
import static com.sugarsnooper.filetransfer.Strings.not_connected;
import static com.sugarsnooper.filetransfer.Strings.something_wrong;

public class ServerService extends Service {

    private ServerSocket httpServer;
    private static int port;
    private String address;
    private boolean stopServer = false;
    private Socket connected;
    private Context mContext;
    private static ServerStatus response = null;
    public static List<String[]> hostedFiles;
    private HTTPPOSTServer Server = null;
    private static HTTPPOSTServer.ServerListener listener;
    private HashMap<String, String> fileNameUriTable = new HashMap<String, String>();
    public static void newInstance(Context baseContext, List<String[]> files, ServerStatus serverStatus) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            baseContext.startForegroundService(new Intent(baseContext, ServerService.class));
        }
        else
        {
            baseContext.startService(new Intent(baseContext, ServerService.class));
        }
        response = serverStatus;
        //hostedFiles = files;
    }

    public static void attatchListener(HTTPPOSTServer.ServerListener listener) {
        ServerService.listener = listener;
    }

    public static void changeHostedFiles(List<String[]> hostedFiles) {
        ServerService.hostedFiles = hostedFiles;
    }

    public static int getPort(){
        return port;
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupNotification();
        }
        mContext = getBaseContext();
        new Thread(() -> {
            saveImages();
            address = NetworkManagement.getIpAddress(getBaseContext());
            switchON();
        }).start();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "PC Share Notication Software";
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
        startForeground(102, notification);
    }


    @Override
    public void onDestroy() {
        stopServer = true;
        if (connected != null) {
            try {
                connected.close();
                connected = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (httpServer != null) {
            try {
                httpServer.close();
                httpServer = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Server = null;
        stopForeground(true);
        super.onDestroy();
    }

    private void switchON(){
        if (address.trim().contains(something_wrong + "!")) {
            if (response!=null)
            response.onConnectionFailed(something_wrong);
        } else if (address.trim().contains(no_hardware)) {
            if (response!=null)
            response.onConnectionFailed(no_hardware);
        } else if (address.trim().isEmpty()) {
            if (response != null)
            response.onConnectionFailed(not_connected);
        } else {
            stopServer = false;
            new Thread(() -> {
                try {
                    port = NetworkManagement.getFreePorts(1234, 9000, 1)[0];
                    httpServer = new ServerSocket(port);
                    if (response != null)
                    response.onConnectionSuccess("http://" + address.trim() + ":" + port);
                    while (!stopServer) {
                        connected = httpServer.accept();
                        Server = new HTTPPOSTServer(connected, address, port, mContext, listener, fileNameUriTable);
                        Server.start();
                    }
                } catch (IOException | IllegalStateException e) {
                    response.onConnectionFailed(address);
                    e.printStackTrace();
                }
            }).start();
        }
    }


    private void saveImages(){
        save(R.drawable.favicon);
        save(R.drawable.pa_folder);
        save(R.drawable.folder);
        save(R.drawable.music_icon);
        save(R.drawable.video_icon);
        save(R.drawable.unknown_file_icon);
        save(R.drawable.document_icon);
        save(R.drawable.compressed_icon);
    }
    private void save(int drawable){
        try {
            String filename;
            if (drawable == R.drawable.favicon) filename =  "favicon.ico";
            else if (drawable == R.drawable.pa_folder) filename = "pa_folder.png";
            else if (drawable == R.drawable.folder) filename = "folder.png";
            else if (drawable == R.drawable.music_icon) filename = "music.png";
            else if (drawable == R.drawable.video_icon) filename = "video.png";
            else if (drawable == R.drawable.unknown_file_icon) filename = "unknown_file.png";
            else if (drawable == R.drawable.compressed_icon) filename = "compressed.png";
            else filename = "document_file.png";
            File file = new File(ImagesLocation + filename);
            if (file.exists()) file.delete();
            File dir = new File(file.getParent());
            if (!dir.exists()) dir.mkdirs();
            file.createNewFile();
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), drawable);
            bitmap.compress(Bitmap.CompressFormat.PNG,0,new FileOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
