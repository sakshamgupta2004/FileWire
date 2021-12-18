package com.sugarsnooper.filetransfer.ConnectToPC.PCSoftware;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;


import com.google.android.material.appbar.AppBarLayout;
import com.sugarsnooper.filetransfer.CustomisedAdActivity;
import com.sugarsnooper.filetransfer.QRCodeFormatter;
import com.sugarsnooper.filetransfer.R;
import com.sugarsnooper.filetransfer.Server.File.Selection.Media;
import com.sugarsnooper.filetransfer.Server.Send_Activity;
import com.sugarsnooper.filetransfer.Server.ServerService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PC_ConnectActivity extends CustomisedAdActivity {
    private Fragment fragment;
    public static String hostToConnectTo = "";
    public static boolean getActivity(){
        return isAlive;
    }
    private static boolean isAlive = false;
    @SuppressLint("Range")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);






        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        setActionBar(toolbar);
        setTitle("Connect to PC");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                ((AppBarLayout) findViewById(R.id.appbar)).setExpanded(false, true);
            }
        }, 1000);




        Intent intent = getIntent();
        if (Intent.ACTION_SEND.equals(intent.getAction()) || Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {

            List<Media> fileList = new CopyOnWriteArrayList<>();
            List<String[]> selectedFiles = new ArrayList<>();
            try {
                if (Intent.ACTION_SEND.equals(intent.getAction())) {
                    Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    if (uri != null) {
                        Cursor cursor = this.getContentResolver().query(uri, null, null, null, null);
                        cursor.moveToFirst();

                        @SuppressLint("Range") long sizeOfInputStram = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
                        fileList.add(new Media(uri, cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)), sizeOfInputStram, 0, true));

                    }
                } else {
                    ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    if (uris != null) {
                        for (Uri uri : uris) {
                            if (uri != null) {
                                Cursor cursor = this.getContentResolver().query(uri, null, null, null, null);
                                cursor.moveToFirst();

                                @SuppressLint("Range") long sizeOfInputStram = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
                                fileList.add(new Media(uri, cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)), sizeOfInputStram, 0, true));
                            }
                        }
                    }
                }

                for (Media media : fileList) {
                    if (media.isChecked())
                        selectedFiles.add(new String[]{String.valueOf(media.getUri()), String.valueOf(media.getSize()), media.getName(), String.valueOf(media.isFolder())});
                }

                ServerService.changeHostedFiles(selectedFiles);
            }
            catch (Exception e) {
                Toast.makeText(this, "Error reading files. Please select manually after connecting", Toast.LENGTH_LONG).show();
            }
            fragment = new PcConnectionStatusFragment(null, selectedFiles.size() > 0);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }
        else {
            fragment = new PcConnectionStatusFragment(getIntent().getStringExtra("pc_connection_string"), getIntent().getBooleanExtra("start_sending", false));
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        isAlive = true;
    }

    @Override
    public boolean onNavigateUp() {
        createDialog();
        return false;
    }

    @Override
    public void onBackPressed() {
        createDialog();
    }

    private void createDialog() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Exit")
                .setMessage("Are you sure, you want to exit?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Application.doRestart(RecieveActivity.this);
                        PC_ConnectActivity.super.onNavigateUp();
                    }
                })
                .show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAlive = false;
        stopService(new Intent(this, ServerService.class));
    }
}
