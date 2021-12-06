package com.sugarsnooper.filetransfer;

import androidx.annotation.Nullable;

import java.util.HashMap;

public class HashMapWithListener<K, V> extends HashMap<K, V> {

    private putListener PutListener;
    public void setPutListener(putListener PutListener) {
        this.PutListener = PutListener;
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        PutListener.onPut(key, value);
        return super.put(key, value);
    }

    public interface putListener {
        public void onPut(Object key, Object value);
    }

}
