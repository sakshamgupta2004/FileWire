package com.sugarsnooper.filetransfer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.aditya.filebrowser.Constants;
import com.aditya.filebrowser.FileBrowser;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.sugarsnooper.filetransfer.Client.GpsUtils;
import com.sugarsnooper.filetransfer.Client.RecieveActivity;
import com.sugarsnooper.filetransfer.ConnectToPC.FTP.PC_Connect_Activity;
import com.sugarsnooper.filetransfer.ConnectToPC.PCSoftware.PC_ConnectActivity;
import com.sugarsnooper.filetransfer.Server.Send_Activity;

import java.io.File;

public class Mode_Selection_Activity extends CustomisedAdActivity {


    private int taskAfterEnableGPS = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_page_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || !new TinyDB(this).getBoolean(Strings.useA12Theme_preference_key))
            toolbar.setTitleTextColor(Color.parseColor("#ffffff"));

        toolbar.setContentInsetsRelative(0,0);
        setActionBar(toolbar);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setLogo(resize(getResources().getDrawable(R.drawable.ic_logo_dark_playstore_without_bg)));
        getActionBar().setDisplayUseLogoEnabled(true);
//
//        toolbar.setPadding(0,30,0,30);
//        Drawable drawable = ContextCompat.getDrawable(this, R.mipmap.ic_logo);
//        getActionBar().setIcon(drawable);
//        create_layout(getResources().getConfiguration().screenHeightDp);
    }
    private Drawable resize(Drawable image) {
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, (int) convertDpToPx(this, 32), (int) convertDpToPx(this, 32), false);
        return new BitmapDrawable(getResources(), bitmapResized);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        findViewById(R.id.front_page_actions_view_holder).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                findViewById(R.id.front_page_actions_view_holder).getViewTreeObserver().removeOnGlobalLayoutListener(this);
                create_layout(getResources().getConfiguration().screenHeightDp);
            }
        });
    }

    private void create_layout(int screenHeightDp) {
        ViewGroup.LayoutParams params;
        if (screenHeightDp == 0)
            params = new ViewGroup.LayoutParams(0, findViewById(R.id.front_page_actions_view_holder).getHeight());
        else
            params = new ViewGroup.LayoutParams(0, (int) convertDpToPx(Mode_Selection_Activity.this, screenHeightDp));

        int blockheight = params.height / 19;
        float width_diff = convertDpToPx(Mode_Selection_Activity.this, 30);
        ((CardView)findViewById(R.id.upload_button)).getLayoutParams().height = 5 * blockheight;
        ((CardView)findViewById(R.id.download_button)).getLayoutParams().height = 5 * blockheight;
        ((CardView)findViewById(R.id.pc_share_button)).getLayoutParams().height = 5 * blockheight;
        ((CardView)findViewById(R.id.view_files_button)).getLayoutParams().height = 5 * blockheight;

        ((CardView)findViewById(R.id.upload_button)).getLayoutParams().width = (int) ((5 * blockheight) - width_diff);
        ((CardView)findViewById(R.id.download_button)).getLayoutParams().width = (int) ((5 * blockheight) - width_diff);
        ((CardView)findViewById(R.id.pc_share_button)).getLayoutParams().width = (int) ((5 * blockheight) - width_diff);
        ((CardView)findViewById(R.id.view_files_button)).getLayoutParams().width = (int) ((5 * blockheight) - width_diff);

        ((CardView)findViewById(R.id.upload_button)).setTranslationX(-0.5f * screenHeightDp);
        ((CardView)findViewById(R.id.upload_button)).setTranslationY(-1 * screenHeightDp);
        ((CardView)findViewById(R.id.download_button)).setTranslationX(+0.5f * screenHeightDp);
        ((CardView)findViewById(R.id.download_button)).setTranslationY(-1 * screenHeightDp);
        ((CardView)findViewById(R.id.pc_share_button)).setTranslationX(+0.5f * screenHeightDp);
        ((CardView)findViewById(R.id.pc_share_button)).setTranslationY(+1 * screenHeightDp);
        ((CardView)findViewById(R.id.view_files_button)).setTranslationX(-0.5f * screenHeightDp);
        ((CardView)findViewById(R.id.view_files_button)).setTranslationY(+1 * screenHeightDp);

        Interpolator interpolator = new DecelerateInterpolator();
        ((CardView)findViewById(R.id.upload_button)).animate().setInterpolator(interpolator).translationX(0f).translationY(0f).setDuration(500).start();
        ((CardView)findViewById(R.id.download_button)).animate().setInterpolator(interpolator).translationX(0f).translationY(0f).setDuration(500).start();
        ((CardView)findViewById(R.id.pc_share_button)).animate().setInterpolator(interpolator).translationX(0f).translationY(0f).setDuration(500).start();
        ((CardView)findViewById(R.id.view_files_button)).animate().setInterpolator(interpolator).translationX(0f).translationY(0f).setDuration(500).start();

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        create_layout(newConfig.screenHeightDp);
    }

    public float convertDpToPx(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
    @Override
    protected void onResume() {
        super.onResume();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        shouldUpdateAd = true;
        isAlive = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAlive = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        shouldUpdateAd = false;
    }

    public static boolean getActivity(){
        return isAlive;
    }
    private static boolean isAlive = false;
    private boolean ask_for_location_permission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1972);
                return false;
            }
        }
        else {
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1970){
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                startActivity(new Intent(this, Send_Activity.class).putExtra(Strings.FileSelectionRequest, false));
            }
            else{
                Toast.makeText(this, "Please enable Storage Permission to use use the App", Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == 1971){
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                gotoclientpage(null);
            }
            else{
                Toast.makeText(this, "Please enable Storage Permission to use use the App", Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == 1973){
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                gotoviewfilespage(null);
            }
            else{
                Toast.makeText(this, "Please enable Storage Permission to use use the App", Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == 1979){
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                startActivity(new Intent(this, PC_ConnectActivity.class));
            }
            else{
                Toast.makeText(this, "Please enable Storage Permission to use use the App", Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == 1989){
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                startActivity(new Intent(this, PC_Connect_Activity.class));
            }
            else{
                Toast.makeText(this, "Please enable Storage Permission to use use the App", Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == 1972){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Please enable Location Permission to use use the App", Toast.LENGTH_LONG).show();
            }
            else {
                if (taskAfterEnableGPS == 1) {
                    gotoserverpage(null);
                }
                else if (taskAfterEnableGPS == 2) {
                    gotoclientpage(null);
                }
            }
        }
    }

    public void gotoserverpage(View view) {
        if (ask_for_location_permission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
                    @Override
                    public void gpsStatus(boolean isGPSEnable) {
                        if (isGPSEnable) {
                            if ((ContextCompat.checkSelfPermission(Mode_Selection_Activity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(Mode_Selection_Activity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                                startActivity(new Intent(Mode_Selection_Activity.this, Send_Activity.class).putExtra(Strings.FileSelectionRequest, false));
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1970);
                                }
                            }
                        } else {
                            taskAfterEnableGPS = 1;
                        }
                    }
                });
            }
            else {
                if ((ContextCompat.checkSelfPermission(Mode_Selection_Activity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(Mode_Selection_Activity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                    startActivity(new Intent(Mode_Selection_Activity.this, Send_Activity.class).putExtra(Strings.FileSelectionRequest, false));
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1970);
                    }
                }
            }

        }
        else {
            taskAfterEnableGPS = 1;
        }
    }

    public void gotopcsharepage(View view) {

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            startActivity(new Intent(this, PC_ConnectActivity.class));
        }
        else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE }, 1979);
            }
        }
