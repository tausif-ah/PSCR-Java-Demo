package nist.p_70nanb17h188.demo.pscr19.logic.link;

import android.app.Application;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import nist.p_70nanb17h188.demo.pscr19.logic.Device;
import nist.p_70nanb17h188.demo.pscr19.logic.Helper;
import nist.p_70nanb17h188.demo.pscr19.logic.log.Log;
import nist.p_70nanb17h188.demo.pscr19.logic.net.NetLayer;

/**
 *
 */
public class TCPConnectionClient {

    public static final String TAG = "TCPConnectionClient";

    public static void startClient() throws FileNotFoundException {
        PrintStream logOut = new PrintStream("log.txt");
        android.util.Log.redirectOutput(logOut);
        Application application = new Application();
        Log.init(1, application);

        Log.d(TAG, "Existing names: %s%n", Arrays.toString(Device.getExistingNames()));
        Device.setName(Device.NAME_PC1);
        Log.d(TAG, "I am %s%n", Device.getName());

        LinkLayer.init(application);
        NetLayer.init(application);
//        NetLayer.init();
//        System.out.println("Initialized!");

        System.out.println("Commands:");
        System.out.println("  w %id% %text%\t\tWrite text %text% to neighbor %id%");
        System.out.println("  r %id% %count%\tWrite ramdom contents with specified length %count% to a neighbor %id%");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                switch (line.charAt(0)) {
                    case 'w': {
                        line = line.substring(1).trim();
                        int idx = line.indexOf(' ');
                        String content;
                        NeighborID id;
                        if (idx < 0) {
                            id = Constants.getNeighborID(line);
                            content = "";
                        } else {
                            id = Constants.getNeighborID(line.substring(0, idx));
                            content = line.substring(idx).trim();
                        }
                        byte[] buf = content.getBytes();
                        boolean succeed = LinkLayer.sendData(id, buf, 0, buf.length);
                        Log.d(TAG, "Send to %s, text=%s, buf_len=%d, %s!", id.name, content, buf.length, succeed ? "succeeded" : "failed");
                    }
                    break;
                    case 'r':
                        line = line.substring(1).trim();
                        int idx = line.indexOf(' ');
                        if (idx < 0) {
                            System.out.println("Should have length!");
                            break;
                        }
                        NeighborID id = Constants.getNeighborID(line.substring(0, idx));
                        int size;
                        try {
                            size = Integer.parseInt(line.substring(idx).trim());
                        } catch (NumberFormatException e) {
                            System.out.println("Cannot parse size!");
                            break;
                        }
                        if (size < 0 || size > 4000) {
                            System.out.println("Size should be in [0,4000]");
                            break;
                        }
                        String content = size == 0 ? "" : Helper.getRandomString(size, size, Helper.CANDIDATE_CHARSET_LETTERS_NUMBERS);
                        byte[] buf = content.getBytes();
                        boolean succeed = LinkLayer.sendData(id, buf, 0, buf.length);
                        String result = succeed ? "succeeded" : "failed";
                        if (content.length() < 40) {
                            Log.d(TAG, "Send to %s, text=%s, buf_len=%d, %s!", id.name, content, buf.length, result);
                        } else {
                            Log.d(TAG, "Send to %s, text_len=%d, buf_len=%d, %s!", id.name, content.length(), buf.length, result);
                        }

                        break;
                    default:
                        System.out.println("Unknown command!");
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, ex, "exit from reader!");
        }
    }

}
