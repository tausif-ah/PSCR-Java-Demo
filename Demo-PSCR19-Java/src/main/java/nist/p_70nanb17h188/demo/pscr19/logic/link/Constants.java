package nist.p_70nanb17h188.demo.pscr19.logic.link;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import nist.p_70nanb17h188.demo.pscr19.logic.Device;

/**
 * Constants used in Link Layer.
 */
class Constants {

    static final int WIFI_DIRECT_SERVER_LISTEN_PORT = 10305;
    static final int BLUETOOTH_SERVER_LISTEN_PORT = 10307;
    static final InetSocketAddress WIFI_DIRECT_SERVER_SOCKET_ADDRESS;
    private static final HashSet<String> WIFI_DIRECT_GROUP_OWNERS = new HashSet<>(Arrays.asList(Device.NAME_M1, Device.NAME_ROUTER));

    static {
        InetSocketAddress tmp = null;
        try {
            tmp = new InetSocketAddress(
                    InetAddress.getByAddress(new byte[]{(byte) 192, (byte) 168, 49, 1}),
                    WIFI_DIRECT_SERVER_LISTEN_PORT);
        } catch (UnknownHostException e) {
            e.printStackTrace(System.err);
        }
        WIFI_DIRECT_SERVER_SOCKET_ADDRESS = tmp;
    }

    private Constants() {
    }

    static boolean isWifiDirectGroupOwner() {
        return WIFI_DIRECT_GROUP_OWNERS.contains(Device.getName());
    }
}
