package org.swiftp;

import java.io.File;

public class CmdMKD extends FtpCmd implements Runnable {
    String input;

    public CmdMKD(SessionThread sessionThread, String input2) {
        super(sessionThread, CmdMKD.class.toString());
        this.input = input2;
    }

    public void run() {
        this.myLog.l(3, "MKD executing");
        String param = getParameter(this.input);
        String errString = null;
        if (param.length() < 1) {
            errString = "550 Invalid name\r\n";
        } else {
            File toCreate = inputPathToChrootedFile(this.sessionThread.getWorkingDir(), param);
            if (violatesChroot(toCreate)) {
                errString = "550 Invalid name or chroot violation\r\n";
            } else if (toCreate.exists()) {
                errString = "550 Already exists\r\n";
            } else if (!toCreate.mkdir()) {
                errString = "550 Error making directory (permissions?)\r\n";
            }
        }
        if (errString != null) {
            this.sessionThread.writeString(errString);
            this.myLog.l(4, "MKD error: " + errString.trim());
        } else {
            this.sessionThread.writeString("250 Directory created\r\n");
        }
        this.myLog.l(4, "MKD complete");
    }
}
