package nist.p_70nanb17h188.demo.pscr19.logic.link;

import nist.p_70nanb17h188.demo.pscr19.logic.Device;
import android.app.Application;
import android.os.Handler;
import android.support.annotation.NonNull;
import java.io.IOException;
import java.nio.channels.SocketChannel;
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
    private static final long DEFAULT_RECONNECT_DELAY_MS = 2000;
    @NonNull
    private final Handler handler;

    /**
     * Singleton pattern, prevent the class to be instantiated by the others.
     */
    LinkLayer_Impl_PC(@NonNull Application application) {
        TCPConnectionManager instance = TCPConnectionManager.init();
        handler = new Handler(application.getApplicationContext().getMainLooper());
        if (instance == null) {
            Log.e(TAG, "Failed in creating TCP Connection Manager!");
            return;
        }
        initConnection();
        Log.d("LinkLayer_Impl", "%s initialized", Device.getName());
    }

    void initConnection() {
        TCPConnectionManager.getDefaultInstance().addSocketChannel(Constants.WIFI_DIRECT_SERVER_SOCKET_ADDRESS, this);
    }

    public boolean sendData(@NonNull NeighborID id, @NonNull byte[] data, int start, int len) {
        // Do not forget to flush stream after each send, when in TCP
        return false;
    }

    @Override
    public void onSocketConnected(@NonNull SocketChannel socketChannel) {
        try {
            Log.v(TAG, "Socket channel connected! addr=%s", socketChannel.getRemoteAddress());
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    @Override
    public void onSocketConnectFailed(@NonNull SocketChannel socketChannel) {
        Log.v(TAG, "Socket channel connection failed! Retry in %dms", DEFAULT_RECONNECT_DELAY_MS);
        handler.postDelayed(this::initConnection, DEFAULT_RECONNECT_DELAY_MS);
    }

    @Override
    public void onSocketChannelClosed(@NonNull SocketChannel socketChannel) {
        Log.v(TAG, "Socket channel closed! Retry in %dms", DEFAULT_RECONNECT_DELAY_MS);
        handler.postDelayed(this::initConnection, DEFAULT_RECONNECT_DELAY_MS);
    }

    @Override
    public void onSocketChannelCloseFailed(@NonNull SocketChannel socketChannel) {
    }

    @Override
    public void onSocketChannelNameReceived(@NonNull SocketChannel socketChannel, @NonNull String name) {
    }

    @Override
    public void onSocketChannelDataReceived(@NonNull SocketChannel socketChannel, @NonNull byte[] data) {
    }
}
