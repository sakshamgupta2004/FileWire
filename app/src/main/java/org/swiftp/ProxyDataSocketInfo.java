package org.swiftp;

import java.net.Socket;

public class ProxyDataSocketInfo extends Socket {
    private int remotePublicPort;
    private Socket socket;

    public Socket getSocket() {
        return this.socket;
    }

    public void setSocket(Socket socket2) {
        this.socket = socket2;
    }

    public ProxyDataSocketInfo(Socket socket2, int remotePublicPort2) {
        this.remotePublicPort = remotePublicPort2;
        this.socket = socket2;
    }

    public int getRemotePublicPort() {
        return this.remotePublicPort;
    }

    public void setRemotePublicPort(int remotePublicPort2) {
        this.remotePublicPort = remotePublicPort2;
    }
}
