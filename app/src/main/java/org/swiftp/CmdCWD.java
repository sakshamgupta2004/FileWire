package org.swiftp;

import java.io.File;
import java.io.IOException;

public class CmdCWD extends FtpCmd implements Runnable {
    protected String input;

    public CmdCWD(SessionThread sessionThread, String input2) {
        super(sessionThread, CmdCWD.class.toString());
        this.input = input2;
    }

    public void run() {
        this.myLog.l(3, "CWD executing");
        File newDir = inputPathToChrootedFile(this.sessionThread.getWorkingDir(), getParameter(this.input));
        if (violatesChroot(newDir)) {
            String errString = "550 Invalid name or chroot violation\r\n";
            this.sessionThread.writeString(errString);
            this.myLog.l(4, errString);
        } else {
            try {
                File newDir2 = newDir.getCanonicalFile();
                if (!newDir2.isDirectory()) {
                    this.sessionThread.writeString("550 Can't CWD to invalid directory\r\n");
                } else if (newDir2.canRead()) {
                    this.sessionThread.setWorkingDir(newDir2);
                    this.sessionThread.writeString("250 CWD successful\r\n");
                } else {
                    this.sessionThread.writeString("550 That path is inaccessible\r\n");
                }
            } catch (IOException e) {
                this.sessionThread.writeString("550 Invalid path\r\n");
            }
        }
        this.myLog.l(3, "CWD complete");
    }
}
