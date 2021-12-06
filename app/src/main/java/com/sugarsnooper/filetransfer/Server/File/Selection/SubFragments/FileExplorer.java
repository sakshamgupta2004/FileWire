package com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.core.util.Consumer;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sugarsnooper.filetransfer.FileTypeLookup;
import com.sugarsnooper.filetransfer.R;
import com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection;
import com.sugarsnooper.filetransfer.Server.File.Selection.Media;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.comparator.NameFileComparator;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;
import me.zhanghai.android.fastscroll.PopupTextProvider;

import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.fileAndFolderList;
import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.fileAndFolderListPositionTableW_R_T_URI;
import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.increase_counter_by;

public class FileExplorer extends Fragment implements ListChangeListener {

    private static FirstPageFragmentListener pagelistener;
    private static RecyclerView recyclerViewNavigation;
    private static RecyclerView recyclerViewItems;
    private static CheckBox selectAllCheckbox;
    private static List<String> path;
    private static List<File> filesToShow;
    private static String pathCurrent;
    private static String nameOriginal;
    private static String pathOriginal;
    private static boolean isCheckBoxStateManuallyChanged = false;

    public FileExplorer(FirstPageFragmentListener listener) {
        pagelistener = listener;
        path = new CopyOnWriteArrayList<>();
        path.add("Files");
    }

