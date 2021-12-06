package org.swiftp;

import android.util.Log;

public class CmdPASS extends FtpCmd implements Runnable {
    String input;

    public CmdPASS(SessionThread sessionThread, String input2) {
        super(sessionThread, CmdPASS.class.toString());
        this.input = input2;
    }

    public void run() {
        this.myLog.l(3, "Executing PASS");
        Log.e("FTPConn", FtpCmd.getParameter(input));
        if (FtpCmd.getParameter(input).equals(Globals.getPassword()) || Globals.getPassword() == null) {
            if (Globals.isUserNameCorrect) {
                this.sessionThread.writeString("230 Access granted\r\n");
                this.sessionThread.authAttempt(true);
            }
            else{
                this.sessionThread.writeString("530 Invalid Password\r\n");
            }
        }
        else{
            Globals.isUserNameCorrect = false;
            this.sessionThread.writeString("530 Invalid Password\r\n");
        }
    }
}
