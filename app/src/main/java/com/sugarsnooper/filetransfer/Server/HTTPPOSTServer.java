package com.sugarsnooper.filetransfer.Server;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.sugarsnooper.filetransfer.Client.EnableHotspot_ShowQrCode;
import com.sugarsnooper.filetransfer.Strings;
import com.sugarsnooper.filetransfer.TinyDB;
import com.sugarsnooper.filetransfer.ZipUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import static com.sugarsnooper.filetransfer.Server.Send_Activity.isTransferComplete;

public class HTTPPOSTServer extends Thread {
    private static final String HTML_START = "<html>" +
            "<title>File Share</title>" +
            "<body>";
    private static final String HTML_END = "</body>" + "</html>";
    private final Socket connectedClient;
    private DataOutputStream outToClient = null;
    private final String addressport;
    private final int port;
    private final Context context;
    private final ServerListener listener;
    private final HashMap<String, String> fileNameUriTable;


    public interface ServerListener {
        public void filesIncoming();
        public void progressChange(String fileName, long sent, long total);
    }




    public HTTPPOSTServer(Socket client, String address, int port, Context context, ServerListener listener, HashMap<String, String> fileNameUriTable) {
        this.connectedClient = client;
        this.addressport = address;
        this.port = port;
        this.context = context;
        this.listener = listener;
        this.fileNameUriTable = fileNameUriTable;
    }

    public void run() {
        String currentLine;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd-hh:mm:ss z", Locale.getDefault());
        try {
            String targetPath = "http://" + addressport.trim() + ":" + port;
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectedClient.getInputStream()));
            outToClient = new DataOutputStream(connectedClient.getOutputStream());
            currentLine = inFromClient.readLine();
            String headerLine = currentLine;
            StringTokenizer tokenizer = new StringTokenizer(headerLine);
            String httpMethod = tokenizer.nextToken();
            String httpQueryString = tokenizer.nextToken();
            if (httpMethod.equals("GET")) {
                    ArrayList<Uri> shareDataUri = new ArrayList<>();
                    try {
                        if (httpQueryString.substring(1).startsWith("content://") || httpQueryString.substring(1).startsWith("file://")) {
                            shareDataUri.add(Uri.parse(httpQueryString.substring(1)));
                        }
                    }
                    catch (IndexOutOfBoundsException e){
                    }
                    if (shareDataUri.size() == 1) {
                    String responseString;
                        responseString = shareDataUri.get(0).toString();
                        Log.e("URI", responseString);
                        sendResponse( 200, responseString, false, true);
                    }
                    else if (httpQueryString.toLowerCase().startsWith("/incoming")) {
                        listener.filesIncoming();
                        sendResponse(200, "Ok", false, false);
                    }
                    else if (httpQueryString.startsWith("/pc")){
                        String fileName = httpQueryString.substring(4);
                        String statusLine;
                        String serverdetails = "Server: Java HTTPServer";
                        String contentLengthLine;

                        String contentTypeLine = "Content type: text/html" + "\r\n";
                        InputStream fin;
                        statusLine = "HTTP/1.1 200 OK" + "\r\n";

                        fin = context.getAssets().open(fileName);;
                        contentLengthLine = "Content length: " + Integer.toString(fin.available()) + "\r\n";
                        if (!fileName.endsWith(".htm") && !fileName.endsWith(".html"))
                            contentTypeLine = "Content type: \r\n";
                        outToClient.writeBytes(statusLine);
                        outToClient.writeBytes(serverdetails);
                        outToClient.writeBytes(contentTypeLine);
                        outToClient.writeBytes(contentLengthLine);
                        outToClient.writeBytes("\r\n");
                        sendFile(fin, outToClient);
                        outToClient.close();
                    }
                else if (httpQueryString.equals("/request")){
                    try {
                        JSONArray jsonArray = new JSONArray();

                        int i = 1;
                        for (String[] hostedFile : ServerService.hostedFiles) {
                            JSONObject obj = new JSONObject();
                            obj.put("id", i);
                            obj.put("link", hostedFile[0]);
                            obj.put("fileSize", hostedFile[1]);
                            obj.put("fileName", hostedFile[2]);
                            obj.put("isFolder", hostedFile[3]);
                            fileNameUriTable.put(hostedFile[0], hostedFile[2]);
                            jsonArray.put(obj);
                            i++;
                        }
                        String result = jsonArray.toString();
                        sendResponse(200, result, false, false);
                    }
                    catch (Exception e){
                        sendResponse(200, "Error", false, false);
                    }
                }
                else if (httpQueryString.startsWith("/transferComplete")){
                    isTransferComplete = true;
                }
                else if (httpQueryString.startsWith("/verify")){
                    try {
                        EnableHotspot_ShowQrCode.ipAddress_Download_From = httpQueryString.substring(httpQueryString.indexOf("?") + 1, httpQueryString.indexOf("&&"));
                        try {
                            EnableHotspot_ShowQrCode.start_Receiving = Boolean.parseBoolean(httpQueryString.substring(httpQueryString.indexOf("&&") + 2));
                        }
                        catch (Exception ignored) {

                        }
                    }
                    catch (Exception e){

                    }
                    sendResponse(200, "OK", false, false);
                }
                else if (httpQueryString.startsWith("/getAvatarAndName")) {
                    JSONObject jsonObject = new JSONObject();
                    TinyDB tinyDB = new TinyDB(context);
                    jsonObject.put("name", tinyDB.getString(Strings.user_name_preference_key));
                    jsonObject.put("avatar", tinyDB.getInt(Strings.avatar_preference_key));
                    sendResponse(200, jsonObject.toString(), false, false);
                    }
                  else {
                        sendResponse(404, "<b>The Requested resource not found ...." +
                                "Usage: http://" + addressport + ":" + port + "</b>", false, false);
                    }
                }
            else if (httpMethod.equals("POST")) {
                Log.e("Query", httpQueryString);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }


