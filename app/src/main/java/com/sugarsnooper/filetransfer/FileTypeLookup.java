package com.sugarsnooper.filetransfer;

public class FileTypeLookup {

    public static String[] fileTypeStrings = {
            "Unknown",
            "Documents",
            "Photos",
            "Videos",
            "Audio",
            "Archives",
            "Apps and AppData",
            "Files"
    };

    public static byte fileType(String filename){
        try {
            String[] temp = filename.split("\\.");
            switch (temp[temp.length - 1].toLowerCase()) {
                case "txt":
                case "xlsx":
                case "xls":
                case "gslides":
                case "ppt":
                case "pptx":
                case "pdf":
                case "doc":
                case "docx":
                case "docm":

                    return 1;
                case "gif":
                case "tif":
                case "ico":
                case "jpg":
                case "jpeg":
                case "png":
                case "tiff":
                    return 2;
                case "3gp":
                case "avi":
                case "flv":
                case "m4v":
                case "mkv":
                case "mov":
                case "mng":
                case "mpeg":
                case "mpg":
                case "mpe":
                case "mp4":
                case "wmv":
                case "webm":
                    return 3;
                case "mp1":
                case "mp2":
                case "mp3":
                case "aac":
                case "wma":
                case "amr":
                case "wav":
                case "opus":
                case "ogg":
                    return 4;
                case "zip":
                case "7z":
                case "cab":
                case "gzip":
                case "bin":
                case "rar":
                case "tar":
                case "iso":
                    return 5;
                case "apk":
                case "xapk":
                case "obb":
                case "aab":
                    return 6;
                default:
                    return 0;
            }
        }
        catch (Exception e) {
            return 0;
        }
    }
    public static String fileTypeString(String fileName, boolean isFolder) {
//        String filetype = "";
//        switch (FileTypeLookup.fileType(fileName)){
//            case 1:
//                filetype = "Documents";
//                break;
//            case 2:
//                filetype = "Photos";
//                break;
//            case 3:
//                filetype = "Videos";
//                break;
//            case 4:
//                filetype = "Audio";
//                break;
//            case 5:
//                filetype = "Archives";
//                break;
//            default:
//                filetype = "Unknown";
//                break;
//        }
//        return filetype;
        if (!isFolder) {
            return fileTypeStrings[fileType(fileName)];
        }
        else {
            return fileTypeStrings[7];
        }
    }
}
