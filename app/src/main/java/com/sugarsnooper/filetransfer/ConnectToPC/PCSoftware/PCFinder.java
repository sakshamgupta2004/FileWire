package com.sugarsnooper.filetransfer.ConnectToPC.PCSoftware;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.StrictMode;
import com.sugarsnooper.filetransfer.NetworkManagement;
import com.sugarsnooper.filetransfer.Server.ServerService;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URI;

public class PCFinder
{
    private Context context;

    public static void foundPC(String pcName, String address, String productId) {
        if (Server_aktiv)
            result_.foundPC(pcName, address, productId);
    }

    interface result {
        public void foundPC (String name, String address, String productId);
    }

    private static boolean Server_aktiv = true;
    private static result result_;

    public PCFinder runUdpServer(result result1, boolean onlyPairingMode, Context context)
    {
        this.context = context;
        result_ = result1;
        Server_aktiv = true;
        Thread thread = new Thread(() -> {
            byte[] lMsg = new byte[4096];
            DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);

            int port;
            if (onlyPairingMode) {

                port = NetworkManagement.getFreePorts(42004, 42009, 1)[0];
            }
            else {
                port = NetworkManagement.getFreePorts(42404, 42409, 1)[0];
            }
            try (DatagramSocket ds = new DatagramSocket(port)) {

                while (Server_aktiv) {
                    ds.receive(dp);
                    String receivedString = new String(lMsg, 0, dp.getLength());

                    try {
                        String[] addresses = receivedString.split("\n");
                        if (addresses.length > 0) {
                            String PCName = "";
                            String addr_main = "";
                            String productId = "";
                            for (String addr:
                                 addresses) {
                                if (addr.contains("FileWire: PCID:-"))
                                    productId = addr.substring(addr.indexOf("PCID:-") + 6);
                                if (onlyPairingMode) {
                                    String tempName = canGetNameAndAvatar("http://" + addr + "/");
                                    if (tempName != null) {
                                        PCName = tempName;
                                        addr_main = "http://" + addr + "/";
                                        if (Server_aktiv)
                                            result1.foundPC(PCName, addr_main, productId);
                                    }
                                }
                                else {
                                    String tempName = canGetNameAndAvatar(addr);
                                    if (tempName != null) {
                                        PCName = tempName;
                                        addr_main = addr;
                                        if (Server_aktiv)
                                            result1.foundPC(PCName, addr_main, productId);
                                    }
                                }
                            }
                        }
                    }
                    catch (Exception ignored){

                    }
                }
            } catch (Exception ignored) {
            }
        });
        thread.start();
        new Thread(() -> {
            while (Server_aktiv) {
                NetworkManagement.sendBroadcast("MobileFileWire\nPort:" + String.valueOf(ServerService.getPort()) + "\n" + NetworkManagement.getAllIpAddress(context), 36000, context);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return this;
    }

    public PCFinder stop_UDP_Server()
    {
        Server_aktiv = false;
        return this;
    }




    public static String canGetNameAndAvatar(String connection) {
        String link = connection + "getAvatarAndName";
        link = link.replaceAll(" ", "%20");
        try {
            HttpClient client = new DefaultHttpClient();
            HttpParams httpParams = client.getParams();
            httpParams.setParameter(
                    CoreConnectionPNames.CONNECTION_TIMEOUT, 500);
            HttpGet request = new HttpGet();
            request.setURI(new URI(link));
            HttpResponse response = client.execute(request);
            BufferedReader in = new BufferedReader(new
                    InputStreamReader(response.getEntity().getContent()));

            StringBuffer sb = new StringBuffer("");
            String line="";

            while ((line = in.readLine()) != null) {
                sb.append(line);
                break;
            }
            in.close();
            String result = sb.toString();
            result = result.substring(result.indexOf("<body>") + 6, result.indexOf("</body>"));
            JSONObject json = new JSONObject(result);
            if (json != null) {
                return json.getString("name");
            }
        }
        catch (Exception ignored){
            return null;
        }
        return null;
    }
}
