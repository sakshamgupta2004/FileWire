package com.sugarsnooper.filetransfer.ConnectToPC.PCSoftware;

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
import com.sugarsnooper.filetransfer.Server.ServerService;

public class PC_ConnectActivity extends CustomisedAdActivity {
    private Fragment fragment;
    public static String hostToConnectTo = "";
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
        setTitle("Connect to PC");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        fragment = new PcConnectionStatusFragment(getIntent().getStringExtra("pc_connection_string"), getIntent().getBooleanExtra("start_sending", false));
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
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
