package org.swiftp;

public class CmdFEAT extends FtpCmd implements Runnable {
    public static final String message = "TEMPLATE!!";

    public CmdFEAT(SessionThread sessionThread, String input) {
        super(sessionThread, CmdFEAT.class.toString());
    }

    public void run() {
        this.sessionThread.writeString("211-Features supported\r\n");
        this.sessionThread.writeString(" UTF8\r\n");
        this.sessionThread.writeString("211 End\r\n");
        this.myLog.l(3, "Gave FEAT response");
    }
}
