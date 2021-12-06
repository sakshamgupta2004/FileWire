package org.swiftp;

public class CmdTYPE extends FtpCmd implements Runnable {
    String input;

    public CmdTYPE(SessionThread sessionThread, String input2) {
        super(sessionThread, CmdTYPE.class.toString());
        this.input = input2;
    }

    public void run() {
        String output;
        this.myLog.l(3, "TYPE executing");
        String param = getParameter(this.input);
        if (param.equals("I") || param.equals("L 8")) {
            output = "200 Binary type set\r\n";
            this.sessionThread.setBinaryMode(true);
        } else if (param.equals("A") || param.equals("A N")) {
            output = "200 ASCII type set\r\n";
            this.sessionThread.setBinaryMode(false);
        } else {
            output = "503 Malformed TYPE command\r\n";
        }
        this.sessionThread.writeString(output);
        this.myLog.l(3, "TYPE complete");
    }
}
