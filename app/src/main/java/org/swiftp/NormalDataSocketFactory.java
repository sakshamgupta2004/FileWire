package org.swiftp;

import com.sugarsnooper.filetransfer.ConnectToPC.FTP.FTPServerService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class NormalDataSocketFactory extends DataSocketFactory {
    boolean isPasvMode = true;
    InetAddress remoteAddr;
    int remotePort;
    ServerSocket server = null;

    public NormalDataSocketFactory() {
        clearState();
    }

    private void clearState() {
        if (this.server != null) {
            try {
                this.server.close();
            } catch (IOException e) {
            }
        }
        this.server = null;
        this.remoteAddr = null;
        this.remotePort = 0;
        this.myLog.l(3, "NormalDataSocketFactory state cleared");
    }

    public int onPasv() {
        int z = 0;
        clearState();
        try {
            this.server = new ServerSocket(0, 5);
            this.myLog.l(3, "Data socket pasv() listen successful");
            return this.server.getLocalPort();
        } catch (IOException e) {
            this.myLog.l(6, "Data socket creation error");
            clearState();
            return z;
        }
    }

    public boolean onPort(InetAddress remoteAddr2, int remotePort2) {
        clearState();
        this.remoteAddr = remoteAddr2;
        this.remotePort = remotePort2;
        return true;
    }

    public Socket onTransfer() {
        Socket socket;
        if (this.server != null) {
            try {
                socket = this.server.accept();
                this.myLog.l(3, "onTransfer pasv accept successful");
            } catch (Exception e) {
                this.myLog.l(4, "Exception accepting PASV socket");
                socket = null;
            }
            clearState();
            return socket;
        } else if (this.remoteAddr == null || this.remotePort == 0) {
            this.myLog.l(4, "PORT mode but not initialized correctly");
            clearState();
            return null;
        } else {
            try {
                Socket socket2 = new Socket(this.remoteAddr, this.remotePort);
                try {
                    socket2.setSoTimeout(Defaults.SO_TIMEOUT_MS);
                    return socket2;
                } catch (Exception e2) {
                    this.myLog.l(6, "Couldn't set SO_TIMEOUT");
                    clearState();
                    return null;
                }
            } catch (IOException e3) {
                this.myLog.l(4, "Couldn't open PORT data socket to: " + this.remoteAddr.toString() + ":" + this.remotePort);
                clearState();
                return null;
            }
        }
    }

    public int getPortNumber() {
        if (this.server != null) {
            return this.server.getLocalPort();
        }
        return -1;
    }

    public InetAddress getPasvIp() {
        return FTPServerService.getWifiIp();
    }

    public void reportTraffic(long bytes) {
    }
}
