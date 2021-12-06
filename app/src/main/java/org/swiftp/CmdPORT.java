package org.swiftp;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class CmdPORT extends FtpCmd implements Runnable {
    String input;

    public CmdPORT(SessionThread sessionThread, String input2) {
        super(sessionThread, CmdPORT.class.toString());
        this.input = input2;
    }

    public void run() {
        this.myLog.l(3, "PORT running");
        String errString = null;
        String param = getParameter(this.input);
        if (!param.contains("|") || !param.contains("::")) {
            String[] substrs = param.split(",");
            if (substrs.length != 6) {
                errString = "550 Malformed PORT argument\r\n";
            } else {
                int i = 0;
                while (true) {
                    if (i >= substrs.length) {
                        byte[] ipBytes = new byte[4];
                        int i2 = 0;
                        while (i2 < 4) {
                            try {
                                int ipByteAsInt = Integer.parseInt(substrs[i2]);
                                if (ipByteAsInt >= 128) {
                                    ipByteAsInt -= 256;
                                }
                                ipBytes[i2] = (byte) ipByteAsInt;
                                i2++;
                            } catch (Exception e) {
                                errString = "550 Invalid PORT format: " + substrs[i2] + "\r\n";
                            }
                        }
                        try {
                            this.sessionThread.onPort(InetAddress.getByAddress(ipBytes), (Integer.parseInt(substrs[4]) * 256) + Integer.parseInt(substrs[5]));
                        } catch (UnknownHostException e2) {
                            errString = "550 Unknown host\r\n";
                        }
                    } else if (!substrs[i].matches("[0-9]+") || substrs[i].length() > 3) {
                        errString = "550 Invalid PORT argument: " + substrs[i] + "\r\n";
                    } else {
                        i++;
                    }
                }
            }
        } else {
            errString = "550 No IPv6 support, reconfigure your client\r\n";
        }
        if (errString == null) {
            this.sessionThread.writeString("200 PORT OK\r\n");
            this.myLog.l(3, "PORT completed");
            return;
        }
        this.myLog.l(4, "PORT error: " + errString);
        this.sessionThread.writeString(errString);
    }
}
