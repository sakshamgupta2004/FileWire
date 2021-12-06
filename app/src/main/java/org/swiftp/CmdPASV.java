package org.swiftp;

import java.net.InetAddress;

public class CmdPASV extends FtpCmd implements Runnable {
    public CmdPASV(SessionThread sessionThread, String input) {
        super(sessionThread, CmdPASV.class.toString());
    }

    public void run() {
        String cantOpen = "502 Couldn't open a port\r\n";
        this.myLog.l(3, "PASV running");
        int port = this.sessionThread.onPasv();
        if (port == 0) {
            this.myLog.l(6, "Couldn't open a port for PASV");
            this.sessionThread.writeString(cantOpen);
            return;
        }
        InetAddress addr = this.sessionThread.getDataSocketPasvIp();
        if (addr == null) {
            this.myLog.l(6, "PASV IP string invalid");
            this.sessionThread.writeString(cantOpen);
            return;
        }
        this.myLog.d("PASV sending IP: " + addr.getHostAddress());
        if (port < 1) {
            this.myLog.l(6, "PASV port number invalid");
            this.sessionThread.writeString(cantOpen);
            return;
        }
        StringBuilder response = new StringBuilder("227 Entering Passive Mode (");
        response.append(addr.getHostAddress().replace('.', ','));
        response.append(",");
        response.append(port / 256);
        response.append(",");
        response.append(port % 256);
        response.append(").\r\n");
        String responseString = response.toString();
        this.sessionThread.writeString(responseString);
        this.myLog.l(3, "PASV completed, sent: " + responseString);
    }
}
