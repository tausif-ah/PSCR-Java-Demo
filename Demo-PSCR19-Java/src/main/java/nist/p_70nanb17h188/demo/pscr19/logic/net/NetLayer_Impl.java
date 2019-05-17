package nist.p_70nanb17h188.demo.pscr19.logic.net;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import java.util.HashMap;
import java.util.HashSet;
import nist.p_70nanb17h188.demo.pscr19.logic.Device;
import nist.p_70nanb17h188.demo.pscr19.logic.link.LinkLayer;
import nist.p_70nanb17h188.demo.pscr19.logic.link.NeighborID;
import nist.p_70nanb17h188.demo.pscr19.logic.log.Log;

/**
 * An implementation of the Naming layer.
 */
public class NetLayer_Impl {

    private static final String TAG = "NetLayer_Impl";
    public static final String ACTION_NEIGHBOR_CHANGED = "nist.p_70nanb17h188.demo.pscr19.logic.net.neighborChanged";

    private final HashMap<Name, HashSet<DataReceivedHandler>> dataHandlers = new HashMap<>();

    private static NetLayer_Impl defaultInstance;
    @NonNull
    private final Application application;
    private final HashSet<NeighborID> connectedNeighbors = new HashSet<>();

    NetLayer_Impl(@NonNull Application application) {
        this.application = application;
        defaultInstance = this;

        Context context = application.getApplicationContext();
        IntentFilter filter = new IntentFilter();
        filter.addAction(LinkLayer.ACTION_LINK_CHANGED);
        filter.addAction(LinkLayer.ACTION_DATA_RECEIVED);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() == null) {
                    return;
                }
                switch (intent.getAction()) {
                    case LinkLayer.ACTION_LINK_CHANGED:
                        onLinkChanged(intent);
                        break;
                    case LinkLayer.ACTION_DATA_RECEIVED:
                        onDataReceived(intent);
                        break;
                }
            }
        }, filter);//        LinkLayer.addConnectionHandler(this::linkConnectionChanged);
//        LinkLayer.addDataReceivedHandler(this::linkDataReceived);
        System.out.printf("NetLayer_Impl on %s initialized!%n", Device.getName());
    }

    public static NetLayer_Impl getDefaultInstance() {
        return defaultInstance;
    }

    private void onLinkChanged(Intent intent) {
        NeighborID neighborID = intent.getParcelableExtra(LinkLayer.EXTRA_NEIGHBOR_ID);
        boolean connected = intent.getBooleanExtra(LinkLayer.EXTRA_CONNECTED, false);
        boolean changed;
        if (connected) {
            changed = connectedNeighbors.add(neighborID);
        } else {
            changed = connectedNeighbors.remove(neighborID);
        }
        if (changed) {
            application.getApplicationContext().sendBroadcast(new Intent(ACTION_NEIGHBOR_CHANGED));
        }
    }

    public NeighborID[] getConnectNeighbors() {
        return connectedNeighbors.toArray(new NeighborID[0]);
    }

    private void onDataReceived(Intent intent) {
        NeighborID neighborID = intent.getParcelableExtra(LinkLayer.EXTRA_NEIGHBOR_ID);
        byte[] data = intent.getByteArrayExtra(LinkLayer.EXTRA_DATA);
        String str = new String(data);
        if (data.length < 40) {
            Log.d(TAG, "Got from %s, text=%s, buf_size=%d", neighborID.name, str, data.length);
        } else {
            Log.d(TAG, "Got from %s, text_len=%d, buf_size=%d", neighborID.name, str.length(), data.length);
        }
    }

    public boolean subscribe(Name n, DataReceivedHandler h) {
        HashSet<DataReceivedHandler> handlers = dataHandlers.get(n);
        if (handlers == null) {
            // new subscription
            dataHandlers.put(n, handlers = new HashSet<>());
            if (n.isMulticast()) {
                // send subscription upstream.
            } else {
                // send GNRS binding upstream.
            }
        }
        return handlers.add(h);
    }

    public boolean unSubscribe(Name n, DataReceivedHandler h) {
        HashSet<DataReceivedHandler> handlers = dataHandlers.get(n);
        if (handlers == null || !handlers.remove(h)) {
            return false;
        }
        // no application subscribing
        if (handlers.isEmpty()) {
            if (n.isMulticast()) {
                // send unsubscription upstream.
            } else {
                // send GNRS unbinding upstream.
            }
        }
        return true;
    }

    public boolean sendData(Name src, Name dst, byte[] data, int start, int len) {
        return false;
    }

    public boolean registerName(Name n, boolean add) {
        return false;
    }

    public boolean registerRelationship(Name parent, Name child, boolean add) {
        return false;
    }

    private void linkConnectionChanged(NeighborID id, boolean connected) {
        System.out.printf("Connection to %s %s!%n", id, connected ? "Established" : "Lost");
    }

    private void linkDataReceived(NeighborID id, byte[] data) {
        System.out.printf("Received data from %s, len=%d%n", id, data.length);
        // need to create a buffer for each neighbor, and extract the "packets" out of it.
    }

}
