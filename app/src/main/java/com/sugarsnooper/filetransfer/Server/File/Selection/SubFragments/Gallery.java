package com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.listeners.OnDismissListener;
import com.stfalcon.imageviewer.loader.ImageLoader;
import com.sugarsnooper.filetransfer.FileTypeLookup;
import com.sugarsnooper.filetransfer.MyFastScroll;
import com.sugarsnooper.filetransfer.R;
import com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection;
import com.sugarsnooper.filetransfer.Server.File.Selection.Media;
import com.sugarsnooper.filetransfer.Server.Send_Activity;
import com.sugarsnooper.filetransfer.TinyDB;
import com.sugarsnooper.filetransfer.VideoPlayerDismissable;
import com.sugarsnooper.filetransfer.readableRootsSurvivor;

import org.apache.commons.io.comparator.NameFileComparator;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static android.view.View.GONE;
import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.blurView;
import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.galleryList;
import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.imageList;
import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.increase_counter_by;
import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.videoList;
import static com.sugarsnooper.filetransfer.VideoPlayerDismissable.setOrientation;
import static com.sugarsnooper.filetransfer.readableRootsSurvivor.db;


public class Gallery extends Fragment implements ListChangeListener {
    private static Drawable cover_image;
    private static Drawable uncover_image;
    private static FragmentActivity activity = null;
    private RecyclerView recyclerView = null;
    private SwipeRefreshLayout galleryRefreshLayout;
    private ExtendedFloatingActionButton fab = null;
    private GalleryRecyclerViewAdapter adapter = null;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private static SimpleDateFormat dateFormat1 = new SimpleDateFormat("MMM-yyyy");
    private static int max_padding = 20;
    private LinkedHashMap<File, String> parentFolders;
    private int lastCheckItem = 0;
    private static List<Media> itemsToShow = null;
    private CheckBox selectAllCheckBox = null;
    private boolean isManuallyChecked = false;
    private Object fragment;
    private MyFastScroll fastScroller;
    private View root;
    private TextView dateViewAtTop;
    private CheckBox dateCheckAtTop;
    private View dateCardViewAtTop;
    private String lastTopDateString = "";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = requireActivity();
        cover_image = getResources().getDrawable(R.drawable.image_overlay);
        uncover_image = getResources().getDrawable(R.drawable.image_overlay_transparent);


