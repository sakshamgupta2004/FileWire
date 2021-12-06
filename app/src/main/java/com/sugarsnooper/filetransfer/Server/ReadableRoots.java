package com.sugarsnooper.filetransfer.Server;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.sugarsnooper.filetransfer.FileTypeLookup;
import com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments.Gallery;
import com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments.Photos;
import com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments.VideoGalleryFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReadableRoots {
    public class file{
        private final String path;
        private final Long modified;

        public file(String path, Long modified) {
            this.path = path;
            this.modified = modified;
        }

        public String getPath() {
            return path;
        }

        public Long getModified() {
            return modified;
        }
    }
    private Context context;
    ArrayList<File> audio = new ArrayList<File>();
    ArrayList<File> docs = new ArrayList<File>();
    ArrayList<File> archives = new ArrayList<File>();
    ArrayList<File> apps = new ArrayList<File>();
    ArrayList<file> vids = new ArrayList<>();
    ArrayList<file> images = new ArrayList<>();
    long files = 0;
    boolean arefilesconstant = false;
    ArrayList<String> readable = new ArrayList<>();
    public ReadableRoots(final Context context) {
        this.context = context;
        new Thread(new Runnable() {
            @Override
            public void run() {
                readable = selectReadable(getAllRoots(context));
                for (String path : readable){
//                    recursive_open();
                    search_for_media(new File(path));
                }
                search_for_media(Environment.getExternalStorageDirectory());
//                recursive_open();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (ReadableRoots.this != null)
                            try {
                                long filesold = files;
                                Thread.sleep(500);
                                long filesnew = files;
                                if (filesold == filesnew){
                                    arefilesconstant = true;
                                }
                                else{
                                    arefilesconstant = false;
                                }
                            }
                            catch (Exception e){

                            }
                    }
                }).start();
            }
        }).start();
    }


    private Map<String, Long> sortByValue(final boolean order, HashMap<String, Long> hashMap) {
//convert HashMap into List
        List<Map.Entry<String, Long>> list = new LinkedList<Map.Entry<String, Long>>(hashMap.entrySet());
//sorting the list elements
        Collections.sort(list, new Comparator<Map.Entry<String, Long>>() {
            public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                if (order) {
//compare two object and return an integer
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());
                }
            }
        });
        Map<String, Long> sortedMap = new LinkedHashMap<String, Long>();
        for (Map.Entry<String, Long> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }


    public Map<String, Long> getVideos(){
        while (!arefilesconstant){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        HashMap<String, Long> hashMap = new HashMap<>();
        for (ReadableRoots.file file : vids){
            if (file != null) {
                hashMap.put(file.getPath(), file.getModified());

            }
        }
        Map<String, Long> sortedMap = sortByValue(false, hashMap);

        return sortedMap;
    }

    public Map<String, Long> getImages(){
        while (!arefilesconstant){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        HashMap<String, Long> hashMap = new HashMap<>();
        for (ReadableRoots.file file : images){
            if (file != null) {
                hashMap.put(file.getPath(), file.getModified());

            }
        }
        Map<String, Long> sortedMap = sortByValue(false, hashMap);

        return sortedMap;
    }

    public Map<String, Long> getGallery(Object fragment){
        if (fragment instanceof Gallery) {
            return getGallery();
        }
        else if (fragment instanceof Photos) {
            return getImages();
        }
        else if (fragment instanceof VideoGalleryFragment) {
            return getVideos();
        }
        else {
            return null;
        }
    }

    public Map<String, Long> getGallery(){
        while (!arefilesconstant){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        HashMap<String, Long> hashMap = new HashMap<>();
        for (ReadableRoots.file file : images){
            if (file != null) {
                hashMap.put(file.getPath(), file.getModified());

            }
        }
        for (ReadableRoots.file file : vids){
            if (file != null) {
                hashMap.put(file.getPath(), file.getModified());

            }
        }
        Map<String, Long> sortedMap = sortByValue(false, hashMap);

        return sortedMap;
    }


    public ArrayList<File> getInstallers(){
        while (!arefilesconstant){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return apps;
    }

    public ArrayList<File> getDocs(){
        while (!arefilesconstant){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return docs;
    }

    public ArrayList<File> getAudio(){
        while (!arefilesconstant){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return audio;
    }

    public ArrayList<File> getArchives(){
        while (!arefilesconstant){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return archives;
    }


    private void search_for_media(File file) {
        files++;

        File[] subfiles = file.listFiles();
        if (subfiles != null) {
            for (final File subfile : subfiles) {
                try {
                    if (subfile.isDirectory()) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                search_for_media(subfile);
                            }
                        }).start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (subfile.isFile())
                        add_to_list_if_mediafile(subfile);
                }
                catch (Exception e){

                }


            }
        }
        else {
            Log.e("ERROR", "null");
        }
    }

    private void add_to_list_if_mediafile(File subfile) {
        files++;
        if ((!subfile.getPath().toLowerCase().contains("/android/data/")) && (!subfile.getPath().toLowerCase().contains("/android/obb/")) && (!subfile.getParentFile().getName().toLowerCase().contains("thumbnails"))) {

            if (subfile.length() > 0) {
                switch (FileTypeLookup.fileType(subfile.getName())) {
                    case 1:
                        docs.add(subfile);
                        break;
                    case 2:
                        images.add(new file(subfile.getPath(), subfile.lastModified()));
                        break;
                    case 3:
                        vids.add(new file(subfile.getPath(), subfile.lastModified()));
                        break;
                    case 4:
                        audio.add(subfile);
                        break;
                    case 5:
                        archives.add(subfile);
                        break;
                    case 6:
                        apps.add(subfile);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String[] temp = subfile.getName().split("\\.");
                                if (temp[temp.length - 1].toLowerCase().equals("apk")) {
                                    File folderToSave = new File(context.getExternalCacheDir().getPath() + File.separator + "APKThumbnails");
                                    folderToSave.mkdirs();
                                    PackageInfo packageInfo = context.getPackageManager()
                                            .getPackageArchiveInfo(subfile.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
                                    if (packageInfo != null) {
                                        ApplicationInfo appInfo = packageInfo.applicationInfo;
                                        appInfo.sourceDir = subfile.getAbsolutePath();
                                        appInfo.publicSourceDir = subfile.getAbsolutePath();
                                        saveBitmapToFile(folderToSave, subfile.getName() + subfile.getPath().hashCode() + ".jpeg", drawableToBitmap(appInfo.loadIcon(context.getPackageManager())), Bitmap.CompressFormat.JPEG, 95);
                                    }
                                }
                            }
                        }).start();
                        break;
                }
            }
        }
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

    private ArrayList<String> selectReadable(ArrayList<String> allRoots) {
        ArrayList<String> readable = new ArrayList<>();
        for (String string : allRoots) {
            try {
                if (new File(string).canRead()) {
                    readable.add(string);
                }
            }
            catch (Exception e){

            }
        }
        return readable;
    }

    private ArrayList<String> getAllRoots(Context context) {
        File[] externalStorageFiles = ContextCompat.getExternalFilesDirs(context, null);
        ArrayList<String> strings = new ArrayList<>();
        for (File file : externalStorageFiles) {
            strings.add(getRootOfInnerSdCardFolder(file));
        }
        return strings;
    }

    private String getRootOfInnerSdCardFolder(File file) {
        if (file == null)
            return null;
        final long totalSpace = file.getTotalSpace();
        while (true) {
            final File parentFile = file.getParentFile();
            if (parentFile == null || parentFile.getTotalSpace() != totalSpace)
                return file.getAbsolutePath();
            file = parentFile;
        }
    }
}
