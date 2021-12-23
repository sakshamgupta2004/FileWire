package com.sugarsnooper.filetransfer;

import android.os.Environment;

public class Strings {
    public static final String ImagesLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.sugarsnooper.filetransfer/";
    public static final String TAG = "MYFILESHARE";
    public static final String wifi_disabled = "Please Enable WiFi";
    public static final String no_hardware = "No Hardware Supported";
    public static final String something_wrong = "Something Wrong";
    public static final String not_connected = "Not Connected to Wifi or HotSpot";
    public static final String Documents = "Documents";
    public static final String Music = "Audio";
    public static final String Installers = "Apps";
    public static final String Archives = "Archives";
    public static final String FileSelectionRequest = "FILESELECTIONREQUEST";
    public static final int GPS_REQUEST = 6969;
    public static final int GPS_PERMISSION = 1969;



    public static String dateString = "";
    public static final String user_name_preference_key = "User_Name";
    public static final String avatar_preference_key = "User_Avatar";
    public static final String pairedPC_preference_key = "PairedPCs";
    public static final String PCDefaultPort = "1234";
}
