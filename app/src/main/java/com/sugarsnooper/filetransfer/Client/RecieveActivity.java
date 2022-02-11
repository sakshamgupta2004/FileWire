package com.sugarsnooper.filetransfer.Client;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sugarsnooper.filetransfer.CustomisedAdActivity;
import com.sugarsnooper.filetransfer.Mode_Selection_Activity;
import com.sugarsnooper.filetransfer.R;
import com.sugarsnooper.filetransfer.Server.ServerService;
import com.sugarsnooper.filetransfer.Strings;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class RecieveActivity extends CustomisedAdActivity {
    static WifiManager.LocalOnlyHotspotReservation mReservation = null;




    public static boolean getActivity(){
        return isAlive;
    }
    private static boolean isAlive = false;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Strings.GPS_REQUEST){
            if (resultCode == 0){
                Toast.makeText(this, "Please Enable Gps", Toast.LENGTH_LONG).show();
                super.onDestroy();
                startActivity(new Intent(this, Mode_Selection_Activity.class));
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        setActionBar(toolbar);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new EnableHotspot_ShowQrCode()).commit();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                ((AppBarLayout) findViewById(R.id.appbar)).setExpanded(false, true);
            }
        }, 1000);

    //    make_progress();
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAlive = false;
        stopService(new Intent(this, ServerService.class));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mReservation != null) {
            mReservation.close();
            mReservation = null;
        }
        else{
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                ApManager.configApState(this, false);
            }
            else if (Settings.System.canWrite(this)){
                ApManager.configApState(this, false);
            }
        }
        getApplication().onTerminate();
        System.exit(0);
    }

    @Override
    public void onBackPressed() {
        createDialog();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                createDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigateUp() {
        triggerRebirth(this);
        return false;
    }

    private void createDialog() {
        new MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setTitle("Exit")
                .setMessage("Are you sure, you want to exit?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Application.doRestart(RecieveActivity.this);
//                        onNavigateUp();
                        finish();
                    }
                })
                .show();
    }


    public static void triggerRebirth(Context context) {
        Intent intent = new Intent(context, Mode_Selection_Activity.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }

        Runtime.getRuntime().exit(0);
    }
    public void gotoHome() {
        finish();
        super.onNavigateUp();
    }
    @Override
    protected void onResume() {
        super.onResume();
        shouldUpdateAd = true;
        isAlive = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        shouldUpdateAd = false;
    }
}