//        new IntentIntegrator(this).setOrientationLocked(false).initiateScan();

    }

    public void gotoclientpage(View view) {
        if (ask_for_location_permission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
                    @Override
                    public void gpsStatus(boolean isGPSEnable) {
                        if (isGPSEnable) {
                            if ((ContextCompat.checkSelfPermission(Mode_Selection_Activity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(Mode_Selection_Activity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                                if (!new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), getString(R.string.app_name)).isDirectory())
                                    new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), getString(R.string.app_name)).mkdirs();

                                for (String type : FileTypeLookup.fileTypeStrings) {
                                    if (!new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + getString(R.string.app_name) + "/" + type + "/").exists()) {
                                        new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + getString(R.string.app_name) + "/" + type + "/").mkdirs();
                                    }
                                }

                                startActivity(new Intent(Mode_Selection_Activity.this, RecieveActivity.class));
                                finish();
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1971);
                                }
                            }
                        } else {
                            taskAfterEnableGPS = 2;
                        }
                    }
                });
            }
            else {
                if ((ContextCompat.checkSelfPermission(Mode_Selection_Activity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(Mode_Selection_Activity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                    if (!new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), getString(R.string.app_name)).isDirectory())
                        new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), getString(R.string.app_name)).mkdirs();

                    for (String type : FileTypeLookup.fileTypeStrings) {
                        if (!new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + getString(R.string.app_name) + "/" + type + "/").exists()) {
                            new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + getString(R.string.app_name) + "/" + type + "/").mkdirs();
                        }
                    }

                    startActivity(new Intent(Mode_Selection_Activity.this, RecieveActivity.class));
                    finish();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1971);
                    }
                }
            }
        }
        else {
            taskAfterEnableGPS = 2;
        }
