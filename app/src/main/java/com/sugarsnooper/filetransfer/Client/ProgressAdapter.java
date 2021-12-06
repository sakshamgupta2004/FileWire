package com.sugarsnooper.filetransfer.Client;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.sugarsnooper.filetransfer.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProgressAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context mContext;
    private final int mResource;
    private final List<List<File_and_Progress>> mObjects;
    private ArrayList<OnItemCancelledListener> onItemCancelledListenerArrayList;
    private OnFileOpenedListener onFileOpenedListener = null;

    class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProgressAdapter.ViewHolder(LayoutInflater.from(mContext).inflate(mResource, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        View convertView = holder.itemView;
        convertView.findViewById(R.id.cancel_button_open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((ExtendedFloatingActionButton) convertView.findViewById(R.id.cancel_button_open)).getText().equals(mContext.getResources().getString(R.string.cancel))) {
                    getItem(position).setIscancelled();
                    notifyDataSetChanged();
                    for (OnItemCancelledListener onItemCancelledListener : onItemCancelledListenerArrayList) {
                        onItemCancelledListener.onCancelled(position);
                    }
                } else if (((ExtendedFloatingActionButton) convertView.findViewById(R.id.cancel_button_open)).getText().equals(mContext.getResources().getString(R.string.open)) || ((ExtendedFloatingActionButton) convertView.findViewById(R.id.cancel_button_open)).getText().equals("Install")) {
                    if (onFileOpenedListener != null) {
                        onFileOpenedListener.onOpen(position);
                    }
                }
            }
        });
        ProgressBar progressBar = convertView.findViewById(R.id.download_progress_bar);
        TextView textViewName = convertView.findViewById(R.id.download_progress_filename);
        TextView textViewSize = convertView.findViewById(R.id.download_progress_filesize);
        ExtendedFloatingActionButton cancel = convertView.findViewById(R.id.cancel_button_open);
        long fileSize = getItem(position).getFileSize();
        int progress;
        if (fileSize == 0) {
            progress = 100;
        }
        else {
            progress = (int) (((getItem(position).getDownloaded() * 100) / getItem(position).getFileSize()));
        }

        if (!getItem(position).isIscancelled()) {
            if (progress < 100) {
                progressBar.setProgress(progress);
                cancel.setText(R.string.cancel);
                cancel.setVisibility(View.GONE);
            } else {
                progressBar.setProgress(progress);
                if (getItem(position).getFileName().endsWith(".apk"))
                    cancel.setText("Install");
                else
                    cancel.setText(R.string.open);
                cancel.setVisibility(View.VISIBLE);
            }
        }
        else{
            progressBar.setProgress(0);
            cancel.setClickable(false);
            cancel.setText(R.string.cancelled);
        }


        textViewName.setText(getItem(position).getFileName());
        textViewSize.setText(getFormatSize(getItem(position).getFileSize()));
    }

    public File_and_Progress getItem (int position) {
        File_and_Progress file;
        int count = 0;
        int batch = 0;
        while (true) {
            count += mObjects.get(batch).size();
            if (count-1 >= position)
                break;
            else
                batch++;
        }
        file = mObjects.get(batch).get(mObjects.get(batch).size() - (count-position));

        return file;
    }

    @Override
    public int getItemCount() {
        int total = 0;
        for (List<File_and_Progress> sublist : mObjects) {
            total += sublist.size();
        }
        return total;
    }

    public ProgressAdapter(Context context, int resource, List<List<File_and_Progress>> objects) {
        mResource = resource;
        mContext = context;
        mObjects = objects;
        onItemCancelledListenerArrayList = new ArrayList<>();
    }

