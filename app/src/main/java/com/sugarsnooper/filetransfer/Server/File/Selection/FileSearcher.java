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
        public void foundResult(Media resultUri, double levenshteinDistance);
        public void searchCompleted();
        public void tooManyResults();
    }
    private int filesFound = 0;
    private ArrayList<Integer> cancelledSearchesId = new ArrayList<Integer>();
    private int requestId = 0;
    private final SearchResult response;
    public FileSearcher(SearchResult result) {
        response = result;
    }
    public void search(String fileName) {
        cancelSearch();
        filesFound = 0;
        requestId += 1;
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int id = requestId;
                String searchQuery = fileName.trim();
                if (!searchQuery.isEmpty()) {
                    searchQuery = searchQuery.toLowerCase();
                    for (Media file : FileSelection.appsList) {
                        if (filesFound<=100  && !cancelledSearchesId.contains(id)) {
                            if (searchCompare(searchQuery, file.getName()) <= searchQuery.split(" ").length - 1 || searchCompare(searchQuery, file.getName()) <= 100) {
                                if (!cancelledSearchesId.contains(id)) {
                                    response.foundResult(file, searchCompare(searchQuery, file.getName()));
                                    filesFound += 1;
                                }
                            }
                        }
                        else {
                            response.tooManyResults();
                            break;
                        }
                    }
                    if (filesFound <= 100)
                    for (File file : readableRootsSurvivor.getAllFiles()) {
                        if (filesFound <= 100 && !cancelledSearchesId.contains(id)) {
                            if (file != null) {
                                if (searchCompare(searchQuery, file.getName()) <= 100) {
                                    if (!cancelledSearchesId.contains(id)) {
                                        Uri uri = Uri.fromFile(file);
                                        Media media = new Media(uri, file.getName(), file.length(), file.lastModified(), file);
                                        media.setChecked(fileAndFolderListPositionTableW_R_T_URI.containsKey(file.getAbsolutePath()) && fileAndFolderList.get(fileAndFolderListPositionTableW_R_T_URI.get(file.getAbsolutePath()).intValue()).isChecked());

                                        if (media.isChecked()) {
                                            fileAndFolderList.get(fileAndFolderListPositionTableW_R_T_URI.get(file.getAbsolutePath()).intValue()).setChecked(false);
                                        }
                                        fileAndFolderList.add(media);
                                        fileAndFolderListPositionTableW_R_T_URI.put(file.getAbsolutePath(), fileAndFolderList.size() - 1);

                                        response.foundResult(media, searchCompare(searchQuery, file.getName()));
                                        filesFound += 1;
                                    }
                                }
                            }
                        }
                        else {
                            response.tooManyResults();
                            break;
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

    public double searchCompare(String searchQuery, String fileName) {

        if (fileName != null) {
            fileName = fileName.toLowerCase();
            List<String> queries = new ArrayList<>(Arrays.asList(searchQuery.split(" ")));
//            queries.addAll(Arrays.asList(searchQuery.split(",")));
//            queries.addAll(Arrays.asList(searchQuery.split("\\.")));

            double numberofmatches = 0.0;
            for (String query: queries) {

                if (fileName.toLowerCase().contains(query) && !query.trim().isEmpty()) {
                    numberofmatches += 100.0;
                }
                else {
                    char[] chars = query.toCharArray();
                    int numOfCharsInFile = 0;
                    for (char c : chars) {
                        if (fileName.toLowerCase().contains(String.valueOf(c))) {
                            numOfCharsInFile++;
                        }
                    }
                    if (numOfCharsInFile == query.length()) {

                        List<String> permutations = generatePermutations(query);
                        double numberofmatchesToAdd = 0.0;
                        for (String perm : permutations) {
                            if (fileName.contains(perm)) {
                                numberofmatchesToAdd += 1.0 / (Double.valueOf(compute_Levenshtein_distance(query, perm)));
                                break;
                            }
                        }

                        if (numberofmatchesToAdd == 0.0) {
                            if (query.length() > 1) {
                                List<String> queryWithOneCharRemoved = new ArrayList<>();
                                boolean hasWithOneCharRemoved = false;
                                for (int i = 0; i < query.length(); i++) {
                                    queryWithOneCharRemoved.add(query.replace(String.valueOf(query.charAt(i)), ""));
                                }
                                for (String q : queryWithOneCharRemoved) {
                                    if (fileName.contains(q)) {
                                        numberofmatches += 0.01;
                                        hasWithOneCharRemoved = true;
                                        break;
                                    }
                                }
                                if (!hasWithOneCharRemoved) {
                                    break;
                                }
                            }
                        }
                        else {
                            numberofmatches += numberofmatchesToAdd;
                        }
                    }
                    else
                    {
                        break;
                    }
                }
            }
            if (numberofmatches == 0)
                return Double.MAX_VALUE;
            else
                return Double.valueOf(queries.size()) - numberofmatches;
        }
        else {
            return Double.MAX_VALUE;
        }
    }
    private void permutation(String prefix, String str, StringBuilder c) {
        int n = str.length();
        if (n == 0)
            c.append(prefix + "\n");
        else {
            for (int i = 0; i < n; i++)
                permutation(prefix + str.charAt(i), str.substring(0, i) + str.substring(i+1, n), c);
        }
    }
    private List<String> generatePermutations(String query) {
//        List<String> permutations = new ArrayList<String>();
//        for (int i = 0; i < query.length(); i++) {
//            String newQuery = "";
//            if (i > 0) {
//                newQuery = query.substring(0, i);
//            }
//            if (i < query.length() - 1) {
//                newQuery += query.substring(i + 1);
//            }
//            for (int j = 0; j < newQuery.length(); j++) {
//                String permutation = "";
//                if (j > 0) {
//                    permutation = newQuery.substring(0, j);
//                }
//                permutation += query.substring(i, i+1);
//                if (j < query.length() - 1) {
//                    permutation += newQuery.substring(j);
//                }
//                permutations.add(permutation);
//            }
//
//        }
//        permutations = removeDuplicates(permutations);
//        return permutations;
        StringBuilder sb = new StringBuilder();
        permutation("", query, sb);
        List<String> permutations = new ArrayList<String>(Arrays.asList(sb.toString().split("\n")));
        permutations = removeDuplicates(permutations);
        return permutations;
    }
    private <T> List<T> removeDuplicates(List<T> list)
    {

        // Create a new ArrayList
        List<T> newList = new ArrayList<T>();

        // Traverse through the first list
        for (T element : list) {

            // If this element is not present in newList
            // then add it
            if (!newList.contains(element)) {

                newList.add(element);
            }
        }

        // return the new list
        return newList;
    }
    private int compute_Levenshtein_distance(String str1,
                                     String str2)
    {
        // If str1 is empty, all
        // characters of str2 are
        // inserted into str1, which is
        // of the only possible method of
        // conversion with minimum
        // operations.

        if (str1.isEmpty())
        {
            return str2.length();
        }

        // If str2 is empty, all
        // characters of str1 are
        // removed, which is the
        // only possible
        // method of conversion with minimum
        // operations.

        if (str2.isEmpty())
        {
            return str1.length();
        }

        // calculate the number of distinct characters to be
        // replaced in str1
        // by recursively traversing each substring

        int replace = compute_Levenshtein_distance(
                str1.substring(1), str2.substring(1))
                + NumOfReplacement(str1.charAt(0),str2.charAt(0));

        // calculate the number of insertions in str1
        // recursively
        int insert = compute_Levenshtein_distance(
                str1, str2.substring(1))+ 1;

        // calculate the number of deletions in str1
        // recursively
        int delete = compute_Levenshtein_distance(
                str1.substring(1), str2)+ 1;

        // returns minimum of three operatoins

        return minm_edits(replace, insert, delete);
    }

    private int NumOfReplacement(char c1, char c2)
    {
        // check for distinct characters
        // in str1 and str2

        return c1 == c2 ? 0 : 1;
    }

    private int minm_edits(Integer... nums1)
    {
        // receives the count of different
        // operations performed and returns the
        // minimum value among them.

        List<Integer> nums = new ArrayList<>(Arrays.asList(nums1));
        nums.add(Integer.MAX_VALUE);
        Integer[] sortedNums = nums.toArray(new Integer[nums.size()]);
        Arrays.sort(sortedNums);
        return sortedNums[0];
    }
}
