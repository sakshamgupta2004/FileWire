package com.sugarsnooper.filetransfer.Client;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.zxing.WriterException;
import com.sugarsnooper.filetransfer.NetworkManagement;
import com.sugarsnooper.filetransfer.QRCodeFormatter;
import com.sugarsnooper.filetransfer.R;
import com.sugarsnooper.filetransfer.Strings;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import static com.sugarsnooper.filetransfer.Client.RecieveActivity.mReservation;
import static com.sugarsnooper.filetransfer.Strings.no_hardware;
import static com.sugarsnooper.filetransfer.Strings.something_wrong;

public class EnableHotspot_ShowQrCode extends Fragment {
    private static final String CHANNEL_ID = "FileWire Connected in Background";
    public static boolean start_Receiving = false;
    private View view;
    public static String ipAddress_Download_From = null;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        requireActivity().getActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);
        View root = inflater.inflate(R.layout.shoq_qr_code_layout, container, false);
        requireActivity().setTitle("QR Code Authentication");
        ImageView bitmap = root.findViewById(R.id.bitmap);
        ExtendedFloatingActionButton switch_5ghz = root.findViewById(R.id.switch_to_5ghz_hotspot);
        if (!((WifiManager)getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE)).is5GHzBandSupported())
            switch_5ghz.setVisibility(View.GONE);
        switch_5ghz.setOnClickListener(v -> new AlertDialog.Builder(getActivity())
                .setTitle("Important")
                .setMessage("You will need to manually Enable WiFi-Hotspot in 5GHz mode and then, on the sending device, you will need to enter the hotspot password manually or you may try scanning the QR Code in the hotspot settings menu.\n\nNote:- The sender also needs to support 5GHz band, otherwise, the app will fail to connect")
                .setPositiveButton("Network Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mReservation != null) {
                            mReservation.close();
                            mReservation = null;
                        }
                        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
                        intent.setComponent(cn);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity( intent);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show());
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        float dip = 90f;
        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
        ViewGroup.LayoutParams params = bitmap.getLayoutParams();
        if (width > height){
            params.width = height - px;
            params.height = height - px;
        }
        else {
            params.height = width - px;
            params.width = width - px;
        }
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;

    }

    private boolean showWritePermissionSettings(Context context, Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if (!Settings.System.canWrite(context)) {
                new AlertDialog.Builder(activity).setTitle("Provide Permission")
                        .setMessage("Please provide permission to Turn on WiFi Hotspot")
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                intent.setData(Uri.parse("package:" + context.getPackageName()));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                activity.startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(requireActivity(), "You need to provide Permission to use the app.", Toast.LENGTH_LONG).show();
                                ((RecieveActivity) requireActivity()).gotoHome();
                            }
                        })
                        .setCancelable(false)
                        .show();

                return false;
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Strings.GPS_PERMISSION){
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    new GpsUtils(requireActivity()).turnGPSOn(new GpsUtils.onGpsListener() {
                        @Override
                        public void gpsStatus(boolean isGPSEnable) {
                            if (isGPSEnable)
                                make_local_only_hotspot(requireActivity());
                        }
                    });
                }
            }
            else {
                Toast.makeText(requireActivity(), "You need to provide Permission to use the app.", Toast.LENGTH_LONG).show();
                ((RecieveActivity) requireActivity()).gotoHome();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isPaused = false;
        if (!hasToStartFragmentOnResume)
            startHotspot(0);
        else
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());
                    notificationManager.cancelAll();
                    ((RecieveActivity) getActivity()).getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_to_left_in, R.anim.right_to_left_out).replace(R.id.fragment_container, TransferProgress.newInstance(current_ip, start_Receiving, false)).commit();
                }
            });
    }

    private void startHotspot(int tries) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                new GpsUtils(requireActivity()).turnGPSOn(new GpsUtils.onGpsListener() {
                    @Override
                    public void gpsStatus(boolean isGPSEnable) {
                        if (isGPSEnable)
                            make_local_only_hotspot(requireActivity());
                    }
                });
            }
            else
                requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, Strings.GPS_PERMISSION);
        }
        else if (showWritePermissionSettings(requireContext(), requireActivity()) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.O)){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    WifiManager wifiManager = (WifiManager) requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    wifiManager.setWifiEnabled(false);
                    while (wifiManager.isWifiEnabled());
                    if (ApManager.configApState(requireActivity(), true)){
                        try {
                            WifiConfiguration HotSpotConfig = ApManager.getWifiApConfiguration(requireContext());
                            if (HotSpotConfig != null) {
                                make_qr_code(HotSpotConfig.SSID, HotSpotConfig.preSharedKey, requireActivity(), view);
                            }
                            else{
                                Toast.makeText(requireActivity(), "Failed to Initialize\nPlease try again", Toast.LENGTH_LONG).show();
                                ((RecieveActivity) requireActivity()).gotoHome();
                            }
                        }
                        catch (Exception e){
                        }
                    }
                    else{
                        try {
                            Toast.makeText(requireActivity(), "Failed to Initialize\nPlease try again", Toast.LENGTH_LONG).show();
                            ((RecieveActivity) requireActivity()).gotoHome();
                        }
                        catch (Exception e){
                        }
                    }
                }
            }).start();
        }
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    WifiManager wifiManager = (WifiManager) requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    wifiManager.setWifiEnabled(false);
                    while (wifiManager.isWifiEnabled());
                    if (ApManager.configApState(requireActivity(), true)){
                        try {
                            WifiConfiguration HotSpotConfig = ApManager.getWifiApConfiguration(requireContext());
                            if (HotSpotConfig != null) {
                                String address = NetworkManagement.getIpAddress(getContext());
                                if (address.trim().contains(something_wrong + "!")
                                        || address.trim().contains(no_hardware)
                                        || address.trim().isEmpty()) {
                                    if (tries < 3) {
                                        Toast.makeText(requireActivity(), "Failed to Initialize\nTrying again in 3 seconds", Toast.LENGTH_LONG).show();
                                        Thread.sleep(3000);
                                        try {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    startHotspot(tries + 1);
                                                }
                                            });
                                        }
                                        catch (Exception e) {
                                        }
                                    }
                                    else {
                                        Toast.makeText(requireActivity(), "Failed to Initialize\nPlease try again", Toast.LENGTH_LONG).show();
                                        ((RecieveActivity) requireActivity()).gotoHome();
                                    }
                                }
                                else {
                                    make_qr_code(HotSpotConfig.SSID, HotSpotConfig.preSharedKey, requireActivity(), view);
                                }
                            }
                            else{
                                Toast.makeText(requireActivity(), "Failed to Initialize\nPlease try again", Toast.LENGTH_LONG).show();
                                ((RecieveActivity) requireActivity()).gotoHome();
                            }
                        }
                        catch (Exception e){
                        }
                    }
                    else{
                        try {
                            Toast.makeText(requireActivity(), "Failed to Initialize\nPlease try again", Toast.LENGTH_LONG).show();
                            ((RecieveActivity) requireActivity()).gotoHome();
                        }
                        catch (Exception e){
                        }
                    }
                }
            }).start();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void make_local_only_hotspot(Activity activity) {

        if (mReservation == null) {
            WifiManager.LocalOnlyHotspotCallback callback = new WifiManager.LocalOnlyHotspotCallback() {
                @Override
                public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                    super.onStarted(reservation);
                    mReservation = reservation;

                    String unm = reservation.getWifiConfiguration().SSID;
                    String pass = reservation.getWifiConfiguration().preSharedKey;
                    make_qr_code(unm, pass, activity, view);
                }

                @Override
                public void onFailed(int reason) {
                    super.onFailed(reason);
                    Log.e("ERROR", String.valueOf(reason));
                    if (WifiManager.LocalOnlyHotspotCallback.ERROR_INCOMPATIBLE_MODE == reason) {
//                        try {
//                            Toast.makeText(activity, "Failed to Initialize\nPlease check if your hotspot is turned on", Toast.LENGTH_LONG).show();
//                            ((RecieveActivity) activity).gotoHome();
//                        } catch (Exception e) {
//                        }

                        ((ExtendedFloatingActionButton) view.findViewById(R.id.switch_to_5ghz_hotspot)).setText(R.string.open_hotspot_settings);
                        view.findViewById(R.id.bitmap).setVisibility(View.GONE);
                        ((TextView) view.findViewById(R.id.text_credentials)).setText(R.string.hotspot_already_turned_on);
                        view.findViewById(R.id.text_scan_instruction).setVisibility(View.GONE);
                        startServer(activity);

                    } else {
                        try {
                            Toast.makeText(activity, "Failed to Initialize\nPlease try again", Toast.LENGTH_LONG).show();
                            ((RecieveActivity) activity).gotoHome();
                        } catch (Exception e) {
                        }
                    }
                }
            };
            try {
                ((WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).startLocalOnlyHotspot(callback, null);
            } catch (IllegalStateException e) {
                Toast.makeText(activity, "Failed to Initialize\nPlease try again", Toast.LENGTH_LONG).show();
                ((RecieveActivity) activity).gotoHome();
            }
        }
    }

    private void make_qr_code(String unm, String pass, Activity activity, View view){
        try {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        QRGEncoder qrgEncoder = new QRGEncoder(QRCodeFormatter.formatSSIDAndPass(unm, pass), null, QRGContents.Type.TEXT, 128);
                        Bitmap bitmap = null;
                        try {
                            bitmap = qrgEncoder.encodeAsBitmap();
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }
                        ImageView qrImage = view.findViewById(R.id.bitmap);
                        TextView textView = view.findViewById(R.id.text_credentials);
                        ((ExtendedFloatingActionButton) view.findViewById(R.id.switch_to_5ghz_hotspot)).setText(R.string.switch_to_5ghz);
                        qrImage.setVisibility(View.VISIBLE);
                        qrImage.setElevation(10);
                        qrImage.setImageBitmap(bitmap);
                        String creadentials = "Or\nConnect via WiFi\nSSID:- " + unm + "\nKey:- " + pass;
                        textView.setText(creadentials);

                        startServer(activity);
                    } catch (Exception e) {
                    }
                }
            });
        }
        catch (Exception e){
        }
    }

    private void startServer(Activity activity) {
        com.sugarsnooper.filetransfer.Server.ServerService.newInstance(activity, null, new com.sugarsnooper.filetransfer.Server.ServerStatus() {
            @Override
            public void onConnectionSuccess(String s) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (ipAddress_Download_From == null){
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        String current_ip = ipAddress_Download_From;
                        ipAddress_Download_From = null;
                        EnableHotspot_ShowQrCode.this.current_ip = current_ip;
                        if (!isPaused)
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((RecieveActivity) activity).getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.right_to_left_in, R.anim.right_to_left_out).replace(R.id.fragment_container, TransferProgress.newInstance(current_ip, start_Receiving, false)).commit();
                                }
                            });
                        else {
//                            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(activity, "FileWire has established a connection to another Device. \n Please return to the app to transfer files", Toast.LENGTH_LONG).show());
                            createNotificationChannel();
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, CHANNEL_ID)
                                    .setSmallIcon(R.mipmap.ic_logo_round)
                                    .setContentTitle("FileWire Connected")
                                    .setContentText("FileWire has established a connection to another Device. Please return to the app to transfer files")
                                    .setStyle(new NotificationCompat.BigTextStyle()
                                            .bigText("FileWire has established a connection to another Device. \n Please return to the app to transfer files"))
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setPriority(NotificationCompat.PRIORITY_HIGH);
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(activity);
                            notificationManager.notify(101, builder.build());
                            hasToStartFragmentOnResume = true;
                        }
                    }
                }).start();
            }

            @Override
            public void onConnectionFailed(String wifi_disabled) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "Some Error has Occurred", Toast.LENGTH_LONG).show();
                        ((RecieveActivity)activity).gotoHome();
                    }
                });
            }

            private void createNotificationChannel() {
                // Create the NotificationChannel, but only on API 26+ because
                // the NotificationChannel class is new and not in the support library
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    CharSequence name = "FileWire Connected in Background";
                    String description = "Occurs when FileWire gets Connected in Background";
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                    channel.setDescription(description);
                    // Register the channel with the system; you can't change the importance
                    // or other notification behaviors after this
                    NotificationManager notificationManager = activity.getSystemService(NotificationManager.class);
                    notificationManager.createNotificationChannel(channel);
                }
            }
        });
    }

    String current_ip = "";
    boolean isPaused = false;
    boolean hasToStartFragmentOnResume = false;
    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;

    }
}
