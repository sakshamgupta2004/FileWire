package org.swiftp;

import java.net.InetAddress;
import java.net.Socket;

public class ProxyDataSocketFactory extends DataSocketFactory {
    InetAddress clientAddress;
    int clientPort;
    ProxyConnector proxyConnector;
    private int proxyListenPort;
    private Socket socket;

    public ProxyDataSocketFactory() {
        clearState();
    }

    private void clearState() {
        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (Exception e) {
            }
        }
        this.socket = null;
        this.proxyConnector = null;
        this.clientAddress = null;
        this.proxyListenPort = 0;
        this.clientPort = 0;
    }

    public InetAddress getPasvIp() {
        ProxyConnector pc = Globals.getProxyConnector();
        if (pc == null) {
            return null;
        }
        return pc.getProxyIp();
    }

    public int onPasv() {
        clearState();
        this.proxyConnector = Globals.getProxyConnector();
        if (this.proxyConnector == null) {
            this.myLog.l(4, "Unexpected null proxyConnector in onPasv");
            clearState();
            return 0;
        }
        ProxyDataSocketInfo info = this.proxyConnector.pasvListen();
        if (info == null) {
            this.myLog.l(4, "Null ProxyDataSocketInfo");
            clearState();
            return 0;
        }
        this.socket = info.getSocket();
        this.proxyListenPort = info.getRemotePublicPort();
        return this.proxyListenPort;
    }

    public boolean onPort(InetAddress dest, int port) {
        clearState();
        this.proxyConnector = Globals.getProxyConnector();
        this.clientAddress = dest;
        this.clientPort = port;
        this.myLog.d("ProxyDataSocketFactory client port settings stored");
        return true;
    }

    public Socket onTransfer() {
        if (this.proxyConnector == null) {
            this.myLog.w("Unexpected null proxyConnector in onTransfer");
            return null;
        } else if (this.socket == null) {
            if (this.proxyConnector == null) {
                this.myLog.l(4, "Unexpected null proxyConnector in onTransfer");
                return null;
            }
            this.socket = this.proxyConnector.dataPortConnect(this.clientAddress, this.clientPort);
            return this.socket;
        } else if (this.proxyConnector.pasvAccept(this.socket)) {
            return this.socket;
        } else {
            this.myLog.w("proxyConnector pasvAccept failed");
            return null;
        }
    }

    public void reportTraffic(long bytes) {
        ProxyConnector pc = Globals.getProxyConnector();
        if (pc == null) {
            this.myLog.d("Can't report traffic, null ProxyConnector");
        } else {
            pc.incrementProxyUsage(bytes);
        }
    }
}
