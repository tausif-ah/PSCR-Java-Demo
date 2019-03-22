package nist.p_70nanb17h188.demo.pscr19;

import java.util.Arrays;
import nist.p_70nanb17h188.demo.pscr19.link.LinkLayer;
import nist.p_70nanb17h188.demo.pscr19.net.NetLayer;

/**
 * Application that runs the demo.
 */
public class Main {
    public static void main(String[] args) {
        System.out.printf("Existing names: %s%n", Arrays.toString(Device.getExistingNames()));
        Device.setName(Device.NAME_PC1);
        System.out.printf("I am %s%n", Device.getName());
        LinkLayer.init();
        NetLayer.init();
        System.out.println("Initialized!");
    }
    
}
