package com.sugarsnooper.filetransfer.ConnectToPC.FTP;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;
import com.sugarsnooper.filetransfer.CustomisedAdActivity;
import com.sugarsnooper.filetransfer.R;

import static com.sugarsnooper.filetransfer.ConnectToPC.FTP.PcConnectionStatusFragment.serverStarted;

public class PC_Connect_Activity extends CustomisedAdActivity {
    Fragment fragment;

    public static boolean getActivity(){
        return isAlive;
    }
    private static boolean isAlive = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        setActionBar(toolbar);
        setTitle("FTP Server");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        fragment = new PcConnectionStatusFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                ((AppBarLayout) findViewById(R.id.appbar)).setExpanded(false, true);
            }
        }, 1000);
    }


    @Override
    protected void onResume() {
        super.onResume();
        isAlive = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAlive = false;
        if (serverStarted)
            stopService(new Intent(this, FTPServerService.class));
    }

    @Override
    public void onBackPressed() {
        if (serverStarted) {
            new AlertDialog.Builder(this).setCancelable(false).setTitle("Exit").setMessage("Are you sure, you want to exit.\nThis will terminate any ongoing file Transfers")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            fragment.onDestroy();
                            PC_Connect_Activity.super.onNavigateUp();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
        else{
            super.onNavigateUp();
        }
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return false;
    }
}
