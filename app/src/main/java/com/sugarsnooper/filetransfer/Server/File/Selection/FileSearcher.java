package com.sugarsnooper.filetransfer.Server.File.Selection;

import android.net.Uri;

import com.sugarsnooper.filetransfer.readableRootsSurvivor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.fileAndFolderList;
import static com.sugarsnooper.filetransfer.Server.File.Selection.FileSelection.fileAndFolderListPositionTableW_R_T_URI;

public class FileSearcher {

    interface SearchResult {
        public void foundResult(Media resultUri);
        public void searchCompleted();
    }
    private ArrayList<Integer> cancelledSearchesId = new ArrayList<Integer>();
    private int requestId = 0;
    private final SearchResult response;
    public FileSearcher(SearchResult result) {
        response = result;
    }
    public void search(String fileName) {
        cancelSearch();
        requestId += 1;
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int id = requestId;
                String searchQuery = fileName.trim();
                if (!searchQuery.isEmpty()) {
                    searchQuery = searchQuery.toLowerCase();
                    for (Media file : FileSelection.appsList) {
                        if (searchCompare(searchQuery, file.getName())) {
                            if (!cancelledSearchesId.contains(id))
                                response.foundResult(file);
                        }
                    }
                    for (File file : readableRootsSurvivor.getAllFiles()) {
                        if (file != null) {
                            if (searchCompare(searchQuery, file.getName())) {
                                if (!cancelledSearchesId.contains(id)) {
                                    Uri uri = Uri.fromFile(file);
                                    Media media = new Media(uri, file.getName(), file.length(), file.lastModified(), file);
                                    media.setChecked(fileAndFolderListPositionTableW_R_T_URI.containsKey(file.getAbsolutePath()) && fileAndFolderList.get(fileAndFolderListPositionTableW_R_T_URI.get(file.getAbsolutePath()).intValue()).isChecked());

                                    if (media.isChecked()) {
                                        fileAndFolderList.get(fileAndFolderListPositionTableW_R_T_URI.get(file.getAbsolutePath()).intValue()).setChecked(false);
                                    }
                                    fileAndFolderList.add(media);
                                    fileAndFolderListPositionTableW_R_T_URI.put(file.getAbsolutePath(), fileAndFolderList.size() - 1);

                                    response.foundResult(media);
                                }
                            }
                        }
                    }
                }
                if (!cancelledSearchesId.contains(id)) {
                    response.searchCompleted();
                }
            }
        }).start();
    }
    public void cancelSearch() {
        cancelledSearchesId.add(requestId);
    }

    private boolean searchCompare(String searchQuery, String fileName) {
        if (fileName != null) {
            List<String> queries = new ArrayList<>(Arrays.asList(searchQuery.split(" ")));
            queries.addAll(Arrays.asList(searchQuery.split(",")));
            queries.addAll(Arrays.asList(searchQuery.split("\\.")));

            for (String query: queries) {

                if (fileName.toLowerCase().contains(query) && !query.trim().isEmpty() && !query.trim().equals(".") && !query.trim().equals(",")) {
                    return true;
                }
            }
            return false;
        }
        else {
            return false;
        }
    }
}