//        new IntentIntegrator(this).setOrientationLocked(false).initiateScan();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ftp_server_menu_item) {
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                startActivity(new Intent(this, PC_Connect_Activity.class));
            }
            else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE }, 1989);
                }
            }
            return true;
        }
        else if (item.getItemId() == R.id.material_you_menu) {
            new TinyDB(this).putBoolean(Strings.useA12Theme_preference_key, !new TinyDB(this).getBoolean(Strings.useA12Theme_preference_key));
            recreate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private int press = 0;

    @Override
    public void onBackPressed() {
        if (press < 1) {
            press++;
            Toast.makeText(this, "Press back again to exit the app", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> press = 0, 1000);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.mode_selection_activity_options, menu);
        menu.findItem(R.id.material_you_menu).setVisible(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S);
        if (new TinyDB(this).getBoolean(Strings.useA12Theme_preference_key))
            menu.findItem(R.id.material_you_menu).setTitle(getResources().getString(R.string.use_material));
        else
            menu.findItem(R.id.material_you_menu).setTitle(getResources().getString(R.string.use_material_you));
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Strings.GPS_REQUEST){
            if (resultCode == 0){
                Toast.makeText(this, "Please Enable Gps", Toast.LENGTH_LONG).show();
            }
            else if (resultCode == RESULT_OK){

                if (taskAfterEnableGPS == 1) {
                    gotoserverpage(null);
                }
                else if (taskAfterEnableGPS == 2) {
                    gotoclientpage(null);
                }
            }
            Log.e("taskAfterGPS", String.valueOf(taskAfterEnableGPS));
            Log.e ("Result", String.valueOf(resultCode));
        }
        else {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    public void gotoviewfilespage(View view) {

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            if (!new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), getString(R.string.app_name)).isDirectory())
                new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), getString(R.string.app_name)).mkdirs();

            for (String type : FileTypeLookup.fileTypeStrings) {
                if (!new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + getString(R.string.app_name) + "/" + type + "/" ).exists()) {
                        new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + getString(R.string.app_name) + "/" + type + "/" ).mkdirs();
                }
            }
            Intent i = new Intent(this, FileBrowser.class); //works for all 3 main classes (i.e FileBrowser, FileChooser, FileBrowserWithCustomHandler)
            i.putExtra(Constants.INITIAL_DIRECTORY, new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(),getString(R.string.app_name)).getAbsolutePath());
            startActivity(i);
        }
        else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE }, 1973);
            }
        }

    }

    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
    private static Bitmap takeScreenShot(Activity activity)
    {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
//        view.buildDrawingCache();
        Bitmap b1 = Bitmap.createBitmap(view.getDrawingCache());
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay().getHeight();

        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height  - statusBarHeight);
        view.destroyDrawingCache();
        return b;
    }
    public Bitmap fastblur(Bitmap sentBitmap, int radius) {
        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];

        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }
}
