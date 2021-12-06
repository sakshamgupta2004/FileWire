package com.sugarsnooper.filetransfer.Client;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DownloaderWithMaxThreadPool {
    private final int num_threads;
    private final Context context;
    private final List<DownloadTask> downloadTasks;
    private final List<DownloadTask> downloadTaskQueue;
    private Thread queueMaintainerThread;


    public DownloaderWithMaxThreadPool(Context context) {
        this.num_threads = 1;
        this.context = context;
        this.downloadTasks = new CopyOnWriteArrayList<>();
        this.downloadTaskQueue = new CopyOnWriteArrayList<>();

        for (int i = 0; i < num_threads; i++) {
            this.downloadTasks.add(null);
        }
    }

    public DownloaderWithMaxThreadPool(Context context, int num_threads) {
        this.num_threads = num_threads;
        this.context = context;
        this.downloadTasks = new ArrayList<>();
        this.downloadTaskQueue = new ArrayList<>();

        for (int i = 0; i < num_threads; i++) {
            this.downloadTasks.add(null);
        }
    }

    public void add(downloadListener downloadListener, boolean isFolder, String... sUrl) {
//        this.downloadTaskQueue.add(new DownloadTask(this.context, isFolder, new DownloadTask.AsynResponse() {
//            @Override
//            public void processFinish(String out, long total) {
//                downloadListener.processFinish(out, total);
//            }
//
//            @Override
//            public void progressupdate(long progress) {
//                downloadListener.progressUpdate(progress);
//            }
//        }, sUrl));
//        if (this.queueMaintainerThread == null || (!this.queueMaintainerThread.isAlive())) {
//            this.queueMaintainerThread = new Thread(() -> runIfPoolHasSpace());
//            this.queueMaintainerThread.start();
//        }
        new DownloadTask(this.context, isFolder, new DownloadTask.AsynResponse() {
            @Override
            public void processFinish(String out, long total) {
                downloadListener.processFinish(out, total);
            }

            @Override
            public void progressupdate(long progress) {
                downloadListener.progressUpdate(progress);
            }
        }, sUrl).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void runIfPoolHasSpace() {
        while (downloadTaskQueue.size() > 0) {
            int count = 0;
            for (count = 0; count < downloadTasks.size(); count++) {
                if (downloadTasks.get(count) == null || downloadTasks.get(count).hasCompleted) {
                    if (downloadTaskQueue.size() > 0) {
                        try {
                            downloadTasks.set(count, downloadTaskQueue.get(0));
                            downloadTasks.get(count).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            downloadTaskQueue.remove(0);
                        }
                        catch (IllegalStateException illegalStateException) {
                            Log.e ("Exception", illegalStateException.toString());
                        }
                    }
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public interface downloadListener {
        void progressUpdate(long progress);
        void processFinish(String out, long total);
    }
}