    public static void backPressed() {
        if (path.size() == 2)
            pagelistener.onSwitchToNextFragment();
        else {
            update_file_list(new File(pathCurrent).getParentFile().getAbsolutePath());
            //recyclerViewItems.getAdapter().notifyDataSetChanged();
            autotickCheckBoxifAllSelected(true);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filesToShow = new CopyOnWriteArrayList<>();
        if (getArguments() != null) {
            nameOriginal = getArguments().getString("NAME");
            pathCurrent = getArguments().getString("PATH");
            assert pathCurrent != null;
            pathOriginal = new String(pathCurrent);
            update_file_list(pathCurrent);
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

                        int num_items_changed = 0;
                        for (File file : filesToShow) {

                            if (!file.isDirectory()) {
                                boolean state = !(fileAndFolderListPositionTableW_R_T_URI.containsKey(file.getAbsolutePath()) && fileAndFolderList.get(fileAndFolderListPositionTableW_R_T_URI.get(file.getAbsolutePath()).intValue()).isChecked());
                                if (state) {
                                    fileAndFolderList.add(new Media(Uri.fromFile(file), file.getName(), file.length(), file.lastModified(), true, file));
                                    fileAndFolderListPositionTableW_R_T_URI.put(file.getAbsolutePath(), fileAndFolderList.size() - 1);
                                    num_items_changed ++;

                                }
                            }
                            else {
                                boolean state = !(fileAndFolderListPositionTableW_R_T_URI.containsKey(file.getAbsolutePath()) && fileAndFolderList.get(fileAndFolderListPositionTableW_R_T_URI.get(file.getAbsolutePath()).intValue()).isChecked());
                                if (state) {
                                    fileAndFolderList.add(new Media(Uri.fromFile(file), file.getName(), folderSize(file), file.lastModified(), true, true));
                                    fileAndFolderListPositionTableW_R_T_URI.put(file.getAbsolutePath(), fileAndFolderList.size() - 1);
                                    num_items_changed ++;
                                }
                            }
                        }
                        recyclerViewItems.getAdapter().notifyDataSetChanged();

                        increase_counter_by(num_items_changed);

                    } else {

                        int num_items_changed = 0;
                        for (File file : filesToShow) {

                            if (!file.isDirectory() || true) {
                                boolean state = !(fileAndFolderListPositionTableW_R_T_URI.containsKey(file.getAbsolutePath()) && fileAndFolderList.get(fileAndFolderListPositionTableW_R_T_URI.get(file.getAbsolutePath()).intValue()).isChecked());
                                if (!state) {
                                    String uri = file.getAbsolutePath();
                                    int i = fileAndFolderListPositionTableW_R_T_URI.get(uri);
                                    fileAndFolderList.get(i).setChecked(false);
                                    num_items_changed--;
                                }
                            }
                        }
                        recyclerViewItems.getAdapter().notifyDataSetChanged();
                        increase_counter_by(num_items_changed);
                    }
                }
                else {
                    isCheckBoxStateManuallyChanged = false;
                }
            }
        });


        Consumer<TextView> DEFAULT = popupView -> {
            Resources resources = popupView.getResources();
            int minimumSize = resources.getDimensionPixelSize(R.dimen.scroll_popup_min_size);
            popupView.setMinimumWidth(minimumSize);
            popupView.setMinimumHeight(minimumSize);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)
                    popupView.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
            layoutParams.setMarginEnd(resources.getDimensionPixelOffset(R.dimen.afs_popup_margin_end));
            popupView.setLayoutParams(layoutParams);
            Context context = popupView.getContext();
            popupView.setBackground(getResources().getDrawable(R.drawable.scroll_popup_background));
            popupView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            popupView.setGravity(Gravity.CENTER);
            popupView.setIncludeFontPadding(false);
            popupView.setSingleLine(true);
            popupView.setTextColor(getResources().getColor(R.color.white));
            popupView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(
                    R.dimen.popup_scroll_text_size));
        };


        new FastScrollerBuilder(recyclerViewItems)
                .setPopupTextProvider(new PopupTextProvider() {
                    @NonNull
                    @Override
                    public String getPopupText(int position) {
                        return filesToShow.get(position).getName().substring(0, 1);
                    }
                })
                .setPopupStyle(DEFAULT)
                .build();



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
        autotickCheckBoxifAllSelected(false);
    }

    private static void autotickCheckBoxifAllSelected(boolean changeManualChange){
        boolean areAllChecked = true;
        int count = 0;
        for (File file: filesToShow) {

            if (true) {
                count++;
                if (!(fileAndFolderListPositionTableW_R_T_URI.containsKey(file.getAbsolutePath()) && fileAndFolderList.get(fileAndFolderListPositionTableW_R_T_URI.get(file.getAbsolutePath()).intValue()).isChecked())) {
                    areAllChecked = false;
                    break;
                }
            }
        }
        boolean b = selectAllCheckbox.isChecked();
        if ( b != areAllChecked ) {
            isCheckBoxStateManuallyChanged = true;
        }
        if (count > 0) {
            selectAllCheckbox.setChecked(areAllChecked);
            selectAllCheckbox.setEnabled(true);
        }
        else {
            isCheckBoxStateManuallyChanged = !isCheckBoxStateManuallyChanged;
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

    private class ItemsRecyclerAdapter extends RecyclerView.Adapter<ItemsRecyclerAdapter.ItemsVH> {

        @NonNull
        @Override
        public ItemsVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ItemsVH(LayoutInflater.from(requireContext()).inflate(R.layout.file_selection_items_recycler_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ItemsVH holder, int position) {
            File file = filesToShow.get(position);
            holder.name.setText(file.getName());
            holder.itemView.setOnClickListener(v -> {
                if (file.isDirectory()) {
                    if (fileAndFolderListPositionTableW_R_T_URI.containsKey(file.getAbsolutePath()) && fileAndFolderList.get(fileAndFolderListPositionTableW_R_T_URI.get(file.getAbsolutePath()).intValue()).isChecked()){
                        FileSelection.selected_item_counter_down();
                        String uri = file.getAbsolutePath();
                        int i = fileAndFolderListPositionTableW_R_T_URI.get(uri);
                        fileAndFolderList.get(i).setChecked(false);
                        autotickCheckBoxifAllSelected(true);
                        holder.checkBox.setChecked(false);
                    }
                    else {
                        update_file_list(file.getAbsolutePath());
                        autotickCheckBoxifAllSelected(true);
                    }
                }
                else {
                    boolean state = !holder.checkBox.isChecked();
                    holder.checkBox.setChecked(state);
                    if (state) {
                        fileAndFolderList.add(new Media(Uri.fromFile(file), file.getName(), file.length(), file.lastModified(), true, file));
                        fileAndFolderListPositionTableW_R_T_URI.put(file.getAbsolutePath(), fileAndFolderList.size() - 1);
                        FileSelection.selected_item_counter_up();
                    }
                    else {
                        FileSelection.selected_item_counter_down();
                        String uri = file.getAbsolutePath();
                        int i = fileAndFolderListPositionTableW_R_T_URI.get(uri);
                        fileAndFolderList.get(i).setChecked(false);
                    }
                    autotickCheckBoxifAllSelected(true);
                }

            });



            holder.checkBox.setVisibility(View.VISIBLE);
            if (file.isDirectory()) {
//                if (file.canRead()) {
//                    holder.itemView.setEnabled(true);
//                    ((CardView) holder.itemView).setBackgroundColor(Color.TRANSPARENT);
                    holder.state.setText(file.list().length + " Files");
//                }
//                else {
//                    holder.itemView.setEnabled(false);
//                    ((CardView) holder.itemView).setBackgroundColor(Color.parseColor("#99666666"));
//                    holder.state.setText("");
//                }

                holder.checkBox.setChecked(fileAndFolderListPositionTableW_R_T_URI.containsKey(file.getAbsolutePath()) && fileAndFolderList.get(fileAndFolderListPositionTableW_R_T_URI.get(file.getAbsolutePath()).intValue()).isChecked());


//                holder.checkBox.setVisibility(View.GONE);
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        boolean state = !holder.checkBox.isChecked();
                        holder.checkBox.setChecked(state);
                        if (state) {
                            fileAndFolderList.add(new Media(Uri.fromFile(file), file.getName(), folderSize(file), file.lastModified(), true, true));
                            fileAndFolderListPositionTableW_R_T_URI.put(file.getAbsolutePath(), fileAndFolderList.size() - 1);
                            FileSelection.selected_item_counter_up();
                        }
                        else {
                            FileSelection.selected_item_counter_down();
                            String uri = file.getAbsolutePath();
                            int i = fileAndFolderListPositionTableW_R_T_URI.get(uri);
                            fileAndFolderList.get(i).setChecked(false);
                        }
                        autotickCheckBoxifAllSelected(true);
                        return true;
                    }


                });
                Glide.with(getContext())
                        .load(R.drawable.ic_folder)
                        .into(holder.imageView);
            }
            else {

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

                    openFileIntent.setDataAndType(uri,mimeType);
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
        }

        @Override
        public int getItemCount() {
            return filesToShow.size();
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


    private static void update_file_list(String s) {
        File f = new File(s);
        if (f.isDirectory()) {
            path = new CopyOnWriteArrayList<>();
            path.add("Files");
            path.add(nameOriginal);

            String relativePath = s.substring(pathOriginal.length());
            if (relativePath.length() > 0 && relativePath.startsWith(File.separator)) {
                try {
                    while (true) {
                        path.add(relativePath.substring(relativePath.indexOf(File.separator) + 1, relativePath.indexOf(File.separator, 1)));
                        relativePath = relativePath.substring(relativePath.indexOf(File.separator, 1));
                    }
                }
                catch (IndexOutOfBoundsException e) {
                    path.add(f.getName());
                }
            }
            pathCurrent = f.getAbsolutePath();
            try {
                recyclerViewNavigation.getAdapter().notifyDataSetChanged();
                recyclerViewNavigation.smoothScrollToPosition(path.size());
            }
            catch (NullPointerException e) {

            }
            File[] files = f.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File current, String name) {
                    File file = new File(current, name);
                    return file.isFile() && file.length() > 0;
                }
            });
            File[] folders = f.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File current, String name) {
                    File file = new File(current, name);
                    return file.isDirectory() && file.canRead();
                }
            });

            Arrays.sort(files, NameFileComparator.NAME_COMPARATOR);
            Arrays.sort(folders, NameFileComparator.NAME_COMPARATOR);
            File[] filesMain = new File[folders.length + files.length];
            System.arraycopy(folders, 0, filesMain, 0, folders.length);
            System.arraycopy(files, 0, filesMain, folders.length, files.length);
            int size = filesMain.length;
            filesToShow = new CopyOnWriteArrayList<>();
            for (int i = 0 ; i < size; i++) {
                filesToShow.add(filesMain[i]);
            }
            try {
                recyclerViewItems.getAdapter().notifyDataSetChanged();
//                recyclerViewItems.scheduleLayoutAnimation();
                recyclerViewItems.scrollToPosition(0);
            }
            catch (NullPointerException e) {

            }
        }
    }

    private long folderSize(File directory) {
        long length = 0;
        if (directory.canRead()) {
            for (File file : directory.listFiles()) {
                if (file.isFile())
                    length += file.length();
                else
                    length += folderSize(file);
            }
        }
        return length;
    }
}
