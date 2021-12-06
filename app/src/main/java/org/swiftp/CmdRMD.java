package org.swiftp;

import java.io.File;

public class CmdRMD extends FtpCmd implements Runnable {
    public static final String message = "TEMPLATE!!";
    protected String input;

    public CmdRMD(SessionThread sessionThread, String input2) {
        super(sessionThread, CmdRMD.class.toString());
        this.input = input2;
    }

    public void run() {
        this.myLog.l(4, "RMD executing");
        String param = getParameter(this.input);
        String errString = null;
        if (param.length() < 1) {
            errString = "550 Invalid argument\r\n";
        } else {
            File toRemove = inputPathToChrootedFile(this.sessionThread.getWorkingDir(), param);
            if (violatesChroot(toRemove)) {
                errString = "550 Invalid name or chroot violation\r\n";
            } else if (!toRemove.isDirectory()) {
                errString = "550 Can't RMD a non-directory\r\n";
            } else if (toRemove.equals(new File("/"))) {
                errString = "550 Won't RMD the root directory\r\n";
            } else if (!recursiveDelete(toRemove)) {
                errString = "550 Deletion error, possibly incomplete\r\n";
            }
        }
        if (errString != null) {
            this.sessionThread.writeString(errString);
            this.myLog.l(4, "RMD failed: " + errString.trim());
        } else {
            this.sessionThread.writeString("250 Removed directory\r\n");
        }
        this.myLog.l(3, "RMD finished");
    }

    /* access modifiers changed from: protected */
    public boolean recursiveDelete(File toDelete) {
        if (!toDelete.exists()) {
            return false;
        }
        if (toDelete.isDirectory()) {
            boolean success = true;
            for (File entry : toDelete.listFiles()) {
                success &= recursiveDelete(entry);
            }
            this.myLog.l(3, "Recursively deleted: " + toDelete);
            if (!success || !toDelete.delete()) {
                return false;
            }
            return true;
        }
        this.myLog.l(3, "RMD deleting file: " + toDelete);
        return toDelete.delete();
    }
}
