package org.swiftp;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CmdLIST extends CmdAbstractListing implements Runnable {
    public static final long MS_IN_SIX_MONTHS = -1627869184;
    private String input;

    public CmdLIST(SessionThread sessionThread, String input2) {
        super(sessionThread, input2);
        this.input = input2;
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0046  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00ac  */
    public void run() {
        File fileToList;
        String errString = null;
        String listing = null;
        String param = getParameter(this.input);
        this.myLog.d("LIST parameter: " + param);
        while (param.startsWith("-")) {
            this.myLog.d("LIST is skipping dashed arg " + param);
            param = getParameter(param);
        }
        if (param.equals("")) {
            fileToList = this.sessionThread.getWorkingDir();
        } else {
            if (param.contains("*")) {
                errString = "550 LIST does not support wildcards\r\n";
            } else {
                fileToList = new File(this.sessionThread.getWorkingDir(), param);
                if (violatesChroot(fileToList)) {
                    errString = "450 Listing target violates chroot\r\n";
                }
            }
            if (errString == null) {
                this.sessionThread.writeString(errString);
                this.myLog.l(3, "LIST failed with: " + errString);
                return;
            }
            this.myLog.l(3, "LIST completed OK");
            return;
        }
        if (fileToList.isDirectory()) {
            StringBuilder response = new StringBuilder();
            errString = listDirectory(response, fileToList);
            if (errString == null) {
                listing = response.toString();
            }
            if (errString == null) {
            }
        } else {
            listing = makeLsString(fileToList);
            if (listing == null) {
                errString = "450 Couldn't list that file\r\n";
                if (errString == null) {
                }
            }
        }
        errString = sendListing(listing);
        if (errString != null) {
        }
        if (errString == null) {
        }
    }

    /* access modifiers changed from: protected */
    public String makeLsString(File file) {
        SimpleDateFormat format;
        StringBuilder response = new StringBuilder();
        if (!file.exists()) {
            staticLog.l(4, "makeLsString had nonexistent file");
            return null;
        }
        String lastNamePart = file.getName();
        if (lastNamePart.contains("*") || lastNamePart.contains("/")) {
            staticLog.l(4, "Filename omitted due to disallowed character");
            return null;
        }
        if (file.isDirectory()) {
            response.append("drwxr-xr-x 1 owner group");
        } else {
            response.append("-rw-r--r-- 1 owner group");
        }
        String sizeString = new Long(file.length()).toString();
        int padSpaces = 13 - sizeString.length();
        while (true) {
            int padSpaces2 = padSpaces;
            padSpaces = padSpaces2 - 1;
            if (padSpaces2 <= 0) {
                break;
            }
            response.append(' ');
        }
        response.append(sizeString);
        if (System.currentTimeMillis() - file.lastModified() > -1627869184) {
            format = new SimpleDateFormat(" MMM dd HH:mm ", Locale.US);
        } else {
            format = new SimpleDateFormat(" MMM dd  yyyy ", Locale.US);
        }
        response.append(format.format(new Date(file.lastModified())));
        response.append(lastNamePart);
        response.append("\r\n");
        return response.toString();
    }
}
