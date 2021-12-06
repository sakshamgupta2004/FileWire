package com.sugarsnooper.filetransfer.Server;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.sugarsnooper.filetransfer.Client.TransferProgress;
import com.sugarsnooper.filetransfer.QRCodeFormatter;
import com.sugarsnooper.filetransfer.R;
import com.sugarsnooper.filetransfer.Strings;
import com.sugarsnooper.filetransfer.TinyDB;
import com.thanosfisherman.wifiutils.WifiUtils;
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode;
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener;

import java.util.ArrayList;
import java.util.List;

import static com.sugarsnooper.filetransfer.Server.Send_Activity.gateway;
import static com.sugarsnooper.filetransfer.Server.Send_Activity.hostedFiles;
import static com.sugarsnooper.filetransfer.Server.Send_Activity.ip;

public class InitializeServerFragment extends Fragment {
    private boolean hasClientVerified = false;
    private ProgressBar spinner;
    private TextView initStatus;
    private ExtendedFloatingActionButton buttonSendMore;


    public static Fragment newInstance(String intentResult, boolean start_Sending) {
        Bundle args = new Bundle();
        args.putString("WiFiConfig", intentResult);
        args.putBoolean("start_Sending", start_Sending);
        InitializeServerFragment initializeServerFragment = new InitializeServerFragment();
        initializeServerFragment.setArguments(args);
        return initializeServerFragment;
    }
    int tries_to_connect = 0;
    private void connect(String result){

        new Thread(new Runnable() {
            @Override
            public void run() {
                String networkSSID = null;
                try {
                    networkSSID = QRCodeFormatter.getSSIDfromQRCodeResult(result);
                } catch (QRCodeFormatter.QRCodeFormatException e) {
                    e.printStackTrace();
                }
                String networkPass = null;
                try {
                    networkPass = QRCodeFormatter.getPassfromQRCodeResult(result);
                } catch (QRCodeFormatter.QRCodeFormatException e) {
                    e.printStackTrace();
                }
                WifiConfiguration conf = new WifiConfiguration();
                conf.SSID = "\"" + networkSSID + "\"";
                if (networkPass.equalsIgnoreCase("null")) {
                    conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                } else {
                    conf.preSharedKey = "\"" + networkPass + "\"";
                }
                try {

                    WifiManager wifiManager = (WifiManager) requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);


                    if (!wifiManager.isWifiEnabled()) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                            new Handler(Looper.getMainLooper())
                                    .post(new Runnable() {
                                        @Override
                                        public void run() {

                                            new AlertDialog.Builder(requireActivity())
                                                    .setTitle("Enable WiFi")
                                                    .setMessage("Please turn on WiFi." +
                                                            "\n" +
                                                            getString(R.string.app_name) +
                                                            " requires you to turn WiFi in order to transfer files.")
                                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            requireActivity().onNavigateUp();
                                                        }
                                                    })
                                                    .setPositiveButton("WiFi Settings", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                                                            startActivityForResult(panelIntent, 2000);
                                                        }
                                                    }).show();
                                        }
                                    });



                        }
                        else {
                            wifiManager.setWifiEnabled(true);
                        }

                    }

                    while (!wifiManager.isWifiEnabled()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                        String finalNetworkSSID = networkSSID;
                        String finalNetworkPass = networkPass;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                WifiUtils.withContext(requireContext().getApplicationContext())
                                        .connectWith(finalNetworkSSID, finalNetworkPass)
                                        .onConnectionResult(new ConnectionSuccessListener() {
                                            @Override
                                            public void success() {
                                                try {
                                                    requireActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            //Toast.makeText(requireContext(), "SUCCESS!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                                catch (IllegalStateException ignored) {

                                                }
                                            }

                                            @Override
                                            public void failed(@NonNull ConnectionErrorCode errorCode) {
                                                try {
                                                    requireActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(requireContext(), "Could not Connect" +
                                                                    "\n" +
                                                                    "Retrying", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                                catch (IllegalStateException ignored) {

                                                }
                                            }
                                        })
                                        .start();
                            }
                        });

                    }
                    else {
                        int netId = wifiManager.addNetwork(conf);
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(netId, true);
                        wifiManager.reconnect();
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//                                WifiUtils.withContext(requireContext()).connectWith(conf.SSID, conf.preSharedKey).onConnectionResult(new ConnectionSuccessListener() {
//                                    @Override
//                                    public void success() {
//
//                                    }
//
//                                    @Override
//                                    public void failed(@NonNull ConnectionErrorCode errorCode) {
//
//                                    }
//                                }).start();
//                            }
//                        });

                    }
                    while (wifiManager.getConnectionInfo() == null) {
                        Log.e("ConnectionInfo", "NULL");
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    while (wifiManager.getConnectionInfo().getSSID() == null) {
                        Log.e("SSID", "NULL");
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    while (!wifiManager.getConnectionInfo().getSSID().equals("\"" + networkSSID + "\"")) {
                        Log.e("ConnectionInfo", wifiManager.getConnectionInfo().getSSID());
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            tries_to_connect += 1;
                            if (tries_to_connect < 10 && tries_to_connect > 1) {
                                connect(result);
                            }
                        }

                    }
                    while (wifiManager.getDhcpInfo().gateway == 0) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    gateway = intToIp(wifiManager.getDhcpInfo().gateway);
                    ip = intToIp(wifiManager.getDhcpInfo().ipAddress);
                    startServer(hostedFiles);

                }
                catch (IllegalStateException ignored) {

                }
            }
        }).start();
    }


    public static String intToIp(int addr) {
        return  ((addr & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.initialize_server_fragment, container, false);
        buttonSendMore = root.findViewById(R.id.button_select_more_files);
        ((CollapsingToolbarLayout) requireActivity().findViewById(R.id.toolbar_layout)).setTitle(getString (R.string.initializing) );
        spinner = root.findViewById(R.id.spinner);
        initStatus = root.findViewById(R.id.server_initialization_status);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        ViewGroup.LayoutParams params = spinner.getLayoutParams();
        if (width > height){
            params.width = height;
            params.height = height;
        }
        else {
            params.height = width;
            params.width = width;
        }
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null){
            String result = getArguments().getString("WiFiConfig");
            connect(result);
        }
    }

    private void startServer(List<String[]> hostedFiles){


        try {
            ServerService.newInstance(requireContext(), hostedFiles, new ServerStatus() {
                @Override
                public void onConnectionSuccess(String ipAddress) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            check_for_client_status(ipAddress);
                            while (!hasClientVerified) {
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            requireActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    spinner.setVisibility(View.GONE);
                                    buttonSendMore.setVisibility(View.VISIBLE);
                                    buttonSendMore.setOnClickListener(v ->
                                            startActivity(new Intent(requireActivity(), Send_Activity.class).putExtra(Strings.FileSelectionRequest, true).putExtra("HOST", "http://" + gateway + ":" + port + "/")));
                                    TinyDB tinyDB = new TinyDB(getContext());
                                    ArrayList<String> timestamps =
                                            tinyDB.getListString("transferDates");

                                    if (!timestamps.contains(Strings.dateString))
                                    {
                                        timestamps.add(Strings.dateString);
                                    }
                                    tinyDB.putListString("transferDates", timestamps);
                                    ((CollapsingToolbarLayout) requireActivity().findViewById(R.id.toolbar_layout)).setTitle(getString(R.string.inProgressTitile));
                                    initStatus.setText(R.string.transfer_in_progress);
                                    String current_ip = "http://" + gateway + ":" + port;
                                    (getActivity()).getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_to_left_in, R.anim.right_to_left_out).replace(R.id.fragment_container, TransferProgress.newInstance(current_ip, false, false), "TRANSFER_PROGRESS").commit();



//                                    new Thread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            while (!isTransferComplete){
//                                                try {
//                                                    Thread.sleep(100);
//                                                } catch (InterruptedException e) {
//                                                    e.printStackTrace();
//                                                }
//                                            }
//                                            isTransferComplete = false;
//                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    try {
//                                                        requireActivity().setTitle(getString(R.string.success));
//                                                        initStatus.setText(R.string.complete);
//                                                        new AlertDialog.Builder(requireActivity())
//                                                                .setCancelable(false)
//                                                                .setTitle("Transfer Complete")
//                                                                .setMessage("Selected Files have been transferred")
//                                                                .setNeutralButton("Close", new DialogInterface.OnClickListener() {
//                                                                    @Override
//                                                                    public void onClick(DialogInterface dialog, int which) {
//                                                                        isTransferComplete = false;
//                                                                        requireActivity().onNavigateUp();
//                                                                    }
//                                                                })
//                                                                .show();
//                                                    }catch (Exception e){
//
//                                                    }
//                                                }
//                                            });
//                                        }
//                                    }).start();
                                }
                            });
                        }
                    }).start();
                }

                @Override
                public void onConnectionFailed(String reason) {

//                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                        @Override
//                        public void run() {
//                           Toast.makeText(getContext(), reason, Toast.LENGTH_LONG).show();
//                        }
//                    });
                    try {
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                requireActivity().stopService(new Intent(requireActivity(), ServerService.class));
                                new AlertDialog.Builder(requireActivity()).setTitle("Failed").setMessage("Some error has occurred while initializing\nPlease try again").setCancelable(false).setNeutralButton("Go Back", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ((Send_Activity) requireActivity()).onNavigateUp();
                                    }
                                }).show();
                            }
                        });
                    }
                    catch (Exception ignored) {

                    }
                }
            });
        }
        catch (Exception e){

        }
    }


    int port = 1234;
    int triesforeachport = 5;
    int treisforcurrentport = 0;

    private void check_for_client_status(String ipAddress) {
        hasClientVerified = false;
        new verify(new verify.AsynResponse() {
            @Override
            public void processFinish(String out) {
                try {
                    if (out.substring(out.indexOf("<body>") + 6, out.indexOf("</body>")).equalsIgnoreCase("OK")){
                        hasClientVerified = true;
                    }
                    else{
                        if (treisforcurrentport < triesforeachport) {
                            treisforcurrentport++;
                        }else{
                            treisforcurrentport = 0;
                            if (port <= 9000) {
                                port++;
                            }
                            else{
                                port = 1234;
                            }
                        }
                        check_for_client_status(ipAddress);
                    }
                }
                catch (Exception e){
                    if (treisforcurrentport < triesforeachport) {
                        treisforcurrentport++;
                    }else{
                        treisforcurrentport = 0;
                        if (port <= 9000) {
                            port++;
                        }
                        else{
                            port = 1234;
                        }
                    }

                    check_for_client_status(ipAddress);
                }
            }
        }, getArguments().getBoolean("start_Sending")).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, gateway + ":" + port, ipAddress);
    }
}
