package org.swiftp;

public class CmdQUIT extends FtpCmd implements Runnable {
    public static final String message = "TEMPLATE!!";

    public CmdQUIT(SessionThread sessionThread, String input) {
        super(sessionThread, CmdQUIT.class.toString());
    }

    public void run() {
        this.myLog.l(3, "QUITting");
        this.sessionThread.writeString("221 Goodbye\r\n");
        this.sessionThread.closeSocket();
    }
}
