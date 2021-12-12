package com.sugarsnooper.filetransfer.Server.File.Selection;

import android.animation.Animator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.sugarsnooper.filetransfer.ConnectToPC.FTP.PC_Connect_Activity;
import com.sugarsnooper.filetransfer.ConnectToPC.PCSoftware.PC_ConnectActivity;
import com.sugarsnooper.filetransfer.FileTypeLookup;
import com.sugarsnooper.filetransfer.QRCodeFormatter;
import com.sugarsnooper.filetransfer.R;
import com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments.AppSelectionFragment;
import com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments.Gallery;
import com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments.Photos;
import com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments.VideoGalleryFragment;
import com.sugarsnooper.filetransfer.Server.InitializeServerFragment;
import com.sugarsnooper.filetransfer.Server.Send_Activity;
import com.sugarsnooper.filetransfer.Server.ServerService;
import com.sugarsnooper.filetransfer.Strings;
import com.thanosfisherman.wifiutils.WifiUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

import static android.view.View.GONE;
import static com.sugarsnooper.filetransfer.Strings.TAG;


public class FileSelection extends Fragment {
    private static final int REQUEST_CODE_FOR_PICK_FILE = 19690;
    public static List<Media> videoList;
    public static List<Media> imageList;
    public static List<Media> appsList;
    public static List<Media> fileList;
    public static List<Media> galleryList;
    public static List<Media> fileAndFolderList;
    public static HashMap<String, Integer> fileAndFolderListPositionTableW_R_T_URI;
    public static HashMap<String, List<String>> selectedFolderMap;
    public static int gridExtraItems = 6;
    private static ExtendedFloatingActionButton view_counter;
    public static ExtendedFloatingActionButton fab1, fab2, fab3;
    private static FloatingActionButton send_fab_substitute;
    public static ViewPager2 viewPager;
    public static BlurView blurView;
    private static int selected_counter = 0;
    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_3, R.string.tab_text_1, R.string.tab_text_2, R.string.tab_text_4, R.string.tab_text_5};
    @DrawableRes
    private static final int[] TAB_ICONS = new int[]{R.drawable.tab_text_3, R.drawable.tab_text_1, R.drawable.tab_text_2, R.drawable.tab_text_4, R.drawable.tab_text_5};
    public static int bottomBarHeight;

    private static boolean isBeingAnimated = false, wasLastScrollDown = false, wasLastScrollUp = false;
    private View view;
    private boolean wifiPanelWasOpen = false;

    public static AppSelectionFragment appSelectionFragment = null;
    public static Photos photosFragment = null;
    public static VideoGalleryFragment videoGalleryFragment = null;
    public static Gallery galleryFragment = null;
    public static FragmentContainerFileSelection filesFragments = null;
    public static SearchFileFragment searchFragment = null;
    private static ActionBar ab;
    private FloatingActionButton selectedFabAppbar;
    private FloatingActionButton sendFabAppbar;

    public static void scrollDown() {


        if (!isBeingAnimated && !wasLastScrollDown) {
//            ab.hide();
            wasLastScrollDown = true;
            wasLastScrollUp = false;
            isBeingAnimated = true;
            Log.e(TAG, "Scroll Down");
            int animateBlurViewBy = blurView.getMeasuredHeight();
            int animateBy = animateBlurViewBy;

            try {
                ExtendedFloatingActionButton fab = fab1;
                fab.animate().setDuration(200).translationY(animateBy).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fab.shrink();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
            } catch (Exception e) {

            }
            try {
                ExtendedFloatingActionButton fab = fab2;
                fab.animate().setDuration(200).translationY(animateBy).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fab.shrink();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
            } catch (Exception e) {

            }
            try {
                ExtendedFloatingActionButton fab = fab3;
                fab.animate().setDuration(200).translationY(animateBy).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fab.shrink();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
            } catch (Exception e) {

            }
            blurView.animate().setDuration(200).translationY(animateBlurViewBy).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    send_fab_substitute.show();
                    isBeingAnimated = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).start();
        }
    }

    public static void scrollUp() {
        if (!isBeingAnimated && !wasLastScrollUp) {
//            ab.show();
            wasLastScrollDown = false;
            wasLastScrollUp = true;
            isBeingAnimated = true;
            Log.e(TAG, "Scroll Up");
            try {
                ExtendedFloatingActionButton fab = fab1;
                fab.animate().setDuration(200).translationX(0).translationY(0).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fab.extend();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
            }
            catch (Exception e){

            }
            try {
                ExtendedFloatingActionButton fab = fab2;
                fab.animate().setDuration(200).translationX(0).translationY(0).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fab.extend();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
            }
            catch (Exception e){

            }
            try {
                ExtendedFloatingActionButton fab = fab3;
                fab.animate().setDuration(200).translationX(0).translationY(0).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        fab.extend();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
            }
            catch (Exception e){

            }
            blurView.animate().setDuration(200).translationX(0).translationY(0).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    send_fab_substitute.hide();
                    isBeingAnimated = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).start();
        }
    }

    public class PagerAdapter extends FragmentStateAdapter {


//        private Fragment mFragmentAtPos0;
//        private final FragmentManager mFragmentManager;
//        private final FirstPageListener listener = new FirstPageListener();
//        public final class FirstPageListener implements
//                FirstPageFragmentListener {
//            public void onSwitchToNextFragment() {
//                mFragmentManager.beginTransaction().remove(mFragmentAtPos0)
//                        .commit();
//                if (mFragmentAtPos0 instanceof FilesandFolder_Others_MainPage){
//                    mFragmentAtPos0 = new FileExplorer(listener);
//                }else{ // Instance of NextFragment
//                    mFragmentAtPos0 = new FilesandFolder_Others_MainPage(listener);
//                }
//                notifyDataSetChanged();
//            }
//            public void onSwitchToNextFragment(Bundle bundle, int FT) {
//                mFragmentManager.beginTransaction().remove(mFragmentAtPos0)
//                        .commit();
//                if (mFragmentAtPos0 instanceof FilesandFolder_Others_MainPage){
//                    mFragmentAtPos0 = new FileExplorer(listener);
//                    mFragmentAtPos0.setArguments(bundle);
//                }else{ // Instance of NextFragment
//                    mFragmentAtPos0 = new FilesandFolder_Others_MainPage(listener);
//                    mFragmentAtPos0.setArguments(bundle);
//                }
//                notifyDataSetChanged();
////                notifyItemChanged(getItemPosition(mFragmentAtPos0));
//            }
//        }


//        @Override
//        public void onBindViewHolder(@NonNull FragmentViewHolder holder, int position, @NonNull List<Object> payloads) {
//            super.onBindViewHolder(holder, position, payloads);
//            Log.e("ID", String.valueOf(holder.getItemId()));
//            holder.itemView.
////            if (position == getItemPosition(mFragmentAtPos0)) {
////
////                Fragment f = mFragmentManager.findFragmentByTag("f" + holder.getItemId());
////                if (f != null) {
////                    mFragmentManager.beginTransaction().replace(holder.getItemId(), )
////                }
////            }
//        }

        private int getItemPosition(Fragment mFragmentAtPos0) {
            for (int i = 0; i < getItemCount(); i++){

                if (createFragment(i).equals(mFragmentAtPos0)){
                    return i;
                }
            }
            return -1;
        }


        public PagerAdapter(FragmentActivity fm) {
            super(fm);
//            mFragmentManager = fm.getSupportFragmentManager();
//            mFragmentAtPos0 = new FilesandFolder_Others_MainPage(listener);
        }


        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position){
                case 0:
                    appSelectionFragment = new AppSelectionFragment();
                    return appSelectionFragment;
                case 1:
                    photosFragment = new Photos();
                    return photosFragment;
                case 2:
                    videoGalleryFragment = new VideoGalleryFragment();
                    return videoGalleryFragment;
                case 3:
                    galleryFragment = new Gallery();
                    return galleryFragment;
                default:
                    filesFragments = new FragmentContainerFileSelection();
                    return filesFragments;
            }
        }

        @Override
        public int getItemCount() {
            return 5;
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requireActivity().setTitle(R.string.select_files);
        setHasOptionsMenu(true);
        ab = getActivity().getActionBar();
        sendFabAppbar = getActivity().findViewById(R.id.send_fab_appbar);
        selectedFabAppbar = getActivity().findViewById(R.id.selected_fab_appbar);
        sendFabAppbar.setOnClickListener(v -> send_fab_click());
        selectedFabAppbar.setOnClickListener(v -> onViewCounterSelectedClick());
        return inflater.inflate(R.layout.file_selection_fragment, container, false);
    }
    enum State {
        EXPANDED,
        COLLAPSED,
        IDLE
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.view = view;


            ((AppBarLayout) getActivity().findViewById(R.id.appbar)).addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                private State state;
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (verticalOffset == 0) {
                        if (state != State.EXPANDED) {
                            Log.e(TAG,"Expanded");
                            sendFabAppbar.show();
                            selectedFabAppbar.show();
                        }
                        state = State.EXPANDED;
                    } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
                        if (state != State.COLLAPSED) {
                            Log.e(TAG,"Collapsed");
                            sendFabAppbar.hide();
                            selectedFabAppbar.hide();
                        }
                        state = State.COLLAPSED;
                    } else {
                        if (state != State.IDLE) {
                            Log.e(TAG,"Idle");
                            sendFabAppbar.hide();
                            selectedFabAppbar.hide();
                        }
                        state = State.IDLE;
                    }
                }
            });

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            ((AppBarLayout) getActivity().findViewById(R.id.appbar)).setOnScrollChangeListener(new View.OnScrollChangeListener() {
//                @Override
//                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//               Log.e("Scroll", String.valueOf(scrollY));
//                    int dY = scrollY - oldScrollY;
//                    if (dY > 1)
//                        ((AppBarLayout) v).setExpanded(false, true);
//                    else if (dY < -1)
//                        ((AppBarLayout) v).setExpanded(true, true);
//                }
//            });
//        }

        StringBuffer infoBuffer = new StringBuffer();

        infoBuffer.append("-------------------------------------\n");
        infoBuffer.append("Model :" + Build.MODEL + "\n");//The end-user-visible name for the end product.
        infoBuffer.append("Device: " + Build.DEVICE + "\n");//The name of the industrial design.
        infoBuffer.append("Manufacturer: " + Build.MANUFACTURER + "\n");//The manufacturer of the product/hardware.
        infoBuffer.append("Board: " + Build.BOARD + "\n");//The name of the underlying board, like "goldfish".
        infoBuffer.append("Brand: " + Build.BRAND + "\n");//The consumer-visible brand with which the product/hardware will be associated, if any.
        infoBuffer.append("Serial: " + Build.SERIAL + "\n");
        infoBuffer.append("-------------------------------------\n");
    /* Android doc:
        This 'Serial' field was deprecated in API level O.
        Use getSerial() instead.
        A hardware serial number, if available.
        Alphanumeric only, case-insensitive. For apps targeting SDK higher than N_MR1 this field is set to UNKNOWN.
    */

//I just used AlertDialog to show device information
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setCancelable(true);
        dialog.setTitle("Device information:");
        dialog.setMessage(infoBuffer);//StringBuffer which we appended the device informations.
//        dialog.show();

        send_fab_substitute = view.findViewById(R.id.send_fab_substitute);
        send_fab_substitute.hide();
        send_fab_substitute.setOnClickListener(v -> send_fab_click());

        float radius = 25f;

        View decorView = getActivity().getWindow().getDecorView();
        //ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        //Set drawable to draw in the beginning of each blurred frame (Optional).
        //Can be used in case your layout has a lot of transparent space and your content
        //gets kinda lost after after blur is applied.
        Drawable windowBackground = decorView.getBackground();
        blurView = ((BlurView) view.findViewById(R.id.blurView));
        blurView.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(getActivity()))
                .setBlurRadius(radius)
                .setBlurAutoUpdate(true)
                .setHasFixedTransformationMatrix(false);
        blurView.setClickable(true);
        blurView.setFocusable(true);

        selected_counter = 0;
        videoList = new CopyOnWriteArrayList<>();
        imageList = new CopyOnWriteArrayList<>();
        appsList = new CopyOnWriteArrayList<>();
        galleryList = new CopyOnWriteArrayList<>();
        fileAndFolderList = new CopyOnWriteArrayList<>();
        fileAndFolderListPositionTableW_R_T_URI = new HashMap<>();
        selectedFolderMap = new HashMap<>();
        fileList = new CopyOnWriteArrayList<>();

        //autoRefreshSelectedSize();

        viewPager = view.findViewById(R.id.file_selection_vp);
        viewPager.setAdapter(new PagerAdapter(requireActivity()));
        TabLayout tabLayout = view.findViewById(R.id.file_selection_tl);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(getString(TAB_TITLES[position])).setIcon(TAB_ICONS[position])
        ).attach();
        viewPager.setOffscreenPageLimit(5);
        view.findViewById(R.id.send_files_fab).setOnClickListener(v -> send_fab_click());
        view_counter = view.findViewById(R.id.selected_files_counter);
        view_counter.setOnClickListener(v -> onViewCounterSelectedClick());
        view_counter.setText("Selected: 0 (0.00 B)");
    }

    private void send_fab_click() {
        if (!getArguments().getBoolean(Strings.FileSelectionRequest)) {
            if (!((WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE)).isWifiEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    new AlertDialog.Builder(requireActivity())
                            .setTitle("Enable WiFi")
                            .setMessage("Please turn on WiFi." +
                                    "\n" +
                                    getString(R.string.app_name) +
                                    " requires you to turn WiFi in order to transfer files.")
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setPositiveButton("WiFi Settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    wifiPanelWasOpen = true;
                                    Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                                    startActivity(panelIntent);
                                }
                            }).show();
                } else {
                    ((WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(true);
                    ProgressDialog pd = new ProgressDialog(getActivity());
                    pd.setCancelable(false);
                    pd.setCanceledOnTouchOutside(false);
                    pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pd.setIndeterminate(true);
                    pd.setTitle("Trying to turn on WiFi");
                    pd.setMessage("Please Wait!");
                    pd.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                int i = 0;
                                while (!((WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE)).isWifiEnabled()) {
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    if (i <= 3) {
                                        i++;
                                    } else {
                                        break;
                                    }
                                }
                                requireActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        pd.dismiss();
                                        if (!((WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE)).isWifiEnabled()) {
                                            Toast.makeText(getContext(), R.string.please_enable_wifi, Toast.LENGTH_LONG).show();
                                        } else {
                                            send_button_click();
                                        }
                                    }
                                });
                            } catch (Exception e) {

                            }
                        }
                    }).start();
                }
            } else {
                long startTime = System.nanoTime();
                send_button_click();
                long endTime = System.nanoTime();

                long duration = (endTime - startTime);
                Log.e("Time to Dialog", String.valueOf(duration / 1000000));

            }
        }
        else {
            send_button_click();
        }
    }

    private void onViewCounterSelectedClick() {
        refreshTotalSize();
        if (selected_counter != 0) {
            Dialog dialog = new Dialog(getActivity(), R.style.PauseDialog);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.view_selected_files_dialog);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            Window window = dialog.getWindow();
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
            window.setAttributes(wlp);
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            CopyOnWriteArrayList<Media> selectedList = new CopyOnWriteArrayList<>();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                class selectedFilesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
                    final CopyOnWriteArrayList<Media> selectedList;
                    public selectedFilesAdapter(CopyOnWriteArrayList<Media> selectedList) {
                        for (Media file : appsList) {
                            if (file.isChecked())
                                selectedList.add(file);
                        }
                        for (Media file : imageList) {
                            if (file.isChecked())
                                selectedList.add(file);
                        }
                        for (Media file : videoList) {
                            if (file.isChecked())
                                selectedList.add(file);
                        }
                        for (Media file : galleryList) {
                            if (file.isChecked())
                                selectedList.add(file);
                        }
                        for (Media file : fileAndFolderList) {
                            if (file.isChecked())
                                selectedList.add(file);
                        }
                        for (Media file : fileList) {
                            if (file.isChecked())
                                selectedList.add(file);
                        }
                        this.selectedList = selectedList;
                    }

                    @NonNull
                    @Override
                    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        return new selectedFilesAdapter.VH(LayoutInflater.from(getContext()).inflate(R.layout.selected_files_recycler_item, parent, false));
                    }

                    @Override
                    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                        ImageView selectedImage = holder.itemView.findViewById(R.id.selected_files_item_image);
                        TextView itemNameView = holder.itemView.findViewById(R.id.selected_files_item_name);
                        TextView itemSizeView = holder.itemView.findViewById(R.id.selected_files_item_size);
                        ImageView deleteItem = holder.itemView.findViewById(R.id.selected_files_remove_item_image);
                        deleteItem.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                selectedList.get(position).setChecked(false);
                                selectedList.remove(position);
