package com.sugarsnooper.filetransfer.Server.File.Selection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sugarsnooper.filetransfer.R;
import com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments.DocumentExplorer;
import com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments.FileExplorer;
import com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments.FilesandFolder_Others_MainPage;
import com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments.FirstPageFragmentListener;
import com.sugarsnooper.filetransfer.Server.File.Selection.SubFragments.ListChangeListener;

public class FragmentContainerFileSelection extends Fragment implements ListChangeListener {

    private Fragment fragment;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_container_view_file_selection, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        switch_fragment();
    }

    private void switch_fragment(){
        fragment = null;
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_file_selection, new FilesandFolder_Others_MainPage(new FirstPageFragmentListener() {
            @Override
            public void onSwitchToNextFragment() {

            }

            @Override
            public void onSwitchToNextFragment(Bundle bundle, int type) {
                if (type == FilesandFolder_Others_MainPage.Storage.TYPE_VOLUME) {
                    fragment = new FileExplorer(new FirstPageFragmentListener() {
                        @Override
                        public void onSwitchToNextFragment() {
                            switch_fragment();
                        }

                        @Override
                        public void onSwitchToNextFragment(Bundle bundle, int typeVolume) {

                        }
                    });

                    fragment.setArguments(bundle);
                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_file_selection, fragment, "FILE_EXPLORER_SELECTION").commit();
                }
                else {
                    fragment = new DocumentExplorer(new FirstPageFragmentListener() {
                        @Override
                        public void onSwitchToNextFragment() {
                            switch_fragment();
                        }

                        @Override
                        public void onSwitchToNextFragment(Bundle bundle, int typeVolume) {

                        }
                    });

                    fragment.setArguments(bundle);
                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_file_selection, fragment, "DOCUMENT_SELECTION").commit();
                }
            }
        })).commit();
    }

    @Override
    public void onChange() {
        if (fragment != null) {
            if (fragment instanceof FileExplorer) {
                ((FileExplorer) fragment).onChange();
            } else if (fragment instanceof DocumentExplorer) {
                ((DocumentExplorer) fragment).onChange();
            }
        }
    }
}
