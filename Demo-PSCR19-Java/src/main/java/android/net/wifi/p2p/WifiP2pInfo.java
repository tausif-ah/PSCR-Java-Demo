package android.net.wifi.p2p;

import java.net.InetAddress;

/**
 * Mimic the android WifiP2pInfo class.
 */
public class WifiP2pInfo {
    /** Indicates if a p2p group has been successfully formed */
    public boolean groupFormed;

    /** Indicates if the current device is the group owner */
    public boolean isGroupOwner;

    /** Group owner address */
    public InetAddress groupOwnerAddress;

    public WifiP2pInfo(boolean groupFormed, boolean isGroupOwner, InetAddress groupOwnerAddress) {
        this.groupFormed = groupFormed;
        this.isGroupOwner = isGroupOwner;
        this.groupOwnerAddress = groupOwnerAddress;
    }

    @Override
    public String toString() {
        return "WifiP2pInfo{" + "groupFormed=" + groupFormed + ", isGroupOwner=" + isGroupOwner + ", groupOwnerAddress=" + groupOwnerAddress + '}';
    }
    
    

    
}
