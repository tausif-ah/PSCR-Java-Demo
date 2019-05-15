package android.content;

import android.os.Looper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Mimic the android Context class.
 */
public class Context {

    private final HashMap<String, HashSet<BroadcastReceiver>> broadcastReceivers = new HashMap<>();
    private Looper looper;

    public synchronized void sendBroadcast(Intent intent) {
        HashSet<BroadcastReceiver> receivers = broadcastReceivers.get(intent.getAction());
        if (receivers == null) {
            return;
        }
        receivers.forEach(receiver -> receiver.onReceive(this, intent));
    }

    public synchronized void registerReceiver(BroadcastReceiver receiver, IntentFilter intentFilter) {
        intentFilter.forEachAction(action -> {
            HashSet<BroadcastReceiver> receivers = broadcastReceivers.get(action);
            if (receivers == null) {
                broadcastReceivers.put(action, receivers = new HashSet<>());
            }
            receivers.add(receiver);
        });
    }

    public synchronized void unregisterReceiver(BroadcastReceiver receiver) {
        ArrayList<String> toRemove = new ArrayList<>();
        broadcastReceivers.forEach((action, receivers) -> {
            receivers.remove(receiver);
            if (receivers.isEmpty()) {
                toRemove.add(action);
            }
        });
        toRemove.forEach(action -> broadcastReceivers.remove(action));

    }

    public synchronized Looper getLooper() {
        if (looper == null) {
            looper = new Looper();
        }
        return looper;
    }
}
