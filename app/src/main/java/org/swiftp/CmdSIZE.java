package org.swiftp;

import java.io.File;
import java.io.IOException;

public class CmdSIZE extends FtpCmd {
    protected String input;

    public CmdSIZE(SessionThread sessionThread, String input2) {
        super(sessionThread, CmdSIZE.class.toString());
        this.input = input2;
    }

    public void run() {
        this.myLog.d("SIZE executing");
        String errString = null;
        String param = getParameter(this.input);
        long size = 0;
        File currentDir = this.sessionThread.getWorkingDir();
        if (param.contains(File.separator)) {
            errString = "550 No directory traversal allowed in SIZE param\r\n";
        } else {
            File target = new File(currentDir, param);
            if (violatesChroot(target)) {
                errString = "550 SIZE target violates chroot\r\n";
            } else if (!target.exists()) {
                errString = "550 Cannot get the SIZE of nonexistent object\r\n";
                try {
                    this.myLog.i("Failed getting size of: " + target.getCanonicalPath());
                } catch (IOException e) {
                }
            } else if (!target.isFile()) {
                errString = "550 Cannot get the size of a non-file\r\n";
            } else {
                size = target.length();
            }
        }
        if (errString != null) {
            this.sessionThread.writeString(errString);
        } else {
            this.sessionThread.writeString("213 " + size + "\r\n");
        }
        this.myLog.d("SIZE complete");
    }
}
