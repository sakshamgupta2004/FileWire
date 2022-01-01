package com.sugarsnooper.filetransfer.Server.File.Selection;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.sugarsnooper.filetransfer.R;
import com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments.FilesandFolder_Others_MainPage;
import com.sugarsnooper.filetransfer.readableRootsSurvivor;

import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.*;
import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.filesFragments;

public class RefreshingFilesAfterStoragePermissionChangeFragment extends Fragment {

    private Fragment originalFrag = null;

    public RefreshingFilesAfterStoragePermissionChangeFragment(Fragment replacedFragment) {
        originalFrag = replacedFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.refreshing_lists_after_permission_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new Thread(new Runnable(){

            @Override
            public void run() {
                readableRootsSurvivor.refreshLists();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            photosFragment.onRefreshList();
                            videoGalleryFragment.onRefreshList();
                            galleryFragment.onRefreshList();
                            getActivity().getSupportFragmentManager().beginTransaction().remove(RefreshingFilesAfterStoragePermissionChangeFragment.this).show(originalFrag).commit();
                        }
                        catch (Exception e)
                        {

                        }
                    }
                });
            }
        }).start();
    }
}
