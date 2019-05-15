package nist.p_70nanb17h188.demo.pscr19.logic.net;

import java.util.HashMap;
import java.util.HashSet;
import nist.p_70nanb17h188.demo.pscr19.logic.Device;
import nist.p_70nanb17h188.demo.pscr19.logic.link.NeighborID;

/**
 * An implementation of the Naming layer.
 */
public class NetLayer_Impl {

    private final HashMap<Name, HashSet<DataReceivedHandler>> dataHandlers = new HashMap<>();

    NetLayer_Impl() {
//        LinkLayer.addConnectionHandler(this::linkConnectionChanged);
//        LinkLayer.addDataReceivedHandler(this::linkDataReceived);
        System.out.printf("NetLayer_Impl on %s initialized!%n", Device.getName());
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
