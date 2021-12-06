package org.swiftp;

import android.util.Log;

import com.sugarsnooper.filetransfer.ConnectToPC.FTP.FTPServerService;


public class MyLog {
    protected String tag;

    public MyLog(String tag2) {
        this.tag = tag2;
    }

    public void l(int level, String str, boolean sysOnly) {
        synchronized (MyLog.class) {
            String str2 = str.trim();
            if (level == 6 || level == 5) {
                Globals.setLastError(str2);
            }
            if (level >= Defaults.getConsoleLogLevel()) {
                Log.println(level, this.tag, str2);
            }
            if (!sysOnly && level >= Defaults.getUiLogLevel()) {
                FTPServerService.log(level, str2);
            }
        }
    }

    public void l(int level, String str) {
        l(level, str, false);
    }

    public void e(String s) {
        l(6, s, false);
    }

    public void w(String s) {
        l(5, s, false);
    }

    public void i(String s) {
        l(4, s, false);
    }

    public void d(String s) {
        l(3, s, false);
    }
}
