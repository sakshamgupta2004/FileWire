package org.swiftp;

import java.net.InetAddress;
import java.net.Socket;

public abstract class DataSocketFactory {
    protected MyLog myLog = new MyLog(getClass().getName());

    public abstract InetAddress getPasvIp();

    public abstract int onPasv();

    public abstract boolean onPort(InetAddress inetAddress, int i);

    public abstract Socket onTransfer();

    public abstract void reportTraffic(long j);
}
