package org.swiftp;

public class CmdSYST extends FtpCmd implements Runnable {
    public static final String response = "215 UNIX Type: L8\r\n";

    public CmdSYST(SessionThread sessionThread, String input) {
        super(sessionThread, CmdSYST.class.toString());
    }

    public void run() {
        this.myLog.l(3, "SYST executing");
        this.sessionThread.writeString(response);
        this.myLog.l(3, "SYST finished");
    }
}
