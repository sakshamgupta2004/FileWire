package com.sugarsnooper.filetransfer.History;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sugarsnooper.filetransfer.CustomisedAdActivity;
import com.sugarsnooper.filetransfer.R;
import com.sugarsnooper.filetransfer.TinyDB;

import java.util.ArrayList;

public class HistoryActivity extends CustomisedAdActivity {
    Fragment fragment;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        setActionBar(toolbar);
        setTitle("History");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        fragment = new historyDateList();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();
        ArrayList<String> dates = new TinyDB(getBaseContext()).getListString("transferDates");
        for (String date : dates) {
            Log.e("Dates", date);
        }
    }
}
