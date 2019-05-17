package nist.p_70nanb17h188.demo.pscr19.logic.link;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Locale;
import static nist.p_70nanb17h188.demo.pscr19.logic.link.WifiTCPConnectionManager.checkValidSendDataParams;
import nist.p_70nanb17h188.demo.pscr19.logic.log.Log;

public abstract class WifiTCPConnectionManager {

    static final long SERVER_SOCKET_RETRY_DURATION_MS = 2000;
    static final long SOCKET_RCONNECT_DURATION_MS = 2000;
    private static WifiTCPConnectionManager DEFAULT_INSTANCE = null;
    @NonNull
    final Application application;
    @NonNull
    final Context context;
    @NonNull
    final Handler handler;

    WifiTCPConnectionManager(@NonNull Application application) {
        this.application = application;
        context = application.getApplicationContext();
        handler = new Handler(application.getApplicationContext().getMainLooper());
    }

    static void init(@NonNull Application application) {
        if (DEFAULT_INSTANCE == null) {
            DEFAULT_INSTANCE = new WifiTCPConnectionManagerClient(application);
        }
    }

    @Nullable
    public static WifiTCPConnectionManager getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    static void checkValidSendDataParams(@NonNull byte[] data, int start, int len) {
        if (len < 0 || start + len > data.length) {
            throw new IllegalArgumentException(String.format(Locale.US, "wrong start(%d) or len(%d) value, data.length=%d", start, len, data.length));
        }
    }

    public abstract boolean isDeviceTCPConnected(@NonNull String name);

    public abstract void modifyConnection(@NonNull String name, boolean establish);

    public abstract boolean sendData(@NonNull NeighborID id, @NonNull byte[] data, int start, int len);

}

class WifiTCPConnectionManagerClient extends WifiTCPConnectionManager implements TCPConnectionManager.SocketChannelEventHandler {

    private static final String TAG = "WifiTCPConnectionManagerClient";
    @Nullable
    private SocketChannel currentSocket;
    private boolean reconnect = false;
    private String connectedName;
    private InetSocketAddress address;
//    @NonNull
//    private final String[] wifiDirectNeighbors;

