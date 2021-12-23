package com.sugarsnooper.filetransfer.ConnectToPC.PCSoftware;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextClock;
import android.widget.TextView;
import com.sugarsnooper.filetransfer.R;

import java.util.ArrayList;

public class PairPCListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<PC> items;

    public PairPCListAdapter(Context context, ArrayList<PC> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public PC getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.paired_pc_listitem, viewGroup, false);
        }
        ((TextView) convertView.findViewById(R.id.pair_pc_item_name)).setText(getItem(position).getPCName());
        int color;
        if (getItem(position).isActive()) {
            color = Color.GREEN;
        }
        else {
            color = Color.RED;
        }
        ((TextView) convertView.findViewById(R.id.pair_pc_item_status)).setTextColor(color);
        return convertView;
    }
}
