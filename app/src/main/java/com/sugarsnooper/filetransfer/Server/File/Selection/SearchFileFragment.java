package com.sugarsnooper.filetransfer.Server.File.Selection;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.sugarsnooper.filetransfer.CustomisedAdActivity;
import com.sugarsnooper.filetransfer.R;

public class SearchFileFragment extends Fragment {
    public static void backPressed(CustomisedAdActivity activity) {
        activity.getActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        ((CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout)).setTitle("File Selection");
        activity.getSupportFragmentManager().beginTransaction().show(activity.getSupportFragmentManager().findFragmentByTag("FILE_SELECTION_FRAGMENT")).remove(activity.getSupportFragmentManager().findFragmentByTag("SEARCH_FILES_FRAGMENT")).commit();
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
        EditText searchBox = view.findViewById(R.id.search_box_input);
        view.findViewById(R.id.cancel_search_box).setOnClickListener((o)->{
            searchBox.setText("");
        });
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                new FileSearcher(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}
