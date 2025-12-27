package com.sugarsnooper.filetransfer;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.RecyclerView;

import com.futuremind.recyclerviewfastscroll.FastScroller;


public class MyFastScroll extends FastScroller {
    private RecyclerView recyclerView;

    public MyFastScroll(Context context) {
        super(context);
    }

    public MyFastScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyFastScroll(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (recyclerView != null) {
            super.onLayout(changed, l, t, r, b);
        }
    }

    @Override
    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        super.setRecyclerView(recyclerView);

        requestLayout();
    }
}
