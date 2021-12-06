package org.swiftp;

import android.content.Context;
import android.util.Log;

public class CmdUSER extends FtpCmd implements Runnable {
    protected String input;
    protected Context context;

    public CmdUSER(SessionThread sessionThread, String input2) {
        super(sessionThread, CmdUSER.class.toString());
        this.input = input2;
    }

    public void run() {
        this.myLog.l(3, "USER executing");
        String username = FtpCmd.getParameter(this.input);
        Log.e("FTPCONN_UserName", username);
        if (Globals.getUsername() != null) {
            if (!username.equals(Globals.getUsername())) {
                this.sessionThread.writeString("331 Send password\r\n");
                return;
            }
        }
        Globals.isUserNameCorrect = true;
        this.sessionThread.writeString("331 Send password\r\n");
        this.sessionThread.account.setUsername(username);
    }
}
