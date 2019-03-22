package nist.p_70nanb17h188.demo.pscr19.net;

public interface DataReceivedHandler {

    /**
     * Called when a piece of data is received from a neighbor.
     *
     * @param src name of the sender.
     * @param dst name of the target.
     * @param data data received.
     */
    void dataReceived(Name src, Name dst, byte[] data);

}
