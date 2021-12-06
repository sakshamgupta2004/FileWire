package org.swiftp;

public class CmdNOOP extends FtpCmd implements Runnable {
    public static final String message = "TEMPLATE!!";

    public CmdNOOP(SessionThread sessionThread, String input) {
        super(sessionThread, CmdNOOP.class.toString());
    }

    public void run() {
        this.sessionThread.writeString("200 NOOP ok\r\n");
    }
}
