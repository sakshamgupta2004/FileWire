package com.sugarsnooper.filetransfer.Server;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.sugarsnooper.filetransfer.CustomisedAdActivity;
import com.sugarsnooper.filetransfer.R;
import com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection;
import com.sugarsnooper.filetransfer.Server.File.Selection.SearchFileFragment;
import com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments.DocumentExplorer;
import com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments.FileExplorer;
import com.sugarsnooper.filetransfer.Strings;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import eightbitlab.com.blurview.BlurView;


public class Send_Activity extends CustomisedAdActivity {
    public static List<String[]> hostedFiles;
    public static String gateway = null;
    public static String ip = null;
    public static boolean isTransferComplete = false;
    public static BlurView blurViewTopLevel = null;
    public static boolean isImagePopedUp = false;
    public static StfalconImageViewer<Uri> iv = null;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        setActionBar(toolbar);
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        }
//        blurViewTopLevel = findViewById(R.id.topLevelBlurView);
//        float radius = 25f;
//
//        View decorView = getWindow().getDecorView();
//        //ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
//        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
//        //Set drawable to draw in the beginning of each blurred frame (Optional).
//        //Can be used in case your layout has a lot of transparent space and your content
//        //gets kinda lost after after blur is applied.
//        Drawable windowBackground = decorView.getBackground();
//        blurViewTopLevel.setupWith(rootView)
//                .setFrameClearDrawable(windowBackground)
//                .setBlurAlgorithm(new RenderScriptBlur(this))
//                .setBlurRadius(radius)
//                .setBlurAutoUpdate(true)
//                .setHasFixedTransformationMatrix(true);
//
//        blurViewTopLevel.setVisibility(View.GONE);

        hostedFiles = new CopyOnWriteArrayList<>();
//        hostedFiles.add(new String[]{"Hello", "123456", ".mp4"});
//        hostedFiles.add(new String[]{"Hello", "123456", ".mp4"});
//        hostedFiles.add(new String[]{"Hello", "123456", ".mp4"});
//        hostedFiles.add(new String[]{"Hello", "123456", ".mp4"});
//        hostedFiles.add(new String[]{"Hello", "123456", ".mp4"});
        Fragment fragment = new FileSelection();
        Bundle bundle = new Bundle();
        bundle.putBoolean(Strings.FileSelectionRequest, getIntent().getBooleanExtra(Strings.FileSelectionRequest, false));
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment, "FILE_SELECTION_FRAGMENT").commit();
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
        shouldUpdateAd = true;
        isAlive = true;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        shouldUpdateAd = false;
    }

    @Override
    public boolean onNavigateUp() {
        if (getIntent().getBooleanExtra(Strings.FileSelectionRequest, false)){
            finish();
            return true;
        }
        else {
            onBackPressed();
            return false;
        }
    }

    @Override
    public void onBackPressed() {

        Fragment fragment = getSupportFragmentManager().findFragmentByTag ("FILE_EXPLORER_SELECTION");
        Fragment fragment2 = getSupportFragmentManager().findFragmentByTag ("DOCUMENT_SELECTION");
        Fragment fragment1 = getSupportFragmentManager().findFragmentByTag ("FILE_SELECTION_FRAGMENT");
        Fragment fragment3 = getSupportFragmentManager().findFragmentByTag ("TRANSFER_PROGRESS");
        Fragment fragment4 = getSupportFragmentManager().findFragmentByTag("SEARCH_FILES_FRAGMENT");
        if (fragment1 != null && fragment1.isVisible() && FileSelection.viewPager != null && FileSelection.viewPager.getCurrentItem() == 4 && fragment != null && fragment.isVisible()) {
            FileExplorer.backPressed();
        }
        else if (fragment1 != null && fragment1.isVisible() && FileSelection.viewPager != null && FileSelection.viewPager.getCurrentItem() == 4 && fragment2 != null && fragment2.isVisible()) {
            DocumentExplorer.backPressed();
        }
        else if (fragment4 != null && fragment4.isVisible()) {
            SearchFileFragment.backPressed(this);
        }
        else if (iv != null) {
            iv.close();
        }
        else{
            createDialog();
        }
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
                        Send_Activity.super.onNavigateUp();
                    }
                })
                .show();
    }

    public static boolean getActivity(){
        return isAlive;
    }
    private static boolean isAlive = false;
    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAlive = false;
        if (!getIntent().getBooleanExtra(Strings.FileSelectionRequest, false))
            stopService(new Intent(getBaseContext(), ServerService.class));
    }
}