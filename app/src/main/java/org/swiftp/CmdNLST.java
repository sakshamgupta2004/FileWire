package org.swiftp;

import java.io.File;

public class CmdNLST extends CmdAbstractListing implements Runnable {
    public static final long MS_IN_SIX_MONTHS = -1627869184;
    private String input;

    public CmdNLST(SessionThread sessionThread, String input2) {
        super(sessionThread, input2);
        this.input = input2;
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0034  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x008a  */
    public void run() {
        File fileToList;
        String errString = null;
        String listing = null;
        String param = getParameter(this.input);
        if (param.startsWith("-")) {
            param = "";
        }
        if (param.equals("")) {
            fileToList = this.sessionThread.getWorkingDir();
        } else {
            if (param.contains("*")) {
                errString = "550 NLST does not support wildcards\r\n";
            } else {
                fileToList = new File(this.sessionThread.getWorkingDir(), param);
                if (violatesChroot(fileToList)) {
                    errString = "450 Listing target violates chroot\r\n";
                } else if (fileToList.isFile()) {
                    errString = "550 NLST for regular files is unsupported\r\n";
                }
            }
            if (errString == null) {
                this.sessionThread.writeString(errString);
                this.myLog.l(3, "NLST failed with: " + errString);
                return;
            }
            this.myLog.l(3, "NLST completed OK");
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
        if (!file.exists()) {
            staticLog.l(4, "makeLsString had nonexistent file");
            return null;
        }
        String lastNamePart = file.getName();
        if (lastNamePart.contains("*") || lastNamePart.contains("/")) {
            staticLog.l(4, "Filename omitted due to disallowed character");
            return null;
        }
        staticLog.l(3, "Filename: " + lastNamePart);
        return new StringBuilder(String.valueOf(lastNamePart)).append("\r\n").toString();
    }
}
