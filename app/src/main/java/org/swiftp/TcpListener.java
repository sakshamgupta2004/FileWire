package org.swiftp;

import com.sugarsnooper.filetransfer.ConnectToPC.FTP.FTPServerService;

import org.swiftp.SessionThread.Source;

import java.net.ServerSocket;
import java.net.Socket;

public class TcpListener extends Thread {
    FTPServerService ftpServerService;
    ServerSocket listenSocket;
    MyLog myLog = new MyLog(getClass().getName());

    public TcpListener(ServerSocket listenSocket2, FTPServerService ftpServerService2) {
        this.listenSocket = listenSocket2;
        this.ftpServerService = ftpServerService2;
    }

    public void quit() {
        try {
            this.listenSocket.close();
        } catch (Exception e) {
            this.myLog.l(3, "Exception closing TcpListener listenSocket");
        }
    }

    public void run() {
        while (true) {
            try {
                Socket clientSocket = this.listenSocket.accept();
                this.myLog.l(4, "New connection, spawned thread");
                SessionThread newSession = new SessionThread(clientSocket, new NormalDataSocketFactory(), Source.LOCAL);
                newSession.start();
                this.ftpServerService.registerSessionThread(newSession);
            } catch (Exception e) {
                this.myLog.l(3, "Exception in TcpListener");
                return;
            }
        }
    }
}
