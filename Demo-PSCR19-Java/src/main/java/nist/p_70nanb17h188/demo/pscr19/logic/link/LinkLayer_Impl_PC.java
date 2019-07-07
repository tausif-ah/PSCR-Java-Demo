package nist.p_70nanb17h188.demo.pscr19.logic.link;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import nist.p_70nanb17h188.demo.pscr19.Device;
import android.support.annotation.NonNull;
import java.io.IOException;
import nist.p_70nanb17h188.demo.pscr19.MyApplication;
import nist.p_70nanb17h188.demo.pscr19.imc.DelayRunner;
import nist.p_70nanb17h188.demo.pscr19.logic.log.Log;

/**
 * The Link layer implementation on PCs.
 *
 * Here, we don't have to worry about the WiFi status, assuming that the
 * connection is made by the user. We only need to establish a connection to
 * WiFi group master and keep trying it.
 */
final class LinkLayer_Impl_PC {

    private static final String TAG = "LinkLayer_Impl_PC";

    //@NonNull
    //private final TCPConnectionManager tcpConnectionManager;
    @NonNull
    private final ThreadTCPConnectionManager threadTCPConnectionManager;
    //@NonNull
    //private final WifiTCPConnectionManager wifiTCPConnectionManager;
    @NonNull
    private final WifiThreadTCPConnectionManager wifiThreadTCPConnectionManager;

    /**
     * Singleton pattern, prevent the class to be instantiated by the others.
     */
    LinkLayer_Impl_PC() {
        //tcpConnectionManager = new TCPConnectionManager();
        threadTCPConnectionManager = new ThreadTCPConnectionManager();
        //tcpConnectionManager.start();
        threadTCPConnectionManager.start();
        //wifiTCPConnectionManager = WifiTCPConnectionManager.createWifiTCPConnectionManager(tcpConnectionManager);
        wifiThreadTCPConnectionManager = WifiThreadTCPConnectionManager.createWifiTCPConnectionManager(threadTCPConnectionManager);

        DelayRunner.getDefaultInstance().postDelayed(1000, () -> {
            WifiP2pInfo groupInfo = new WifiP2pInfo(true, false, Constants.WIFI_DIRECT_SERVER_ADDRESS);

            MyApplication.getDefaultInstance().getApplicationContext().sendBroadcast(
                    new Intent(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
                            .putExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO, groupInfo));

        });
        Log.d(TAG, "%s initialized", Device.getName());
    }

    boolean sendData(@NonNull NeighborID id, @NonNull byte[] data, int start, int len) {
        // prefer Wifi over Bluetooth
        //return wifiTCPConnectionManager.sendData(id, data, start, len);
        return wifiThreadTCPConnectionManager.sendData(id, data, start, len);
    }

}
