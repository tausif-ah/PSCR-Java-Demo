package nist.p_70nanb17h188.demo.pscr19.logic.link;

import nist.p_70nanb17h188.demo.pscr19.logic.Device;
import android.app.Application;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.annotation.NonNull;
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
    private static final long DEFAULT_RECONNECT_DELAY_MS = 2000;

    /**
     * Singleton pattern, prevent the class to be instantiated by the others.
     */
    LinkLayer_Impl_PC(@NonNull Application application) {
        TCPConnectionManager instance = TCPConnectionManager.init();
        if (instance == null) {
            Log.e(TAG, "Failed in creating TCPConnectionManager!!");
        }
        WifiTCPConnectionManager.init(application);
        Log.d("LinkLayer_Impl", "%s initialized", Device.getName());
        WifiP2pInfo groupInfo = new WifiP2pInfo(true, false, Constants.WIFI_DIRECT_SERVER_ADDRESS);

        application.getApplicationContext().sendBroadcast(
                new Intent(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
                        .putExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO, groupInfo));
    }

    boolean sendData(@NonNull NeighborID id, @NonNull byte[] data, int start, int len) {
        // prefer Wifi over Bluetooth
        WifiTCPConnectionManager manager = WifiTCPConnectionManager.getDefaultInstance();
        if (manager == null) {
            return false;
        }
        return manager.sendData(id, data, start, len);
    }

}
