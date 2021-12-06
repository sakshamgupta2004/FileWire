package com.sugarsnooper.filetransfer.ConnectToPC.FTP;

import android.content.Context;

public class Security {
    public static void setPassword(Context context, String pass) {
        context.getSharedPreferences("security", Context.MODE_PRIVATE).edit().putString("pass", pass).commit();
    }
    public static void clearPassword(Context context){
        context.getSharedPreferences("security", Context.MODE_PRIVATE).edit().putString("pass", null).commit();
    }
    public static String getPassword(Context context){
        return context.getSharedPreferences("security", Context.MODE_PRIVATE).getString("pass", null);
    }

    public static void setUsername(Context context, String user) {
        context.getSharedPreferences("security", Context.MODE_PRIVATE).edit().putString("user", user).commit();
    }
    public static void clearUsername(Context context){
        context.getSharedPreferences("security", Context.MODE_PRIVATE).edit().putString("user", null).commit();
    }
    public static String getUsername(Context context){
        return context.getSharedPreferences("security", Context.MODE_PRIVATE).getString("user", null);
    }
}
