package nist.p_70nanb17h188.demo.pscr19;

import android.app.Application;
import android.os.Handler;
import java.io.PrintStream;
import java.util.Arrays;
import nist.p_70nanb17h188.demo.pscr19.logic.link.LinkLayer;
import nist.p_70nanb17h188.demo.pscr19.logic.link.TCPConnectionClient;
import nist.p_70nanb17h188.demo.pscr19.logic.log.Log;
import nist.p_70nanb17h188.demo.pscr19.logic.net.NetLayer;

/**
 * Application that runs the demo.
 */
public class Main {

    public static final String TAG = "Main";

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("java Main %deviceName%");
            return;
        }

        PrintStream logOut = new PrintStream("log.txt");
        android.util.Log.redirectOutput(logOut);
        Log.init(1000);

        Device.setName(args[0]);
        Log.d(TAG, "Existing names: %s", Arrays.toString(Device.getExistingNames()));
        Log.d(TAG, "I am %s", Device.getName());

        LinkLayer.init();
        NetLayer.init();
        
        TCPConnectionClient.startClient();
    }

    public static void testLooper(Application application) throws InterruptedException {
        Handler h = new Handler(application.getApplicationContext().getMainLooper());
        System.out.printf("[%d] now%n", System.currentTimeMillis());
        h.postDelayed(() -> {
            System.out.printf("[%d] after delay 3s%n", System.currentTimeMillis());
        }, 3000);
        h.postDelayed(() -> {
            System.out.printf("[%d] after delay 2s%n", System.currentTimeMillis());
            h.post(() -> {
                System.out.printf("[%d] added immediately%n", System.currentTimeMillis());
            });
            h.postAt(() -> {
                System.out.printf("[%d] poste long before%n", System.currentTimeMillis());
            }, 0);
        }, 2000);

        Thread.sleep(5000);
    }

}
