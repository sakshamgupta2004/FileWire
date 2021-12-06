package org.swiftp;

import java.io.File;

public class CmdRNFR extends FtpCmd implements Runnable {
    protected String input;

    public CmdRNFR(SessionThread sessionThread, String input2) {
        super(sessionThread, CmdRNFR.class.toString());
        this.input = input2;
    }

    public void run() {
        String errString = null;
        File file = inputPathToChrootedFile(this.sessionThread.getWorkingDir(), getParameter(this.input));
        if (violatesChroot(file)) {
            errString = "550 Invalid name or chroot violation\r\n";
        } else if (!file.exists()) {
            errString = "450 Cannot rename nonexistent file\r\n";
        }
        if (errString != null) {
            this.sessionThread.writeString(errString);
            this.myLog.l(4, "RNFR failed: " + errString.trim());
            this.sessionThread.setRenameFrom(null);
            return;
        }
        this.sessionThread.writeString("350 Filename noted, now send RNTO\r\n");
        this.sessionThread.setRenameFrom(file);
    }
}
