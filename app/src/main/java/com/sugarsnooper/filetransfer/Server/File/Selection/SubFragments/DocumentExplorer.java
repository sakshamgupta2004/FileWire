package com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sugarsnooper.filetransfer.FileTypeLookup;
import com.sugarsnooper.filetransfer.MyFastScroll;
import com.sugarsnooper.filetransfer.R;
import com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection;
import com.sugarsnooper.filetransfer.Server.File.Selection.Media;
import com.sugarsnooper.filetransfer.Strings;
import com.sugarsnooper.filetransfer.readableRootsSurvivor;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.comparator.NameFileComparator;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.fileAndFolderList;
import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.fileAndFolderListPositionTableW_R_T_URI;
import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.increase_counter_by;

public class DocumentExplorer extends Fragment implements ListChangeListener {

    private static FirstPageFragmentListener pagelistener;
    private static RecyclerView recyclerViewNavigation;
    private static RecyclerView recyclerViewItems;
    private CheckBox selectAllCheckbox;
    private static List<String> path;
    private static List<File> filesToShow;
    private static String nameOriginal;
    private MyFastScroll fastScroller;
    private static boolean isCheckBoxStateManuallyChanged = false;

    public DocumentExplorer(FirstPageFragmentListener listener) {
        pagelistener = listener;
    }

    public static void backPressed() {
        pagelistener.onSwitchToNextFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filesToShow = new CopyOnWriteArrayList<>();
        if (getArguments() != null) {
            nameOriginal = getArguments().getString("NAME");
            path = new CopyOnWriteArrayList<>();
            path.add("Files");
            path.add(nameOriginal);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.file_explorer_file_selector_for_sending_mobile, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerViewNavigation = view.findViewById(R.id.file_selection_file_explorer_navigation);
        recyclerViewItems = view.findViewById(R.id.file_selection_file_explorer_files);
        selectAllCheckbox = view.findViewById(R.id.select_all_checkbox_file_list);
        selectAllCheckbox.setChecked(false);
        selectAllCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isCheckBoxStateManuallyChanged) {
                    if (isChecked) {

                        int i = 0;
                        for (File file : filesToShow) {

                            if (!file.isDirectory()) {
                                boolean state = !(fileAndFolderListPositionTableW_R_T_URI.containsKey(file.getAbsolutePath()) && fileAndFolderList.get(fileAndFolderListPositionTableW_R_T_URI.get(file.getAbsolutePath()).intValue()).isChecked());
                                if (state) {
                                    fileAndFolderList.add(new Media(Uri.fromFile(file), file.getName(), file.length(), file.lastModified(), true, file));
                                    fileAndFolderListPositionTableW_R_T_URI.put(file.getAbsolutePath(), fileAndFolderList.size() - 1);
                                    i++;

                                } else {
//                            FileSelection.selected_item_counter_down();
//                            String uri = file.getAbsolutePath();
//                            int i = fileAndFolderListPositionTableW_R_T_URI.get(uri);
//                            fileAndFolderList.get(i).setChecked(false);
                                }
                            }
                        }
                        recyclerViewItems.getAdapter().notifyDataSetChanged();
                        increase_counter_by(i);


                    } else {
                        int items = 0;
                        for (File file : filesToShow) {

                            if (!file.isDirectory()) {
                                boolean state = !(fileAndFolderListPositionTableW_R_T_URI.containsKey(file.getAbsolutePath()) && fileAndFolderList.get(fileAndFolderListPositionTableW_R_T_URI.get(file.getAbsolutePath()).intValue()).isChecked());
                                if (state) {
//                                fileAndFolderList.add(new Media(Uri.fromFile(file), file.getName(), file.length(), file.lastModified(), true));
//                                fileAndFolderListPositionTableW_R_T_URI.put(file.getAbsolutePath(), fileAndFolderList.size() - 1);
//                                FileSelection.selected_item_counter_up();

                                } else {
                                    String uri = file.getAbsolutePath();
                                    int i = fileAndFolderListPositionTableW_R_T_URI.get(uri);
                                    fileAndFolderList.get(i).setChecked(false);
                                    items--;
                                }
                            }
                        }
                        recyclerViewItems.getAdapter().notifyDataSetChanged();
                        increase_counter_by(items);
                    }
                }
                else {
                    isCheckBoxStateManuallyChanged = false;
                }
            }
        });
        fastScroller = view.findViewById(R.id.fastscroll);
