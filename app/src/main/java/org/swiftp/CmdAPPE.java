package org.swiftp;

public class CmdAPPE extends CmdAbstractStore implements Runnable {
    protected String input;

    public CmdAPPE(SessionThread sessionThread, String input2) {
        super(sessionThread, CmdAPPE.class.toString());
        this.input = input2;
    }

    public void run() {
        doStorOrAppe(getParameter(this.input), true);
    }
}
