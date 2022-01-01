package com.sugarsnooper.filetransfer.ConnectToPC.FTP;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.os.*;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.sugarsnooper.filetransfer.NetworkManagement;
import com.sugarsnooper.filetransfer.R;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import org.swiftp.Globals;

import static com.sugarsnooper.filetransfer.Strings.no_hardware;
import static com.sugarsnooper.filetransfer.Strings.not_connected;
import static com.sugarsnooper.filetransfer.Strings.something_wrong;

public class PcConnectionStatusFragment extends Fragment implements Runnable {
    private String Address = null;
    private boolean connected = false;
    private ExtendedFloatingActionButton startStop;
    private ExtendedFloatingActionButton connection_Link;
    private View root;
    protected static boolean serverStarted = false;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pc_connection_status_ftp_fragment, container, false);
    }
    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                BlurView bv = root.findViewById(R.id.blurviewbackground);
                bv.setVisibility(View.VISIBLE);
                bv.setupWith((ViewGroup) getActivity().getWindow().getDecorView().getRootView())
                        .setBlurAutoUpdate(true)
                        .setBlurRadius(15f)
                        .setHasFixedTransformationMatrix(false)
                        .setBlurAlgorithm(new RenderScriptBlur(getContext()))
                        .setBlurEnabled(true);
                ExtendedFloatingActionButton openStorageSettings = root.findViewById(R.id.manage_all_files_button);
                openStorageSettings.setOnClickListener((o) -> {
                    startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
                });

            }
            else {
                BlurView bv = root.findViewById(R.id.blurviewbackground);
                bv.setVisibility(View.GONE);
                bv.setBlurEnabled(false);
            }
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) view.findViewById(R.id.ipaddressTv)).setText(R.string.wait);
        root = view;
        root.findViewById(R.id.open_wifi_settings_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
        new Thread(this).start();
        startStop = view.findViewById(R.id.start_connection_pc_button);
        connection_Link = view.findViewById(R.id.connection_link_ftp);
        startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serverStarted) {
                    ((TextView) view.findViewById(R.id.instruction_connect_pc_simple_tv)).setText(R.string.start_to_be_able_to_manage_files_using_a_computer);
                    v.setBackgroundColor(getResources().getColor(R.color.button_color_green));
                    startStop.setTextColor(getResources().getColor(R.color.black));
                    startStop.setIcon(getResources().getDrawable(R.drawable.ic_baseline_play_arrow_24));
                    startStop.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.black)));
                    startStop.setText(R.string.start);
                    connection_Link.setVisibility(View.GONE);
                    stop_server();
                    serverStarted = false;
                }
                else {
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            start_server();
//                        }
//                    }).start();

                    final Dialog dialog = new Dialog(requireActivity(), R.style.PauseDialog);
                    dialog.setCancelable(true);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.setContentView(R.layout.transfer_to_pc_dialog_settings);
                    Window window = dialog.getWindow();
                    WindowManager.LayoutParams wlp = window.getAttributes();
                    wlp.gravity = Gravity.BOTTOM;
                    window.setAttributes(wlp);
                    window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    dialog.show();
                    RadioButton openServer = dialog.findViewById(R.id.open_security_selection_pc_transfer_radio);
                    openServer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                Security.clearPassword(getContext());
                                Security.clearUsername(getContext());
                            }
                            if (!isChecked) {
                                dialog.findViewById(R.id.pc_share_security_username).setVisibility(View.VISIBLE);
                                dialog.findViewById(R.id.pc_share_security_password).setVisibility(View.VISIBLE);
                                dialog.findViewById(R.id.pc_share_security_password_and_username_info).setVisibility(View.VISIBLE);
                            }
                            else{
                                dialog.findViewById(R.id.pc_share_security_username).setVisibility(View.GONE);
                                dialog.findViewById(R.id.pc_share_security_password).setVisibility(View.GONE);
                                dialog.findViewById(R.id.pc_share_security_password_and_username_info).setVisibility(View.GONE);
                            }
                        }
                    });
                    RadioButton closeServer = dialog.findViewById(R.id.closed_security_selection_pc_transfer_radio);
                    closeServer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                dialog.findViewById(R.id.pc_share_security_username).setVisibility(View.VISIBLE);
                                dialog.findViewById(R.id.pc_share_security_password).setVisibility(View.VISIBLE);
                                dialog.findViewById(R.id.pc_share_security_password_and_username_info).setVisibility(View.VISIBLE);
                            }
                            else{
                                dialog.findViewById(R.id.pc_share_security_username).setVisibility(View.GONE);
                                dialog.findViewById(R.id.pc_share_security_password).setVisibility(View.GONE);
                                dialog.findViewById(R.id.pc_share_security_password_and_username_info).setVisibility(View.GONE);
                            }
                        }
                    });
                    if (Security.getUsername(getContext()) == null || Security.getPassword(getContext()) == null){
                        Security.clearUsername(getContext());
                        Security.clearPassword(getContext());
                        openServer.setChecked(true);
                    }
                    else {
                        ((EditText) dialog.findViewById(R.id.pc_share_security_username)).setText(Security.getUsername(getContext()));
                        ((EditText) dialog.findViewById(R.id.pc_share_security_password)).setText(Security.getPassword(getContext()));
                        closeServer.setChecked(true);
                    }
                    dialog.findViewById(R.id.button_cancel_pc_connection).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.findViewById(R.id.button_connect_pc_connection).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (closeServer.isChecked()){
                                String unm = ((EditText) dialog.findViewById(R.id.pc_share_security_username)).getText().toString();
                                String pwd = ((EditText) dialog.findViewById(R.id.pc_share_security_password)).getText().toString();
                                boolean credentials_correct = checkCredentials(unm) && checkCredentials(pwd);
                                if (credentials_correct) {
                                    Security.setUsername(getContext(), unm);
                                    Security.setPassword(getContext(), pwd);
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            start_server();
                                        }
                                    }).start();
                                    dialog.dismiss();
                                }
                                else{
                                    Toast.makeText(getContext(), "Please check username and password", Toast.LENGTH_LONG).show();
                                }
                            }
                            else if (openServer.isChecked()){
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        start_server();
                                    }
                                }).start();
                                dialog.dismiss();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((TextView) root.findViewById(R.id.instruction_connect_pc_simple_tv)).setText(R.string.start_to_be_able_to_manage_files_using_a_computer);
        startStop.setBackgroundColor(getResources().getColor(R.color.button_color_green));
        startStop.setTextColor(getResources().getColor(R.color.black));
        startStop.setIcon(getResources().getDrawable(R.drawable.ic_baseline_play_arrow_24));
        startStop.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.black)));
        startStop.setText(R.string.start);
        connection_Link.setVisibility(View.GONE);
        stop_server();
        serverStarted = false;
    }

    private boolean checkCredentials(String s) {
        if (s == null) // checks if the String is null {
            return false;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            // checks whether the character is neither a letter nor a digit
            // if it is neither a letter nor a digit then it will return false
            if ((!Character.isLetterOrDigit(s.charAt(i)))) {
                return false;
            }
        }
        if (len >= 4 && len <= 16)
            return true;
        else
            return false;
    }

    private void start_server() {
        if (connected) {



//            ServerService.newInstance(requireContext(), null, new ServerStatus() {
//                @Override
//                public void onConnectionSuccess(String s) {
//                    //Snackbar.make(root, s, Snackbar.LENGTH_INDEFINITE).show();
//
//                }
//
//                @Override
//                public void onConnectionFailed(String wifi_disabled) {
//                    requireActivity().stopService(new Intent(requireActivity(), ServerService.class));
//                }
//            });



            requireActivity().startService(new Intent(requireActivity(), FTPServerService.class));
            while (FTPServerService.serverThread == null || !FTPServerService.serverThread.isAlive() || FTPServerService.port == 0){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Globals.setUsername(Security.getUsername(getContext()));
            Globals.setPassword(Security.getPassword(getContext()));
            serverStarted = true;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    startStop.setBackgroundColor(getResources().getColor(R.color.button_color_red));
                    startStop.setTextColor(getResources().getColor(R.color.white));
                    startStop.setIcon(getResources().getDrawable(R.drawable.ic_baseline_stop_24));
                    startStop.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    startStop.setText(R.string.stop);
                }
            });
        }
        else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), not_connected, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void stop_server(){
        //requireActivity().stopService(new Intent(requireActivity(), ServerService.class));
        requireActivity().stopService(new Intent(requireActivity(), FTPServerService.class));
        FTPServerService.serverThread = null;
        FTPServerService.port = 0;
        Globals.setUsername(null);
        Globals.setPassword(null);
    }


    @Override
    public void run() {
        while (true) {
            try {
                String result = null;
                String address = NetworkManagement.getIpAddress(getContext());
                if (address.trim().contains(something_wrong + "!")) {
                    result = something_wrong;
                    connected = false;
                } else if (address.trim().contains(no_hardware)) {
                    result = no_hardware;
                    connected = false;
                } else if (address.trim().isEmpty()) {
                    result = not_connected;
                    connected = false;
                } else {
                    connected = true;
                    result = address;
                }
                this.Address = result;

                try {
                    String ssid = ((WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getSSID();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (connected)
                                ((TextView) root.findViewById(R.id.ipaddressTv)).setText(ssid + "\n" + Address);
                            else
                                ((TextView) root.findViewById(R.id.ipaddressTv)).setText(Address);
                        }
                    });
                } catch (Exception e) {
                }
                try {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (serverStarted) {
                                if (connected) {
                                    ((TextView) root.findViewById(R.id.instruction_connect_pc_simple_tv)).setText(R.string.instruction_connect_to_pc_simple);
                                    String s = "ftp://" + Address + ":" + FTPServerService.port + "/";
                                    connection_Link.setText(s);
                                    connection_Link.setVisibility(View.VISIBLE);
                                } else {
                                    ((TextView) root.findViewById(R.id.instruction_connect_pc_simple_tv)).setText(R.string.instruction_connect_to_pc_simple);
                                    connection_Link.setText(not_connected);
                                    connection_Link.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    });
                } catch (Exception e) {

                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            catch (NullPointerException e){

            }
        }
    }

}
