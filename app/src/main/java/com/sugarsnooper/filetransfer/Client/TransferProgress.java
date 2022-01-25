package com.sugarsnooper.filetransfer.Client;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.view.*;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aditya.filebrowser.Constants;
import com.aditya.filebrowser.FileBrowser;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.JsonObject;
import com.sugarsnooper.filetransfer.ConnectToPC.PCSoftware.PC;
import com.sugarsnooper.filetransfer.FileTypeLookup;
import com.sugarsnooper.filetransfer.R;
import com.sugarsnooper.filetransfer.Server.HTTPPOSTServer;
import com.sugarsnooper.filetransfer.Server.Send_Activity;
import com.sugarsnooper.filetransfer.Server.ServerService;
import com.sugarsnooper.filetransfer.Server.verify;
import com.sugarsnooper.filetransfer.Strings;

import com.sugarsnooper.filetransfer.TinyDB;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

public class TransferProgress extends Fragment {

    private String ipAddress = null;
    private ProgressBar progressBarTop = null;
    private TextView progress_status = null;
    private long eta_last_updated;
    private boolean isDownloadComplete = false;
    private int num_threads = 10;
    private boolean shouldShowEndDialog = true;
    private List<List<File_and_Progress>> fileAndProgress = null;
    private View view = null;
    private RecyclerView listView;
    private DownloaderWithMaxThreadPool downloaderWithMaxThreadPool;
    private static boolean start_Sending = false;
    private JSONObject connectedAvatarAndName = null;

    public static Fragment newInstance(String current_ip, boolean startReceiving, boolean connectedToPC) {
        Bundle bundle = new Bundle();
        bundle.putString("IP", current_ip);
        bundle.putBoolean("PC_Connection", connectedToPC);
        bundle.putBoolean("start_Receiving", startReceiving);
        TransferProgress transferProgress = new TransferProgress();
        transferProgress.setArguments(bundle);
        start_Sending = false;
        return transferProgress;
    }
    private ProgressAdapter pa = null;

    public static Fragment newInstance(String current_ip, boolean startReceiving, boolean connectedToPC, boolean startSending) {
        Bundle bundle = new Bundle();
        bundle.putString("IP", current_ip);
        bundle.putBoolean("PC_Connection", connectedToPC);
        bundle.putBoolean("start_Receiving", startReceiving);
        TransferProgress transferProgress = new TransferProgress();
        transferProgress.setArguments(bundle);
        start_Sending = startSending;
        return transferProgress;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            Drawable d = getResources().getDrawable(R.drawable.ic_baseline_close_24);
            d.setColorFilter(new TinyDB(getContext()).getBoolean(Strings.useA12Theme_preference_key) ? getResources().getColor(R.color.textcolor) : getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP );
            requireActivity().getActionBar().setHomeAsUpIndicator(d);
        }
        catch (NullPointerException ignored) {

        }

        downloaderWithMaxThreadPool = new DownloaderWithMaxThreadPool(requireContext(), num_threads);
//        TinyDB tinyDB = new TinyDB(getContext());
//        ArrayList<String> timestamps =
//                tinyDB.getListString("transferDates");
//
//        if (!timestamps.contains(Strings.dateString))
//        {
//            timestamps.add(Strings.dateString);
//        }
//        tinyDB.putListString("transferDates", timestamps);

