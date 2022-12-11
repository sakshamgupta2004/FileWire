package com.sugarsnooper.filetransfer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import android.os.StrictMode;
import com.sugarsnooper.filetransfer.Server.InitializeServerFragment;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.*;
import java.nio.ByteOrder;
import java.util.Enumeration;

import static com.sugarsnooper.filetransfer.Strings.no_hardware;
import static com.sugarsnooper.filetransfer.Strings.something_wrong;

public class NetworkManagement {


    public static int[] getFreePorts (int rangeMin, int rangeMax, int count) {
        int currPortCount = 0;
        int port[] = new int [count];
        for (int currPort = rangeMin; currPortCount < count && currPort <= rangeMax; ++currPort) {
            if (isPortFree(currPort))
                port[currPortCount++] = currPort;
        }
        if (currPortCount < count)
            throw new IllegalStateException ("Could not find " + count + " free ports to allocate within range " +
                    rangeMin + "-" + rangeMax + ".");
        return port;
    }
    private static boolean isPortFree (int port){
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(port);
            socket.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }




    public static String getAllIpAddress(Context context) {
        String ip = "";
        try {
            NetworkInterface networkInterface = NetworkInterface.getByName("wlan0");
//            NetworkInterface networkInterface = NetworkInterface.getByName("eth0");
            if (networkInterface != null) {
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress() + "\n";
                    }
                }
            }
            else {
//                ip += no_hardware;
            }
            networkInterface = NetworkInterface.getByName("eth0");
            if (networkInterface != null) {
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress() + "\n";
                    }
                }
            }
            networkInterface = NetworkInterface.getByName("ap0");
            if (networkInterface != null) {
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress() + "\n";
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ip += something_wrong + "! " + e.toString();
        }
            try {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager.getDhcpInfo().ipAddress != 0) {
                    ip += InitializeServerFragment.intToIp(wifiManager.getDhcpInfo().ipAddress) + "\n";
                }
            }catch (NullPointerException ne){

            }
            //ip += getHotspotIPAddress(context) + "\n";
            ip += getWifiApIpAddress() + "\n";

        if (isConnectedToWifi(context) || isEnabledWifiHotspot(context) || ip.startsWith("192.168"))
            return ip;
        //Log.e(String.valueOf(isEnabledWifiHotspot(context)), String.valueOf(isConnectedToWifi(context)));

        return "";
    }

    public static String getIpAddress(Context context) {
        String ip = "";
        try {
            NetworkInterface networkInterface = NetworkInterface.getByName("wlan0");
//            NetworkInterface networkInterface = NetworkInterface.getByName("eth0");
            if (networkInterface != null) {
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress();
                    }
                }
            }
            else {
//                ip += no_hardware;
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ip += something_wrong + "! " + e.toString();
        }
        if (ip.trim().isEmpty()){
            try {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager.getDhcpInfo().ipAddress != 0) {
                    ip = InitializeServerFragment.intToIp(wifiManager.getDhcpInfo().ipAddress);
                }
            }catch (NullPointerException ne){

            }
        }
        if (ip.trim().isEmpty()){
            ip = getHotspotIPAddress(context);
        }
        if (ip.trim().isEmpty()){
            // ip = InitializeServerFragment.intToIp(((WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getDhcpInfo().serverAddress);
            ip = getWifiApIpAddress();
        }

        if (isConnectedToWifi(context) || isEnabledWifiHotspot(context) || ip.startsWith("192.168"))
            return ip;
        //Log.e(String.valueOf(isEnabledWifiHotspot(context)), String.valueOf(isConnectedToWifi(context)));

        return "";
    }

    public static boolean isConnectedToWifi(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected() && activeNetworkInfo.getType() == 1;
    }
    private static Boolean callIsWifiApEnabled(WifiManager wifiManager) {
        try {
            return (Boolean) wifiManager.getClass().getDeclaredMethod("isWifiApEnabled", new Class[0]).invoke(wifiManager, new Object[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
        }
        return null;
    }

    public static boolean isEnabledWifiHotspot(Context context) {
        Boolean callIsWifiApEnabled = callIsWifiApEnabled((WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE));
        if (callIsWifiApEnabled != null) {
            return callIsWifiApEnabled.booleanValue();
        }
        return false;
    }








    private static String getWifiApIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                    .hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                //   if (intf.getName().contains("wlan")) {
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                        .hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && (inetAddress.getAddress().length == 4)) {

                        return inetAddress.getHostAddress();
                    }
                    //     }
                }
            }
        } catch (SocketException ex) {

        }
        return "";
    }

    private static String getHotspotIPAddress(Context context) {

        int ipAddress = ((WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getDhcpInfo().serverAddress;

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            ipAddressString = "";
        }

        return ipAddressString;

    }



    public static void sendBroadcast(String messageStr, int port, Context context) {
        // Hack Prevent crash (sending should be done using an async task)
        StrictMode.ThreadPolicy policy = new   StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            //Open a random port to send the package
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            byte[] sendData = messageStr.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, getBroadcastAddress(context), port);
            socket.send(sendPacket);
        } catch (IOException e) {
//            Log.e(TAG, "IOException: " + e.getMessage());
        }
    }

    private static InetAddress getBroadcastAddress(Context context) throws IOException {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }


}
