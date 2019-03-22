package nist.p_70nanb17h188.demo.pscr19.net;

/**
 * The Naming layer functions.
 */
public class NetLayer {

    private static NetLayer_Impl defaultInstance;

    /**
     * Initializes the network layer.
     *
     * Should be initiated after the link layer!
     */
    public static void init() {
        defaultInstance = new NetLayer_Impl();
    }

    /**
     * Subscribe to a name (either unicast or multicast).
     * 
     * @param n the name to subscribe to.
     * @param h the data handler when the name is received.
     * @return true if successfully added, false otherwise.
     */
    public static boolean subscribe(Name n, DataReceivedHandler h) {
        return defaultInstance.subscribe(n, h);
    }
    
    /**
     * Unsubscribe from a name (either unicast or multicast).
     * 
     * @param n the name to unsubscribe from.
     * @param h the data handler to remove.
     * @return true if successfully removed, false otherwise.
     */
    public static boolean unSubscribe(Name n, DataReceivedHandler h) {
        return defaultInstance.unSubscribe(n, h);
    }

    /**
     * Send a piece of data to a dst (either unicast or multicast).
     * 
     * @param src the source name.
     * @param dst the destination name.
     * @param data the data.
     * @param start the beginning of the data.
     * @param len the size of the data.
     * @return true if successfully sent.
     */
    public static boolean sendData(Name src, Name dst, byte[] data, int start, int len) {
        return defaultInstance.sendData(src, dst, data, start, len);
    }

    /**
     * Create or remove a name.
     * 
     * @param n the name to be created/removed.
     * @param add true of add name, false if remove.
     * @return if the operation is successful.
     */
    public static boolean registerName(Name n, boolean add) {
        return defaultInstance.registerName(n, add);
    }

    /**
     * Create or remove a relationship between 2 names.
     * 
     * @param parent the name of the parent.
     * @param child the name of the child.
     * @param add true if add relationship, false if remove.
     * @return if the operation is successful.
     */
    public static boolean registerRelationship(Name parent, Name child, boolean add) {
        return defaultInstance.registerRelationship(parent, child, add);
    }

    /**
     * This class should not be instantiated.
     */
    private NetLayer() {

    }
}
