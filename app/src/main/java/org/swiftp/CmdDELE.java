package org.swiftp;

import java.io.File;

public class CmdDELE extends FtpCmd implements Runnable {
    protected String input;

    public CmdDELE(SessionThread sessionThread, String input2) {
        super(sessionThread, CmdDELE.class.toString());
        this.input = input2;
    }

    public void run() {
        this.myLog.l(4, "DELE executing");
        File storeFile = inputPathToChrootedFile(this.sessionThread.getWorkingDir(), getParameter(this.input));
        String errString = null;
        if (violatesChroot(storeFile)) {
            errString = "550 Invalid name or chroot violation\r\n";
        } else if (storeFile.isDirectory()) {
            errString = "550 Can't DELE a directory\r\n";
        } else if (!storeFile.delete()) {
            errString = "450 Error deleting file\r\n";
        }
        if (errString != null) {
            this.sessionThread.writeString(errString);
            this.myLog.l(4, "DELE failed: " + errString.trim());
        } else {
            this.sessionThread.writeString("250 File successfully deleted\r\n");
            Util.deletedFileNotify(storeFile.getPath());
        }
        this.myLog.l(4, "DELE finished");
    }
}
