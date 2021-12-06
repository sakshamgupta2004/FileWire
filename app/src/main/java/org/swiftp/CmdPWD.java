package org.swiftp;

import java.io.File;
import java.io.IOException;

public class CmdPWD extends FtpCmd implements Runnable {
    public CmdPWD(SessionThread sessionThread, String input) {
        super(sessionThread, CmdPWD.class.toString());
    }

    public void run() {
        String currentDir;
        this.myLog.l(3, "PWD executing");
        try {
            File workingDir = this.sessionThread.getWorkingDir();
            if (workingDir != null) {
                currentDir = workingDir.getCanonicalPath();
            } else {
                currentDir = Globals.getChrootDir().getCanonicalPath();
            }
            String currentDir2 = currentDir.substring(Globals.getChrootDir().getCanonicalPath().length());
            if (currentDir2.length() == 0) {
                currentDir2 = "/";
            }
            this.sessionThread.writeString("257 \"" + currentDir2 + "\"\r\n");
        } catch (IOException e) {
            this.myLog.l(6, "PWD canonicalize");
            this.sessionThread.closeSocket();
        }
        this.myLog.l(3, "PWD complete");
    }
}
