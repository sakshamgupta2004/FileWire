package com.sugarsnooper.filetransfer.Client;
import android.content.*;
import android.net.wifi.*;
import java.lang.reflect.*;

public class ApManager {

    private static Method wifiApConfigurationMethod;
    //check whether wifi hotspot on or off
    public static boolean isApOn(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        try {
            Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifimanager);
        }
        catch (Throwable ignored) {}
        return false;
    }
    public static WifiConfiguration getWifiApConfiguration(Context context)
    {
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        try{
            wifiApConfigurationMethod = mWifiManager.getClass().getMethod("getWifiApConfiguration",null);
            return (WifiConfiguration)wifiApConfigurationMethod.invoke(mWifiManager, null);
        }
        catch(Exception e)
        {
            return null;
        }
    }
    // toggle wifi hotspot on or off
    public static boolean configApState(Context context, boolean b) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        WifiConfiguration wificonfiguration = null;
        try {
            // if WiFi is on, turn it off
            if(isApOn(context)) {
                wifimanager.setWifiEnabled(false);
            }
            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(wifimanager, wificonfiguration, b);
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
} // end of class
