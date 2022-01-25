package com.sugarsnooper.filetransfer;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.util.Random;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

import static com.sugarsnooper.filetransfer.Application.avatars;
import static net.yslibrary.android.keyboardvisibilityevent.util.UIUtil.hideKeyboard;

public class splash_screen extends Activity {
    private static boolean isAlive = false;
    private TinyDB tinyDb;
    private float cardMovedBy;
    private int selectedAvatar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (new TinyDB(getApplicationContext()).getString(Strings.user_name_preference_key,  null) == null)) {
            new TinyDB(getApplicationContext()).putBoolean(Strings.useA12Theme_preference_key, true);
            this.setTheme(R.style.DynamicTheme);
            start();
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            startActivity(new Intent(this, Mode_Selection_Activity.class));
            finish();
        }
        else {
            start();
        }
    }

    private void start() {
        setContentView(R.layout.splash_screen);

        isAlive = true;
        tinyDb = new TinyDB(getApplicationContext());
        int nightModeFlags =
                this.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                ((ImageView) findViewById(R.id.splash_logo)).setImageResource(R.drawable.ic_logo_dark_playstore_without_bg);
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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (tinyDb.getString(Strings.user_name_preference_key, null) == null) {
                    showAvatarAndNameDialog();
                } else {
                    switchToMainActivity();
                }
            }
        }, 400);
    }

    private void switchToMainActivity() {
        startActivity(new Intent(splash_screen.this, Mode_Selection_Activity.class));
        overridePendingTransition(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit);
        finish();
    }

    @SuppressLint("ResourceType")
    private void showAvatarAndNameDialog() {
        Random r = new Random();
        ImageView imageView = findViewById(R.id.splash_logo);
        selectedAvatar = r.nextInt(avatars.length);
        ObjectAnimator animation = ObjectAnimator.ofFloat(imageView, "translationY", -500f);



        int dialogStyle = R.style.LoginDialog;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            dialogStyle = R.style.LoginDialogDynamic;
        final Dialog dialog = new Dialog(splash_screen.this, dialogStyle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.name_avtar_entry_view);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        window.setAttributes(wlp);
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        EditText namebox = dialog.findViewById(R.id.name_box_firstpage);
        namebox.setText(Build.MODEL);
        KeyboardVisibilityEvent.setEventListener(this, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                if (isOpen)
                    imageView.setVisibility(View.INVISIBLE);
                else
                    imageView.setVisibility(View.VISIBLE);
            }
        });
        animation.setDuration(500);
        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                BlurView blurBehind = dialog.findViewById(R.id.blur_behind_avtar_panel);

                TypedValue a = new TypedValue();
                getTheme().resolveAttribute(android.R.attr.colorBackground, a, true);

                int red = Color.red(a.data);
                int green = Color.green(a.data);
                int blue = Color.blue(a.data);
                int alpha = 156;
                blurBehind.setupWith((ViewGroup) getWindow().getDecorView().getRootView())
                        .setBlurAlgorithm(new RenderScriptBlur(splash_screen.this))
                        .setBlurAutoUpdate(true)
                        .setHasFixedTransformationMatrix(false)
                        .setBlurRadius(2f)
                        .setOverlayColor(Color.argb(alpha, red, green, blue))
                        .setBlurEnabled(true);

                blurBehind = dialog.findViewById(R.id.blur_behind_avtar_select_panel);
                blurBehind.setupWith((ViewGroup) getWindow().getDecorView().getRootView())
                        .setBlurAlgorithm(new RenderScriptBlur(splash_screen.this))
                        .setBlurAutoUpdate(true)
                        .setHasFixedTransformationMatrix(false)
                        .setBlurRadius(2f)
                        .setOverlayColor(Color.argb(alpha, red, green, blue))
                        .setBlurEnabled(true);



            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animation.start();

        final CardView name_avtar_entry_card_view = dialog.findViewById(R.id.name_avtar_entry_card);
        final CardView avtarSelectorCard = dialog.findViewById(R.id.avtar_selector_card);



        TypedValue a = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.colorBackground, a, true);

        int red = Color.red(a.data);
        int green = Color.green(a.data);
        int blue = Color.blue(a.data);
        int alpha = 156;

        name_avtar_entry_card_view.setCardBackgroundColor(Color.argb(alpha, red, green, blue));
        avtarSelectorCard.setCardBackgroundColor(Color.argb(alpha, red, green, blue));

        final ImageView iv = dialog.findViewById(R.id.avtar_selector_image);
        iv.setImageResource(avatars[selectedAvatar]);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(splash_screen.this);
                cardMovedBy = (float) (name_avtar_entry_card_view.getWidth() * -1.5);
                ObjectAnimator animation1 = ObjectAnimator.ofFloat(avtarSelectorCard, "translationX", cardMovedBy * -1);
                animation1.setDuration(200);
                animation1.start();
                ObjectAnimator animation = ObjectAnimator.ofFloat(name_avtar_entry_card_view, "translationX", cardMovedBy);
                animation.setDuration(250);
                animation.start();
                animation.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        // name_avtar_entry_card_view.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });

                animation1.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        avtarSelectorCard.setVisibility(View.VISIBLE);
                        ObjectAnimator animation = ObjectAnimator.ofFloat(avtarSelectorCard, "translationX", 0);
                        animation.setDuration(250);
                        animation.start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });

            }
        });


        Button register = dialog.findViewById(R.id.save_name_avatar_button);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText namebox = dialog.findViewById(R.id.name_box_firstpage);
                if (namebox.getText().toString().trim().isEmpty()) {
                    if (!namebox.hasFocus())
                        namebox.requestFocus();
                    namebox.setError("Please enter your name");
                } else {
                    hideKeyboard(splash_screen.this);
                    tinyDb.putString(Strings.user_name_preference_key, namebox.getText().toString());
                    tinyDb.putInt(Strings.avatar_preference_key, selectedAvatar);
                    switchToMainActivity();
                }
            }
        });


        RecyclerView recyclerView = dialog.findViewById(R.id.avtar_selection_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(new AvatarSelectorAdpter());


        Button avtarConfirm = dialog.findViewById(R.id.avatar_selection_confirm_button);
        avtarConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // name_avtar_entry_card_view.setVisibility(View.VISIBLE);
                iv.setImageResource(avatars[selectedAvatar]);
                ObjectAnimator animation = ObjectAnimator.ofFloat(avtarSelectorCard, "translationX", -1 * cardMovedBy);
                animation.setDuration(250);
                animation.start();

                animation.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {

                        ObjectAnimator animation = ObjectAnimator.ofFloat(name_avtar_entry_card_view, "translationX", 0);
                        animation.setDuration(250);
                        animation.start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });

            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                startService(new Intent(getApplicationContext(), readableRootsSurvivor.class));
            }
        });
    }

    public static boolean getActivity(){
        return isAlive;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAlive = false;
    }


    private class AvatarSelectorAdpter extends RecyclerView.Adapter<AvatarSelectorAdpter.AvatarSelectorViewHolder> {
        @NonNull
        @Override
        public AvatarSelectorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AvatarSelectorViewHolder(LayoutInflater.from(splash_screen.this).inflate(R.layout.avatar_selector_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull AvatarSelectorViewHolder holder, final int position) {
            Glide.with(splash_screen.this).load(avatars[position]).into((ImageView) holder.itemView.findViewById(R.id.avatar_selector_item_imageview));
            if (position == selectedAvatar) {
                ((CheckBox) holder.itemView.findViewById(R.id.checkbox_avatar_checked_round)).setChecked(true);
            }
            else {
                ((CheckBox) holder.itemView.findViewById(R.id.checkbox_avatar_checked_round)).setChecked(false);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedAvatar = position;
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return avatars.length;
        }

        class AvatarSelectorViewHolder extends RecyclerView.ViewHolder {
            public AvatarSelectorViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}
