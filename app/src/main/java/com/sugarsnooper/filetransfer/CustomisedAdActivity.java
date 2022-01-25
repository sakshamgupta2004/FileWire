package com.sugarsnooper.filetransfer;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

public class CustomisedAdActivity extends FragmentActivity {

    public boolean shouldUpdateAd = false;
    private String adImageUrl = "";
    private String url_getAdImage = "http://www.sugarsnooper.com/file_share/ads.php";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (new TinyDB(this).getBoolean(Strings.useA12Theme_preference_key))
                this.setTheme(R.style.DynamicTheme);
        }
        super.onCreate(savedInstanceState);
//        Explode explodeAnimation = new Explode();
//        explodeAnimation.setDuration(750);
//        getWindow().setEnterTransition(explodeAnimation);

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        Log.e("Ui Thread Priority", String.valueOf(Thread.currentThread().getPriority()));


            int nightModeFlags =
                    this.getResources().getConfiguration().uiMode &
                            Configuration.UI_MODE_NIGHT_MASK;
            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
//                getWindow().getDecorView().getRootView().setBackground(new ColorDrawable(Color.parseColor("#000000")));
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || !new TinyDB(this).getBoolean(Strings.useA12Theme_preference_key))
                        getWindow().getDecorView().getRootView().setBackground(getDrawable(R.drawable.background_dark));
                    else
                    {
                        Drawable d = getDrawable(R.drawable.background_dark);
                        d.setColorFilter(getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
                        getWindow().getDecorView().getRootView().setBackground(d);
                    }
                    break;

                case Configuration.UI_MODE_NIGHT_NO:

                case Configuration.UI_MODE_NIGHT_UNDEFINED:
//                getWindow().getDecorView().getRootView().setBackground(new ColorDrawable(Color.parseColor("#F8F8F8")));
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || !new TinyDB(this).getBoolean(Strings.useA12Theme_preference_key) || true)
                        getWindow().getDecorView().getRootView().setBackground(getDrawable(R.drawable.background));
                    else
                    {
                        Drawable d = getDrawable(R.drawable.background);
                        d.setColorFilter(getColor(R.color.colorAccent), PorterDuff.Mode.OVERLAY);
                        getWindow().getDecorView().getRootView().setBackground(d);
                    }
                    break;
            }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        new Thread(new Runnable() {
            @Override
            public void run() {
//                startService(new Intent(CustomisedAdActivity.this, UpdateActivityService.class));
                while (true) {
                    if (shouldUpdateAd) {
                        adImageUrl = getAd();
                        if (adImageUrl != null){
                            String hyperlink = getLink(adImageUrl);
                            String imUrl = getImURL(adImageUrl);
                            if (hyperlink != null && imUrl != null){
                                try {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Uri uri = Uri.parse(imUrl);
                                            findViewById(R.id.customised_add_iv).setVisibility(View.VISIBLE);
                                            Glide.with(getBaseContext())
                                                    .load(uri).addListener(new RequestListener<Drawable>() {
                                                @Override
                                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                    return false;
                                                }

                                                @Override
                                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                    findViewById(R.id.customised_add_iv).setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(hyperlink));
                                                            startActivity(i);
                                                        }
                                                    });
                                                    return false;
                                                }
                                            }).into((ImageView) findViewById(R.id.customised_add_iv));
                                        }
                                    });
                                }
                                catch (Exception e){

                                }
                            }
                            else {
                                hide_ad();
                            }
                        }
                        else{
                            hide_ad();
                        }
                    }
                    else {
                        break;
                    }
                }
            }
        }).start();
    }

    private void hide_ad() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(this, readableRootsSurvivor.class));
    }

    private String getImURL(String adImageUrl) {
        try {
            return adImageUrl.substring(0, adImageUrl.indexOf("<br><br>"));
        }
        catch (Exception e){
            return null;
        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private String getLink(String adImageUrl) {
        try {
            return adImageUrl.substring(adImageUrl.indexOf("<br><br>") + 8);
        }
        catch (Exception e){
            return null;
        }
    }

    private String getAd() {
        String link = url_getAdImage;
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
            return sb.toString();
        }
        catch (Exception f){
            return null;
        }
    }
}
