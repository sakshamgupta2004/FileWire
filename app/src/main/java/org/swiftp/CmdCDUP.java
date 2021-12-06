package org.swiftp;

import java.io.File;
import java.io.IOException;

public class CmdCDUP extends FtpCmd implements Runnable {
    protected String input;

    public CmdCDUP(SessionThread sessionThread, String input2) {
        super(sessionThread, CmdCDUP.class.toString());
    }

    public void run() {
        this.myLog.l(3, "CDUP executing");
        String errString = null;
        File newDir = this.sessionThread.getWorkingDir().getParentFile();
        if (newDir == null) {
            errString = "550 Current dir cannot find parent\r\n";
        } else if (violatesChroot(newDir)) {
            errString = "550 Invalid name or chroot violation\r\n";
        } else {
            try {
                File newDir2 = newDir.getCanonicalFile();
                if (!newDir2.isDirectory()) {
                    errString = "550 Can't CWD to invalid directory\r\n";
                } else if (newDir2.canRead()) {
                    this.sessionThread.setWorkingDir(newDir2);
                } else {
                    errString = "550 That path is inaccessible\r\n";
                }
            } catch (IOException e) {
                errString = "550 Invalid path\r\n";
            }
        }
        if (errString != null) {
            this.sessionThread.writeString(errString);
            this.myLog.i("CDUP error: " + errString);
            return;
        }
        this.sessionThread.writeString("200 CDUP successful\r\n");
        this.myLog.l(3, "CDUP success");
    }
}
