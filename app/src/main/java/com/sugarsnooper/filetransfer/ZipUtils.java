package com.sugarsnooper.filetransfer;

import android.net.Uri;

import com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection;
import com.sugarsnooper.filetransfer.Server.HTTPPOSTServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    private List <String> fileList;
    private final File SOURCE_FOLDER; // SourceFolder path
    private long totalZipSize = 0;
    private long totalZipWritten = 0;

    public ZipUtils(File inputFolder) {
        fileList = new ArrayList < String > ();
        this.SOURCE_FOLDER = inputFolder;
    }

//    public static void main(String[] args) {
//        ZipUtils appZip = new ZipUtils();
//        appZip.generateFileList(new File(SOURCE_FOLDER));
//        appZip.zipIt(OUTPUT_ZIP_FILE);
//    }

    public void zipIt(OutputStream fos, String filename, HTTPPOSTServer.ServerListener listener) {
//        generateFileList(SOURCE_FOLDER);
        fileList = FileSelection.selectedFolderMap.get(Uri.fromFile(SOURCE_FOLDER).toString()).getKey();
        totalZipSize = FileSelection.selectedFolderMap.get(Uri.fromFile(SOURCE_FOLDER).toString()).getValue();
        byte[] buffer = new byte[409600];
        String source = SOURCE_FOLDER.getName();
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(fos);
            zos.setLevel(Deflater.NO_COMPRESSION);
//            System.out.println("Output to Zip : " + zipFile);
            FileInputStream in = null;

            for (String file: this.fileList) {
                System.out.println("File Added : " + file);
                if (!new File(SOURCE_FOLDER.getPath() + File.separator + file).isDirectory()) {
                    ZipEntry ze = new ZipEntry(source + File.separator + file);
                    zos.putNextEntry(ze);
                    try {
                        in = new FileInputStream(SOURCE_FOLDER.getPath() + File.separator + file);
                        int len;
                        while ((len = in.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                            totalZipWritten += len;
                            listener.progressChange(filename, totalZipWritten, totalZipSize);
                        }
                        listener.progressChange(filename, totalZipWritten, totalZipSize);
                    } finally {
                        in.close();
                    }
                }
                else {
                    ZipEntry ze = new ZipEntry(source + "/" + file + "/");
                    ze.setSize(0);
                    zos.putNextEntry(ze);
                }
            }
            listener.progressChange(filename, totalZipWritten, totalZipSize);
            zos.closeEntry();
            System.out.println("Folder successfully compressed");

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                zos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void generateFileList(File node) {
        // add file only
        if (node.isFile() && node.canRead()) {
            fileList.add(generateZipEntry(node.toString()));
            totalZipSize += node.length();
        }

        if (node.isDirectory() && node.canRead()) {
            String[] subNote = node.list();
            for (String filename: subNote) {
                generateFileList(new File(node, filename));
            }
            if (subNote.length == 0) {
                if (node != SOURCE_FOLDER)
                    fileList.add(generateZipEntry(node.toString()));
            }
        }
    }

    private String generateZipEntry(String file) {
        return file.substring(SOURCE_FOLDER.getPath().length() + 1, file.length());
    }
}