//                                notifyDataSetChanged();
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, selectedList.size());
                                appSelectionFragment.onChange();
                                photosFragment.onChange();
                                videoGalleryFragment.onChange();
                                galleryFragment.onChange();
                                filesFragments.onChange();
                                if (searchFragment != null)
                                    searchFragment.onChange();
                                selected_counter--;
                                refreshTotalSize();
                                if (selected_counter == 0) {
                                    dialog.dismiss();
                                }
                            }
                        });
                        if (!selectedList.get(position).isFolder()) {
                            switch (FileTypeLookup.fileType(selectedList.get(position).getName())) {
                                case 1:
                                    Glide.with(getContext())
                                            .load(R.drawable.ic_paper)
                                            .into(selectedImage);
                                    break;
                                case 2:
                                case 3:
                                    Glide.with(getContext())
                                            .load(selectedList.get(position).getUri())
                                            .into(selectedImage);
                                    break;
                                case 4:
                                    Glide.with(getContext())
                                            .load(R.drawable.ic_music)
                                            .into(selectedImage);
                                    break;
                                case 5:
                                    Glide.with(getContext())
                                            .load(R.drawable.ic_files_and_folders)
                                            .into(selectedImage);
                                    break;
                                case 6:
                                    try {
                                        String sourcePath = selectedList.get(position).getFullPath();
                                        if (selectedList.get(position).getName().endsWith(".apk")) {

                                            Glide.with(getContext())
                                                    .load(new File(getContext().getExternalCacheDir().getPath() + File.separator + "APKThumbnails" + File.separator + selectedList.get(position).getName() + sourcePath.hashCode() + ".jpeg"))
                                                    .transition(DrawableTransitionOptions.withCrossFade())
                                                    .addListener(new RequestListener<Drawable>() {
                                                        @Override
                                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                            PackageInfo packageInfo = requireContext().getPackageManager()
                                                                    .getPackageArchiveInfo(sourcePath, PackageManager.GET_ACTIVITIES);
                                                            if (packageInfo != null) {
                                                                ApplicationInfo appInfo = packageInfo.applicationInfo;
                                                                appInfo.sourceDir = sourcePath;
                                                                appInfo.publicSourceDir = sourcePath;
                                                                File folderToSave1 = new File(getContext().getExternalCacheDir().getPath() + File.separator + "APKThumbnails");
                                                                folderToSave1.mkdirs();
                                                                saveBitmapToFile(folderToSave1, selectedList.get(position).getName() + sourcePath.hashCode() + ".jpeg", drawableToBitmap(appInfo.loadIcon(getContext().getPackageManager())), Bitmap.CompressFormat.JPEG, 95);

                                                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        Glide.with(getContext())
                                                                                .load(appInfo.loadIcon(getContext().getPackageManager()))
                                                                                .transition(DrawableTransitionOptions.withCrossFade())
                                                                                .into(selectedImage);

                                                                    }
                                                                });
                                                            } else {
                                                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        Glide.with(getContext())
                                                                                .load(R.drawable.unknown_file_icon)
                                                                                .into(selectedImage);
                                                                    }
                                                                });
                                                            }
                                                            return false;
                                                        }

                                                        @Override
                                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                            return false;
                                                        }
                                                    })
                                                    .into(selectedImage);
                                            break;
                                        }
                                    }
                                    catch (NullPointerException ne) {

                                    }
                                default:
                                    Glide.with(getContext())
                                            .load(R.drawable.unknown_file_icon)
                                            .into(selectedImage);
                                    break;
                            }
                        } else {
                            Glide.with(getContext())
                                    .load(R.drawable.ic_folder)
                                    .into(selectedImage);
                        }
                        itemNameView.setText(selectedList.get(position).getName());
                        itemSizeView.setText(getFormatSize(selectedList.get(position).getSize()));
                    }

                    @Override
                    public int getItemCount() {
                        return selectedList.size();
                    }

                    class VH extends RecyclerView.ViewHolder {
                        public VH(@NonNull View itemView) {
                            super(itemView);
                        }
                    }
                }

                @Override
                public void onShow(DialogInterface d) {
                    RecyclerView selectedFilesRecycler = dialog.findViewById(R.id.selected_files_recycler);
                    selectedFilesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
                    selectedFilesRecycler.setAdapter(new selectedFilesAdapter(selectedList));
                    dialog.findViewById(R.id.clear_selected_files_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            for (Media media : selectedList) {
                                media.setChecked(false);
                                selected_counter--;
                            }
                            appSelectionFragment.onChange();
                            photosFragment.onChange();
                            videoGalleryFragment.onChange();
                            galleryFragment.onChange();
                            filesFragments.onChange();
                            if (searchFragment != null)
                                searchFragment.onChange();
                            refreshTotalSize();
                            dialog.dismiss();
                        }
                    });
                }
            });
            dialog.findViewById(R.id.send_dialog_bg).setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        }
        else {
            Toast.makeText(getActivity(), "No Files Selected", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (wifiPanelWasOpen) {
            wifiPanelWasOpen = false;
            if (!((WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE)).isWifiEnabled()) {
                        Toast.makeText(getContext(), R.string.please_enable_wifi, Toast.LENGTH_LONG).show();
                    }
                    else {
                        send_button_click();
                    }
        }
    }


    boolean saveBitmapToFile(File dir, String fileName, Bitmap bm,
                             Bitmap.CompressFormat format, int quality) {

        File imageFile = new File(dir, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);

            bm.compress(format, quality, fos);

            fos.close();

            return true;
        } catch (IOException e) {
            Log.e("app", e.getMessage());
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }

    public Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private boolean discoverWiFiNetworks = false;
    private Thread WiFiDiscoveryThread = null;
    private void send_button_click() {
        if (!getArguments().getBoolean(Strings.FileSelectionRequest)) {
            Dialog dialog = new Dialog(getActivity(), R.style.PauseDialog);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.send_files_dialog);
            dialog.setCancelable(true);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (WiFiDiscoveryThread != null && WiFiDiscoveryThread.isAlive()) {
                        discoverWiFiNetworks = false;
                    }
                }
            });
            dialog.setCanceledOnTouchOutside(true);
            dialog.findViewById(R.id.send_dialog_card).setTranslationY(200f);
            dialog.findViewById(R.id.scan_qr_code_button_dialog).setTranslationY(400f);
            dialog.findViewById(R.id.enter_credentials_button_dialog).setTranslationY(600f);
            dialog.findViewById(R.id.send_dialog_bg).setOnClickListener(v -> dialog.dismiss());
            dialog.findViewById(R.id.scan_qr_code_button_dialog).setOnClickListener(v -> {
                dialog.dismiss();
                IntentIntegrator.forSupportFragment(FileSelection.this).setOrientationLocked(false).setBarcodeImageEnabled(false).setPrompt("Scan QR code on receiving device to connect").initiateScan();
            });

            for (int i = 1; i <= 16; i++) {
                FloatingActionButton floatingActionButton = dialog.findViewById(getActivity().getResources().getIdentifier("send_files_dialog_fab" + i, "id", getActivity().getPackageName()));
                Random rnd = new Random();
                floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.argb(255, rnd.nextInt(128) + 128, rnd.nextInt(128) + 128, rnd.nextInt(128) + 128)));
                floatingActionButton.setVisibility(GONE);
                floatingActionButton.setScaleX(0f);
                floatingActionButton.setScaleY(0f);
                TextView ssidText = dialog.findViewById(getActivity().getResources().getIdentifier("send_files_dialog_text" + i, "id", getActivity().getPackageName()));
                ssidText.setVisibility(GONE);
                ssidText.setScaleX(0f);
                ssidText.setScaleY(0f);
            }

            dialog.findViewById(R.id.enter_credentials_button_dialog).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.findViewById(R.id.wifi_scan_view_manual_connect).setTranslationX(1f * dialog.findViewById(R.id.send_dialog_bg).getMeasuredWidth());
                    dialog.findViewById(R.id.wifi_scan_view_manual_connect).setVisibility(View.VISIBLE);
                    dialog.findViewById(R.id.connection_mode_select_view).animate().translationX(-1f * dialog.findViewById(R.id.send_dialog_bg).getMeasuredWidth()).withEndAction(() -> dialog.findViewById(R.id.connection_mode_select_view).setVisibility(GONE)).start();
                    dialog.findViewById(R.id.wifi_scan_view_manual_connect).animate().translationX(0f).start();
                    discoverWiFiNetworks = true;
                    WiFiDiscoveryThread = new Thread(() -> {
                        while (true) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                try {
                                    WifiUtils.withContext(getContext()).scanWifi(scanResults -> {
                                        for (int i = 1; i <= 16; i++) {
                                            FloatingActionButton floatingActionButton = dialog.findViewById(getActivity().getResources().getIdentifier("send_files_dialog_fab" + i, "id", getActivity().getPackageName()));
                                            Random rnd = new Random();
                                            floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.argb(255, rnd.nextInt(128) + 128, rnd.nextInt(128) + 128, rnd.nextInt(128) + 128)));
                                            floatingActionButton.setVisibility(GONE);
                                            floatingActionButton.setScaleX(0f);
                                            floatingActionButton.setScaleY(0f);
                                            TextView ssidText = dialog.findViewById(getActivity().getResources().getIdentifier("send_files_dialog_text" + i, "id", getActivity().getPackageName()));
                                            ssidText.setVisibility(GONE);
                                            ssidText.setScaleX(0f);
                                            ssidText.setScaleY(0f);
                                        }
                                        ArrayList<Integer> usedSpots = new ArrayList<>();
                                        for (ScanResult scanResult : scanResults) {
                                            if (scanResult.SSID.trim().length() > 0) {
                                                int freq = Math.round((float) scanResult.frequency / 1000);
                                                Drawable drawable;
                                                if (scanResult.SSID.toLowerCase().startsWith("androidshare")) {
                                                    drawable = getResources().getDrawable(R.drawable.ic_android_share_hotspot);
                                                } else {
                                                    if (freq == 5) {
                                                        drawable = getResources().getDrawable(R.drawable.ic_hotspot_5g);
                                                    } else {
                                                        drawable = getResources().getDrawable(R.drawable.ic_hotspot_2g);
                                                    }
                                                }
                                                int i;
                                                while (true) {
                                                    boolean containsNewSpot = false;
                                                    i = new Random().nextInt(16) + 1;
                                                    for (Integer spot : usedSpots) {
                                                        if (spot == i) {
                                                            containsNewSpot = true;
                                                        }
                                                    }
                                                    if (!containsNewSpot) {
                                                        usedSpots.add(i);
                                                        break;
                                                    }
                                                }
                                                FloatingActionButton floatingActionButton = dialog.findViewById(getActivity().getResources().getIdentifier("send_files_dialog_fab" + i, "id", getActivity().getPackageName()));
                                                floatingActionButton.setImageDrawable(drawable);

                                                TextView ssidText = dialog.findViewById(getActivity().getResources().getIdentifier("send_files_dialog_text" + i, "id", getActivity().getPackageName()));
                                                ssidText.setText(scanResult.SSID);
                                                ssidText.setSelected(true);


                                                new Handler(Looper.getMainLooper())
                                                        .postDelayed(() -> {
                                                            floatingActionButton.setVisibility(View.VISIBLE);
                                                            ssidText.setVisibility(View.VISIBLE);
                                                            floatingActionButton.animate().scaleY(1f).scaleX(1f).start();
                                                            ssidText.animate().scaleY(1f).scaleX(1f).start();
                                                            floatingActionButton.setOnClickListener(v1 -> {
                                                                View v2 = dialog.findViewById(R.id.ssid_password_entry_dialog);
                                                                v2.setScaleX(0f);
                                                                v2.setScaleY(0f);
                                                                v2.setVisibility(View.VISIBLE);
                                                                v2.animate().scaleX(1f).scaleY(1f).start();
                                                                EditText et = ((EditText)dialog.findViewById(R.id.share_wifi_ssid_dialog)), et1 = ((EditText)dialog.findViewById(R.id.share_wifi_key_dialog));
                                                                et.setText(scanResult.SSID);
                                                                et1.setText("");

                                                                dialog.findViewById(R.id.wifi_scan_view_manual_connect).animate().scaleX(0f).scaleY(0f).withEndAction(() -> {
                                                                    dialog.findViewById(R.id.wifi_scan_view_manual_connect).setVisibility(GONE);
                                                                    dialog.findViewById(R.id.wifi_scan_view_manual_connect).setScaleX(1f);
                                                                    dialog.findViewById(R.id.wifi_scan_view_manual_connect).setScaleY(1f);
                                                                }).start();

                                                                dialog.findViewById(R.id.cancel_floating_password_dialog_entry).setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        hideKeyboard(getActivity());
                                                                        dialog.findViewById(R.id.wifi_scan_view_manual_connect).setScaleX(0f);
                                                                        dialog.findViewById(R.id.wifi_scan_view_manual_connect).setScaleY(0f);
                                                                        dialog.findViewById(R.id.wifi_scan_view_manual_connect).setVisibility(View.VISIBLE);
                                                                        dialog.findViewById(R.id.wifi_scan_view_manual_connect).animate().scaleX(1f).scaleY(1f).start();
                                                                        v2.animate().scaleX(0f).scaleY(0f).withEndAction(() -> {
                                                                            v2.setVisibility(GONE);
                                                                            v2.setScaleX(1f);
                                                                            v2.setScaleY(1f);
                                                                        }).start();
                                                                    }

                                                                    public void hideKeyboard(Activity activity) {
                                                                        View view = activity.findViewById(android.R.id.content);
                                                                        if (view != null) {
                                                                            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                                                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                                                        }
                                                                    }
                                                                });

                                                                dialog.findViewById(R.id.ok_floating_password_dialog_entry).setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        hideKeyboard(getActivity());
                                                                        String ssid = et.getText().toString();
                                                                        String pass = et1.getText().toString();
                                                                        dialog.dismiss();
                                                                        start_initialize( QRCodeFormatter.formatSSIDAndPass(ssid, pass), false);

                                                                    }
                                                                    public void hideKeyboard(Activity activity) {
                                                                        View view = activity.findViewById(android.R.id.content);
                                                                        if (view != null) {
                                                                            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                                                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                                                        }
                                                                    }
                                                                });

                                                            });
                                                        }, usedSpots.size() * 200);
                                            }
                                        }

                                    }).start();
                                } catch (Exception ne) {
                                }
                            });
                            try {
                                Thread.sleep(30 * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (!discoverWiFiNetworks) {
                                break;
                            }
                        }
                    });
                    WiFiDiscoveryThread.start();


                }
            });
            dialog.findViewById(R.id.scan_qr_code_instead_button_dialog).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.findViewById(R.id.connection_mode_select_view).setVisibility(View.VISIBLE);
                    dialog.findViewById(R.id.wifi_scan_view_manual_connect).animate().translationX(1f * dialog.findViewById(R.id.send_dialog_bg).getMeasuredWidth()).withEndAction(() -> dialog.findViewById(R.id.wifi_scan_view_manual_connect).setVisibility(GONE)).start();
                    dialog.findViewById(R.id.connection_mode_select_view).animate().translationX(0f).start();
                    if (WiFiDiscoveryThread != null && WiFiDiscoveryThread.isAlive()) {
                        discoverWiFiNetworks = false;
                    }
                }
            });
            Window window = dialog.getWindow();
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
            window.setAttributes(wlp);
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            dialog.setOnShowListener(d -> dialog.findViewById(R.id.send_dialog_bg).animate().alpha(1F).withEndAction(() -> {
                dialog.findViewById(R.id.send_dialog_card).animate().setInterpolator(new DecelerateInterpolator()).translationY(0f).start();
                dialog.findViewById(R.id.scan_qr_code_button_dialog).animate().setInterpolator(new DecelerateInterpolator()).translationY(0f).start();
                dialog.findViewById(R.id.enter_credentials_button_dialog).animate().setInterpolator(new DecelerateInterpolator()).translationY(0f).start();
            }).start());
            dialog.show();

            Glide.with(getContext())
                    .load(Uri.parse("file:///android_asset/qr_code_scanning.gif"))
                    .fitCenter()
                    .into((ImageView) dialog.findViewById(R.id.send_dialog_qr_scanning_gif));


            int nightModeFlags =
                    this.getResources().getConfiguration().uiMode &
                            Configuration.UI_MODE_NIGHT_MASK;
            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    Glide.with(getContext())
                            .load(Uri.parse("file:///android_asset/scanning.gif"))
                            .centerCrop()
                            .into((ImageView) dialog.findViewById(R.id.scanning_wifi_gif));
                    break;

                case Configuration.UI_MODE_NIGHT_NO:

                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    Glide.with(getContext())
                            .load(Uri.parse("file:///android_asset/scanning_light.gif"))
                            .centerCrop()
                            .into((ImageView) dialog.findViewById(R.id.scanning_wifi_gif));
                    break;
            }
        }
        else {
            start_initialize(null, true);
        }
    }

    private static void autoRefreshSelectedSize() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    if (!isCurrentlyUpdated){
                        if (System.currentTimeMillis() - lastUpdateTime > 100) {
                            lastUpdateTime = System.currentTimeMillis();
                            refreshTotalSize();
                            isCurrentlyUpdated = true;
                        }
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        int files_too_large = 0;
        int num_files_added = 0;
        if(requestCode == REQUEST_CODE_FOR_PICK_FILE) {
            if(null != data) { // checking empty selection
                if(null != data.getClipData()) { // checking multiple selection or not
                    for(int i = 0; i < data.getClipData().getItemCount(); i++) {
                        try {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            InputStream is = getContext().getContentResolver().openInputStream(uri);
                            long sizeOfInputStram = is.available();
                            if (sizeOfInputStram > 0) {
                                fileList.add(new Media(uri, getFileName(uri), sizeOfInputStram, 0, true));
                                selected_item_counter_up();
                                num_files_added++;
                            }
                            else {
                                files_too_large++;
                            }
                        }
                        catch (Exception e){

                        }
                    }

                    if (files_too_large > 0){
                        Toast.makeText(requireActivity(), num_files_added + " Files selected\n" + files_too_large + " files were too large", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(requireActivity(), num_files_added + " Files selected", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        Uri uri = data.getData();
                        InputStream is = getContext().getContentResolver().openInputStream(uri);
                        long sizeOfInputStram = is.available();
                        if (sizeOfInputStram > 0) {
                            fileList.add(new Media(uri, getFileName(uri), sizeOfInputStram, 0, true));
                            Toast.makeText(requireActivity(), "1 File selected", Toast.LENGTH_SHORT).show();
                            selected_item_counter_up();
                        }
                        else {
                            Toast.makeText(requireActivity(), "File too Large", Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch (Exception e){

                    }
                }
            }
        }
        else {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    start_initialize(result.getContents(), false);

                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static void selected_item_counter_up(){
        refreshTotalSize();
        selected_counter++;
        //view_counter.setText("Selected (" + selected_counter + ")");
    }
    public static void selected_item_counter_down(){
        refreshTotalSize();
        selected_counter--;
        //view_counter.setText("Selected (" + selected_counter + ")");
    }
    public static void increase_counter_by(int count) {
        refreshTotalSize();
        selected_counter += count;
       // view_counter.setText("Selected (" + selected_counter + ")");
    }
    private static boolean isCurrentlyUpdated = true;
    private static long lastUpdateTime = 0;
    private static void refreshTotalSize() {
        long time = System.currentTimeMillis();
            if ((time - lastUpdateTime) > 100) {
                new Thread(() -> {
                    long totalSize = 0;
                    for (Media file : appsList) {
                        if (file.isChecked())
                            totalSize += file.getSize();
                    }
                    for (Media file : imageList) {
                        if (file.isChecked())
                            totalSize += file.getSize();
                    }
                    for (Media file : videoList) {
                        if (file.isChecked())
                            totalSize += file.getSize();
                    }
                    for (Media file : galleryList) {
                        if (file.isChecked())
                            totalSize += file.getSize();
                    }
                    for (Media file : fileAndFolderList) {
                        if (file.isChecked())
                            totalSize += file.getSize();
                    }
                    for (Media file : fileList) {
                        if (file.isChecked())
                            totalSize += file.getSize();
                    }
                    String size = getFormatSize(totalSize);
                    new Handler(Looper.getMainLooper()).post(() -> view_counter.setText("Selected: " + selected_counter + " (" + size + ")"));
                    isCurrentlyUpdated = true;
                }).start();
            }
            else {
                isCurrentlyUpdated = false;
            }
            lastUpdateTime = time;
    }
    private static String getFormatSize(long size_given){
        final double size = size_given;
        final double KB = 1024.00;
        final double MB = 1024.00 * KB;
        final double GB = 1024.00 * MB;
        if (size/KB < 0.90) {
            return String.format( Locale.getDefault(),"%.2f",size) + " B";
        } else if (size/MB < 0.90) {
            return String.format( Locale.getDefault(),"%.2f",size/KB) + " KB";
        } else if (size/GB  < 0.90) {
            return String.format( Locale.getDefault(),"%.2f", size/MB) + " MB";
        } else {
            return String.format( Locale.getDefault(),"%.2f",size/GB) + " GB";
        }
    }

    private void start_initialize(String result1, boolean isFileSelectionMode) {
        Send_Activity.hostedFiles = new CopyOnWriteArrayList<>();
        try {
            if (imageList != null)
                for (Media media : imageList) {
                    if (media.isChecked())
                        Send_Activity.hostedFiles.add(new String[]{String.valueOf(media.getUri()), String.valueOf(media.getSize()), media.getName(), String.valueOf(media.isFolder())});
                }
            if (videoList != null)
                for (Media media : videoList) {
                    if (media.isChecked())
                        Send_Activity.hostedFiles.add(new String[]{String.valueOf(media.getUri()), String.valueOf(media.getSize()), media.getName(), String.valueOf(media.isFolder())});
                }
            if (appsList != null)
                for (Media media : appsList) {
                    if (media.isChecked())
                        Send_Activity.hostedFiles.add(new String[]{String.valueOf(media.getUri()), String.valueOf(media.getSize()), media.getName(), String.valueOf(media.isFolder())});
                }
            if (fileList != null)
                for (Media media : fileList) {
                    if (media.isChecked())
                        Send_Activity.hostedFiles.add(new String[]{String.valueOf(media.getUri()), String.valueOf(media.getSize()), media.getName(), String.valueOf(media.isFolder())});
                }
            if (galleryList != null)
                for (Media media : galleryList) {
                    if (media.isChecked())
                        Send_Activity.hostedFiles.add(new String[]{String.valueOf(media.getUri()), String.valueOf(media.getSize()), media.getName(), String.valueOf(media.isFolder())});
                }
            if (fileAndFolderList != null)
                for (Media media : fileAndFolderList) {
                    if (media.isChecked())
                        Send_Activity.hostedFiles.add(new String[]{String.valueOf(media.getUri()), String.valueOf(media.getSize()), media.getName(), String.valueOf(media.isFolder())});
                }

        try {
            if (!isFileSelectionMode) {
                QRCodeFormatter.getSSIDfromQRCodeResult(result1);
                QRCodeFormatter.getPassfromQRCodeResult(result1);
            }

                if (Send_Activity.hostedFiles.size() > 0) {

//                    ArrayList<Media> hostedMediaFiles = new ArrayList<>();
//                    for (String[] file : Send_Activity.hostedFiles){
//                        try {
//                            hostedMediaFiles.add(new Media(Uri.parse(file[0]), file[2], Long.parseLong(file[1])));
//                        }
//                        catch (Exception ignored){
//
//                        }
//                    }

                    if (!isFileSelectionMode) {
//                        new TinyDB(getContext()).putListMedia(Strings.dateString + "_SENT", hostedMediaFiles);
//                        try {
//                            requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, InitializeServerFragment.newInstance(result)).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
//                        }
//                        catch (Exception e) {
                        ServerService.changeHostedFiles(Send_Activity.hostedFiles);

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || true)
                                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, InitializeServerFragment.newInstance(result1, true)).commit();
                                else
                                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, InitializeServerFragment.newInstance(result1, true)).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                            }
                        });
//                        }
                    }
                    else {
//                        new TinyDB(getContext()).putListMedia(Strings.dateString + "_PC", hostedMediaFiles);
                        ServerService.changeHostedFiles(Send_Activity.hostedFiles);
                        new Thread(() -> {
                            HttpURLConnection urlConnection = null;
                            String out = "";
                            try {
                                URL url = new URL(requireActivity().getIntent().getStringExtra("HOST") + "INCOMING:" + ServerService.getPort());
                                urlConnection = (HttpURLConnection) url.openConnection();
                                BufferedReader rd = new BufferedReader(new InputStreamReader(
                                        urlConnection.getInputStream()));

                                String line;
                                while ((line = rd.readLine()) != null) {
                                    out += line;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                if (urlConnection != null) {
                                    urlConnection.disconnect();
                                }
                            }
                        }).start();

                        Toast.makeText(getContext(), Send_Activity.hostedFiles.size() + " Files Selected", Toast.LENGTH_LONG).show();
                        requireActivity().finish();
                    }
                }
                else {
                    if (!isFileSelectionMode) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || true)
                                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, InitializeServerFragment.newInstance(result1, false)).commit();
                                else
                                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, InitializeServerFragment.newInstance(result1, false)).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                            }
                        });
                    }
                    else {
                        Toast.makeText(requireContext(), "Please select files to transfer", Toast.LENGTH_LONG).show();
                    }
                }
            }
            catch (ConcurrentModificationException ce){
                Toast.makeText(requireActivity(), "Files are still being loaded\nPlease Wait", Toast.LENGTH_LONG).show();
            }



        }
        catch (QRCodeFormatter.QRCodeFormatException e) {
            try {
                QRCodeFormatter.continueIfItIsPcFormat(result1);
                Toast.makeText(requireContext(), "PC Code Detected", Toast.LENGTH_LONG).show();
                Intent i = new Intent(getContext(), PC_ConnectActivity.class);
                i.putExtra("pc_connection_string", result1);
                i.putExtra("start_sending", Send_Activity.hostedFiles.size() > 0);
                ServerService.changeHostedFiles(Send_Activity.hostedFiles);
                getActivity().startActivity(i);
                getActivity().finish();
            }
            catch (Exception e1) {
                Toast.makeText(requireContext(), "Invalid QR Code", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.file_selection, menu);
        if (getArguments().getBoolean(Strings.FileSelectionRequest)) {
            menu.getItem(1).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.open_explorer) {
            Intent filesIntent;
            filesIntent = new Intent(Intent.ACTION_GET_CONTENT);
            filesIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            filesIntent.addCategory(Intent.CATEGORY_OPENABLE);
            filesIntent.setType("*/*");  //use image/* for photos, etc.
            startActivityForResult(filesIntent, REQUEST_CODE_FOR_PICK_FILE);
            return true;
        }
        else if (item.getItemId() == R.id.pc_connect_menu_item){
            startActivity(new Intent(requireActivity(), PC_Connect_Activity.class));
            requireActivity().finish();
            return true;
        }
        else if (item.getItemId() == R.id.search_file_menuitem) {
            searchFragment = new SearchFileFragment();
            getActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.fragment_container, searchFragment, "SEARCH_FILES_FRAGMENT").commit();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }
}
