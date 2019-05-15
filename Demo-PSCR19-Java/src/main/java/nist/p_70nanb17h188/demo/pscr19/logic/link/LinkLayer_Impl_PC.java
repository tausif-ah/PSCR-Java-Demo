package nist.p_70nanb17h188.demo.pscr19.logic.link;

import nist.p_70nanb17h188.demo.pscr19.logic.Device;
import android.app.Application;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import nist.p_70nanb17h188.demo.pscr19.logic.log.Log;

/**
 * The Link layer implementation on PCs.
 *
 * Here, we don't have to worry about the WiFi status, assuming that the
 * connection is made by the user. We only need to establish a connection to
 * WiFi group master and keep trying it.
 */
final class LinkLayer_Impl_PC implements TCPConnectionManager.SocketChannelEventHandler {

    private static final String TAG = "LinkLayer_Impl_PC";

    private final InetSocketAddress groupOwnerAddress;

    /**
     * Singleton pattern, prevent the class to be instantiated by the others.
     */
    LinkLayer_Impl_PC(Application application, InetSocketAddress groupOwnerAddress) {
        System.out.printf("LinkLayer_Impl on %s initialized!%n", Device.getName());
        TCPConnectionManager instance = TCPConnectionManager.init();
        this.groupOwnerAddress = groupOwnerAddress;
        if (instance == null) {
            Log.e(TAG, "Failed in creating TCP Connection Manager!");
            return;
        }
        initConnection();
    }

    private void initConnection() {
        TCPConnectionManager.getDefaultInstance().addSocketChannel(groupOwnerAddress, this);
    }

    public boolean sendData(NeighborID id, byte[] data, int start, int len) {
        // Do not forget to flush stream after each send, when in TCP
        return false;
    }

    /**
     * Example function on forwarding a data to all the handlers.
     *
     * @param id neighbor id.
     * @param data data received.
     */
    private void onDataReceived(NeighborID id, byte[] data) {
//        Intent intent = new Intent();
//        intent.putExtra

//        for (DataReceivedHandler dataHandler : dataHandlers) {
//            byte[] toForward = new byte[data.length];
//            // make a copy and send, so that the users have the freedom to update the content.
//            System.arraycopy(data, 0, toForward, 0, data.length);
//            dataHandler.dataReceived(id, toForward);
//        }
//        dataHandlers.forEach(h -> {
//            byte[] toForward = new byte[data.length];
//            // make a copy and send, so that the users have the freedom to update the content.
//            System.arraycopy(data, 0, toForward, 0, data.length);
//            h.dataReceived(id, toForward);
//        });
    }

    @Override
    public void onSocketConnected(SocketChannel socketChannel) {
        try {
            Log.v(TAG, "Socket channel connected! addr=%s", socketChannel.getRemoteAddress());
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    @Override
    public void onSocketConnectFailed(SocketChannel socketChannel) {
        Log.v(TAG, "Socket channel connection failed!");
    }

    @Override
    public void onSocketChannelNameReceived(SocketChannel socketChannel, String name) {
    }

    @Override
    public void onSocketChannelDataReceived(SocketChannel socketChannel, byte[] data) {
    }

    @Override
    public void onSocketChannelClosed(SocketChannel socketChannel) {
        Log.v(TAG, "Socket channel closed!");
    }

    @Override
    public void onSocketChannelCloseFailed(SocketChannel socketChannel) {
    }
}
