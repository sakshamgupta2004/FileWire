package com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sugarsnooper.filetransfer.R;
import com.sugarsnooper.filetransfer.Strings;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments.FilesandFolder_Others_MainPage.Storage.TYPE_VOLUME;

public class FilesandFolder_Others_MainPage extends Fragment {

    private static List<Storage> volumes;
    private RecyclerView recyclerView;
    private static Drawable internal;
    private static Drawable external;
    private static FirstPageFragmentListener pageFragmentListener;

    public FilesandFolder_Others_MainPage() {
    }
    public FilesandFolder_Others_MainPage(FirstPageFragmentListener firstPageFragmentListener) {
        pageFragmentListener = firstPageFragmentListener;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_files_and_folder, container, false);
        internal = getResources().getDrawable(R.drawable.ic_smartphone);
        external = getResources().getDrawable(R.drawable.ic_memory_card);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler_view_files_and_folders);
        volumes = new CopyOnWriteArrayList<>();

        volumes.add(new Storage(Strings.Documents, getResources().getDrawable(R.drawable.ic_paper)));
        volumes.add(new Storage(Strings.Music, getResources().getDrawable(R.drawable.ic_music)));
        volumes.add(new Storage(Strings.Archives, getResources().getDrawable(R.drawable.ic_files_and_folders)));
        volumes.add(new Storage(Strings.Installers, getResources().getDrawable(R.drawable.ic_installer)));
        final File[] appsDir = requireContext().getExternalFilesDirs(null);
        final CopyOnWriteArrayList<File> extRootPaths = new CopyOnWriteArrayList<>();
        for(final File file : appsDir) {
            try {
                extRootPaths.add(file.getParentFile().getParentFile().getParentFile().getParentFile());
            }
            catch (Exception ignored){}
        }
        for (File file : extRootPaths){
            if (file != null) {
                volumes.add(new Storage(file));
            }
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (volumes.get(position).isFileType()) {
                    return 1;
                }
                else
                    return 3;
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(new DocumentsFrontPageRecyclerAdapter(getContext(), getActivity()));
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



    private static class DocumentsFrontPageRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final Context context;
        private final Activity activity;

        public DocumentsFrontPageRecyclerAdapter(Context context, Activity activity) {
            this.context = context;
            this.activity = activity;
        }


        @Override
        public int getItemViewType(int position) {
            if (volumes.get(position).isFileType()) {
                return Storage.TYPE_TYPE;
            }
            else {
                return TYPE_VOLUME;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == Storage.TYPE_TYPE) {
                return new TypesVH(LayoutInflater.from(context).inflate(R.layout.type_storage_recycler_view_resource, parent, false));
            }
            else {
                return new VolumesVH(LayoutInflater.from(context).inflate(R.layout.volume_storage_recycler_view_resource, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == Storage.TYPE_TYPE) {
                ( (TypesVH) holder).getImageView().setImageDrawable(volumes.get(position).icon);
                ( (TypesVH) holder).getTextView().setText(volumes.get(position).FileType);
                holder.itemView.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("NAME", volumes.get(position).FileType);
                    pageFragmentListener.onSwitchToNextFragment(bundle, Storage.TYPE_TYPE);
                });
            }
            else {
                ( (VolumesVH) holder).getImageView().setImageDrawable(volumes.get(position).icon);
                if (volumes.get(position).isExternal) {
                    ( (VolumesVH) holder).volume.setText(R.string.external);
                }
                else {
                    ( (VolumesVH) holder).volume.setText(R.string.internal);
                }

                String available = "Available: " + getFormatSize(volumes.get(position).freeSpace) + "/" + getFormatSize(volumes.get(position).totalSpace);
                ( (VolumesVH) holder).state.setText(available);
                ( (VolumesVH) holder).progress.setProgress(volumes.get(position).fillPercentage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString("PATH", volumes.get(position).path);
                        if (volumes.get(position).isExternal)
                            bundle.putString("NAME", "External Storage");
                        else
                            bundle.putString("NAME", "Internal Storage");
                        pageFragmentListener.onSwitchToNextFragment(bundle, TYPE_VOLUME);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return volumes.size();
        }

        private class VolumesVH extends RecyclerView.ViewHolder {
            FloatingActionButton imageView;
            ProgressBar progress;
            TextView volume;
            TextView state;
            public VolumesVH(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageview_volume_file_selection);
                volume = itemView.findViewById(R.id.volume_name_file_selection_recycler_view_resource);
                state = itemView.findViewById(R.id.volume_state_file_selection_recycler_view_resource);
                progress = itemView.findViewById(R.id.volume_progress_file_selection_recycler_view_resource);
            }

            public FloatingActionButton getImageView() {
                return imageView;
            }
        }

        private class TypesVH extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView textView;
            public TypesVH(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageview_filetype_file_selection);
                textView = itemView.findViewById(R.id.textview_filetype_file_selection);
            }

            public ImageView getImageView() {
                return imageView;
            }

            public TextView getTextView() {
                return textView;
            }
        }
    }

    public class Storage {


        public static final int TYPE_VOLUME = 1;
        public static final int TYPE_TYPE = 2;


        private final Drawable icon;
        private int fillPercentage;
        private long freeSpace;
        private long totalSpace;
        private boolean isExternal;
        private String path;
        private boolean isFileType = false;
        private String FileType = null;
        public Storage (File file) {
            this.freeSpace = file.getFreeSpace();
            this.totalSpace = file.getTotalSpace();
            this.fillPercentage = (int) (((totalSpace - freeSpace) * 100) / totalSpace);
            this.isExternal = !file.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath());
            this.path = file.getAbsolutePath();
            if (isExternal)
                icon = external;
            else
                icon = internal;
        }

        public Storage (String name, Drawable icon) {
            isFileType = true;
            FileType = name;
            this.icon = icon;
        }

        public boolean isFileType() {
            return isFileType;
        }
    }
}