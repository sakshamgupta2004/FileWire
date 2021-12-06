package com.sugarsnooper.filetransfer;

import android.util.Log;

import com.sugarsnooper.filetransfer.Client.DownloadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnzipUtility {
    private long lastTime = 0;


    public long unzipWhileDownloading(InputStream zipIn, String destDirectory, long total, DownloadTask.AsynResponse asynResponse) throws IOException{
        File destDir = new File(destDirectory);
        byte[] buffer = new byte[409600];
        ZipInputStream zis = new ZipInputStream(zipIn);
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            Log.e("ZipEntry", zipEntry.getName());
            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    total += len;
                    long timeNow = new Date().getTime();
                    if (timeNow - lastTime > 210) {
                        lastTime = timeNow;
                        asynResponse.progressupdate(total);
                    }
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
        return total;
    }



    public File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
