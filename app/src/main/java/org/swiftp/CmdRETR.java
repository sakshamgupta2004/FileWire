package org.swiftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CmdRETR extends FtpCmd implements Runnable {
    protected String input;

    public CmdRETR(SessionThread sessionThread, String input2) {
        super(sessionThread, CmdRETR.class.toString());
        this.input = input2;
    }

    public void run() {
        this.myLog.l(3, "RETR executing");
        String errString = null;
        File fileToRetr = inputPathToChrootedFile(this.sessionThread.getWorkingDir(), getParameter(this.input));
        if (violatesChroot(fileToRetr)) {
            errString = "550 Invalid name or chroot violation\r\n";
        } else if (fileToRetr.isDirectory()) {
            this.myLog.l(3, "Ignoring RETR for directory");
            errString = "550 Can't RETR a directory\r\n";
        } else if (!fileToRetr.exists()) {
            this.myLog.l(4, "Can't RETR nonexistent file: " + fileToRetr.getAbsolutePath());
            errString = "550 File does not exist\r\n";
        } else if (!fileToRetr.canRead()) {
            this.myLog.l(4, "Failed RETR permission (canRead() is false)");
            errString = "550 No read permissions\r\n";
        } else {
            try {
                FileInputStream in = new FileInputStream(fileToRetr);
                byte[] buffer = new byte[Defaults.getDataChunkSize()];
                if (this.sessionThread.startUsingDataSocket()) {
                    this.myLog.l(3, "RETR opened data socket");
                    this.sessionThread.writeString("150 Sending file\r\n");
                    if (!this.sessionThread.isBinaryMode()) {
                        this.myLog.l(3, "Transferring in ASCII mode");
                        boolean lastBufEndedWithCR = false;
                        while (true) {
                            int bytesRead = in.read(buffer);
                            if (bytesRead == -1) {
                                break;
                            }
                            int startPos = 0;
                            byte[] crnBuf = {13, 10};
                            int endPos = 0;
                            while (endPos < bytesRead) {
                                if (buffer[endPos] == 10) {
                                    this.sessionThread.sendViaDataSocket(buffer, startPos, endPos - startPos);
                                    if (endPos == 0) {
                                        if (!lastBufEndedWithCR) {
                                            this.sessionThread.sendViaDataSocket(crnBuf, 1);
                                        }
                                    } else if (buffer[endPos - 1] != 13) {
                                        this.sessionThread.sendViaDataSocket(crnBuf, 1);
                                    }
                                    startPos = endPos;
                                }
                                endPos++;
                            }
                            this.sessionThread.sendViaDataSocket(buffer, startPos, endPos - startPos);
                            if (buffer[bytesRead - 1] == 13) {
                                lastBufEndedWithCR = true;
                            } else {
                                lastBufEndedWithCR = false;
                            }
                        }
                    } else {
                        this.myLog.l(3, "Transferring in binary mode");
                        while (true) {
                            int bytesRead2 = in.read(buffer);
                            if (bytesRead2 != -1) {
                                if (!this.sessionThread.sendViaDataSocket(buffer, bytesRead2)) {
                                    errString = "426 Data socket error\r\n";
                                    this.myLog.l(4, "Data socket error");
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    }
                } else {
                    errString = "425 Error opening socket\r\n";
                    this.myLog.l(4, "Error in initDataSocket()");
                }
            } catch (FileNotFoundException e) {
                errString = "550 File not found\r\n";
            } catch (IOException e2) {
                errString = "425 Network error\r\n";
            }
        }
        this.sessionThread.closeDataSocket();
        if (errString != null) {
            this.sessionThread.writeString(errString);
        } else {
            this.sessionThread.writeString("226 Transmission finished\r\n");
        }
        this.myLog.l(3, "RETR done");
    }
}
