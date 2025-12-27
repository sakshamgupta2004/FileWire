package com.sugarsnooper.filetransfer;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;


public class VideoPlayerDismissable extends Activity {
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            }
            setOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR, this);
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // Hide the nav bar and status bar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player_popup);




        findViewById(R.id.back_button_image_view_fullscreen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, VideoPlayerDismissable.this);
                finishAfterTransition();
            }
        });
//        findViewById(R.id.dismissable).setOnTouchListener(new SwipeImageTouchListener(findViewById(R.id.dismissable), new SwipeImageTouchListener.onDismissListener() {
//            @Override
//            public void onDismiss() {
//                finish();
//            }
//        }));

        ((TextView) findViewById(R.id.photo_view_name)).setText(getIntent().getStringExtra("NAME"));
        play();

    }
    ExoPlayer player;
    private void play() {
        String path = getIntent().getStringExtra("URI");
        if (path == null) return;

        // 1. Create ExoPlayer instance
        player = new ExoPlayer.Builder(this).build();

        // 2. Bind the player to the view
        PlayerView playerView = findViewById(R.id.dismissable);
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);

        // 3. Create MediaItem
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(path));

        // 4. Prepare the player
        player.setMediaItem(mediaItem);
        player.prepare();
        player.setPlayWhenReady(true);

        playerView.hideController();
        findViewById(R.id.overlay_image_view_and_exo_player).setVisibility(View.GONE);

        // 5. Handle display cutout / notch
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutParams.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().setAttributes(layoutParams);

        // 6. Handle controller visibility changes
        playerView.setControllerVisibilityListener(new PlayerControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                View overlay = findViewById(R.id.overlay_image_view_and_exo_player);
                WindowManager.LayoutParams lp = getWindow().getAttributes();

                if (visibility == View.VISIBLE) {
                    overlay.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
                    }
                    getWindow().setAttributes(lp);
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                } else {
                    overlay.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                    }
                    getWindow().setAttributes(lp);
                    getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    );
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.release();
            player = null;
        }
    }


    private static class SwipeImageTouchListener implements View.OnTouchListener{

        interface onDismissListener{
            public void onDismiss();
        }

        private final View swipeView;
        private final onDismissListener onDismissListener;

        public SwipeImageTouchListener(View swipeView, onDismissListener onDismissListener) {
            this.swipeView = swipeView;
            this.onDismissListener = onDismissListener;
        }

        // Allows us to know if we should use MotionEvent.ACTION_MOVE
        private boolean tracking = false;
        // The Position where our touch event started
        private float startY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Rect hitRect = new Rect();
                    swipeView.getHitRect(hitRect);
                    if (hitRect.contains((int) event.getX(), (int) event.getY())) {
                        tracking = true;
                    }
                    startY = event.getY();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    tracking = false;
                    animateSwipeView(v.getHeight());
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (tracking) {
                        swipeView.setTranslationY(event.getY() - startY);
                    }
                    return true;
            }
            return false;
        }

        /**
         * Using the current translation of swipeView decide if it has moved
         * to the point where we want to remove it.
         */
        private void animateSwipeView(int parentHeight) {
            int quarterHeight = parentHeight / 6;
            float currentPosition = swipeView.getTranslationY();
            float animateTo = 0.0f;
//            if (currentPosition < -quarterHeight) {
//                animateTo = -parentHeight;
//            }else
            if (currentPosition > quarterHeight) {
                animateTo = parentHeight;
            }
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(swipeView, "translationY", currentPosition, animateTo)
                    .setDuration(200);
            float finalAnimateTo = animateTo;
            objectAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (finalAnimateTo == parentHeight)
                        onDismissListener.onDismiss();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            objectAnimator.start();
        }
    }





















    @Override
    protected void onPause() {
        super.onPause();
        player.stop();
        player.release();

//        overridePendingTransition(0, 0);
    }

    public static void setOrientation(int orientation, Activity activity) {
        try {
            activity.setRequestedOrientation(orientation);
        }
        catch (IllegalStateException e){
            setOrientation(orientation, activity);
        }
    }

    @Override
    public void onBackPressed() {
        finishAfterTransition();
    }
}
