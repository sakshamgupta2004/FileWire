package com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments;

import android.animation.Animator;
import android.content.pm.ApplicationInfo;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.sugarsnooper.filetransfer.R;
import com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection;
import com.sugarsnooper.filetransfer.Server.File.Selection.Media;
import com.sugarsnooper.filetransfer.TinyDB;
import com.sugarsnooper.filetransfer.readableRootsSurvivor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static android.view.View.GONE;
import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.appsList;
import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.selected_item_counter_down;
import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.selected_item_counter_up;

public class AppSelectionFragment extends Fragment implements ListChangeListener {
    private static Drawable cover_image;
    private static Drawable uncover_image;
    private RecyclerView gridView;
    private App_List_Adapter ga;
    private boolean isCheckboxManuallyChecked = false;
    private static List<String> appPackageList;
    private View root = null;
    private boolean listMade = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        appPackageList = new CopyOnWriteArrayList<>();
        cover_image = getResources().getDrawable(R.drawable.image_overlay);
        uncover_image = getResources().getDrawable(R.drawable.image_overlay_transparent);
        return inflater.inflate(R.layout.gridview, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        root = view;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    gridView = view.findViewById(R.id.gridview);

                    appsList = new TinyDB(getContext()).getListObject("appsList", Media.class);
                    appPackageList = new TinyDB(getContext()).getListString("appsPackagesList");
                    if (appsList.size() != 0 && appsList.size() == appPackageList.size()) {
                        makeList(view);
                    }
                    else {
                        listMade = true;
                    }

                    while (!listMade) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            List<ApplicationInfo> apps = readableRootsSurvivor.getAppList();
                            List<String> appPackageList1 = readableRootsSurvivor.getAppPackageList();
                            List<Media> appsList1 = new CopyOnWriteArrayList<>();
                            for (ApplicationInfo applicationInfo : apps) {
                                appsList1.add(new Media(Uri.fromFile(new File(applicationInfo.sourceDir)), String.valueOf(applicationInfo.loadLabel(requireContext().getPackageManager())) + ".apk", new File(applicationInfo.sourceDir).length(), new File(applicationInfo.sourceDir).lastModified(), new File(applicationInfo.sourceDir)));
                            }
                            new TinyDB(getContext()).putListObject("appsList", appsList1);
                            new TinyDB(getContext()).putListString("appsPackagesList", appPackageList1);
                            if (ga == null) {
                                appsList = appsList1;
                                appPackageList = appPackageList1;
                                makeList(view);
                            }
                            else {
//                                if ()
                                boolean listChanged = false;
                                if (appPackageList.size() == appPackageList1.size()) {
                                    int counter = 0;
                                    for (String appPackage : appPackageList) {
                                        if (!appPackage.equals(appPackageList1.get(counter))) {
                                            listChanged = true;
                                            break;
                                        }
                                        counter++;
                                    }
                                }
                                else {
                                    listChanged = true;
                                }

                                if (listChanged) {
//                                    if (appPackageList.size() == appPackageList1.size()) {
//                                        ga.notifyDataSetChanged();
//                                    }
//                                    else {
                                        int count = 0;
                                        ArrayList<Integer> itemsToBeRemoved = new ArrayList<>();
                                        for (String app: appPackageList) {
                                            boolean isAppRemoved = true;

                                            for (String appPackage: appPackageList1) {
                                                if (app.equals(appPackage)) {
                                                    isAppRemoved = false;
                                                    break;
                                                }
                                            }
                                            if (isAppRemoved) {
                                                itemsToBeRemoved.add(count - itemsToBeRemoved.size());
                                            }
                                            count++;
                                        }
                                        for (Integer itemToBeRemoved : itemsToBeRemoved) {
                                            appsList.remove(itemToBeRemoved.intValue());
                                            appPackageList.remove(itemToBeRemoved.intValue());
                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ga.notifyItemRemoved(itemToBeRemoved);
                                                    ga.notifyItemRangeChanged(itemToBeRemoved, appPackageList.size());
                                                }
                                            });
                                        }
                                    count = 0;
                                    HashMap<String, Media> itemsToBeAdded = new HashMap<>();
                                    for (String app: appPackageList1) {
                                        boolean isAppAdded = true;

                                        for (String appPackage: appPackageList) {
                                            if (app.equals(appPackage)) {
                                                isAppAdded = false;
                                                break;
                                            }
                                        }
                                        if (isAppAdded) {
                                            itemsToBeAdded.put(app, appsList1.get(count));
                                        }
                                        count++;
                                    }
                                    for (Map.Entry<String, Media> item : itemsToBeAdded.entrySet()) {
                                        int itemInsertPosition = Collections.binarySearch(appsList, item.getValue(), new Comparator<Media>() {
                                            @Override
                                            public int compare(Media o1, Media o2) {
                                                return o1.getName().compareTo(o2.getName());
                                            }
                                        });
                                        if (itemInsertPosition < 0) {
                                            itemInsertPosition = -itemInsertPosition -1;
                                        }
                                        appsList.add(itemInsertPosition, item.getValue());
                                        appPackageList.add(itemInsertPosition, item.getKey());
//                                        appsList.remove(itemToBeRemoved.intValue());
//                                        appPackageList.remove(itemToBeRemoved.intValue());
                                        int finalItemInsertPosition = itemInsertPosition;
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                ga.notifyItemInserted(finalItemInsertPosition);
                                                ga.notifyItemRangeChanged(finalItemInsertPosition-1, appPackageList.size());
                                            }
                                        });
                                    }
