package com.sugarsnooper.filetransfer;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.sugarsnooper.filetransfer.Client.RecieveActivity;
import com.sugarsnooper.filetransfer.ConnectToPC.FTP.PC_Connect_Activity;
import com.sugarsnooper.filetransfer.ConnectToPC.PCSoftware.PC_ConnectActivity;
import com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments.Gallery;
import com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments.Photos;
import com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments.VideoGalleryFragment;
import com.sugarsnooper.filetransfer.Server.ReadableRoots;
import com.sugarsnooper.filetransfer.Server.Send_Activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class readableRootsSurvivor extends Service implements Runnable {
    private static boolean isReadAsStorageManager = false;
    private static Map<String, Long> imagesMap = null;
    private static Map<String, Long> videosMap = null;
    private static Map<String, Long> galleryMap = null;
    private static List<File> allFileList = null;
    private static List<File> docsList = null;
    private static List<File> archivesList = null;
    private static List<File> audioList = null;
    private static List<File> installerList = null;
    private static List<String> appPackageList = null;
    private static List<ApplicationInfo> appList = null;
    private static boolean hasPhotosRefreshed = false;
    private static boolean hasVideosRefreshed = false;
    private static boolean hasGalleryRefreshed = false;
    private static Context context;
    public static TinyDB db;


    public static boolean isIsReadAsStorageManager()
    {
        return isReadAsStorageManager;
    }
    public static List<String> getAppPackageList() {
        while (appList == null) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return new CopyOnWriteArrayList<>(appPackageList);
    }

    public static List<ApplicationInfo> getAppList() {
        while (appList == null) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return new CopyOnWriteArrayList<>(appList);
    }

    public static boolean hasRefreshed(Object fragment) {
        if (fragment instanceof Photos) {
            return hasPhotosRefreshed;
        }
        else if (fragment instanceof VideoGalleryFragment) {
            return hasVideosRefreshed;
        }
        else if (fragment instanceof Gallery) {
            return hasGalleryRefreshed;
        }
        else {
            return false;
        }
    }

    public static void refreshLists() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            isReadAsStorageManager = Environment.isExternalStorageManager();
        }
        else {
            isReadAsStorageManager = true;
        }
        ReadableRoots readableRoots = new ReadableRoots(context);
        imagesMap = new LinkedHashMap<>(readableRoots.getImages());
        videosMap = new LinkedHashMap<>(readableRoots.getVideos());
        galleryMap = new LinkedHashMap<>(readableRoots.getGallery());
        docsList = new CopyOnWriteArrayList<>(readableRoots.getDocs());
        archivesList = new CopyOnWriteArrayList<>(readableRoots.getArchives());
        audioList = new CopyOnWriteArrayList<>(readableRoots.getAudio());
        installerList = new CopyOnWriteArrayList<>(readableRoots.getInstallers());
        allFileList = new CopyOnWriteArrayList<>(readableRoots.getAllFiles());
        readableRoots = null;
    }

    public static Map<String, Long> getGallery(Object fragment) {
        if (fragment instanceof Gallery) {
            while (galleryMap == null) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            hasGalleryRefreshed = true;
            return new LinkedHashMap<>(galleryMap);
        }
        else if (fragment instanceof Photos) {
            while (imagesMap == null) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            hasPhotosRefreshed = true;
            return new LinkedHashMap<>(imagesMap);
        }
        else if (fragment instanceof VideoGalleryFragment) {
            while (videosMap == null) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            hasVideosRefreshed = true;
            return new LinkedHashMap<>(videosMap);
        }
        else {
            return null;
        }
    }

    public static List<File> getInstallers() {
        while (installerList == null) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return installerList;
    }
    public static List<File> getAudio() {
        while (audioList == null) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return audioList;
    }
    public static List<File> getArchives() {
        while (archivesList == null) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return archivesList;
    }
    public static List<File> getDocs() {
        while (docsList == null) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return docsList;
    }
    public static List<File> getAllFiles() {
        while (allFileList == null) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return allFileList;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupNotification();
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db = new TinyDB(getContext());
        context = getBaseContext();
        new Thread(this).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = getString(R.string.app_name);
        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.setDescription(channelId);
        notificationChannel.setSound(null, null);

        String text = "File Wire Running";
        notificationManager.createNotificationChannel(notificationChannel);
        Notification notification = new Notification.Builder(this, channelId)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setPriority(Notification.PRIORITY_MIN)
                .build();
        startForeground(103, notification);
    }

    @Override
    public void run() {

        int i = 0;
        final boolean[] result = {true};
            while ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (i == 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true) {
                                boolean first = Send_Activity.getActivity() || RecieveActivity.getActivity() || PC_Connect_Activity.getActivity() || Mode_Selection_Activity.getActivity() || splash_screen.getActivity() || PC_ConnectActivity.getActivity();
                                try {
                                    Thread.sleep(10000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                boolean second = Send_Activity.getActivity() || RecieveActivity.getActivity() || PC_Connect_Activity.getActivity() || Mode_Selection_Activity.getActivity() || splash_screen.getActivity() || PC_ConnectActivity.getActivity();
                                result[0] = first || second;
                                if (!result[0]) {
                                    break;
                                }
                            }
                        }
                    }).start();
                    i ++;
                }
                if (!result[0]) {
                    break;
                }
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (appList == null || appPackageList == null) {
                        appPackageList = new CopyOnWriteArrayList<>();
                        appList = null;
                        appList = getApps();
                    }
                }
            }).start();

            if (imagesMap == null || videosMap == null || galleryMap == null || docsList == null || archivesList == null || audioList == null || installerList == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    isReadAsStorageManager = Environment.isExternalStorageManager();
                }
                else {
                    isReadAsStorageManager = true;
                }
                ReadableRoots readableRoots = new ReadableRoots(getBaseContext());
                imagesMap = new LinkedHashMap<>(readableRoots.getImages());
                videosMap = new LinkedHashMap<>(readableRoots.getVideos());
                galleryMap = new LinkedHashMap<>(readableRoots.getGallery());
                docsList = new CopyOnWriteArrayList<>(readableRoots.getDocs());
                archivesList = new CopyOnWriteArrayList<>(readableRoots.getArchives());
                audioList = new CopyOnWriteArrayList<>(readableRoots.getAudio());
                installerList = new CopyOnWriteArrayList<>(readableRoots.getInstallers());
                allFileList = new CopyOnWriteArrayList<>(readableRoots.getAllFiles());
                readableRoots = null;
            }

        while (true){
            boolean first = Send_Activity.getActivity() || RecieveActivity.getActivity() || PC_Connect_Activity.getActivity() || Mode_Selection_Activity.getActivity() || splash_screen.getActivity() || PC_ConnectActivity.getActivity();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean second = Send_Activity.getActivity() || RecieveActivity.getActivity() || PC_Connect_Activity.getActivity() || Mode_Selection_Activity.getActivity() || splash_screen.getActivity() || PC_ConnectActivity.getActivity();
            boolean result1 = first || second;
            if (!result1){
                break;
            }
        }

        imagesMap = null;
        videosMap = null;
        galleryMap = null;
        docsList = null;
        archivesList = null;
        audioList = null;
        installerList = null;
        stopSelf();
        stopForeground(true);
        getApplication().onTerminate();
        System.exit(0);

    }

    private Context getContext () {
        return getApplicationContext();
    }

    private List<ApplicationInfo> getApps() {
        PackageManager packageManager = null;
        List<ApplicationInfo> appsList = new CopyOnWriteArrayList<>();
        try {
            packageManager = getContext().getPackageManager();
            if (packageManager != null) {
                appsList = new CopyOnWriteArrayList<>(packageManager.getInstalledApplications(0));
            }
        } catch (Exception e1) {
        }
        List<ApplicationInfo> sysappsList = new CopyOnWriteArrayList<>();
        try {
            packageManager = getContext().getPackageManager();
            if (packageManager != null) {
                sysappsList = new CopyOnWriteArrayList<>(packageManager.getInstalledApplications(PackageManager.MATCH_SYSTEM_ONLY));
            }
        } catch (Exception e1) {
        }

        List<String> strings = new CopyOnWriteArrayList<>();

        for (ApplicationInfo applicationInfo : sysappsList){
            strings.add(applicationInfo.packageName);
        }

        sysappsList = new CopyOnWriteArrayList<>();

        for (ApplicationInfo applicationInfo : appsList){
            if (!strings.contains(applicationInfo.packageName)){
                sysappsList.add(applicationInfo);
            }
        }
        Collections.sort(sysappsList, new ApplicationInfo.DisplayNameComparator(packageManager));
        for (ApplicationInfo app : sysappsList) {
//            appIconList.add(app.loadIcon(packageManager));

            appPackageList.add(app.packageName);
            try {
                File folderToSave = new File(getContext().getExternalCacheDir().getPath() + File.separator + "APPThumbnails");
                File folderToSave1 = new File(getContext().getExternalCacheDir().getPath() + File.separator + "APKThumbnails");
                folderToSave.mkdirs();
                folderToSave1.mkdirs();
                saveBitmapToFile(folderToSave, app.packageName + ".jpeg", drawableToBitmap(app.loadIcon(packageManager)), Bitmap.CompressFormat.JPEG, 20);
                saveBitmapToFile(folderToSave1, String.valueOf(app.loadLabel(packageManager)) + ".apk" + new File(app.sourceDir).getPath().hashCode() + ".jpeg", drawableToBitmap(app.loadIcon(packageManager)), Bitmap.CompressFormat.JPEG, 95);
            }
            catch (NullPointerException ne) {

            }
        }
        return sysappsList;
    }


    boolean saveBitmapToFile(File dir, String fileName, Bitmap bm,
                             Bitmap.CompressFormat format, int quality) {

        File imageFile = new File(dir,fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);

            bm.compress(format,quality,fos);

            fos.close();

            return true;
        }
        catch (IOException e) {
            Log.e("app",e.getMessage());
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}