    private void sendResponse(int statusCode, String responseString, boolean isFile, boolean isUri)
            throws Exception {
        String statusLine;
        String serverdetails = "Server: Java HTTPServer";
        String contentLengthLine;
        String fileName;
        String contentTypeLine = "Content-Type: text/html" + "\r\n";
        FileInputStream fin;
        if (statusCode == 200)
            statusLine = "HTTP/1.1 200 OK" + "\r\n";
        else
            statusLine = "HTTP/1.1 404 Not Found" + "\r\n";
        if (isUri) {
            Uri uri = Uri.parse(responseString);
            if (!responseString.contains("file:///")) {
                if (context.checkUriPermission(
                        uri,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.os.Process.myPid(),
                        android.os.Process.myUid(),
                        Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        == PackageManager.PERMISSION_GRANTED) {
                    InputStream in = context.getContentResolver().openInputStream(uri);
                    if (in != null) {
                        Log.e("URI", "Has permission and writing to output");
                        contentLengthLine = "Content-Length: -1\r\n";
                        outToClient.writeBytes(statusLine);
                        outToClient.writeBytes(serverdetails);
                        outToClient.writeBytes(contentTypeLine);
               //         outToClient.writeBytes(contentLengthLine);
                        outToClient.writeBytes("\r\n");
                        shareFile(in, outToClient);
                        outToClient.close();
                    }
                } else {
                    responseString = HTTPPOSTServer.HTML_START + "<b>Internal Server Error ...." +
                            "Usage: http://" + addressport + ":" + port + "</b>" + HTTPPOSTServer.HTML_END;
                    contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";
                    PrintWriter pout = new PrintWriter(outToClient);
                    pout.print("HTTP/1.1 404 Not Found" + "\r\n");
                    pout.print(serverdetails);
                    pout.print(contentTypeLine);
              //      pout.print(contentLengthLine);
                    pout.print("\r\n");
                    pout.print(responseString + "\r\n");
                    pout.close();
                    outToClient.close();
                }
            } else {
                File file = new File(uri.getPath());
                String filename = fileNameUriTable.get(responseString);
                if (filename == null) {
                    Log.e("ResponseString", responseString);
                    for (Map.Entry<String, String> entry : fileNameUriTable.entrySet()) {
                        Log.e("TableItem" ,"Uri: " + entry.getKey() +             "Name: " + entry.getValue());
                    }
                    filename = file.getName();
                }
                if (!file.isDirectory()) {
                    fin = new FileInputStream(file);

                    contentLengthLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n";
                    outToClient.writeBytes(statusLine);
                    outToClient.writeBytes(serverdetails);
                    outToClient.writeBytes(contentTypeLine);
            //        outToClient.writeBytes(contentLengthLine);
                    outToClient.writeBytes("\r\n");
                    sendFile(fin, outToClient, filename, file.length());
                    outToClient.close();
                }
                else {
                    contentLengthLine = "Content-Length: -1" + "\r\n";
                    outToClient.writeBytes(statusLine);
                    outToClient.writeBytes(serverdetails);
                    outToClient.writeBytes(contentTypeLine);
           //         outToClient.writeBytes(contentLengthLine);
                    outToClient.writeBytes("Content-Disposition: attachment; filename=\"out.zip\"\r\n");
                    outToClient.writeBytes("\r\n");
                    new ZipUtils(file).zipIt(outToClient, filename, listener);
                    outToClient.close();
                }
            }
        } else if (isFile) {
            fileName = responseString;
            fin = new FileInputStream(fileName);
            contentLengthLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n";
            if (!fileName.endsWith(".htm") && !fileName.endsWith(".html"))
                contentTypeLine = "Content type: \r\n";
            outToClient.writeBytes(statusLine);
            outToClient.writeBytes(serverdetails);
           // outToClient.writeBytes(contentTypeLine);
            outToClient.writeBytes(contentLengthLine);
            outToClient.writeBytes("\r\n");
            sendFile(fin, outToClient);
            outToClient.close();
        } else {
            responseString = HTTPPOSTServer.HTML_START + responseString + HTTPPOSTServer.HTML_END;
            contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";
            PrintWriter pout = new PrintWriter(outToClient);
            pout.print(statusLine);
            pout.print(serverdetails);
            pout.print(contentTypeLine);
          //  pout.print(contentLengthLine);
            pout.print("\r\n");
            pout.print(responseString + "\r\n");
            pout.close();
            outToClient.close();
        }

    }

    private void sendFile(FileInputStream fin, DataOutputStream out)
            throws Exception {
        byte[] buffer = new byte[409600];
        int bytesRead;
        while ((bytesRead = fin.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        fin.close();
    }

    private void sendFile(FileInputStream fin, DataOutputStream out, String filename, long total)
            throws Exception {
        byte[] buffer = new byte[409600];
        int bytesRead;
        long totalSent = 0;
        while ((bytesRead = fin.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
            totalSent += bytesRead;
            listener.progressChange(filename, totalSent, total);
        }
        listener.progressChange(filename, totalSent, total);
        fin.close();
    }

    private void sendFile(InputStream fin, DataOutputStream out)
            throws Exception {
        byte[] buffer = new byte[409600];
        int bytesRead;
        while ((bytesRead = fin.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        fin.close();
    }

    private void shareFile(InputStream fin, DataOutputStream out)
            throws Exception {
        byte[] buffer = new byte[409600];
        int bytesRead;
        while ((bytesRead = fin.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        fin.close();
    }


    private long getDirSize(File dir){
        long size = 0;
        if (dir.isDirectory() && dir.canRead()){
            for (File file : dir.listFiles()){
                if (file.isFile() && file.canRead()){
                    size += file.length();
                } else {
                    size += getDirSize(file);
                }
            }
        } else if (dir.isFile() && dir.canRead()){
            size += dir.length();
        } else if (dir.getName().equals("emulated")){
            size += getDirSize(new File(dir.getAbsolutePath() + "/0"));
        }
        return size;
    }

    private String decodeFromFirebaseData(String string) {
        return string
                .replace("%2E", ".")
                .replace("%23", "#")
                .replace("%24", "$")
                .replace("%2F", "/")
                .replace("%5B", "[")
                .replace("%20", " ")
                .replace("%5D", "]")
                .replace("%5C", "\\")
                .replace("%21", "!")
                .replace("%22", "\"")
                .replace("%26", "&")
                .replace("%27", "\'")
                .replace("%28", "(")
                .replace("%29", ")")
                .replace("%2A", "*")
                .replace("%2C", ",")
                .replace("%3A", ":")
                .replace("%3B", ";")
                .replace("%3C", "<")
                .replace("%3D", "=")
                .replace("%3E", ">")
                .replace("%3F", "?")
                .replace("%40", "@")
                .replace("%5F", "^")
                .replace("%60", "`")
                .replace("%7B", "{")
                .replace("%7C", "|")
                .replace("%7D", "}")
                .replace("%7E", "~")
                .replace("%25", "%");
    }

    private String encodeAsFirebaseData(String string) {
        return string
                .replace("%", "%25")
                .replace(".", "%2E")
                .replace("#", "%23")
                .replace("$", "%24")
                .replace("/", "%2F")
                .replace("[", "%5B")
                .replace(" ", "%20")
                .replace("]", "%5D")
                .replace("\\", "%5C")
                .replace("!", "%21")
                . replace("\"", "%22")
                . replace("&", "%26")
                . replace("\'", "%27")
                . replace("(", "%28")
                . replace(")", "%29")
                . replace("*", "%2A")
                . replace(",", "%2C")
                . replace(":", "%3A")
                . replace(";", "%3B")
                . replace("<", "%3C")
                . replace("=", "%3D")
                . replace(">", "%3E")
                . replace("?", "%3F")
                . replace("@", "%40")
                . replace("^", "%5F")
                . replace("`", "%60")
                . replace("{", "%7B")
                . replace("|", "%7C")
                . replace("}", "%7D")
                . replace("~", "%7E");
    }

}