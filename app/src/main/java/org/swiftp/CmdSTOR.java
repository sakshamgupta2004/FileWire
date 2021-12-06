package org.swiftp;

public class CmdSTOR extends CmdAbstractStore implements Runnable {
    protected String input;

    public CmdSTOR(SessionThread sessionThread, String input2) {
        super(sessionThread, CmdSTOR.class.toString());
        this.input = input2;
    }

    public void run() {
        doStorOrAppe(getParameter(this.input), false);
    }
}
