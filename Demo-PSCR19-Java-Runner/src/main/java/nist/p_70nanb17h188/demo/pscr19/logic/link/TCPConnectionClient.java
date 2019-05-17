package nist.p_70nanb17h188.demo.pscr19.logic.link;

import android.app.Application;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import nist.p_70nanb17h188.demo.pscr19.logic.Device;
import nist.p_70nanb17h188.demo.pscr19.logic.log.Log;

/**
 *
 */
public class TCPConnectionClient {

    public static final String TAG = "TCPConnectionClient";

    public static void startClient() {
        Application application = new Application();
        Log.init(1, application);

        Log.d(TAG, "Existing names: %s%n", Arrays.toString(Device.getExistingNames()));
        Device.setName(Device.NAME_PC1);
        Log.d(TAG, "I am %s%n", Device.getName());

        InetAddress addr;
        try {
            addr = Inet4Address.getByAddress(new byte[]{127, 0, 0, 1});
        } catch (UnknownHostException ex) {
            Log.e(TAG, ex, "Error in setting addr!");
            return;
        }
        InetSocketAddress groupOwnerAddress = new InetSocketAddress(addr, 10305);

        LinkLayer.init(application);
//        NetLayer.init();
//        System.out.println("Initialized!");
        for (;;) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
    }

}
