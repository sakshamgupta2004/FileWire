package org.swiftp;

public class CmdTemplate extends FtpCmd implements Runnable {
    public static final String message = "TEMPLATE!!";

    public CmdTemplate(SessionThread sessionThread, String input) {
        super(sessionThread, CmdTemplate.class.toString());
    }

    public void run() {
        this.sessionThread.writeString("TEMPLATE!!");
        this.myLog.l(4, "Template log message");
    }
}
