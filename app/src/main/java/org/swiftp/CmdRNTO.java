package org.swiftp;

import java.io.File;

public class CmdRNTO extends FtpCmd implements Runnable {
    protected String input;

    public CmdRNTO(SessionThread sessionThread, String input2) {
        super(sessionThread, CmdRNTO.class.toString());
        this.input = input2;
    }

    public void run() {
        String param = getParameter(this.input);
        String errString = null;
        this.myLog.l(3, "RNTO executing\r\n");
        this.myLog.l(4, "param: " + param);
        File toFile = inputPathToChrootedFile(this.sessionThread.getWorkingDir(), param);
        this.myLog.l(4, "RNTO parsed: " + toFile.getPath());
        if (violatesChroot(toFile)) {
            errString = "550 Invalid name or chroot violation\r\n";
        } else {
            File fromFile = this.sessionThread.getRenameFrom();
            if (fromFile == null) {
                errString = "550 Rename error, maybe RNFR not sent\r\n";
            } else if (!fromFile.renameTo(toFile)) {
                errString = "550 Error during rename operation\r\n";
            }
        }
        if (errString != null) {
            this.sessionThread.writeString(errString);
            this.myLog.l(4, "RNFR failed: " + errString.trim());
        } else {
            this.sessionThread.writeString("250 rename successful\r\n");
        }
        this.sessionThread.setRenameFrom(null);
        this.myLog.l(3, "RNTO finished");
    }
}
