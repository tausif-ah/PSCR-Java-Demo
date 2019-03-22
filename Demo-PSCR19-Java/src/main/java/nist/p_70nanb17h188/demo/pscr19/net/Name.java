package nist.p_70nanb17h188.demo.pscr19.net;

/**
 * A Name in the naming layer.
 *
 * Using long as representation. Multicast when value &lt; 0, unicast otherwise.
 *
 */
public class Name {

    private final long value;

    /**
     * Initiates a name with a value representation.
     *
     * @param value the value of the name.
     */
    public Name(long value) {
        this.value = value;
    }

    /**
     * Get the value representation of the name.
     *
     * @return the value representation.
     */
    public long getValue() {
        return value;
    }

    /**
     * Checks if the name is multicast name.
     *
     * @return if the name is multicast.
     */
    public boolean isMulticast() {
        return value < 0;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int) (this.value ^ (this.value >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Name other = (Name) obj;
        return this.value == other.value;
    }

    @Override
    public String toString() {
        return "Name{" + "value=" + value + '}';
    }

}
