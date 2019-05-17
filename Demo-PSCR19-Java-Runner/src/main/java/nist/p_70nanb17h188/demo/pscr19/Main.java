package nist.p_70nanb17h188.demo.pscr19;

import android.app.Application;
import android.os.Handler;
import java.net.UnknownHostException;
import nist.p_70nanb17h188.demo.pscr19.logic.link.TCPConnectionClient;
import nist.p_70nanb17h188.demo.pscr19.logic.link.TCPConnectionServer;

/**
 * Application that runs the demo.
 */
public class Main {

    public static final String TAG = "Main";


    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("S")) {
            TCPConnectionServer.startServer();
        } else {
            TCPConnectionClient.startClient();
        }
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