    WifiTCPConnectionManagerClient(@NonNull Application application) {
        super(application);
//        wifiDirectNeighbors = Constants.getWifiDirectNeighbors();

        // need to wait for group formation, and then connect to group owner
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || !WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(intent.getAction())) {
                    return;
                }
                WifiP2pInfo wifiP2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
                Log.v(TAG, "Connection Changed: %nwifiP2pInfo=%s", wifiP2pInfo);
                // has to be connection changed action
                reconnect = wifiP2pInfo.groupFormed;
                if (reconnect) {
                    address = new InetSocketAddress(wifiP2pInfo.groupOwnerAddress, Constants.WIFI_DIRECT_SERVER_LISTEN_PORT);
                    establishConnection();
                } else {
                    closeConnection();
                }
            }
        }, filter);
    }

    private void closeConnection() {
        synchronized (this) {
            if (currentSocket != null) {
                Log.v(TAG, "closeConnection, currentSocket=%s", currentSocket);
                TCPConnectionManager.getDefaultInstance().closeSocketChannel(currentSocket);
            } else {
                Log.v(TAG, "closeConnection, currentSocket == null");
            }
        }
    }

    private void establishConnection() {
        synchronized (this) {
            // If there is already a connection, do nothing. If it is a staled one, we can wait for it to timeout.
            // If we somehow decided to disconnect (reconnect == false) we also don't connect.
            Log.v(TAG, "establishConnection, currentSocket=%s, reconnect = %b", currentSocket, reconnect);

            if (currentSocket != null || !reconnect) {
                return;
            }
            TCPConnectionManager.getDefaultInstance().addSocketChannel(address, this);
        }
    }

    @Override
    public boolean isDeviceTCPConnected(@NonNull String name) {
        return name.equals(connectedName);
    }

    @Override
    public void modifyConnection(@NonNull String name, boolean establish) {
        reconnect = establish;
        if (establish) {
            // need to wait for the physical connection to establish, do nothing.
            Log.v(TAG, "Try to establish connection, name=%s", name);
        } else {
            Log.v(TAG, "Try to close connection, name=%s", name);
            closeConnection();
        }
    }

    @Override
    public boolean sendData(@NonNull NeighborID id, @NonNull byte[] data, int start, int len) {
        checkValidSendDataParams(data, start, len);
        synchronized (this) {
            if (!id.name.equals(connectedName)) {
                return false;
            }
            assert currentSocket != null;
            byte[] buf = new byte[len];
            if (len > 0) {
                System.arraycopy(data, start, buf, 0, len);
            }
            return TCPConnectionManager.getDefaultInstance().writeToSocket(currentSocket, buf);
        }
    }

    @Override
    public void onSocketConnected(@NonNull SocketChannel socketChannel) {
        // do nothing, wait for the other side to give me the name.
        Log.v(TAG, "Socket connected: %s", socketChannel);
    }

    @Override
    public void onSocketConnectFailed(@NonNull SocketChannel socketChannel) {
        Log.v(TAG, "onSocketConnectFailed: %s, reconnect=%b", socketChannel, reconnect);
        if (reconnect) {
            Log.i(TAG, "Connection failed on socketChannel %s, reconnect in %dms.", socketChannel, SOCKET_RCONNECT_DURATION_MS);
            handler.postDelayed(this::establishConnection, SOCKET_RCONNECT_DURATION_MS);
        }
    }

    @Override
    public void onSocketChannelNameReceived(@NonNull SocketChannel socketChannel, @NonNull String name) {
//        // if that is not my neighbor, disconnect.
//        int i;
//        for (i = 0; i < wifiDirectNeighbors.length; i++) {
//            if (name.equals(wifiDirectNeighbors[i])) {
//                break;
//            }
//        }
//        if (i == wifiDirectNeighbors.length) {
//            Log.i(TAG, "Connected device (%s) is not my neighbor, colse connection!", name);
//            TCPConnectionManager.getDefaultInstance().closeSocketChannel(socketChannel);
//            return;
//        }
        synchronized (this) {
            currentSocket = socketChannel;
            connectedName = name;
            Log.i(TAG, "Connected to %s, %s", name, socketChannel);
        }
        context.sendBroadcast(new Intent(LinkLayer.ACTION_LINK_CHANGED).
                putExtra(LinkLayer.EXTRA_NEIGHBOR_ID, new NeighborID(name)).
                putExtra(LinkLayer.EXTRA_CONNECTED, true));

    }

    @Override
    public void onSocketChannelClosed(@NonNull SocketChannel socketChannel) {
        // If I'm a client and I know who I am, and the user still wants to connect
        String name = connectedName;
        Log.v(TAG, "onSocketChannelClosed: %s, name=%s, reconnect=%b", socketChannel, name, reconnect);
        synchronized (this) {
            // not closing the current socket, ignore
            if (socketChannel != currentSocket) {
                return;
            }
            currentSocket = null;
            connectedName = null;
        }
        context.sendBroadcast(new Intent(LinkLayer.ACTION_LINK_CHANGED).
                putExtra(LinkLayer.EXTRA_NEIGHBOR_ID, new NeighborID(name)).
                putExtra(LinkLayer.EXTRA_CONNECTED, false));
        if (reconnect) {
            Log.i(TAG, "Closed a remote socket to %s, socketChannel=%s, reconnect in %dms", name, socketChannel, SOCKET_RCONNECT_DURATION_MS);
            handler.postDelayed(this::establishConnection, SOCKET_RCONNECT_DURATION_MS);
        } else {
            Log.i(TAG, "Closed a remote socket to %s socketChannel=%s", name, socketChannel);
        }

    }

    @Override
    public void onSocketChannelCloseFailed(@NonNull SocketChannel socketChannel) {
        Log.e(TAG, "Failed in closing a remote socket, socketChannel=%s", socketChannel);
    }

    @Override
    public void onSocketChannelDataReceived(@NonNull SocketChannel socketChannel, @NonNull byte[] data) {
        // from a neighbor that does not exist? how can that be?
        Log.d(TAG, "onSocketChannelDataReceived!");
        if (connectedName == null) {
            return;
        }
        application.getApplicationContext().sendBroadcast(new Intent(LinkLayer.ACTION_DATA_RECEIVED).putExtra(LinkLayer.EXTRA_NEIGHBOR_ID, new NeighborID(connectedName)).putExtra(LinkLayer.EXTRA_DATA, data));

    }

}
