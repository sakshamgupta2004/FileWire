package org.swiftp;

public class CmdMap {
    protected Class<? extends FtpCmd> cmdClass;
    String name;

    public CmdMap(String name2, Class<? extends FtpCmd> cmdClass2) {
        this.name = name2;
        this.cmdClass = cmdClass2;
    }

    public Class<? extends FtpCmd> getCommand() {
        return this.cmdClass;
    }

    public void setCommand(Class<? extends FtpCmd> cmdClass2) {
        this.cmdClass = cmdClass2;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
    }
}