        View view = inflater.inflate(R.layout.gallery_frag, container, false);
        dateViewAtTop = view.findViewById(R.id.date_text_view_gallery_view);
        dateCheckAtTop = view.findViewById(R.id.select_all_checkbox_gallery_view);
        dateCardViewAtTop = view.findViewById(R.id.date_view_ll_gallery_view);
        dateCardViewAtTop.setVisibility(View.INVISIBLE);
        dateCardViewAtTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = dateCheckAtTop;
                checkBox.setChecked(!checkBox.isChecked());
                String lastModifiedShown = lastTopDateString;
                boolean isChecked = checkBox.isChecked();
                if (isChecked) {

                    int newly_selected = 0;
                    int position = 0;
                    for (Media media : itemsToShow) {
                        if (lastModifiedShown.equals(dateFormat.format(new Date(media.getModified())))) {
                            if (!media.isSeperator()) {
                                if (!media.isChecked()) {
                                    newly_selected++;
                                    itemsToShow.get(position).setChecked(true);
                                }
                            }
                        }
                        position++;
                    }
                    int finalNewly_selected = newly_selected;
                    increase_counter_by(finalNewly_selected);
                    adapter.notifyDataSetChanged();

//                                Toast.makeText(requireContext(), "All items present on this date have been selected", Toast.LENGTH_LONG).show();
                } else {
                    int newly_deselected = 0;
                    int position = 0;
                    for (Media media : itemsToShow) {
                        if (lastModifiedShown.equals(dateFormat.format(new Date(media.getModified())))) {
                            if (!media.isSeperator()) {
                                if (media.isChecked()) {
                                    newly_deselected--;
                                    itemsToShow.get(position).setChecked(false);
                                }
                            }
                        }
                        position++;
                    }
                    int finalNewly_deselected = newly_deselected;
                    increase_counter_by(finalNewly_deselected);
                    adapter.notifyDataSetChanged();

//                                Toast.makeText(requireContext(), "All items present on this date have been unselected", Toast.LENGTH_LONG).show();
                }
                autoCheckSelectAllBox();
            }
        });
        fragment = this;
        fab = view.findViewById(R.id.floating_action_button_filter_gallery);
        if (fragment instanceof Photos) {
            FileSelection.fab1 = fab;
        }
        else if (fragment instanceof VideoGalleryFragment) {
            FileSelection.fab2 = fab;
        }
        else if (fragment instanceof Gallery) {
            FileSelection.fab3 = fab;
        }
        root = view;
        galleryRefreshLayout = view.findViewById(R.id.gallery_refresh_layout);

        selectAllCheckBox = view.findViewById(R.id.select_all_checkbox);
        selectAllCheckBox.setEnabled(false);
        return view;
    }
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        new Thread(new Runnable() {
            @Override
            public void run() {
                itemsToShow = null;
                if (new TinyDB(getContext()).getListObject(fragment.getClass().getName() + "_ListCache", Media.class).size() == 0) {
                    List<Media> mediaList = new CopyOnWriteArrayList<>();
                    if (fragment instanceof Photos) {
                        imageList = mediaList;
                    } else if (fragment instanceof VideoGalleryFragment) {
                        videoList = mediaList;
                    } else if (fragment instanceof Gallery) {
                        galleryList = mediaList;
                    }
                    parentFolders = new LinkedHashMap<>();
                    for (Map.Entry<String, Long> entry : readableRootsSurvivor.getGallery(fragment).entrySet()) {
                        File file = new File(entry.getKey());
                        if (mediaList.size() >= 1) {
                            if (!dateFormat.format(new Date(mediaList.get(mediaList.size() - 1).getModified())).equals(dateFormat.format(new Date(entry.getValue())))) {
                                mediaList.add(new Media(entry.getValue()));
                            }
                        } else if (mediaList.size() == 0 && readableRootsSurvivor.getGallery(fragment).entrySet().size() >= 1) {
                            mediaList.add(new Media(entry.getValue()));
                        }
                        parentFolders.put(file.getParentFile(), file.getParentFile().getName());
                        byte type = FileTypeLookup.fileType(file.getName());
                        if (type == 2) {
                            mediaList.add(new Media(Uri.fromFile(file), file.getName(), file.length(), entry.getValue(), file.getParentFile(), Media.TYPE_IMAGE, dateFormat1));
                        } else if (type == 3) {
                            mediaList.add(new Media(Uri.fromFile(file), file.getName(), file.length(), entry.getValue(), file.getParentFile(), Media.TYPE_VIDEO, dateFormat1));
                        }
                    }
                    orderByValue(parentFolders, NameFileComparator.NAME_COMPARATOR);
                    List<File> parentFoldersList = new ArrayList<>(parentFolders.keySet());
                    List<String> parentStringFoldersList = new ArrayList<>(parentFolders.values());
                    db.putListObject(fragment.getClass().getName() + "_ListCache", mediaList);
                    db.putListObject(fragment.getClass().getName() + "_FoldersFileListCache", parentFoldersList);
                    db.putListObject(fragment.getClass().getName() + "_FoldersStringListCache", parentStringFoldersList);
                    createList(view, mediaList);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            view.findViewById(R.id.gallery_refreshing_progressbar).setVisibility(GONE);
                            galleryRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                @Override
                                public void onRefresh() {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            readableRootsSurvivor.refreshLists();
                                            getAndRefreshList(mediaList, view);
                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    galleryRefreshLayout.setRefreshing(false);
                                                }
                                            });
                                        }
                                    }).start();
                                }
                            });
                        }
                    });
                }
                else {
                    List<Media> mediaList = new CopyOnWriteArrayList<>(new TinyDB(getContext()).getListObject(fragment.getClass().getName() + "_ListCache", Media.class));
                    List<File> parentFoldersList = new TinyDB(getContext()).getListObject(fragment.getClass().getName() + "_FoldersFileListCache", File.class);
                    List<String> parentStringFoldersList = new TinyDB(getContext()).getListObject(fragment.getClass().getName() + "_FoldersStringListCache", String.class);
                    if (fragment instanceof Photos) {
                        imageList = mediaList;
                    } else if (fragment instanceof VideoGalleryFragment) {
                        videoList = mediaList;
                    } else if (fragment instanceof Gallery) {
                        galleryList = mediaList;
                    }
                    parentFolders = new LinkedHashMap<>();
                    if (parentFoldersList.size() == parentStringFoldersList.size()) {
                        for (int i = 0; i < parentFoldersList.size(); i++) {
                            parentFolders.put(parentFoldersList.get(i), parentStringFoldersList.get(i));
                        }
                    }
                    createList(view, mediaList);





                    if (!readableRootsSurvivor.hasRefreshed(fragment)) {
                        getAndRefreshList(mediaList, view);
                    }
                    else {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                view.findViewById(R.id.gallery_refreshing_progressbar).setVisibility(GONE);
                            }
                        });
                    }


                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            galleryRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                @Override
                                public void onRefresh() {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            readableRootsSurvivor.refreshLists();
                                            getAndRefreshList(mediaList, view);
                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    galleryRefreshLayout.setRefreshing(false);
                                                }
                                            });
                                        }
                                    }).start();
                                }
                            });
                        }
                    });
                }
            }}).start();
    }

    private void getAndRefreshList(List<Media> mediaList, View view) {
        List<Media> mediaList1 = new CopyOnWriteArrayList<>();
        LinkedHashMap<File, String> parentFolders1 = new LinkedHashMap<>();
        Set<Map.Entry<String, Long>> gallerySet = readableRootsSurvivor.getGallery(fragment).entrySet();

        if (gallerySet.size() >= 1) {
            mediaList1.add(new Media(((Map.Entry<String, Long>)gallerySet.toArray()[0]).getValue()));
        }

        for (Map.Entry<String, Long> entry : gallerySet) {
            File file = new File(entry.getKey());
            if (!dateFormat.format(new Date(mediaList1.get(mediaList1.size() - 1).getModified())).equals(dateFormat.format(new Date(entry.getValue())))) {
                mediaList1.add(new Media(entry.getValue()));
            }
            parentFolders1.put(file.getParentFile(), file.getParentFile().getName());
            byte type = FileTypeLookup.fileType(file.getName());
            if (type == 2) {
                mediaList1.add(new Media(Uri.fromFile(file), file.getName(), file.length(), entry.getValue(), file.getParentFile(), Media.TYPE_IMAGE, dateFormat1));
            } else if (type == 3) {
                mediaList1.add(new Media(Uri.fromFile(file), file.getName(), file.length(), entry.getValue(), file.getParentFile(), Media.TYPE_VIDEO, dateFormat1));
            }
        }
        orderByValue(parentFolders1, NameFileComparator.NAME_COMPARATOR);
        List<File> parentFoldersList1 = new ArrayList<>(parentFolders1.keySet());
        List<String> parentStringFoldersList1 = new ArrayList<>(parentFolders1.values());
        db.putListObject(fragment.getClass().getName() + "_ListCache", mediaList1);
        db.putListObject(fragment.getClass().getName() + "_FoldersFileListCache", parentFoldersList1);
        db.putListObject(fragment.getClass().getName() + "_FoldersStringListCache", parentStringFoldersList1);


/*                        boolean listChanged = false;
                        if (mediaList.size() == mediaList1.size()) {
                            int counter = 0;
                            for (Media med : mediaList) {
                                try {
                                    if (!med.getUri().equals(mediaList1.get(counter).getUri())) {
                                        listChanged = true;
                                        break;
                                    }
                                } catch (Exception e) {

                                }
                                counter++;
                            }
                        } else {
                            listChanged = true;
                        }


                        if (listChanged) {
                            int count = 0;
                            ArrayList<Integer> itemsToBeRemoved = new ArrayList<>();

                            for (Media media : mediaList) {
                                boolean isItemRemoved = true;
                                if (!media.isSeperator()) {
                                    for (Media media1 : mediaList1) {
                                        if (!media1.isSeperator()) {
                                            if (media.getUri().toString().equals(media1.getUri().toString())) {
                                                isItemRemoved = false;
                                                break;
                                            }
                                        }
                                    }
                                }
                                else {
                                    for (Media media1 : mediaList1) {
                                        if (media1.isSeperator()) {
                                            if (media.getModified() == media1.getModified()) {
                                                isItemRemoved = false;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (isItemRemoved) {
                                    itemsToBeRemoved.add(count - itemsToBeRemoved.size());
                                }
                                count++;
                            }
                            int totalRemovedChecked = 0;
                            for (Integer itemToBeRemoved : itemsToBeRemoved) {
                                if (mediaList.get(itemToBeRemoved.intValue()).isChecked()) {
                                    if (!mediaList.get(itemToBeRemoved.intValue()).isSeperator()) {
                                        totalRemovedChecked++;
                                    }
                                }
                                mediaList.remove(itemToBeRemoved.intValue());
                            }
                            increase_counter_by(-1 * totalRemovedChecked);


                            count = 0;
                            for (Media media : mediaList1) {
                                boolean isItemAdded = true;
                                if (!media.isSeperator()) {
                                    for (Media media1 : mediaList) {
                                        if (!media1.isSeperator()) {
                                            if (media.getUri().toString().equals(media1.getUri().toString())) {
                                                isItemAdded = false;
                                                break;
                                            }
                                        }
                                    }
                                }
                                else {
                                    for (Media media1 : mediaList) {
                                        if (media1.isSeperator()) {
                                            if (media.getModified() == media1.getModified()) {
                                                isItemAdded = false;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (isItemAdded) {
                                    mediaList.add(count, media);
                                }
                                count++;
                            }
*/


        final boolean[] isRecyclerDisabled = {true};
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
                    @Override
                    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                        return isRecyclerDisabled[0];
                    }
                });
                recyclerView.setEnabled(false);
                selectAllCheckBox.setEnabled(false);
            }
        });

        int itemsRemoved = 0;
        for (Media m : mediaList) {
            if (m.isChecked()) {
                boolean isAvailableInNewList = false;
                for (Media m1 : mediaList1) {
                    if ((!m1.isSeperator()) && (!m.isSeperator())) {
                        if (m.getUri().toString().equals(m1.getUri().toString())) {
                            m1.setChecked(true);
                            isAvailableInNewList = true;
                            break;
                        }
                    }
                    else if ((m1.isSeperator()) && (m.isSeperator()) && dateFormat.format(new Date(m.getModified())).equals(dateFormat.format(new Date(m1.getModified())))) {
                        m1.setChecked(true);
                        isAvailableInNewList = true;
                        break;
                    }
                }
                if (!isAvailableInNewList) {
                    itemsRemoved++;
                }
            }
        }

        mediaList.clear();
        mediaList.addAll(mediaList1);
        FileSelection.increase_counter_by(-1 * itemsRemoved);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                recyclerView.setEnabled(true);
                selectAllCheckBox.setEnabled(true);
                isRecyclerDisabled[0] = false;
            }
        });
        if (true) {
            int i = 0;
            int which = lastCheckItem;
            if (which == 0) {
                itemsToShow.clear();
                itemsToShow.addAll(mediaList);
            } else {
                itemsToShow.clear();

                for (Media media : mediaList) {
                    if (!media.isSeperator()) {
                        if (media.getParentFile().getPath().equals(((Map.Entry<File, String>) parentFolders.entrySet().toArray()[which - 1]).getKey().getPath())) {
                            itemsToShow.add(media);
                        }
                    } else {
                        try {
                            boolean hasItemsUnderIt = false;
                            int pics_under_seperator = 1;
                            while (true) {
                                if (!mediaList.get(i + pics_under_seperator).isSeperator()) {
                                    if (mediaList.get(i + pics_under_seperator).getParentFile().getPath().equals(((Map.Entry<File, String>) parentFolders.entrySet().toArray()[which - 1]).getKey().getPath())) {
                                        hasItemsUnderIt = true;
                                        break;
                                    }
                                } else {
                                    break;
                                }
                                pics_under_seperator++;
                            }
                            if (hasItemsUnderIt) {
                                itemsToShow.add(media);
                            }
                        } catch (IndexOutOfBoundsException e) {

                        }
                    }
                    i++;
                }
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });


            if (lastCheckItem > 0) {
                String oldItem = ((Map.Entry<File, String>) parentFolders.entrySet().toArray()[lastCheckItem - 1]).getValue();
                boolean isFolderPresent = false;
                int c = 0;
                for (Map.Entry<File, String> parentFolder : parentFolders1.entrySet()) {
                    if (parentFolder.getValue().equals(oldItem)) {
                        isFolderPresent = true;
                        lastCheckItem = c + 1;
                        break;
                    }
                    c++;
                }
                if (!isFolderPresent) {
                    lastCheckItem = 0;
                    itemsToShow = new CopyOnWriteArrayList<>(mediaList);
                    createList(view, mediaList);
                }
            }
            parentFolders = parentFolders1;
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                view.findViewById(R.id.gallery_refreshing_progressbar).setVisibility(GONE);
            }
        });
        autoCheckSelectAllBox();
    }


    private void createList(View view, List<Media> mediaList){
        if (itemsToShow == null) {
            itemsToShow = new CopyOnWriteArrayList<>(mediaList);
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                dateCardViewAtTop.setVisibility(View.VISIBLE);
                selectAllCheckBox.setEnabled(true);
                view.findViewById(R.id.file_info_loading_progress_bar).setVisibility(GONE);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String[] listItems = new String[parentFolders.size() + 1];
//                                listItems = parentFolders.toArray(listItems);
                        listItems[0] = "All";
                        int i = 1;
                        for (Map.Entry<File, String> set : parentFolders.entrySet()) {
                            int j = 0;
                            boolean containsValue = false;
                            for (String item : listItems) {
                                if (set.getValue().equals(item)) {
                                    String parentFolderPath = ((Map.Entry<File, String>) parentFolders.entrySet().toArray()[j - 1]).getKey().getPath();
                                    listItems[j] = item + " (" + parentFolderPath + ")";
                                    containsValue = true;
                                }
                                j++;
                            }
                            if (!containsValue)
                                listItems[i] = set.getValue();
                            else
                                listItems[i] = set.getValue() + " (" + set.getKey().getPath() + ")";
                            i++;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                        builder.setTitle(getResources().getString(R.string.album_selection));

                        int checkedItem = lastCheckItem; //this will checked the item when user open the dialog
                        String[] finalListItems = listItems;
                        builder.setSingleChoiceItems(listItems, checkedItem, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int i = 0;
                                if (which == 0)
                                    itemsToShow = new CopyOnWriteArrayList<>(mediaList);
                                else{
                                    itemsToShow = new CopyOnWriteArrayList<>();

                                    for (Media media : mediaList){
                                        if (!media.isSeperator()){
                                            if (media.getParentFile().getPath().equals(((Map.Entry<File, String>) parentFolders.entrySet().toArray()[which - 1]).getKey().getPath())){
                                                itemsToShow.add(media);
                                            }
                                        }
                                        else{
                                            try {
                                                boolean hasItemsUnderIt = false;
                                                int pics_under_seperator = 1;
                                                while (true) {
                                                    if (!mediaList.get(i + pics_under_seperator).isSeperator()) {
                                                        if (mediaList.get(i + pics_under_seperator).getParentFile().getPath().equals(((Map.Entry<File, String>) parentFolders.entrySet().toArray()[which - 1]).getKey().getPath())) {
                                                            hasItemsUnderIt = true;
                                                            break;
                                                        }
                                                    }
                                                    else{
                                                        break;
                                                    }
                                                    pics_under_seperator++;
                                                }
                                                if (hasItemsUnderIt) {
                                                    itemsToShow.add(media);
                                                }
                                            }catch (IndexOutOfBoundsException e){

                                            }
                                        }
                                        i++;
                                    }
                                }
                                lastCheckItem = which;
                                autoCheckSelectAllBox();
                                createList(view, mediaList);
                            }
                        });

                        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });

                selectAllCheckBox = view.findViewById(R.id.select_all_checkbox);
                selectAllCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!isManuallyChecked) {
                            dateCheckAtTop.setChecked(isChecked);
                            if (isChecked) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        int count = 0;
                                        for (Media media : itemsToShow) {
                                            if (!media.isSeperator()) {
                                                if (!media.isChecked()) {
                                                    media.setChecked(true);
                                                    count++;
                                                }
                                            }
                                        }
                                        int finalCount = count;
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                FileSelection.increase_counter_by(finalCount);
                                            }
                                        });
                                    }
                                }).start();
                            } else {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        int count = 0;
                                        for (Media media : itemsToShow) {
                                            if (!media.isSeperator()) {
                                                if (media.isChecked()) {
                                                    count--;
                                                    media.setChecked(false);
                                                }
                                            }
                                        }

                                        int finalCount = count;
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                FileSelection.increase_counter_by(finalCount);
                                            }
                                        });
                                    }
                                }).start();
                            }
                            adapter.notifyDataSetChanged();
                        }
                        else {
                            isManuallyChecked = false;
                        }
                    }
                });
                adapter = new GalleryRecyclerViewAdapter(new GalleryRecyclerViewAdapter.onClickListener() {
                    @Override
                    public void onClick(View item, int pos, boolean seperator) {
                        if (seperator){
                            CheckBox checkBox = item.findViewById(R.id.select_all_checkbox_recycler_view);
                            checkBox.setChecked(!checkBox.isChecked());
                            String lastModifiedShown = ((TextView) item.findViewById(R.id.date_text_view_recycler_view)).getText().toString();
                            boolean isChecked = checkBox.isChecked();
                            if (isChecked) {

                                int newly_selected = 0;
                                int position = 0;
                                for (Media media : itemsToShow) {
                                    if (lastModifiedShown.equals(dateFormat.format(new Date(media.getModified())))) {
                                        if (!media.isSeperator()) {
                                            if (!media.isChecked()) {
                                                newly_selected++;
                                                itemsToShow.get(position).setChecked(true);
                                            }
                                        }
                                    }
                                    position++;
                                }
                                int finalNewly_selected = newly_selected;
                                increase_counter_by(finalNewly_selected);
                                adapter.notifyDataSetChanged();

//                                Toast.makeText(requireContext(), "All items present on this date have been selected", Toast.LENGTH_LONG).show();
                            } else {
                                int newly_deselected = 0;
                                int position = 0;
                                for (Media media : itemsToShow) {
                                    if (lastModifiedShown.equals(dateFormat.format(new Date(media.getModified())))) {
                                        if (!media.isSeperator()) {
                                            if (media.isChecked()) {
                                                newly_deselected--;
                                                itemsToShow.get(position).setChecked(false);
                                            }
                                        }
                                    }
                                    position++;
                                }
                                int finalNewly_deselected = newly_deselected;
                                increase_counter_by(finalNewly_deselected);
                                adapter.notifyDataSetChanged();

//                                Toast.makeText(requireContext(), "All items present on this date have been unselected", Toast.LENGTH_LONG).show();
                            }
                        }
                        else{
                            if (itemsToShow.get(pos).isChecked()){
                                CheckBox itemChecked = item.findViewById(R.id.checkbox_item_checked_round);
                                ImageView imageView = item.findViewById(R.id.imageView);
//                                LinearLayout ivParent = item.findViewById(R.id.iv_parent);
//                                ivParent.setBackgroundColor(Color.parseColor("#00777777"));
//                                imageView.setPadding(0, 0, 0, 0);









                                View parentEmptyRelativeLayout = LayoutInflater.from(getActivity()).inflate(R.layout.image_view_in_relative_layout, null, false);
                                ImageView floating = parentEmptyRelativeLayout.findViewById(R.id.floating_image_view);
                                RelativeLayout parent = root.findViewById(R.id.floating_image_parent_of_parent);
                                parent.addView(parentEmptyRelativeLayout, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


                                int[] params = {0, 0};
                                item.getLocationOnScreen(params);
                                int[] paramsParent = {0, 0};
                                parentEmptyRelativeLayout.getLocationOnScreen(paramsParent);


                                parentEmptyRelativeLayout.setPadding(0, 0, 0, 0);
                                final Point point = new Point();
                                getActivity().getWindow().getWindowManager().getDefaultDisplay().getSize(point);
                                floating.setTranslationY(point.y);
                                floating.setTranslationX(0);
                                floating.getLayoutParams().height = item.getHeight();
                                floating.getLayoutParams().width = item.getWidth();
                                floating.setScaleX(0.3f);
                                floating.setScaleY(0.3f);
                                floating.setVisibility(View.VISIBLE);
                                try {
                                    floating.animate().translationY(params[1]-paramsParent[1]).translationX(params[0]-paramsParent[0]).scaleX(1f).scaleY(1f).setDuration(500).setListener(new Animator.AnimatorListener() {
                                        @Override
                                        public void onAnimationStart(Animator animation) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            floating.animate().alpha(0f).setDuration(200).withEndAction(() -> {
                                                floating.setVisibility(GONE);
                                            });
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
                                Glide.with(getContext())
                                        .load(itemsToShow.get(pos).getUri())
                                        .into(floating);












                                itemChecked.setChecked(false);



                                ((FrameLayout)((CardView) item).findViewById(R.id.image_cover_grid)).setForeground(uncover_image);
                                itemsToShow.get(pos).setChecked(false);
                                FileSelection.selected_item_counter_down();
                            }
                            else {
                                CheckBox itemChecked = item.findViewById(R.id.checkbox_item_checked_round);
                                ImageView imageView = item.findViewById(R.id.imageView);
//                                LinearLayout ivParent = item.findViewById(R.id.iv_parent);


                                View parentEmptyRelativeLayout = LayoutInflater.from(getActivity()).inflate(R.layout.image_view_in_relative_layout, null, false);
                                ImageView floating = parentEmptyRelativeLayout.findViewById(R.id.floating_image_view);
                                RelativeLayout parent = root.findViewById(R.id.floating_image_parent_of_parent);
                                parent.addView(parentEmptyRelativeLayout, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


                                int[] params = {0, 0};
                                item.getLocationOnScreen(params);
                                int[] paramsParent = {0, 0};
                                parentEmptyRelativeLayout.getLocationOnScreen(paramsParent);


                                parentEmptyRelativeLayout.setPadding(0, 0, 0, 0);
                                floating.setTranslationY(params[1]-paramsParent[1]);
                                floating.setTranslationX(params[0]-paramsParent[0]);
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
                                }
                                catch (Exception e){

                                }
                                Glide.with(getContext())
                                        .load(itemsToShow.get(pos).getUri())
                                        .into(floating);
                                ((FrameLayout)((CardView) item).findViewById(R.id.image_cover_grid)).setForeground(cover_image);
                                itemChecked.setChecked(true);
                                itemsToShow.get(pos).setChecked(true);
                                FileSelection.selected_item_counter_up();
                            }
                            while (pos >= 0){
                                if (itemsToShow.get(pos).isSeperator()){
                                    adapter.notifyItemChanged(pos);
                                    break;
                                }else {
                                    pos--;
                                }
                            }
                        }
                        autoCheckSelectAllBox();
                        //autoCheckDateSelectCheckBox();
                    }

                    @Override
                    public void onLongClick(View item, int position, boolean seperator) {
                        if (itemsToShow.get(position).getFILETYPE() == Media.TYPE_IMAGE) {

//                            hideSystemUI();

                            blurView.setVisibility(GONE);
                            List<Uri> uris = new CopyOnWriteArrayList<>();
                            uris.add(itemsToShow.get(position).getUri());
                            View overlay = LayoutInflater.from(requireActivity()).inflate(R.layout.back_button_bar_layout, null, false);
                            ImageView back = overlay.findViewById(R.id.back_button_image_view_fullscreen);
                            ImageView info = overlay.findViewById(R.id.info_button_image_view_fullscreen);
                            TextView title = overlay.findViewById(R.id.photo_view_name);

                            Bitmap map=darkenBitMap(getResizedBitmap(takeScreenShot(getActivity()), 360));
                            Bitmap fast=fastblur(map, 15);

                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeFile(new File(itemsToShow.get(position).getUri().getPath()).getAbsolutePath(), options);
                            int imageHeight = options.outHeight;
                            int imageWidth = options.outWidth;
                            int res = (imageHeight * imageWidth) / (1000 * 1000);
                            Send_Activity.iv = new StfalconImageViewer.Builder<Uri>(requireActivity(), uris, new ImageLoader<Uri>() {
                                @Override
                                public void loadImage(ImageView imageView, Uri image) {
                                    if (res <= 16) {
                                        Glide.with(requireActivity()).load(image)
                                                .apply(new RequestOptions()
                                                        .fitCenter()
                                                        .format(DecodeFormat.PREFER_ARGB_8888)
                                                        .override(Target.SIZE_ORIGINAL))
                                                .thumbnail(Glide.with(requireActivity())
                                                        .load(itemsToShow.get(position).getUri())
                                                        .apply(new RequestOptions().fitCenter().format(DecodeFormat.PREFER_RGB_565)).override(240, 240))
                                                .into(imageView);
                                    }
                                    else {
                                        Glide.with(requireActivity()).load(image)
                                                .apply(new RequestOptions()
                                                        .fitCenter()
                                                        .format(DecodeFormat.PREFER_ARGB_8888)
                                                        .override(3500))
                                                .thumbnail(Glide.with(requireActivity())
                                                        .load(itemsToShow.get(position).getUri())
                                                        .apply(new RequestOptions().fitCenter().format(DecodeFormat.PREFER_RGB_565)).override(240, 240))
                                                .into(imageView);
                                    }
                                }
                            })
                                    .withHiddenStatusBar(false)
                                    .withBackgroundColor(Color.parseColor("#000000"))
                                    .withBackgroundDrawable( new BitmapDrawable(getResources(),fast))
                                    .withTransitionFrom(item.findViewById(R.id.imageView))
                                    .withOverlayView(overlay).withDismissListener(new OnDismissListener() {
                                        @Override
                                        public void onDismiss() {
                                            //           blurViewTopLevel.setVisibility(GONE);
//                                    showSystemUI();
                                            Send_Activity.iv = null;
                                            blurView.setVisibility(View.VISIBLE);
                                            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                        }
                                    }).build();
                            back.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Send_Activity.iv.close();
                                }
                            });
                            title.setText(itemsToShow.get(position).getName());
                            info.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            });
                            //blurView.setVisibility(GONE);
                            //  blurViewTopLevel.setVisibility(View.VISIBLE);
                            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

                            Send_Activity.iv.show();

                        }
                        else if (itemsToShow.get(position).getFILETYPE() == Media.TYPE_VIDEO){
                            setOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR, requireActivity());

                            ActivityOptionsCompat options = ActivityOptionsCompat.
                                    makeSceneTransitionAnimation(requireActivity(), item, "openTransition");
                            startActivity(new Intent(requireActivity(), VideoPlayerDismissable.class).putExtra("URI", itemsToShow.get(position).getUri().toString()).putExtra("NAME", itemsToShow.get(position).getName()), options.toBundle());
                        }
                    }
                });
                adapter.setHasStableIds(true);
                recyclerView = view.findViewById(R.id.gallery_recycler_view);
                fastScroller = view.findViewById(R.id.fastscroll);




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


