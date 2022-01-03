package com.sugarsnooper.filetransfer.Server.File.Selection;

import android.net.Uri;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Media implements Serializable {

    public static final int TYPE_VIDEO = 10001;
    public static final int TYPE_IMAGE = 10002;
    public static final int TYPE_UNKNOWN = 99999;


    private final String uri;
    private final String name;
    private final long size;
    private File parentFile = null;
    private boolean isFolder = false;
    private String modifiedString = null;
    private String parent = null;
    private boolean isChecked;
    private boolean hastoanimate;
    private final long Modified;
    private boolean isSeperator = false;
    private int FILETYPE;
    private boolean hasToShow = true;
    private String duration = null;
    private String fullPath = null;
    private String parentPath = null;

    public Media (){
        isSeperator = true;
        Modified = 0;
        size = 0;
        name = null;
        uri = null;
    }

    public Media (long modified){
        isSeperator = true;
        Modified = modified;
        size = 0;
        name = null;
        uri = null;
    }

    public Media (Uri uri, String name, long size, long modified, int FILETYPE) {
        this.uri = uri.toString();
        this.name = name;
        this.size = size;
        this.isChecked = false;
        this.hastoanimate = false;
        this.Modified = modified;
        this.FILETYPE = FILETYPE;
    }
    public Media (Uri uri, String name, long size, long modified, boolean setChecked, int FILETYPE) {
        this.uri = uri.toString();
        this.name = name;
        this.size = size;
        this.isChecked = setChecked;
        this.hastoanimate = false;
        this.Modified = modified;
        this.FILETYPE = FILETYPE;
    }
    public Media (Uri uri, String name, long size, long modified, String parent, int FILETYPE) {
        this.uri = uri.toString();
        this.name = name;
        this.size = size;
        this.isChecked = false;
        this.hastoanimate = false;
        this.Modified = modified;
        this.parent = parent;
        this.FILETYPE = FILETYPE;
    }

    public Media (Uri uri, String name, long size, long modified, String parent) {
        this.uri = uri.toString();
        this.name = name;
        this.size = size;
        this.isChecked = false;
        this.hastoanimate = false;
        this.Modified = modified;
        this.parent = parent;
    }
    public Media (Uri uri, String name, long size, long modified, File file) {
        this.uri = uri.toString();
        this.name = name;
        this.size = size;
        this.isChecked = false;
        this.hastoanimate = false;
        this.Modified = modified;
        this.fullPath = file.getPath();
    }
    public Media (Uri uri, String name, long size) {
        this.uri = uri.toString();
        this.name = name;
        this.size = size;
        this.isChecked = false;
        this.hastoanimate = false;
        this.Modified = 0;
    }
    public Media (Uri uri, String name, long size, long modified, boolean setChecked, File file) {
        this.uri = uri.toString();
        this.name = name;
        this.size = size;
        this.isChecked = setChecked;
        this.hastoanimate = false;
        this.Modified = modified;
        this.fullPath = file.getPath();
    }
    public Media (Uri uri, String name, long size, long modified, boolean setChecked) {
        this.uri = uri.toString();
        this.name = name;
        this.size = size;
        this.isChecked = setChecked;
        this.hastoanimate = false;
        this.Modified = modified;
    }
    public Media (Uri uri, String name, long size, long modified, boolean setChecked, boolean isFolder) {
        this.uri = uri.toString();
        this.name = name;
        this.size = size;
        this.isChecked = setChecked;
        this.hastoanimate = false;
        this.Modified = modified;
        this.isFolder = isFolder;
    }

    public Media(Uri uri, String name, long size, long modified, String parent, int FILETYPE, String duration) {
        this.uri = uri.toString();
        this.name = name;
        this.size = size;
        this.isChecked = false;
        this.hastoanimate = false;
        this.Modified = modified;
        this.parent = parent;
        this.FILETYPE = FILETYPE;
        this.duration = duration;
    }

    public Media (Uri uri, String name, long size, long modified, String parent, int FILETYPE, SimpleDateFormat simpleDateFormat) {
        this.uri = uri.toString();
        this.name = name;
        this.size = size;
        this.isChecked = false;
        this.hastoanimate = false;
        this.Modified = modified;
        this.parent = parent;
        this.FILETYPE = FILETYPE;
        modifiedString = simpleDateFormat.format(new Date(modified));
    }

    public Media (Uri uri, String name, long size, long modified, File parent, int FILETYPE, SimpleDateFormat simpleDateFormat) {
        this.uri = uri.toString();
        this.name = name;
        this.size = size;
        this.isChecked = false;
        this.hastoanimate = false;
        this.Modified = modified;
        this.parentFile = parent;
        this.parentPath = parent.getPath();
        this.FILETYPE = FILETYPE;
        modifiedString = simpleDateFormat.format(new Date(modified));
    }

    public File getParentFile() {
        return parentFile;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDuration() {
        return duration;
    }

    public String getParent() {
        return parent;
    }

    public boolean isHasToShow() {
        return hasToShow;
    }

    public void setHasToShow(boolean hasToShow) {
        this.hasToShow = hasToShow;
    }

    public boolean isSeperator() {
        return isSeperator;
    }

    public int getFILETYPE() {
        return FILETYPE;
    }

    public long getModified() {
        return Modified;
    }

    public boolean isHastoanimate() {
        return hastoanimate;
    }

    public void setHastoanimate(boolean hastoanimate) {
        this.hastoanimate = hastoanimate;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public Uri getUri() {
        return Uri.parse(uri);
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getModifiedString() {
        return modifiedString;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public String getParentPath() {
        return parentPath;
    }
}
