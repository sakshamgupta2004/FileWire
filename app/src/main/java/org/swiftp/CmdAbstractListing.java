package org.swiftp;

import java.io.File;

public abstract class CmdAbstractListing extends FtpCmd {
    protected static MyLog staticLog = new MyLog(CmdLIST.class.toString());

    /* access modifiers changed from: 0000 */
    public abstract String makeLsString(File file);

    public CmdAbstractListing(SessionThread sessionThread, String input) {
        super(sessionThread, CmdAbstractListing.class.toString());
    }

    public String listDirectory(StringBuilder response, File dir) {
        if (!dir.isDirectory()) {
            return "500 Internal error, listDirectory on non-directory\r\n";
        }
        this.myLog.l(3, "Listing directory: " + dir.toString());
        File[] entries = dir.listFiles();
        if (entries == null) {
            return "500 Couldn't list directory. Check config and mount status.\r\n";
        }
        this.myLog.l(3, "Dir len " + entries.length);
        for (File entry : entries) {
            String curLine = makeLsString(entry);
            if (curLine != null) {
                response.append(curLine);
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public String sendListing(String listing) {
        if (this.sessionThread.startUsingDataSocket()) {
            this.myLog.l(3, "LIST/NLST done making socket");
            this.sessionThread.writeString("150 Opening " + (this.sessionThread.isBinaryMode() ? "BINARY" : "ASCII") + " mode data connection for file list\r\n");
            this.myLog.l(3, "Sent code 150, sending listing string now");
            if (!this.sessionThread.sendViaDataSocket(listing)) {
                this.myLog.l(3, "sendViaDataSocket failure");
                this.sessionThread.closeDataSocket();
                return "426 Data socket or network error\r\n";
            }
            this.sessionThread.closeDataSocket();
            this.myLog.l(3, "Listing sendViaDataSocket success");
            this.sessionThread.writeString("226 Data transmission OK\r\n");
            return null;
        }
        this.sessionThread.closeDataSocket();
        return "425 Error opening data socket\r\n";
    }
}