//                new FastScrollerBuilder(recyclerView)
//                        .setPopupTextProvider(new PopupTextProvider() {
//                            @NonNull
//                            @Override
//                            public String getPopupText(int position) {
//                                return itemsToShow.get(position).getModifiedString();
//                            }
//                        })
//                        .setPopupStyle(DEFAULT)
//                        .setThumbDrawable(getResources().getDrawable(R.drawable.thumb_drawable))
//                        .build();

                GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx,int dy){
                        super.onScrolled(recyclerView, dx, dy);

                        int position = gridLayoutManager.findFirstVisibleItemPosition();
                        try {
                            if (itemsToShow.get(position).isSeperator()) {
                                String text = dateFormat.format(new Date(itemsToShow.get(position + 1).getModified()));
                                dateViewAtTop.setText(text);
                                if (!text.equals(lastTopDateString))
                                {
                                    lastTopDateString = text;
                                    autoCheckDateSelectCheckBox();
                                }
                            } else {
                                    String text = dateFormat.format(new Date(itemsToShow.get(position - 1).getModified()));
                                    if (!text.equals(lastTopDateString))
                                    {
                                        dateViewAtTop.setText(text);
                                        lastTopDateString = text;
                                        autoCheckDateSelectCheckBox();
                                    }
                            }
                        }
                        catch (Exception e){

                        }

                        if (dy > 20) {
                            // Scroll Down
//                            if (fab.isShown()) {
//                                fab.hide();
//                            }
                            FileSelection.scrollDown();
                        }
                        else if (dy < -20) {
                            // Scroll Up
//                            if (!fab.isShown()) {
//                                fab.show();
//                            }
                            FileSelection.scrollUp();
                        }

                    }
                });

                recyclerView.setLayoutManager(gridLayoutManager);
                gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        try {
                            if (itemsToShow.get(position).isSeperator()) {
                                return 3;
                            } else {
                                return 1;
                            }
                        }
                        catch (Exception e){
                            return 1;
                        }
                    }
                });
                adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
                recyclerView.setAdapter(adapter);
