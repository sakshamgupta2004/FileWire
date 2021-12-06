package com.sugarsnooper.filetransfer.History;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sugarsnooper.filetransfer.R;
import com.sugarsnooper.filetransfer.TinyDB;

import java.util.ArrayList;

public class historyDateList extends Fragment {
    private static RecyclerView recyclerView;
    private View root;
    private ArrayList<String> dates;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dates = new TinyDB(getContext()).getListString("transferDates");
        return inflater.inflate(R.layout.recyclerview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        root = view;
        if (dates.size() == 0){
            root.findViewById(R.id.nothing_here).setVisibility(View.VISIBLE);
        }
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new RecyclerAdapter());
    }


    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerItem> {
        @NonNull
        @Override
        public RecyclerItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RecyclerItem(LayoutInflater.from(getContext()).inflate(R.layout.textview, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerItem holder, int position) {
            ( (TextView) holder.itemView.findViewById(R.id.text)).setText(dates.get(position));
        }

        @Override
        public int getItemCount() {
            return dates.size();
        }

        class RecyclerItem extends RecyclerView.ViewHolder {

            public RecyclerItem(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}