//    @NonNull
//    @Override
//    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//            if (convertView == null) {
//                convertView = LayoutInflater.from(getContext()).inflate(mResource, parent, false);
//            }
//        View finalConvertView = convertView;
//        convertView.findViewById(R.id.cancel_button_open).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (((ExtendedFloatingActionButton) finalConvertView.findViewById(R.id.cancel_button_open)).getText().equals(getContext().getResources().getString(R.string.cancel))) {
//                    getItem(position).setIscancelled();
//                    notifyDataSetChanged();
//                    for (OnItemCancelledListener onItemCancelledListener : onItemCancelledListenerArrayList) {
//                        onItemCancelledListener.onCancelled(position);
//                    }
//                } else if (((ExtendedFloatingActionButton) finalConvertView.findViewById(R.id.cancel_button_open)).getText().equals(getContext().getResources().getString(R.string.open)) || ((ExtendedFloatingActionButton) finalConvertView.findViewById(R.id.cancel_button_open)).getText().equals("Install")) {
//                    if (onFileOpenedListener != null) {
//                        onFileOpenedListener.onOpen(position);
//                    }
//                }
//            }
//        });
//            ProgressBar progressBar = convertView.findViewById(R.id.download_progress_bar);
//            TextView textViewName = convertView.findViewById(R.id.download_progress_filename);
//            TextView textViewSize = convertView.findViewById(R.id.download_progress_filesize);
//            ExtendedFloatingActionButton cancel = convertView.findViewById(R.id.cancel_button_open);
//            int progress = (int) (((getItem(position).getDownloaded() * 100) / getItem(position).getFileSize()));
//            if (!getItem(position).isIscancelled()) {
//                if (progress < 100) {
//                    progressBar.setProgress(progress);
//                    cancel.setText(R.string.cancel);
//                    cancel.setVisibility(View.GONE);
//                } else {
//                    progressBar.setProgress(progress);
//                    if (getItem(position).getFileName().endsWith(".apk"))
//                        cancel.setText("Install");
//                    else
//                        cancel.setText(R.string.open);
//                    cancel.setVisibility(View.VISIBLE);
//                }
//            }
//            else{
//                progressBar.setProgress(0);
//                cancel.setClickable(false);
//                cancel.setText(R.string.cancelled);
//            }
//
//
//            textViewName.setText(getItem(position).getFileName());
//            textViewSize.setText(getFormatSize(getItem(position).getFileSize()));
//        return convertView;
//    }
//
//
    public void updateProgress(int batch, int position, long downloaded){
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    int old_percentage, new_percentage;
                    if (mObjects.get(batch).get(position).getFileSize() != 0 ) {
                        old_percentage = (int) ((mObjects.get(batch).get(position).getDownloaded() * 100) / mObjects.get(batch).get(position).getFileSize());
                        new_percentage = (int) (((downloaded * 100) / mObjects.get(batch).get(position).getFileSize()));
                    }
                    else {
                        old_percentage = 99;
                        new_percentage = 100;
                    }
                    if (new_percentage > old_percentage) {

                        mObjects.get(batch).get(position).setDownloaded(downloaded);
                        Handler uiHandler = new Handler(Looper.getMainLooper());
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                notifyDataSetChanged();
                            }
                        };
                        uiHandler.post(runnable);
                    }
                }
                catch (IndexOutOfBoundsException out){
                    out.printStackTrace();
                }
            }
        }).start();
    }
//
//
//    public void addOnItemCancelledListener(OnItemCancelledListener onItemCancelledListener){
//        onItemCancelledListenerArrayList.add(onItemCancelledListener);
//    }
//
    public void setOnFileOpenedListener(OnFileOpenedListener onFileOpenedListener){
        this.onFileOpenedListener = onFileOpenedListener;
    }

    private String getFormatSize(long size_given){
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
}

interface OnItemCancelledListener{
    public void onCancelled(int position);
}

interface OnFileOpenedListener{
    public void onOpen(int position);
}

class File_and_Progress{
    private final long fileSize;
    private long downloaded;
    private String fileName;
    private final String link;
    private boolean iscancelled = false;
    private boolean isFolder;

    public File_and_Progress(long fileSize, String fileName, String link, boolean isFolder) {
        this.fileSize = fileSize;
        this.downloaded = 0;
        this.fileName = fileName;
        this.link = link;
        this.isFolder = isFolder;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setIscancelled() {
        this.iscancelled = true;
    }

    public boolean isIscancelled() {
        return iscancelled;
    }

    public void setDownloaded(long downloaded) {
        this.downloaded = downloaded;
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getDownloaded() {
        return downloaded;
    }

    public String getFileName() {
        return fileName;
    }

    public String getLink() {
        return link;
    }
}
