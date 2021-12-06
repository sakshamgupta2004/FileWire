package org.swiftp;

public class CmdOPTS extends FtpCmd implements Runnable {
    public static final String message = "TEMPLATE!!";
    private String input;

    public CmdOPTS(SessionThread sessionThread, String input2) {
        super(sessionThread, CmdOPTS.class.toString());
        this.input = input2;
    }

    public void run() {
        String param = getParameter(this.input);
        String errString = null;
        if (param == null) {
            errString = "550 Need argument to OPTS\r\n";
            this.myLog.w("Couldn't understand empty OPTS command");
        } else {
            String[] splits = param.split(" ");
            if (splits.length != 2) {
                errString = "550 Malformed OPTS command\r\n";
                this.myLog.w("Couldn't parse OPTS command");
            } else {
                String optName = splits[0].toUpperCase();
                String optVal = splits[1].toUpperCase();
                if (!optName.equals("UTF8")) {
                    this.myLog.d("Unrecognized OPTS option: " + optName);
                    errString = "502 Unrecognized option\r\n";
                } else if (optVal.equals("ON")) {
                    this.myLog.d("Got OPTS UTF8 ON");
                    this.sessionThread.setEncoding("UTF-8");
                } else {
                    this.myLog.i("Ignoring OPTS UTF8 for something besides ON");
                }
            }
        }
        if (errString != null) {
            this.sessionThread.writeString(errString);
            this.myLog.i("Template log message");
            return;
        }
        this.sessionThread.writeString("200 OPTS accepted\r\n");
        this.myLog.d("Handled OPTS ok");
    }
}
