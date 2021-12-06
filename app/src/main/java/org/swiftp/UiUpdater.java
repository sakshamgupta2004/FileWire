package org.swiftp;

import android.os.Handler;
import java.util.ArrayList;
import java.util.List;

public class UiUpdater {
    protected static List<Handler> clients = new ArrayList();
    protected static MyLog myLog = new MyLog("UiUpdater");

    public static void registerClient(Handler client) {
        if (!clients.contains(client)) {
            clients.add(client);
        }
    }

    public static void unregisterClient(Handler client) {
        while (clients.contains(client)) {
            clients.remove(client);
        }
    }

    public static void updateClients() {
        for (Handler client : clients) {
            client.sendEmptyMessage(0);
        }
    }
}
