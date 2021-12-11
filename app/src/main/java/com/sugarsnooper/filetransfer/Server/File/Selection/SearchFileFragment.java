package com.sugarsnooper.filetransfer.Server.File.Selection;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sugarsnooper.filetransfer.CustomisedAdActivity;
import com.sugarsnooper.filetransfer.FileTypeLookup;
import com.sugarsnooper.filetransfer.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.appSelectionFragment;
import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.filesFragments;
import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.galleryFragment;
import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.photosFragment;
import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.videoGalleryFragment;

public class SearchFileFragment extends Fragment {

    private static FileSearcher fs;

    private EditText searchBox;
    private View searchProgress;
    private RecyclerView searchResults;
    private searchResultsAdapter resultsAdapter;

    private List<Media> searchResultsMedia = new CopyOnWriteArrayList<Media>();

    public static void backPressed(CustomisedAdActivity activity) {
        activity.getActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        ((CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout)).setTitle("File Selection");
        fs.cancelSearch();
        hideKeyboard(activity);
        FileSelection.searchFragment = null;
        activity.getSupportFragmentManager().beginTransaction().show(activity.getSupportFragmentManager().findFragmentByTag("FILE_SELECTION_FRAGMENT")).remove(activity.getSupportFragmentManager().findFragmentByTag("SEARCH_FILES_FRAGMENT")).commit();
    }
    public static void hideKeyboard(Activity activity) {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((CollapsingToolbarLayout) requireActivity().findViewById(R.id.toolbar_layout)).setTitle("Search");
        requireActivity().getActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);
        return inflater.inflate(R.layout.file_search_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.cancel_search_box).setVisibility(View.GONE);
        resultsAdapter = new searchResultsAdapter();
        searchBox = view.findViewById(R.id.search_box_input);
        searchProgress = view.findViewById(R.id.fileSearchProgressBar);
        searchResults = view.findViewById(R.id.searchResultsRecyclerView);
        searchProgress.setVisibility(View.INVISIBLE);
        fs = new FileSearcher(new FileSearcher.SearchResult() {
            @Override
            public void tooManyResults() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Too many results. Please be more specific for what you are searching. ", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void searchCompleted() {
                searchProgress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void foundResult(Media scanResult, double levenshteinDistance) {
                int insertPos = 0;
                for (Media m : searchResultsMedia) {


                    if (fs.searchCompare(searchBox.getText().toString().toLowerCase(), m.getName()) > levenshteinDistance) {
                        break;
                    }
                    insertPos++;
                }
                int finalInsertPos = insertPos;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        searchResultsMedia.add(finalInsertPos, scanResult);
                        resultsAdapter.notifyItemInserted(finalInsertPos);

                        if (finalInsertPos == 0) {
                            searchResults.scrollToPosition(0);
                        }
                    }
                });
            }
        });
        view.findViewById(R.id.cancel_search_box).setOnClickListener((o)->{
            searchBox.setText("");
            fs.cancelSearch();
            searchResultsMedia.clear();
            resultsAdapter.notifyDataSetChanged();
            searchProgress.setVisibility(View.INVISIBLE);
        });
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0) {
                    view.findViewById(R.id.cancel_search_box).setVisibility(View.GONE);
                }
                else {
                    view.findViewById(R.id.cancel_search_box).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        searchBox.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    searchResultsMedia.clear();
                    resultsAdapter.notifyDataSetChanged();
                    fs.search(searchBox.getText().toString());
                    searchProgress.setVisibility(View.VISIBLE);
                    hideKeyboard(getActivity());
                    return false;
                }
                return false;
            }
        });
        searchResults.setLayoutManager(new LinearLayoutManager(getActivity()));
        searchResults.setAdapter(resultsAdapter);
    }

    public void onChange() {
        resultsAdapter.notifyDataSetChanged();
    }

    class searchResultsAdapter extends RecyclerView.Adapter<searchResultsAdapter.searchResultsViewItem> {

        class searchResultsViewItem extends RecyclerView.ViewHolder {
            FloatingActionButton imageView;
            TextView name;
            TextView state;
            CheckBox checkBox;
            public searchResultsViewItem(@NonNull View itemView) {
                super(itemView);
                checkBox = itemView.findViewById(R.id.checkbox_item_checked_round);
                imageView = itemView.findViewById(R.id.imageview_item_file_selection);
                name = itemView.findViewById(R.id.item_name_file_selection_recycler_view_resource);
                state = itemView.findViewById(R.id.item_state_file_selection_recycler_view_resource);
            }
        }

        @NonNull
        @Override
        public searchResultsViewItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new searchResultsViewItem(LayoutInflater.from(parent.getContext()).inflate(R.layout.file_selection_items_recycler_item, parent, false));
        }

        @Override
        public void onBindViewHolder(searchResultsViewItem holder, int position) {
            holder.name.setText(searchResultsMedia.get(position).getName());
            holder.state.setText(getFormatSize(searchResultsMedia.get(position).getSize()));
            FloatingActionButton selectedImage = holder.imageView;
            holder.checkBox.setChecked(searchResultsMedia.get(position).isChecked());
            if (!searchResultsMedia.get(position).isFolder()) {
                switch (FileTypeLookup.fileType(searchResultsMedia.get(position).getName())) {
                    case 1:
                        Glide.with(getContext())
                                .load(R.drawable.ic_paper)
                                .into(selectedImage);
                        break;
                    case 2:
                    case 3:
                        Glide.with(getContext())
                                .load(searchResultsMedia.get(position).getUri())
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
                            String sourcePath = searchResultsMedia.get(position).getFullPath();
                            if (searchResultsMedia.get(position).getName().endsWith(".apk")) {

                                Glide.with(getContext())
                                        .load(new File(getContext().getExternalCacheDir().getPath() + File.separator + "APKThumbnails" + File.separator + searchResultsMedia.get(position).getName() + sourcePath.hashCode() + ".jpeg"))
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
                                                    saveBitmapToFile(folderToSave1, searchResultsMedia.get(position).getName() + sourcePath.hashCode() + ".jpeg", drawableToBitmap(appInfo.loadIcon(getContext().getPackageManager())), Bitmap.CompressFormat.JPEG, 95);

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



            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!new File(searchResultsMedia.get(position).getFullPath()).isDirectory()) {
                        boolean state = !holder.checkBox.isChecked();
                        holder.checkBox.setChecked(state);
                        if (state) {
                            FileSelection.selected_item_counter_up();
                        }
                        else {
                            FileSelection.selected_item_counter_down();
                        }
                        searchResultsMedia.get(position).setChecked(state);
                    }

                    appSelectionFragment.onChange();
                    photosFragment.onChange();
                    videoGalleryFragment.onChange();
                    galleryFragment.onChange();
                    filesFragments.onChange();

                }
            });
        }

        @Override
        public int getItemCount() {
            return searchResultsMedia.size();
        }
        private boolean saveBitmapToFile(File dir, String fileName, Bitmap bm,
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
        private Bitmap drawableToBitmap(Drawable drawable) {
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
}