//                                    }
                                }
                            }
                        }
                    }).start();


                } catch (IllegalStateException ie) {

                }

            }
        }).start();

    }

    private void makeList(View view) {
        ga = new App_List_Adapter();

        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.findViewById(R.id.file_info_loading_progress_bar).setVisibility(GONE);
                    gridView.setAdapter(ga);
                    gridView.scheduleLayoutAnimation();
                    gridView.setLayoutManager(new GridLayoutManager(getContext(), 3));

//                            gridView.setDrawingCacheEnabled(true);
//                            gridView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                    gridView.setPersistentDrawingCache(40);
                    ga.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(View view1, int position) {
                            if (appsList.get(position).isChecked()) {







                                View parentEmptyRelativeLayout = LayoutInflater.from(getActivity()).inflate(R.layout.image_view_in_relative_layout, null, false);
                                ImageView floating = parentEmptyRelativeLayout.findViewById(R.id.floating_image_view);
                                RelativeLayout parent = root.findViewById(R.id.floating_image_parent_of_parent);
                                parent.addView(parentEmptyRelativeLayout, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


                                View item = view1.findViewById(R.id.app_icon_iv);
                                int[] params = {0, 0};
                                item.getLocationOnScreen(params);
                                int[] paramsParent = {0, 0};
                                parentEmptyRelativeLayout.getLocationOnScreen(paramsParent);


                                final Point point = new Point();
                                getActivity().getWindow().getWindowManager().getDefaultDisplay().getSize(point);
                                parentEmptyRelativeLayout.setPadding(0, 0, 0, 0);
                                floating.setTranslationY(point.y);
                                floating.setTranslationX(0);
                                floating.getLayoutParams().height = item.getHeight();
                                floating.getLayoutParams().width = item.getWidth();

                                floating.setScaleX(0.3f);
                                floating.setScaleY(0.3f);
                                floating.setVisibility(View.VISIBLE);
                                try {
                                    floating.animate().translationY(params[1] - paramsParent[1]).translationX(params[0] - paramsParent[0]).scaleX(1f).scaleY(1f).setDuration(500).setListener(new Animator.AnimatorListener() {
                                        @Override
                                        public void onAnimationStart(Animator animation) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            floating.setVisibility(GONE);

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
                                floating.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                Glide.with(getContext())
                                        .load(new File(getContext().getExternalCacheDir().getPath() + File.separator + "APPThumbnails" + File.separator + appPackageList.get(position) + ".jpeg"))
                                        .circleCrop()
                                        .into(floating);










                                appsList.get(position).setChecked(false);
                                selected_item_counter_down();
//                    view1.findViewById(R.id.app_list_card_view_background).setBackgroundColor(Color.TRANSPARENT);
                                ((CardView) view1).setForeground(uncover_image);
                                ((CheckBox) view1.findViewById(R.id.checkbox_item_checked_round)).setChecked(false);
                            } else {

                                View parentEmptyRelativeLayout = LayoutInflater.from(getActivity()).inflate(R.layout.image_view_in_relative_layout, null, false);
                                ImageView floating = parentEmptyRelativeLayout.findViewById(R.id.floating_image_view);
                                RelativeLayout parent = root.findViewById(R.id.floating_image_parent_of_parent);
                                parent.addView(parentEmptyRelativeLayout, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


                                View item = view1.findViewById(R.id.app_icon_iv);
                                int[] params = {0, 0};
                                item.getLocationOnScreen(params);
                                int[] paramsParent = {0, 0};
                                parentEmptyRelativeLayout.getLocationOnScreen(paramsParent);


                                parentEmptyRelativeLayout.setPadding(0, 0, 0, 0);
                                floating.setTranslationY(params[1] - paramsParent[1]);
                                floating.setTranslationX(params[0] - paramsParent[0]);
                                floating.getLayoutParams().height = item.getHeight();
                                floating.getLayoutParams().width = item.getWidth();
                                floating.setVisibility(View.VISIBLE);
                                try {
                                    final Point point = new Point();
                                    getActivity().getWindow().getWindowManager().getDefaultDisplay().getSize(point);
                                    final float translation = floating.getY() - point.y;
                                    floating.animate().translationYBy(-translation).translationX(0).scaleX(0.3f).scaleY(0.3f).setDuration(1000).setListener(new Animator.AnimatorListener() {
                                        @Override
                                        public void onAnimationStart(Animator animation) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            floating.setVisibility(GONE);

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
                                floating.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                Glide.with(getContext())
                                        .load(new File(getContext().getExternalCacheDir().getPath() + File.separator + "APPThumbnails" + File.separator + appPackageList.get(position) + ".jpeg"))
                                        .circleCrop()
                                        .into(floating);

//                                        Picasso.get()
//                                                .load(new File(getContext().getExternalCacheDir().getPath() + File.separator + "APPThumbnails" + File.separator + appPackageList.get(position) + ".jpeg"))
//                                                .into(floating);


                                appsList.get(position).setChecked(true);
                                selected_item_counter_up();
//                    view1.findViewById(R.id.app_list_card_view_background).setBackgroundColor(getResources().getColor(R.color.app_selected_color));
                                ((CardView) view1).setForeground(cover_image);
                                ((CheckBox) view1.findViewById(R.id.checkbox_item_checked_round)).setChecked(true);
                            }
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    sync_check_box(view);
                                }
                            }).start();

                        }
                    });

                    gridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);

                            if (dy > 20) {
                                FileSelection.scrollDown();
                            } else if (dy < -20) {
                                FileSelection.scrollUp();
                            }

                        }
                    });

                    ((CheckBox) view.findViewById(R.id.select_all_checkbox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (!isCheckboxManuallyChecked) {
                                if (isChecked) {
                                    int newly_selected = 0;
                                    int position = 0;
                                    for (Media media : appsList) {
                                        if (!media.isChecked()) {
                                            newly_selected++;
                                            appsList.get(position).setChecked(true);
                                        }
                                        position++;
                                    }
                                    for (int i = 0; i < newly_selected; i++) {
                                        selected_item_counter_up();
                                    }
                                    ga.notifyDataSetChanged();
                                    Toast.makeText(requireContext(), "All apps have been selected", Toast.LENGTH_LONG).show();
                                } else {
                                    int newly_deselected = 0;
                                    int position = 0;
                                    for (Media media : appsList) {
                                        if (media.isChecked()) {
                                            newly_deselected++;
                                            appsList.get(position).setChecked(false);
                                        }
                                        position++;
                                    }
                                    for (int i = 0; i < newly_deselected; i++) {
                                        selected_item_counter_down();
                                    }
                                    ga.notifyDataSetChanged();
                                    Toast.makeText(requireContext(), "All apps have been unselected", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                isCheckboxManuallyChecked = false;
                            }
                        }
                    });
                    listMade = true;
                }
            });
        }
        catch (NullPointerException ne) {

        }

    }


    private void sync_check_box(View view) {
        int no_check = 0;
        int no_total = 0;
        for (Media media : appsList){
                    if (media.isChecked()){
                        no_check++;
                    }
                    no_total++;
        }

        int finalNo_check = no_check;
        int finalNo_total = no_total;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (finalNo_check == finalNo_total){
                    if (!((CheckBox) view.findViewById(R.id.select_all_checkbox)).isChecked())
                        isCheckboxManuallyChecked = true;
                    ((CheckBox) view.findViewById(R.id.select_all_checkbox)).setChecked(true);
                }
                else{
                    if (((CheckBox) view.findViewById(R.id.select_all_checkbox)).isChecked())
                        isCheckboxManuallyChecked = true;
                    ((CheckBox) view.findViewById(R.id.select_all_checkbox)).setChecked(false);
                }
            }
        });
    }




    @Override
    public void onChange() {
        try {
            gridView.getAdapter().notifyDataSetChanged();
        }
        catch (Exception ignored) {

        }
    }


    interface OnItemClickListener {
        void onItemClick(View view1, int position);
    }

    class App_List_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        private OnItemClickListener onItemClickListener;

        public Object getItem(int position) {
            return appsList.get(position);
        }



        private class AppsList_ViewHolder extends RecyclerView.ViewHolder{

            public AppsList_ViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 1)
                return new AppsList_ViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.cardview_item_app, parent, false));
            else
                return new EmptyViewHolder(LayoutInflater.from(requireActivity()).inflate(R.layout.grid_item_empty, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof AppsList_ViewHolder) {
                View itemView = holder.itemView;
                TextView app_name_tv;
                ImageView app_icon_iv;
                CardView background;
                CheckBox checked;

                app_name_tv = itemView.findViewById(R.id.app_name_tv);
                app_icon_iv = itemView.findViewById(R.id.app_icon_iv);
//            background = itemView.findViewById(R.id.app_list_card_view_background);
                background = (CardView) itemView;
                checked = itemView.findViewById(R.id.checkbox_item_checked_round);
                if (appsList.get(position).isChecked()) {
//                background.setBackgroundColor(getResources().getColor(R.color.app_selected_color));
                    background.setForeground(cover_image);
                    checked.setChecked(true);
                } else {
//                background.setBackgroundColor(Color.TRANSPARENT);
                    background.setForeground(uncover_image);
                    checked.setChecked(false);
                }

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClickListener.onItemClick(itemView, position);
                    }
                });

//            app_name_tv.setText(apps.get(position).loadLabel(requireContext().getPackageManager()));
                app_name_tv.setText(appsList.get(position).getName());
                Glide.with(requireContext()).load(new File(requireContext().getExternalCacheDir().getPath() + File.separator + "APPThumbnails" + File.separator + appPackageList.get(position) + ".jpeg")).transition(DrawableTransitionOptions.withCrossFade()).circleCrop().into(app_icon_iv);
            }
        }

        @Override
        public int getItemCount() {
            return appsList.size() + FileSelection.gridExtraItems;
        }

        @Override
        public int getItemViewType(int position) {
            if (getItemCount() - position > FileSelection.gridExtraItems)
                return 1;
            else
                return 2;
        }

        public class EmptyViewHolder extends RecyclerView.ViewHolder {

            public EmptyViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        private void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }
    }
}