        boolean startReceiving = getArguments().getBoolean("start_Receiving");
        ((CollapsingToolbarLayout) requireActivity().findViewById(R.id.toolbar_layout)).setTitle("Transferring Files");
        if (getArguments() != null) {
            ipAddress = getArguments().getString("IP");
        }
        Log.e (ipAddress, ipAddress);
        if (ipAddress != null) {
            if (!getArguments().getBoolean("PC_Connection")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject result = getConnectedNameAndAvatar();
                        if (result != null) {
                            connectedAvatarAndName = result;
                            try {
                                requireActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            ((CollapsingToolbarLayout) requireActivity().findViewById(R.id.toolbar_layout)).setTitle("Connected with " + result.getString("name"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }).start();
            }
            else {
                ((CollapsingToolbarLayout) requireActivity().findViewById(R.id.toolbar_layout)).setTitle("Connected to PC");
                new verify(new verify.AsynResponse() {
                    @Override
                    public void processFinish(String out) {
                        if (!out.equals("OK")) {
                            try {
                                Toast.makeText(getContext(), "Some Error has Occurred", Toast.LENGTH_LONG).show();
                                requireActivity().finish();
                            }
                            catch (Exception ignored) {

                            }
                        }
                        else {
                            if (start_Sending) {
                                new Thread(() -> {
                                    HttpURLConnection urlConnection = null;
                                    String out1 = "";
                                    try {
                                        URL url = new URL(ipAddress + "/" + "INCOMING:" + ServerService.getPort());
                                        urlConnection = (HttpURLConnection) url.openConnection();
                                        BufferedReader rd = new BufferedReader(new InputStreamReader(
                                                urlConnection.getInputStream()));

                                        String line;
                                        while ((line = rd.readLine()) != null) {
                                            out1 += line;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        if (urlConnection != null) {
                                            urlConnection.disconnect();
                                        }
                                    }
                                }).start();
                            }
                        }
                    }
                }, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ipAddress.substring(7), String.valueOf(ServerService.getPort()));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject result = getConnectedNameAndAvatar();
                        if (result != null) {
                            connectedAvatarAndName = result;
                            try {
                                requireActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            ((CollapsingToolbarLayout) requireActivity().findViewById(R.id.toolbar_layout)).setTitle("Connected with " + result.getString("name"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }).start();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean pcIsNotInList = true;
                        String productId = getProductId();
                        TinyDB td = new TinyDB(getContext());
                        ArrayList<PC> pcList = td.getListObject(Strings.pairedPC_preference_key, PC.class);
                        for (PC pc : pcList) {
                            if (pc.getProductId().equals(productId)) {
                                pcIsNotInList = false;
                                break;
                            }
                        }
                        setHasOptionsMenu(getArguments().getBoolean("PC_Connection", false) && pcIsNotInList);
                    }
                }).start();
            }
        }

        if (ipAddress != null) {
            fileAndProgress = new CopyOnWriteArrayList<>();
            ServerService.attatchListener(new HTTPPOSTServer.ServerListener() {
                @Override
                public void filesIncoming() {
                    getFilesToReceive();
                }

                @Override
                public void progressChange(String fileName, long sent, long total) {
                    Log.e("Sending File", "FileName: " + fileName + "    Sent: " + sent + "/" + total);
                }
            });
            if (startReceiving)
                getFilesToReceive();
        } else {
            Toast.makeText(requireContext(), "Some Error has Occurred", Toast.LENGTH_LONG).show();
            requireActivity().finish();
        }

        View parent = inflater.inflate(R.layout.transfer_progress_fragment, container, false);

        ExtendedFloatingActionButton buttonSendMore = parent.findViewById(R.id.button_select_more_files);
        buttonSendMore.setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), Send_Activity.class).putExtra(Strings.FileSelectionRequest, true).putExtra("HOST", ipAddress + "/")));


        listView = parent.findViewById(R.id.share_progress_list_view);
        progressBarTop = parent.findViewById(R.id.top_progress_bar);
        progress_status = parent.findViewById(R.id.progress_status_text_view);
        progress_status.setText(getString(R.string.no_files_received_yet));
        listView.setAdapter(new ProgressAdapter(getContext(), R.layout.custom_download_progress_bar, new ArrayList<List<File_and_Progress>>()));
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (!startReceiving)
        {
            parent.findViewById(R.id.file_info_progress_bar).setVisibility(View.GONE);
            parent.findViewById(R.id.progress_views).setVisibility(View.VISIBLE);
        }

        return parent;
    }

    private void getFilesToReceive() {
        new GetFilesToBeTransferred(out -> {
            if (!out.equalsIgnoreCase("error")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String filestobetransferred_json = out.substring(out.indexOf("<body>") + 6, out.indexOf("</body>"));
                        ArrayList<String[]> filestobetransferred = json_parse(filestobetransferred_json);

                        List<File_and_Progress> fileAndProgresses =
                                new CopyOnWriteArrayList<>();
                        for (String[] file : filestobetransferred) {
                            fileAndProgresses.add(new File_and_Progress(Long.parseLong(file[3]), file[2], ipAddress + "/" + file[1], Boolean.parseBoolean(file[4])));


//                            ArrayList<Media> receivingMediaFiles = new ArrayList<>();
//                            try {xs
//                                receivingMediaFiles.add(new Media(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.pathSeparator + file[2])), file[2], Long.parseLong(file[3])));
//                            }
//                            catch (Exception ignored){
//
//                            }
//                            new TinyDB(getContext()).putListMedia(Strings.dateString + "_RECEIVE", receivingMediaFiles);
//                            Log.e ("File", "Size = " + file[3] + "     Name = " + file[2] + "      Provided link = " + file[1] + "       Moded link = " + ipAddress + "/" + file[1]);

                        }
                        fileAndProgress.add(fileAndProgresses);
                        while (view == null) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            getActivity().runOnUiThread(() -> {
                                view.findViewById(R.id.file_info_progress_bar).setVisibility(View.GONE);
                                view.findViewById(R.id.progress_views).setVisibility(View.VISIBLE);
                                make_progress_and_download();
                            });
                        }
                        catch (NullPointerException ignored) {

                        }
                    }
                }).start();
            } else {
                Toast.makeText(requireContext(), "Some Error has Occurred", Toast.LENGTH_LONG).show();
                requireActivity().finish();
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ipAddress);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;


