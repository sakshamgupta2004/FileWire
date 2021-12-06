package org.swiftp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public abstract class CmdAbstractStore extends FtpCmd {
    public static final String message = "TEMPLATE!!";

    public CmdAbstractStore(SessionThread sessionThread, String input) {
        super(sessionThread, CmdAbstractStore.class.toString());
    }

    public void doStorOrAppe(String param, boolean append) {
        this.myLog.l(3, "STOR/APPE executing with append=" + append);
        File storeFile = inputPathToChrootedFile(this.sessionThread.getWorkingDir(), param);
        String errString = null;
        FileOutputStream out = null;
        if (violatesChroot(storeFile)) {
            errString = "550 Invalid name or chroot violation\r\n";
        } else if (storeFile.isDirectory()) {
            errString = "451 Can't overwrite a directory\r\n";
        } else {
            try {
                if (storeFile.exists() && !append) {
                    if (!storeFile.delete()) {
                        errString = "451 Couldn't truncate file\r\n";
                    } else {
                        Util.deletedFileNotify(storeFile.getPath());
                    }
                }
                FileOutputStream out2 = new FileOutputStream(storeFile, append);
                if (this.sessionThread.startUsingDataSocket()) {
                    this.myLog.l(3, "Data socket ready");
                    this.sessionThread.writeString("150 Data socket ready\r\n");
                    byte[] buffer = new byte[Defaults.getDataChunkSize()];
                    if (this.sessionThread.isBinaryMode()) {
                        this.myLog.d("Mode is binary");
                    } else {
                        this.myLog.d("Mode is ascii");
                    }
                    while (true) {
                        int numRead = this.sessionThread.receiveFromDataSocket(buffer);
                        switch (numRead) {
                            case -2:
                                errString = "425 Could not connect data socket\r\n";
                                out = out2;
                                break;
                            case -1:
                                this.myLog.l(3, "Returned from final read");
                                out = out2;
                                break;
                            case 0:
                                errString = "426 Couldn't receive data\r\n";
                                out = out2;
                                break;
                            default:
                                try {
                                    if (this.sessionThread.isBinaryMode()) {
                                        out2.write(buffer, 0, numRead);
                                    } else {
                                        int startPos = 0;
                                        int endPos = 0;
                                        while (endPos < numRead) {
                                            if (buffer[endPos] == 13) {
                                                out2.write(buffer, startPos, endPos - startPos);
                                                startPos = endPos + 1;
                                            }
                                            endPos++;
                                        }
                                        if (startPos < numRead) {
                                            out2.write(buffer, startPos, endPos - startPos);
                                        }
                                    }
                                    out2.flush();
                                } catch (IOException e) {
                                    errString = "451 File IO problem. Device might be full.\r\n";
                                    this.myLog.d("Exception while storing: " + e);
                                    this.myLog.d("Message: " + e.getMessage());
                                    this.myLog.d("Stack trace: ");
                                    StackTraceElement[] traceElems = e.getStackTrace();
                                    int length = traceElems.length;
                                    for (int i = 0; i < length; i++) {
                                        this.myLog.d(traceElems[i].toString());
                                    }
                                    out = out2;
                                    break;
                                }
                        }
                    }
                } else {
                    errString = "425 Couldn't open data socket\r\n";
                    out = out2;
                }
            } catch (FileNotFoundException e2) {
                try {
                    errString = "451 Couldn't open file \"" + param + "\" aka \"" + storeFile.getCanonicalPath() + "\" for writing\r\n";
                } catch (IOException e3) {
                    errString = "451 Couldn't open file, nested exception\r\n";
                }
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e4) {
            }
        }
        if (errString != null) {
            this.myLog.l(4, "STOR error: " + errString.trim());
            this.sessionThread.writeString(errString);
        } else {
            this.sessionThread.writeString("226 Transmission complete\r\n");
            Util.newFileNotify(storeFile.getPath());
        }
        this.sessionThread.closeDataSocket();
        this.myLog.l(3, "STOR finished");
    }
}
