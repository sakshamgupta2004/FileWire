package org.swiftp;

import java.io.File;

public abstract class FtpCmd implements Runnable {
    protected static MyLog staticLog = new MyLog(FtpCmd.class.toString());
    protected MyLog myLog;
    protected SessionThread sessionThread;

    public abstract void run();

    private static FtpCmd getCmd(String cmd, SessionThread sessionThread2, String input) {
        if ("SYST".equals(cmd)) {
            return new CmdSYST(sessionThread2, input);
        }
        if ("USER".equals(cmd)) {
            return new CmdUSER(sessionThread2, input);
        }
        if ("PASS".equals(cmd)) {
            return new CmdPASS(sessionThread2, input);
        }
        if ("TYPE".equals(cmd)) {
            return new CmdTYPE(sessionThread2, input);
        }
        if ("CWD".equals(cmd)) {
            return new CmdCWD(sessionThread2, input);
        }
        if ("PWD".equals(cmd)) {
            return new CmdPWD(sessionThread2, input);
        }
        if ("LIST".equals(cmd)) {
            return new CmdLIST(sessionThread2, input);
        }
        if ("PASV".equals(cmd)) {
            return new CmdPASV(sessionThread2, input);
        }
        if ("RETR".equals(cmd)) {
            return new CmdRETR(sessionThread2, input);
        }
        if ("NLST".equals(cmd)) {
            return new CmdNLST(sessionThread2, input);
        }
        if ("NOOP".equals(cmd)) {
            return new CmdNOOP(sessionThread2, input);
        }
        if ("STOR".equals(cmd)) {
            return new CmdSTOR(sessionThread2, input);
        }
        if ("DELE".equals(cmd)) {
            return new CmdDELE(sessionThread2, input);
        }
        if ("RNFR".equals(cmd)) {
            return new CmdRNFR(sessionThread2, input);
        }
        if ("RNTO".equals(cmd)) {
            return new CmdRNTO(sessionThread2, input);
        }
        if ("RMD".equals(cmd)) {
            return new CmdRMD(sessionThread2, input);
        }
        if ("MKD".equals(cmd)) {
            return new CmdMKD(sessionThread2, input);
        }
        if ("OPTS".equals(cmd)) {
            return new CmdOPTS(sessionThread2, input);
        }
        if ("PORT".equals(cmd)) {
            return new CmdPORT(sessionThread2, input);
        }
        if ("QUIT".equals(cmd)) {
            return new CmdQUIT(sessionThread2, input);
        }
        if ("FEAT".equals(cmd)) {
            return new CmdFEAT(sessionThread2, input);
        }
        if ("SIZE".equals(cmd)) {
            return new CmdSIZE(sessionThread2, input);
        }
        if ("CDUP".equals(cmd)) {
            return new CmdCDUP(sessionThread2, input);
        }
        if ("APPE".equals(cmd)) {
            return new CmdAPPE(sessionThread2, input);
        }
        if ("XCUP".equals(cmd)) {
            return new CmdCDUP(sessionThread2, input);
        }
        if ("XPWD".equals(cmd)) {
            return new CmdPWD(sessionThread2, input);
        }
        if ("XMKD".equals(cmd)) {
            return new CmdMKD(sessionThread2, input);
        }
        if ("XRMD".equals(cmd)) {
            return new CmdRMD(sessionThread2, input);
        }
        return null;
    }

    public FtpCmd(SessionThread sessionThread2, String logName) {
        this.sessionThread = sessionThread2;
        this.myLog = new MyLog(logName);
    }

    protected static void dispatchCommand(SessionThread session, String inputString) {
        String[] strings = inputString.split(" ");
        String unrecognizedCmdMsg = "502 Command not recognized\r\n";
        if (strings == null) {
            String errString = "502 Command parse error\r\n";
            staticLog.l(4, errString);
            session.writeString(errString);
        } else if (strings.length < 1) {
            staticLog.l(4, "No strings parsed");
            session.writeString(unrecognizedCmdMsg);
        } else {
            String verb = strings[0];
            if (verb.length() < 1) {
                staticLog.l(4, "Invalid command verb");
                session.writeString(unrecognizedCmdMsg);
                return;
            }
            String verb2 = verb.trim().toUpperCase();
            FtpCmd cmdInstance = getCmd(verb2, session, inputString);
            if (cmdInstance == null) {
                staticLog.l(3, "Ignoring unrecognized FTP verb: " + verb2);
                session.writeString(unrecognizedCmdMsg);
            } else if (session.isAuthenticated() || cmdInstance.getClass().equals(CmdUSER.class) || cmdInstance.getClass().equals(CmdPASS.class)) {
                cmdInstance.run();
            } else {
                session.writeString("530 Login first with USER and PASS\r\n");
            }
        }
    }

    public static String getParameter(String input, boolean silent) {
        if (input == null) {
            return "";
        }
        int firstSpacePosition = input.indexOf(32);
        if (firstSpacePosition == -1) {
            return "";
        }
        String retString = input.substring(firstSpacePosition + 1).replaceAll("\\s+$", "");
        if (silent) {
            return retString;
        }
        staticLog.l(3, "Parsed argument: " + retString);
        return retString;
    }

    public static String getParameter(String input) {
        return getParameter(input, false);
    }

    public static File inputPathToChrootedFile(File existingPrefix, String param) {
        try {
            if (param.charAt(0) == '/') {
                return new File(Globals.getChrootDir(), param);
            }
        } catch (Exception e) {
        }
        return new File(existingPrefix, param);
    }

    public boolean violatesChroot(File file) {
        File chroot = Globals.getChrootDir();
        try {
            String canonicalPath = file.getCanonicalPath();
            if (canonicalPath.startsWith(chroot.toString())) {
                return false;
            }
            this.myLog.l(4, "Path violated folder restriction, denying");
            this.myLog.l(3, "path: " + canonicalPath);
            this.myLog.l(3, "chroot: " + chroot.toString());
            return true;
        } catch (Exception e) {
            this.myLog.l(4, "Path canonicalization problem: " + e.toString());
            this.myLog.l(4, "When checking file: " + file.getAbsolutePath());
            return true;
        }
    }
}