//                recyclerView.setItemViewCacheSize(50);
//                recyclerView.setDrawingCacheEnabled(true);
//                recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                fastScroller.setRecyclerView(recyclerView);
            }
        });
    }

    private void autoCheckDateSelectCheckBox() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                boolean checked = true;
                for (Media media : itemsToShow) {
                    if (!media.isSeperator()) {
                        if (dateFormat.format(new Date(media.getModified())).equals(lastTopDateString)) {
                            if (!media.isChecked()) {
                                checked = false;
                                break;
                            }
                        }
                    }
                }
                boolean finalChecked = checked;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        dateCheckAtTop.setChecked(finalChecked);
                    }
                });
            }
        }).start();
    }

    private void autoCheckSelectAllBox() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                boolean areAllChecked = true;
                boolean checked = true;
                for (Media media : itemsToShow){
                    if (!media.isSeperator()) {
                        if (!media.isChecked()) {
                            areAllChecked = false;
                            if (dateFormat.format(new Date(media.getModified())).equals(lastTopDateString)) {
                                checked = false;
                            }
                        }
                    }
                }
                boolean finalAreAllChecked = areAllChecked;
                boolean finalChecked = checked;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        dateCheckAtTop.setChecked(finalChecked);
                        if (finalAreAllChecked && !selectAllCheckBox.isChecked()) {
                            isManuallyChecked = true;
                            selectAllCheckBox.setChecked(finalAreAllChecked);
                        }
                        else if (!finalAreAllChecked && selectAllCheckBox.isChecked()) {
                            isManuallyChecked = true;
                            selectAllCheckBox.setChecked(finalAreAllChecked);
                        }
                    }
                });

    }


    static <K, V> void orderByValue(
            LinkedHashMap<K, V> m, final Comparator<? super K> c) {
        List<Map.Entry<K, V>> entries = new ArrayList<>(m.entrySet());

        Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> lhs, Map.Entry<K, V> rhs) {
                return c.compare(lhs.getKey(), rhs.getKey());
            }
        });

        m.clear();
        for(Map.Entry<K, V> e : entries) {
            m.put(e.getKey(), e.getValue());
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
    private static Bitmap takeScreenShot(Activity activity) {
        View screenView = activity.getWindow().getDecorView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
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
    private Bitmap darkenBitMap(Bitmap bm) {

        Canvas canvas = new Canvas(bm);
        Paint p = new Paint(Color.RED);
        //ColorFilter filter = new LightingColorFilter(0xFFFFFFFF , 0x00222222); // lighten
        ColorFilter filter = new LightingColorFilter(0x99999999, 0x00000000);    // darken
        p.setColorFilter(filter);
        canvas.drawBitmap(bm, new Matrix(), p);

        return bm;
    }

    @Override
    public void onChange() {
        try {
            recyclerView.getAdapter().notifyDataSetChanged();
            autoCheckSelectAllBox();
        }
        catch (Exception ignored) {

        }
    }


    private static class GalleryRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements SectionTitleProvider {

        private final onClickListener onClickListener;
        private final HashMap<RecyclerView.ViewHolder, ViewTreeObserver.OnScrollChangedListener> dateStickListeners = new HashMap<RecyclerView.ViewHolder, ViewTreeObserver.OnScrollChangedListener>();

        private FragmentActivity requireActivity(){
            return activity;
        }


        @Override
        public String getSectionTitle(int position) {
            //this String will be shown in a bubble for specified position
            try {
                String text = itemsToShow.get(position).getModifiedString();
                try {
                    if (text == null) {
                        text = itemsToShow.get(position + 1).getModifiedString();
                    }
                } catch (IndexOutOfBoundsException ignored) {

                }
                return text;
            }
            catch (Exception e) {
                return "";
            }
        }




        private interface onClickListener{
            public void onClick(View item, int position, boolean seperator);
            public void onLongClick(View item, int position, boolean seperator);
        }

        public GalleryRecyclerViewAdapter(onClickListener onClickListener) {
            this.onClickListener = onClickListener;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 1) {
                View item = LayoutInflater.from(requireActivity()).inflate(R.layout.grid_item, parent, false);
                return new MyViewHolder(item);
            }
            else
                return new EmptyViewHolder(LayoutInflater.from(requireActivity()).inflate(R.layout.grid_item_empty, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//            Log.e("View Load", String.valueOf(itemsToShow.get(position).isHasToShow()));
            if (holder instanceof MyViewHolder) {
                MyViewHolder holder1 = (MyViewHolder) holder;
                holder1.itemView.setVisibility(View.VISIBLE);



                if (itemsToShow.get(position).isSeperator()) {
                    dateStickListeners.put(holder, new ViewTreeObserver.OnScrollChangedListener() {
                        @Override
                        public void onScrollChanged() {
                            int offset = -holder1.itemView.getTop();
                            if (offset <= 0 && offset >= -1 * convertDpToPx(requireActivity(), 30)) {
                                String text = dateFormat.format(new Date(itemsToShow.get(position + 1).getModified()));
//                                dateViewAtTop.setText(text);
//                                holder1.itemView.setVisibility(View.GONE);
                            }
//                            else if (offset < -convertDpToPx(requireActivity(), 30)) {
//                                try {
//                                    String text = dateFormat.format(new Date(itemsToShow.get(position - 1).getModified()));
//                                    dateViewAtTop.setText(text);
//                                }
//                                catch (Exception e) {
//
//                                }
//                                holder1.itemView.setVisibility(View.VISIBLE);
//                            }
                            else {

                                holder1.itemView.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    //holder.itemView.getViewTreeObserver().addOnScrollChangedListener(dateStickListeners.get(holder));
                    holder1.label.setVisibility(View.VISIBLE);
                    holder1.ivparent.setVisibility(GONE);
                    ((FrameLayout)((CardView) holder1.itemView).findViewById(R.id.image_cover_grid)).setForeground(uncover_image);
                    String date = dateFormat.format(new Date(itemsToShow.get(position + 1).getModified()));
                    holder1.date_label.setText(date);
                    boolean isChecked = true;
                    for (Media media : itemsToShow) {
                        try {
                            if (dateFormat.format(new Date(media.getModified())).equals(date)) {
                                if (!media.isSeperator()) {
                                    if (!media.isChecked()) {
                                        isChecked = false;
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {

                        }
                    }
                    holder1.checkBox.setChecked(isChecked);
                } else {

                    holder.itemView.getViewTreeObserver().removeOnScrollChangedListener(dateStickListeners.get(holder));
                    //dateStickListeners.remove(holder);
                    if (!itemsToShow.get(position).isChecked()) {
                   // holder1.ivparent.setBackgroundColor(Color.parseColor("#00777777"));
//                    holder.imageView.setPadding(0, 0, 0, 0);
                 //       ((CardView) holder1.itemView).setForeground(uncover_image);
                        ((FrameLayout)((CardView) holder1.itemView).findViewById(R.id.image_cover_grid)).setForeground(uncover_image);
                        holder1.itemChecked.setChecked(false);
                    } else {
                    //holder1.ivparent.setBackgroundColor(requireActivity().getResources().getColor(R.color.media_selected_border));

//                    holder.imageView.setPadding(max_padding, max_padding, max_padding, max_padding);
                 //       ((CardView) holder1.itemView).setForeground(cover_image);

                        ((FrameLayout)((CardView) holder1.itemView).findViewById(R.id.image_cover_grid)).setForeground(cover_image);
                        holder1.itemChecked.setChecked(true);
                    }
                    if (itemsToShow.get(position).getFILETYPE() == Media.TYPE_VIDEO) {
                        holder1.durationText.setVisibility(View.VISIBLE);
                        Media media = itemsToShow.get(position);
                        if (media.getDuration() == null)
                            new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                                        retriever.setDataSource(requireActivity(), itemsToShow.get(position).getUri());
                                        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                        long timeInMillisec = Long.parseLong(time);
                                        retriever.release();
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                String time = formatTime(timeInMillisec);
                                                media.setDuration(time);
                                                holder1.durationText.setText(time);
                                            }
                                        });
                                    } catch (NullPointerException ne) {
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                holder1.durationText.setText("?");
                                            }
                                        });
                                    } catch (Exception ie) {
                                        ie.printStackTrace();
                                    }
                                }
                            }).start();
                        else
                            holder1.durationText.setText(media.getDuration());
                    } else {
                        holder1.durationText.setVisibility(GONE);
                    }
                    holder1.label.setVisibility(GONE);
                    holder1.ivparent.setVisibility(View.VISIBLE);
                    Glide.with(requireActivity())
                            .load(itemsToShow.get(position).getUri())
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .apply(new RequestOptions().format(DecodeFormat.PREFER_RGB_565))
//                        .thumbnail(Glide.with(requireActivity()).load(R.drawable.ic_image))
                            .into(holder1.imageView);
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickListener.onClick(holder.itemView, position, itemsToShow.get(position).isSeperator());
                    }
                });
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        onClickListener.onLongClick(holder.itemView, position, itemsToShow.get(position).isSeperator());
                        if (!itemsToShow.get(position).isSeperator())
                            return true;
                        else
                            return false;
                    }
                });
            }
        }

        @Override
        public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
            super.onViewRecycled(holder);
            if (holder instanceof MyViewHolder) {
                ((MyViewHolder) holder).recycle();
            }
        }



        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return itemsToShow.size() + FileSelection.gridExtraItems;
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

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ConstraintLayout ivparent;
            View label;
            ImageView imageView;
            TextView date_label;
            CheckBox checkBox;
            CheckBox itemChecked;
            TextView durationText;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.imageView);
                ivparent = itemView.findViewById(R.id.iv_parent);
                label = itemView.findViewById(R.id.multi_selection_and_date_change_bar);
                date_label = itemView.findViewById(R.id.date_text_view_recycler_view);
                checkBox = itemView.findViewById(R.id.select_all_checkbox_recycler_view);
                durationText = itemView.findViewById(R.id.media_duration_text);
                itemChecked = itemView.findViewById(R.id.checkbox_item_checked_round);
            }

            public void recycle() {
                Glide.with(requireActivity()).clear(imageView);
            }
        }
        private String formatTime(long milliseconds) {
            int seconds = (int) (milliseconds / 1000) % 60 ;
            int minutes = (int) ((milliseconds / (1000*60)) % 60);
            int hours   = (int) ((milliseconds / (1000*60*60)) % 24);
            String hh = "";
            if (hours <= 9){
                hh = "0" + hours;
            }
            else{
                hh = String.valueOf(hours);
            }
            String mm = "";
            if (minutes <= 9){
                mm = "0" + minutes;
            }
            else{
                mm = String.valueOf(minutes);
            }
            String ss = "";
            if (seconds <= 9){
                ss = "0" + seconds;
            }
            else{
                ss = String.valueOf(seconds);
            }
            if (hours > 0)
                return hh + ":" + mm + ":" + ss;
            else
                return mm + ":" + ss;
        }

    }
    public static float convertDpToPx(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
}