//        view.findViewById(R.id.progress_loading_files_docs_audio_etc).setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {


                if (nameOriginal.equals(Strings.Documents))
                    filesToShow = new CopyOnWriteArrayList<>(readableRootsSurvivor.getDocs());
                else if (nameOriginal.equals(Strings.Music))
                    filesToShow = new CopyOnWriteArrayList<>(readableRootsSurvivor.getAudio());
                else if (nameOriginal.equals(Strings.Archives))
                    filesToShow = new CopyOnWriteArrayList<>(readableRootsSurvivor.getArchives());
                else if (nameOriginal.equals(Strings.Installers))
                    filesToShow = new CopyOnWriteArrayList<>(readableRootsSurvivor.getInstallers());




//                filesToShow.sort(NameFileComparator.NAME_COMPARATOR);

                try {
                    File[] media = new File[filesToShow.size()];
                    media = filesToShow.toArray(media);
                    Arrays.sort(media, NameFileComparator.NAME_COMPARATOR);
                    for (int i = 0; i < media.length; i++) {
                        filesToShow.set(i, media[i]);
                    }
                }
                catch (Exception e) {
                    try {
                        Collections.sort(filesToShow, NameFileComparator.NAME_COMPARATOR);
                    }
                    catch(Exception e1) {

                    }
                }

                new Handler(Looper.getMainLooper())
                        .post(new Runnable() {
                            @Override
                            public void run() {
//                                view.findViewById(R.id.progress_loading_files_docs_audio_etc).setVisibility(View.GONE);
//                                Consumer<TextView> DEFAULT = popupView -> {
//                                    Resources resources = popupView.getResources();
//                                    int minimumSize = resources.getDimensionPixelSize(R.dimen.scroll_popup_min_size);
//                                    popupView.setMinimumWidth(minimumSize);
//                                    popupView.setMinimumHeight(minimumSize);
//                                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)
//                                            popupView.getLayoutParams();
//                                    layoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
//                                    layoutParams.setMarginEnd(resources.getDimensionPixelOffset(R.dimen.afs_popup_margin_end));
//                                    popupView.setLayoutParams(layoutParams);
//                                    Context context = popupView.getContext();
//                                    popupView.setBackground(getResources().getDrawable(R.drawable.scroll_popup_background));
//                                    popupView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
//                                    popupView.setGravity(Gravity.CENTER);
//                                    popupView.setIncludeFontPadding(false);
//                                    popupView.setSingleLine(true);
//                                    popupView.setTextColor(getResources().getColor(R.color.white));
//                                    popupView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(
//                                            R.dimen.popup_scroll_text_size));
//                                };


//                                new FastScrollerBuilder(recyclerViewItems)
//                                        .setPopupTextProvider(new PopupTextProvider() {
//                                            @NonNull
//                                            @Override
//                                            public String getPopupText(int position) {
//                                                return filesToShow.get(position).getName().substring(0, 1);
//                                            }
//                                        })
//                                        .setPopupStyle(DEFAULT)
//                                        .build();

                                recyclerViewItems.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                    @Override
                                    public void onScrolled(RecyclerView recyclerView, int dx,int dy){
                                        super.onScrolled(recyclerView, dx, dy);

                                        if (dy > 20) {
                                            FileSelection.scrollDown();
                                        }
                                        else if (dy < -20) {
                                            FileSelection.scrollUp();
                                        }

                                    }
                                });


                                recyclerViewItems.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
                                recyclerViewNavigation.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
                                recyclerViewItems.setLayoutManager(new LinearLayoutManager(requireContext()));
                                recyclerViewNavigation.setAdapter(new NavigationRecyclerAdapter());
                                recyclerViewItems.setAdapter(new ItemsRecyclerAdapter());
                                fastScroller.setRecyclerView(recyclerViewItems);
                                recyclerViewItems.scheduleLayoutAnimation();
                                autotickCheckBoxifAllSelected(false);
                            }
                        });
            }
        }).start();
    }
    private void autotickCheckBoxifAllSelected(boolean changeManualChange){
        boolean areAllChecked = true;
        int count = 0;
        for (File file: filesToShow) {

            if (!file.isDirectory()) {
                count++;
                if (!(fileAndFolderListPositionTableW_R_T_URI.containsKey(file.getAbsolutePath()) && fileAndFolderList.get(fileAndFolderListPositionTableW_R_T_URI.get(file.getAbsolutePath()).intValue()).isChecked())) {
                    areAllChecked = false;
                    break;
                }
            }
        }
//        isCheckBoxStateManuallyChanged = changeManualChange;
        boolean b = selectAllCheckbox.isChecked();
        if ( b != areAllChecked ) {
            if (changeManualChange) {
                isCheckBoxStateManuallyChanged = true;
            }
        }
        if (count > 0) {
            selectAllCheckbox.setChecked(areAllChecked);
            selectAllCheckbox.setEnabled(true);
        }
        else {
            selectAllCheckbox.setChecked(false);
            selectAllCheckbox.setEnabled(false);
        }
    }

    private static String getFormatSize(long size_given){
        final double KB = 1024.00;
        final double MB = 1024.00 * KB;
        final double GB = 1024.00 * MB;
        if ((double) size_given /KB < 0.90) {
            return String.format( Locale.getDefault(),"%.2f", (double) size_given) + " B";
        } else if ((double) size_given /MB < 0.90) {
            return String.format( Locale.getDefault(),"%.2f", (double) size_given /KB) + " KB";
        } else if ((double) size_given /GB  < 0.90) {
            return String.format( Locale.getDefault(),"%.2f", (double) size_given /MB) + " MB";
        } else {
            return String.format( Locale.getDefault(),"%.2f", (double) size_given /GB) + " GB";
        }
    }

    @Override
    public void onChange() {
        try {
            recyclerViewItems.getAdapter().notifyDataSetChanged();
        }
        catch (Exception ignored) {

        }
    }

    private class NavigationRecyclerAdapter extends RecyclerView.Adapter<NavigationRecyclerAdapter.NavigationViewHolder>{
        @NonNull
        @Override
        public NavigationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new NavigationViewHolder(LayoutInflater.from(requireContext()).inflate(R.layout.navigation_item_recycler_file_selection, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull NavigationViewHolder holder, int position) {
            holder.textView.setText(path.get(position));
            if (position == 0) {
                holder.arrow.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return path.size();
        }

        private class NavigationViewHolder extends RecyclerView.ViewHolder{
            private final TextView textView;
            private final ImageView arrow;
            public NavigationViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.navigation_file_selection_recycler_text_folder);
                arrow = itemView.findViewById(R.id.navigation_file_selection_recycler_arrow_folder);
            }
        }
    }

    private class ItemsRecyclerAdapter extends RecyclerView.Adapter<ItemsRecyclerAdapter.ItemsVH> implements SectionTitleProvider {

        @NonNull
        @Override
        public ItemsVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ItemsVH(LayoutInflater.from(requireContext()).inflate(R.layout.file_selection_items_recycler_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ItemsVH holder, int position) {
            File file = filesToShow.get(position);
            try {
                holder.name.setText(file.getName());
                holder.itemView.setOnClickListener(v -> {
                    boolean state = !holder.checkBox.isChecked();
                    holder.checkBox.setChecked(state);
                    if (state) {
                        fileAndFolderList.add(new Media(Uri.fromFile(file), file.getName(), file.length(), file.lastModified(), true, file));
                        fileAndFolderListPositionTableW_R_T_URI.put(file.getAbsolutePath(), fileAndFolderList.size() - 1);
                        FileSelection.selected_item_counter_up();
                    } else {
                        FileSelection.selected_item_counter_down();
                        String uri = file.getAbsolutePath();
                        int i = fileAndFolderListPositionTableW_R_T_URI.get(uri);
                        fileAndFolderList.get(i).setChecked(false);
                    }

                    autotickCheckBoxifAllSelected(true);
                });

                holder.checkBox.setVisibility(View.VISIBLE);

                holder.checkBox.setChecked(fileAndFolderListPositionTableW_R_T_URI.containsKey(file.getAbsolutePath()) && fileAndFolderList.get(fileAndFolderListPositionTableW_R_T_URI.get(file.getAbsolutePath()).intValue()).isChecked());

                holder.state.setText(getFormatSize(file.length()));
                holder.itemView.setOnLongClickListener(v -> {
                    MimeTypeMap mimeMap = MimeTypeMap.getSingleton();
                    Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
                    String mimeType = mimeMap.getMimeTypeFromExtension(FilenameUtils.getExtension(file.getName()));
                    Uri uri;
                    try {
                        uri = FileProvider.getUriForFile(getContext(), getContext().getString(R.string.filebrowser_provider), file);
                    }
                    catch (IllegalArgumentException ie) {
                        if (Build.VERSION.SDK_INT >= 24) {
                            try {
                                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                                m.invoke(null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        uri = Uri.fromFile(file);
                    }

                    openFileIntent.setDataAndType(uri, mimeType);
                    openFileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    openFileIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try {
                        getContext().startActivity(openFileIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getContext(), getContext().getString(R.string.no_app_to_handle), Toast.LENGTH_LONG).show();
                    }
                    return true;
                });
                switch (FileTypeLookup.fileType(file.getName())) {
                    case 1:
                        Glide.with(getContext())
                                .load(R.drawable.ic_paper)
                                .into(holder.imageView);
                        break;
                    case 2:
                    case 3:
                        Glide.with(getContext())
                                .load(Uri.fromFile(file))
                                .into(holder.imageView);
                        break;
                    case 4:
                        Glide.with(getContext())
                                .load(R.drawable.ic_music)
                                .into(holder.imageView);
                        break;
                    case 5:
                        Glide.with(getContext())
                                .load(R.drawable.ic_files_and_folders)
                                .into(holder.imageView);
                        break;
                    case 6:
                        String sourcePath = file.getAbsolutePath();
                        if (sourcePath.endsWith(".apk")) {
                            Glide.with(getContext())
                                    .load(new File(getContext().getExternalCacheDir().getPath() + File.separator + "APKThumbnails" + File.separator + file.getName() + file.getPath().hashCode() + ".jpeg"))
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
                                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Glide.with(getContext())
                                                                .load(appInfo.loadIcon(getContext().getPackageManager()))
                                                                .transition(DrawableTransitionOptions.withCrossFade())
                                                                .into(holder.imageView);
                                                    }
                                                });
                                            } else {
                                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Glide.with(getContext())
                                                                .load(R.drawable.unknown_file_icon)
                                                                .into(holder.imageView);
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
                                    .into(holder.imageView);
                        }
                            break;
                    default:
                        Glide.with(getContext())
                                .load(R.drawable.unknown_file_icon)
                                .into(holder.imageView);
                        break;
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return filesToShow.size();
        }

        @Override
        public String getSectionTitle(int position) {
            return filesToShow.get(position).getName().substring(0, 1);
        }

        private class ItemsVH extends RecyclerView.ViewHolder {
            FloatingActionButton imageView;
            TextView name;
            TextView state;
            CheckBox checkBox;
            public ItemsVH(@NonNull View itemView) {
                super(itemView);
                checkBox = itemView.findViewById(R.id.checkbox_item_checked_round);
                imageView = itemView.findViewById(R.id.imageview_item_file_selection);
                name = itemView.findViewById(R.id.item_name_file_selection_recycler_view_resource);
                state = itemView.findViewById(R.id.item_state_file_selection_recycler_view_resource);
            }
        }
    }
}