//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                getFilesToReceive();
//            }
//        }, 3000);
    }


    private void make_progress_and_download() {
//        List<File_and_Progress> fileAndProgresses = new CopyOnWriteArrayList<>();
//        for (List<File_and_Progress> file_and_progresses : fileAndProgress) {
//            fileAndProgresses.addAll(file_and_progresses);
//        }
//

        if (pa == null) {
            pa = new ProgressAdapter(requireActivity(), R.layout.custom_download_progress_bar, fileAndProgress);
            listView.setLayoutManager(new LinearLayoutManager(requireActivity()));
            listView.setAdapter(pa);
        }
        else {
            pa.notifyDataSetChanged();
        }
//        listView.setItemsCanFocus(false);
        pa.setOnFileOpenedListener(position -> openFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + getString(R.string.app_name) + "/" + FileTypeLookup.fileTypeString(pa.getItem(position).getFileName(), pa.getItem(position).isFolder()) + "/" + pa.getItem(position).getFileName())));


        for (int i = 0; i < num_threads ; i++) {
            int finalI = i;
            new Thread(() -> downloadFile_single_thread(pa, requireContext(), new int[]{finalI}, 1, fileAndProgress.size() - 1)).start();
        }

    }

    private void down_complete_dialog(){
        if (shouldShowEndDialog) {
            new AlertDialog.Builder(requireActivity())
                    .setTitle("Transfer Complete")
                    .setMessage("All files have been successfully transferred to your device")
                    .setCancelable(false)
                    .setPositiveButton("Show Files", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            shouldShowEndDialog = false;
                        }
                    })
                    .setNeutralButton("Show in Explorer", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(getContext(), FileBrowser.class); //works for all 3 main classes (i.e FileBrowser, FileChooser, FileBrowserWithCustomHandler)
                            i.putExtra(Constants.INITIAL_DIRECTORY, new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), getString(R.string.app_name)).getAbsolutePath());
                            startActivity(i);
                        }
                    })
                    .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requireActivity().finish();
                        }
                    }).show();
        }
    }

    private void downloadFile_single_thread(ProgressAdapter pa, Context mContext, int[] filePositionOffset, int rename_tries, int batch) {
        if (filePositionOffset[0] < fileAndProgress.get(batch).size()) {
            if (new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + getString(R.string.app_name) + "/" + FileTypeLookup.fileTypeString(fileAndProgress.get(batch).get(filePositionOffset[0]).getFileName(), fileAndProgress.get(batch).get(filePositionOffset[0]).isFolder()) + "/" + fileAndProgress.get(batch).get(filePositionOffset[0]).getFileName()).exists()) {
                if (rename_tries == 1) {
                    try {
                        fileAndProgress.get(batch).get(filePositionOffset[0]).setFileName(fileAndProgress.get(batch).get(filePositionOffset[0]).getFileName().substring(0, fileAndProgress.get(batch).get(filePositionOffset[0]).getFileName().lastIndexOf(".")) + " (" + rename_tries + ") " + fileAndProgress.get(batch).get(filePositionOffset[0]).getFileName().substring(fileAndProgress.get(batch).get(filePositionOffset[0]).getFileName().lastIndexOf(".")));
                    } catch (Exception e) {
                        fileAndProgress.get(batch).get(filePositionOffset[0]).setFileName(fileAndProgress.get(batch).get(filePositionOffset[0]).getFileName() + " (" + rename_tries + ") ");
                    }
                }
                else{
                    String old_try = " (" + String.valueOf(rename_tries - 1) + ") ";
                    String to_replace = " (" + String.valueOf(rename_tries) + ") ";
                    fileAndProgress.get(batch).get(filePositionOffset[0]).setFileName(fileAndProgress.get(batch).get(filePositionOffset[0]).getFileName().replace(old_try, to_replace));
                }
                rename_tries++;
                downloadFile_single_thread(pa, mContext, filePositionOffset, rename_tries, batch);
            } else {
                new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + getString(R.string.app_name) + "/" + FileTypeLookup.fileTypeString(fileAndProgress.get(batch).get(filePositionOffset[0]).getFileName(), fileAndProgress.get(batch).get(filePositionOffset[0]).isFolder()) + "/" ).mkdirs();
//                new DownloadTask(mContext, new DownloadTask.AsynResponse() {
//                    @Override
//                    public void processFinish(String out, long total) {
//                        download_finish(out, total, batch, filePositionOffset, mContext);
//                    }
//
//                    @Override
//                    public void progressupdate(long progress) {
//                        progress_update(progress, batch, filePositionOffset);
//                    }
//                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, fileAndProgress.get(batch).get(filePositionOffset[0]).getLink(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + getString(R.string.app_name) + "/" + FileTypeLookup.fileTypeString(fileAndProgress.get(batch).get(filePositionOffset[0]).getFileName()) + "/" + fileAndProgress.get(batch).get(filePositionOffset[0]).getFileName());
                
                

                downloaderWithMaxThreadPool.add(
                        new DownloaderWithMaxThreadPool.downloadListener(){
                            @Override
                            public void progressUpdate(long progress) {
                                progress_update(progress, batch, filePositionOffset);
                            }

                            @Override
                            public void processFinish(String out, long total) {
                                download_finish(out, total, batch, filePositionOffset, mContext);
                            }
                        }, fileAndProgress.get(batch).get(filePositionOffset[0]).isFolder(), fileAndProgress.get(batch).get(filePositionOffset[0]).getLink(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + getString(R.string.app_name) + "/" + FileTypeLookup.fileTypeString(fileAndProgress.get(batch).get(filePositionOffset[0]).getFileName(), fileAndProgress.get(batch).get(filePositionOffset[0]).isFolder()) + "/" + fileAndProgress.get(batch).get(filePositionOffset[0]).getFileName());
            }
        }
        else if (filePositionOffset[0] == fileAndProgress.get(batch).size()){
            update_top_progress();
        }
    }

    private void progress_update(long progress, int batch, int[] filePositionOffset) {
        pa.updateProgress(batch, filePositionOffset[0], progress);
        update_top_progress();
    }

    private void download_finish(String out, long total, int batch, int[] filePositionOffset, Context mContext) {
        if ( out != null || total != fileAndProgress.get(batch).get(filePositionOffset[0]).getFileSize()) {
//                            Log.e("DownloadError", out);
            new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + getString(R.string.app_name) + "/" + FileTypeLookup.fileTypeString(fileAndProgress.get(batch).get(filePositionOffset[0]).getFileName(), fileAndProgress.get(batch).get(filePositionOffset[0]).isFolder()) + "/" + fileAndProgress.get(batch).get(filePositionOffset[0]).getFileName()).delete();
          try {
              new AlertDialog.Builder(requireActivity()).setMessage("Some unexpected error has Occurred")
                      .setTitle("Transfer Failed")
                      .setCancelable(false)
                      .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialog, int which) {
                              downloadFile_single_thread(pa, mContext, filePositionOffset, 1, batch);
                          }
                      })
                      .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialog, int which) {
                              requireActivity().finish();
                          }
                      })
                      .setNeutralButton("Skip File", new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialog, int which) {
                              filePositionOffset[0] += num_threads;
                              downloadFile_single_thread(pa, mContext, filePositionOffset, 1, batch);
                          }
                      }).show();
          }
          catch (IllegalStateException ignored) {

          }
        }
        else {
            pa.updateProgress(batch, filePositionOffset[0], total);
            filePositionOffset[0] += num_threads;
            new Thread(() -> {
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                update_top_progress();
            }).start();

            downloadFile_single_thread(pa, mContext, filePositionOffset, 1, batch);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isDownloadComplete)
            down_complete_dialog();
    }

    private void update_top_progress() {
        new Thread(() -> {
            if (new Date().getTime() - eta_last_updated > 100){
                long currentlydownloaded = 0;
                long total_download_size = 0;
                for (List<File_and_Progress> fileAndProgresses : fileAndProgress) {
                    for (File_and_Progress file_and_progress : fileAndProgresses) {
                        total_download_size += file_and_progress.getFileSize();
                        currentlydownloaded += file_and_progress.getDownloaded();
                    }
                }
//                int overall_progress = (int) ((currentlydownloaded * 100L)/total_download_size);
                int overall_progress;
                if (total_download_size != 0) {
                    overall_progress = (int) ((currentlydownloaded * 100L) / total_download_size);
                }
                else {
                    overall_progress = 100;
                }
//                if (currentlydownloaded == total_download_size){
//                    new Thread(()->tell_server_about_completion()).start();
//                    isDownloadComplete = true;
//                    new Handler(Looper.getMainLooper()).post(this::down_complete_dialog);
//                }
                String progress = getFormatSize(currentlydownloaded) + " of " + getFormatSize(total_download_size);
                new Handler(Looper.getMainLooper()).post(() -> {
                    progressBarTop.setProgress(overall_progress);
                    progress_status.setText(progress);
                });
                eta_last_updated = new Date().getTime();
            }
        }).start();
    }

    private String getFormatSize(long size_given){
        final double KB = 1024.00;
        final double MB = 1024.00 * KB;
        final double GB = 1024.00 * MB;
        if ((double) size_given /KB < 0.90) {
            return String.format( Locale.getDefault(),"%.2f", (double) size_given) + " B";
        } else if ((double) size_given /MB < 0.90) {
            return String.format( Locale.getDefault(),"%.2f", (double) size_given /KB) + " KB";
        } else if ((double) size_given /GB  < 0.90) {
            return String.format( Locale.getDefault(),"%.2f", (double) size_given /MB) + " MB";
        } else {
            return String.format( Locale.getDefault(),"%.3f", (double) size_given /GB) + " GB";
        }
    }

    private void tell_server_about_completion() {
        String link = ipAddress + "/transferComplete";
        link = link.replaceAll(" ", "%20");
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            request.setURI(new URI(link));
            HttpResponse response = client.execute(request);
            BufferedReader in = new BufferedReader(new
                    InputStreamReader(response.getEntity().getContent()));
            in.close();
        }
        catch (Exception ignored){
        }
    }

    private JSONObject getConnectedNameAndAvatar() {
        String link = ipAddress + "/getAvatarAndName";
        link = link.replaceAll(" ", "%20");
        try {
            HttpClient client = new DefaultHttpClient();
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
            return new JSONObject(result);
        }
        catch (Exception ignored){
            return null;
        }
    }
    private void openFile(File url) {
        if (!url.isDirectory()) {
            MimeTypeMap mimeMap = MimeTypeMap.getSingleton();
            Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
            String mimeType = mimeMap.getMimeTypeFromExtension(FilenameUtils.getExtension(url.getName()));
            Uri uri;
            try {
                uri = FileProvider.getUriForFile(getContext(), getContext().getString(R.string.filebrowser_provider), url);
            } catch (IllegalArgumentException ie) {
                if (Build.VERSION.SDK_INT >= 24) {
                    try {
                        Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                        m.invoke(null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                uri = Uri.fromFile(url);
            }

            openFileIntent.setDataAndType(uri, mimeType);
            openFileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            openFileIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                getContext().startActivity(openFileIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getContext(), getContext().getString(R.string.no_app_to_handle), Toast.LENGTH_LONG).show();
            }
        }
        else {
            try {
                Uri selectedUri = Uri.parse(url.getPath());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(selectedUri, "resource/folder");
                startActivity(intent);
            }
            catch (Exception e) {
//                Intent chooser = new Intent(Intent.ACTION_GET_CONTENT);
//                Uri uri = Uri.parse(url.getAbsolutePath());
//                chooser.addCategory(Intent.CATEGORY_OPENABLE);
//                chooser.setDataAndType(uri, "*/*");
//                try {
//                    startActivity(chooser);
//                }
//                catch (android.content.ActivityNotFoundException ex)
//                {
                    Intent i = new Intent(getActivity(), FileBrowser.class); //works for all 3 main classes (i.e FileBrowser, FileChooser, FileBrowserWithCustomHandler)
                    i.putExtra(Constants.INITIAL_DIRECTORY, url.getAbsolutePath());
                    startActivity(i);
//                }
            }
        }
    }

    private ArrayList<String[]> json_parse(String s) {
        ArrayList<String[]> stringArrayList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(s);

            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject object1 = jsonArray.getJSONObject(i);
                    String[] objects = new String[6];
                    objects[3] = object1.getString("fileSize");
                    objects[2] = object1.getString("fileName");
                    objects[0] = object1.getString("id");
                    objects[1] = object1.getString("link");
                    objects[4] = object1.getString("isFolder");
                    stringArrayList.add(objects);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringArrayList;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.pc_transfer_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().toString().equals(getResources().getString(R.string.pair_pc)))
        {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Pair PC")
                    .setMessage("Tap proceed to pair a PC and send any file to the PC easily even if Filewire is not opened on the PC")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            getPCProductIdAndPair();
                        }
                    })
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getPCProductIdAndPair() {
        new Thread(() -> {
            String productId = getProductId();
            if (productId != null) {
                try {
                    TinyDB td = new TinyDB(getContext());
                    ArrayList<PC> pcList = td.getListObject(Strings.pairedPC_preference_key, PC.class);
                    pcList.add(new PC(connectedAvatarAndName.getString("name"), "", productId));
                    td.putListObject(Strings.pairedPC_preference_key, pcList);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            setHasOptionsMenu(false);
                            Toast.makeText(getContext(), "Pairing Successful", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                catch (Exception e) {
                    Log.e("Error", e.toString());
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "Some Error has Occurred", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
            else {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Some Error has Occurred", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

    private String getProductId() {
        String link = ipAddress + "/getid";
        link = link.replaceAll(" ", "%20");
        try {
            HttpClient client = new DefaultHttpClient();
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
            return result;
        }
        catch (Exception ignored){
            return null;
        }
    }
}
