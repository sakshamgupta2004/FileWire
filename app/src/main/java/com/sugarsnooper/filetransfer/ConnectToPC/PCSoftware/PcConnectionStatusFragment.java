package com.sugarsnooper.filetransfer.ConnectToPC.PCSoftware;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.sugarsnooper.filetransfer.*;
import com.sugarsnooper.filetransfer.Client.TransferProgress;
import com.sugarsnooper.filetransfer.Server.ServerService;
import com.sugarsnooper.filetransfer.Server.ServerStatus;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import static android.view.View.GONE;
import static com.sugarsnooper.filetransfer.Application.avatars;
import static com.sugarsnooper.filetransfer.Strings.no_hardware;
import static com.sugarsnooper.filetransfer.Strings.something_wrong;

public class PcConnectionStatusFragment extends androidx.fragment.app.Fragment {
    private View root;
    private boolean serverStarted = false;
    private boolean serverStartFailed = false;
    private boolean pc_search_cancelled = false;
    private List<Thread> PCSearchThreadList;
    private HashMapWithListener<String, String> NearbyPCList;
    private static String connection_string = null;
    private static boolean startSending = false;
    private final PCFinder finder = new PCFinder();
    private ArrayList<PC> pairedPCList;

    public PcConnectionStatusFragment(String pc_connection_string, boolean start_sending) {
        connection_string = pc_connection_string;
        startSending = start_sending;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        finder.stop_UDP_Server();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pc_connection_status_software_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        root = getActivity().getWindow().getDecorView().getRootView();


        view.findViewById(R.id.scan_qr_code_pc_connection_software_button).setOnClickListener(v -> {
                    IntentIntegrator.forSupportFragment(PcConnectionStatusFragment.this).setBeepEnabled(false).setOrientationLocked(false).setBarcodeImageEnabled(false).setPrompt("Scan QR code on PC to connect").initiateScan();
                }
        );
        view.findViewById(R.id.find_nearby_pc_button).setOnClickListener(v -> {
            Dialog dialog = new Dialog(getActivity(), R.style.PauseDialog);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.search_nearby_pcs_dialog);
            dialog.setCancelable(true);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    pc_search_cancelled = true;
                }
            });
            dialog.setCanceledOnTouchOutside(true);
            dialog.findViewById(R.id.dialog_bg).setOnClickListener(v1 -> dialog.dismiss());
            Window window = dialog.getWindow();
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
            window.setAttributes(wlp);
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            dialog.show();
            int nightModeFlags =
                    this.getResources().getConfiguration().uiMode &
                            Configuration.UI_MODE_NIGHT_MASK;
            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    Glide.with(getContext())
                            .load(Uri.parse("file:///android_asset/scanning.gif"))
                            .centerCrop()
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    startSearching(dialog);
                                    return false;
                                }
                            })
                            .into((ImageView) dialog.findViewById(R.id.scanning_wifi_gif));
                    break;

                case Configuration.UI_MODE_NIGHT_NO:

                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    Glide.with(getContext())
                            .load(Uri.parse("file:///android_asset/scanning_light.gif"))
                            .centerCrop()
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    startSearching(dialog);
                                    return false;
                                }
                            })
                            .into((ImageView) dialog.findViewById(R.id.scanning_wifi_gif));
                    break;
            }

        });
        if (connection_string != null) {
            try {
                serverStartFailed = true;
                QRCodeFormatter.continueIfItIsPcFormat(connection_string);
                tryToConnectToPc(connection_string);
                connection_string = null;
            }
            catch (Exception e) {
                Toast.makeText(getContext(), "Some Error has Occurred", Toast.LENGTH_LONG).show();
            }
        }
        else {
            ServerService.newInstance(getContext(), null, new ServerStatus() {
                @Override
                public void onConnectionSuccess(String s) {
                    serverStarted = true;
                }

                @Override
                public void onConnectionFailed(String wifi_disabled) {
                    serverStartFailed = true;
                    try {
                        getActivity().stopService(new Intent(getContext(), ServerService.class));
                    } catch (Exception ignored) {

                    }
                }
            });
        }

        createPairPCList(view);
    }

    private void createPairPCList(View view) {

        ListView listView = view.findViewById(R.id.pair_pc_list_view);
        TinyDB td = new TinyDB(getContext());
        pairedPCList = td.getListObject(Strings.pairedPC_preference_key, PC.class);
        for (PC pc: pairedPCList) {
            pc.setActive(false);
        }
        finder.runUdpServer(new PCFinder.result() {
            @Override
            public void foundPC(String name, String address, String productId) {
                Log.e("PC Found", name + ";" + address + ";" + productId);

                for (PC pc : pairedPCList) {
                    if (pc.getProductId().equals(productId)) {
//                        finder.stop_UDP_Server();
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
//                                tryToConnectToPc(address + "\n")
                                pc.setActive(true);
                                pc.setPCAddress(address);
                                ((PairPCListAdapter) listView.getAdapter()).notifyDataSetChanged();
                            }
                        });
                        break;
                    }
                }
            }
        }, true);
        if (pairedPCList.size() == 0)
            view.findViewById(R.id.paired_pc_heading).setVisibility(GONE);
        listView.setAdapter(new PairPCListAdapter(getActivity(), pairedPCList));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (pairedPCList.get(i).isActive()) {
                    tryToConnectToPc(pairedPCList.get(i).getPCAddress() + "\n");
                }
                else {
                    Toast.makeText(getContext(), "Selected PC is offline", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void startSearching(Dialog dialog) {
        View bg_retry = dialog.findViewById(R.id.fab_retry_find_pcs_bg);
        ((TextView) dialog.findViewById(R.id.find_nearby_pc_text)).setText(getString(R.string.scanning_for_nearby_pcs));
        bg_retry.setAlpha(0f);
        bg_retry.setVisibility(GONE);
        dialog.findViewById(R.id.fab_retry_find_pcs).setOnClickListener(v -> {
            startSearching(dialog);
        });
        for (int i = 1; i <= 16; i++) {
            FloatingActionButton floatingActionButton = dialog.findViewById(getActivity().getResources().getIdentifier("send_files_dialog_fab" + i, "id", getActivity().getPackageName()));
            Random rnd = new Random();
            floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.argb(255, rnd.nextInt(128) + 128, rnd.nextInt(128) + 128, rnd.nextInt(128) + 128)));
            floatingActionButton.setVisibility(GONE);
            floatingActionButton.setScaleX(0f);
            floatingActionButton.setScaleY(0f);
            TextView pcName = dialog.findViewById(getActivity().getResources().getIdentifier("send_files_dialog_text" + i, "id", getActivity().getPackageName()));
            pcName.setVisibility(GONE);
            pcName.setScaleX(0f);
            pcName.setScaleY(0f);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Integer> usedSpots = new ArrayList<>();
                pc_search_cancelled = false;
                startPCScan();
                boolean scan_complete = false;
                NearbyPCList.setPutListener(new HashMapWithListener.putListener() {
                    @Override
                    public void onPut(Object key, Object value) {
                        int i;
                        Random r = new Random();
                        int drawableId = r.nextInt(avatars.length);
                        Drawable drawable = getResources().getDrawable(avatars[drawableId]);
                        while (true) {
                            boolean containsNewSpot = false;
                            i = new Random().nextInt(16) + 1;
                            for (Integer spot : usedSpots) {
                                if (spot == i) {
                                    containsNewSpot = true;
                                }
                            }
                            if (!containsNewSpot) {
                                if (usedSpots.size() != 0) {
                                    usedSpots.add(i);
                                    break;
                                }
                                else if ( i != 1 && i != 2 && i != 3 && i != 4 && i != 5 && i != 8 && i != 9 && i != 12 && i != 13 && i != 14 && i != 15 && i != 16) {
                                    usedSpots.add(i);
                                    break;
                                }
                            }
                        }
                        int finalI = i;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                FloatingActionButton floatingActionButton = dialog.findViewById(getActivity().getResources().getIdentifier("send_files_dialog_fab" + finalI, "id", getActivity().getPackageName()));
                                floatingActionButton.setImageDrawable(drawable);

                                TextView pcName = dialog.findViewById(getActivity().getResources().getIdentifier("send_files_dialog_text" + finalI, "id", getActivity().getPackageName()));
                                pcName.setText((String) value);
                                pcName.setSelected(true);

                                floatingActionButton.setVisibility(View.VISIBLE);
                                pcName.setVisibility(View.VISIBLE);
                                floatingActionButton.animate().scaleY(1f).scaleX(1f).start();
                                pcName.animate().scaleY(1f).scaleX(1f).start();

                                floatingActionButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                        tryToConnectToPc(((String) key) + "/\n");
                                    }
                                });


                            }
                        });
                    }
                });
                while (!scan_complete) {
                    int numCompleted = 0;
                    for (Thread t : PCSearchThreadList) {
                        if (!t.isAlive()) {
                            numCompleted++;
                        }
                    }
                    if (numCompleted == PCSearchThreadList.size()) {
                        scan_complete = true;
                    }
                }
                if (NearbyPCList.size() == 0) {
                    try {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    bg_retry.setVisibility(View.VISIBLE);
                                    bg_retry.animate().alpha(1f).start();
                                    ((TextView) dialog.findViewById(R.id.find_nearby_pc_text)).setText(getString(R.string.could_not_find_pcs));
                                }
                                catch (Exception e){

                                }
                            }
                        });
                    }
                    catch (Exception e) {

                    }
                }
                Log.e("Completed", "Scan Complete");
            }
        }).start();
    }

    private void startPCScan() {
        PCSearchThreadList = new CopyOnWriteArrayList<>();
        NearbyPCList = new HashMapWithListener<>();
        String add = NetworkManagement.getAllIpAddress(getContext());
//                            add += "192.168.42.129\n";
        boolean connected;
        if (add.trim().contains(something_wrong + "!")) {
            connected = false;
        } else if (add.trim().contains(no_hardware)) {
            connected = false;
        } else if (add.trim().isEmpty()) {
            connected = false;
        } else {
            connected = true;
        }
        if (connected) {
            try {
                String[] addresses = add.split("\n");
                addresses = new HashSet<String>(Arrays.asList(addresses)).toArray(new String[0]);
                for (String address : addresses) {
                    Log.e("Address", address);
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
                            int myIP = Integer.parseInt(address.substring(address.lastIndexOf(".") + 1));
                            for (int def = 0; def <= 10; def++) {
                                int finalDef = def;

                                        for (int j = 0; j < 10; j++) {
                                            int finalJ = j;
                                            Thread thread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                            if (!pc_search_cancelled) {
                                                for (int i = (finalDef * 25); i <= (finalDef * 25) + 25 && i <= 255; i++) {
                                                    if (!pc_search_cancelled) {
                                                        if (i != myIP) {
                                                            String callToAddress = "http://" + address.substring(0, address.lastIndexOf(".")) + "." + i + ":" + String.valueOf(Integer.parseInt(Strings.PCDefaultPort) + finalJ);
                                                            String name = canGetNameAndAvatar(callToAddress);
                                                            if (name != null) {
                                                                Log.e("Call To Ip", name);
                                                                NearbyPCList.put(callToAddress, name);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                            });
                                            PCSearchThreadList.add(thread);
                                            thread.start();
                                    }
                            }
//                        }
//                    }).start();
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                tryToConnectToPc(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    Snackbar snackbar = null;
    ProgressDialog progressDialog = null;
    boolean hasToShowProgressDialog = true;
    private void tryToConnectToPc(String ipAddresses) {
        hasToShowProgressDialog = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (hasToShowProgressDialog)
                    try {
                        progressDialog = new ProgressDialog(requireActivity());
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setMessage("Connecting");
                        progressDialog.setTitle("Please Wait");
                        progressDialog.show();
                    }
                    catch (NullPointerException ne){
                    }
            }
        }, 300);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String myIpAddress = NetworkManagement.getIpAddress(getContext());
                String[] ipAddressList = ipAddresses.split("\n");
                boolean isAbleToConnect = false;

                for (String address : ipAddressList) {
                    if (sendCommandToConnect(myIpAddress, address))
                    {
                        PC_ConnectActivity.hostToConnectTo = address;
                        isAbleToConnect = true;
                        break;
                    }
                }
                hasToShowProgressDialog = false;
                if (progressDialog != null)
                    progressDialog.dismiss();
                if (isAbleToConnect) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (snackbar != null && snackbar.isShown())
                                    snackbar.dismiss();

                                if (serverStarted) {
                                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, TransferProgress.newInstance(PC_ConnectActivity.hostToConnectTo.substring(0, PC_ConnectActivity.hostToConnectTo.lastIndexOf("/")), false, true, startSending)).setCustomAnimations(R.anim.right_to_left_out, R.anim.right_to_left_in).commit();
                                }
                                else {
                                    if (serverStartFailed) {
                                        progressDialog = new ProgressDialog(requireActivity());
                                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                        progressDialog.setMessage("Starting Server");
                                        progressDialog.setTitle("Please Wait");
                                        progressDialog.setCancelable(false);
                                        progressDialog.setCanceledOnTouchOutside(false);
                                        progressDialog.show();
                                        ServerService.newInstance(getContext(), null, new ServerStatus() {
                                            @Override
                                            public void onConnectionSuccess(String s) {
                                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (snackbar != null) {
                                                            if (snackbar.isShown())
                                                                snackbar.dismiss();
                                                        }
                                                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, TransferProgress.newInstance(PC_ConnectActivity.hostToConnectTo.substring(0, PC_ConnectActivity.hostToConnectTo.lastIndexOf("/")), false, true, startSending)).setCustomAnimations(R.anim.right_to_left_out, R.anim.right_to_left_in).commit();
                                                        progressDialog.dismiss();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onConnectionFailed(String wifi_disabled) {
                                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            snackbar = Snackbar.make(root, "Waiting for Server to start. Please retry after a few seconds", BaseTransientBottomBar.LENGTH_LONG);
                                                            snackbar.show();
                                                            progressDialog.dismiss();
                                                            try {
                                                                getActivity().stopService(new Intent(getContext(), ServerService.class));
                                                            } catch (Exception ignored) {

                                                            }
                                                        }catch (Exception e) {

                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }
                                    else {
                                        snackbar = Snackbar.make(root, "Waiting for Server to start. Please retry after a few seconds", BaseTransientBottomBar.LENGTH_LONG);
                                        snackbar.show();
                                    }
                                }
                            }
                            catch (Exception ne) {
                                Toast.makeText(getContext(), "Some Error has Occured\nPlease try again", Toast.LENGTH_LONG).show();
                                getActivity().finish();

                            }

                        }
                    });
                }
                else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                snackbar = Snackbar.make(root, "Unable to Connect\nPlease make sure both devices are connected to the same network", BaseTransientBottomBar.LENGTH_INDEFINITE);
                                View snackbarView = snackbar.getView();
                                TextView textView = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                                textView.setMaxLines(5);
                                snackbar.show();
                            }
                            catch (Exception e){

                            }
                        }
                    });
                }
            }

            private boolean sendCommandToConnect(String myIpAddress, String address) {
                boolean isConnected = true;
                HttpURLConnection urlConnection = null;
                String out = "";
                try {
                    URL url = new URL(address + "STARTSERVER:" + myIpAddress);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(1000);
                    BufferedReader rd = new BufferedReader(new InputStreamReader(
                            urlConnection.getInputStream()));

                    String line;
                    while ((line = rd.readLine()) != null) {
                        out += line;
                    }
                } catch (Exception e) {
                    isConnected = false;
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }

                isConnected &= out.equalsIgnoreCase("ok");

                return isConnected;
            }
        }).start();
    }
    private String canGetNameAndAvatar(String connection) {
        String link = connection + "/getAvatarAndName";
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
