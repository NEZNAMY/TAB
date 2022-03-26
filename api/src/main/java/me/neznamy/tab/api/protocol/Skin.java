package me.neznamy.tab.api.protocol;

import me.neznamy.tab.api.util.Preconditions;

/**
 * Class representing a minecraft skin as a value - signature pair.
 */
public class Skin {

    /** Skin value */
    private final String value;

    /** Skin signature, can be null */
    private final String signature;

    /**
     * Constructs new instance with given parameters
     *
     * @param   value
     *          skin value
     * @param   signature
     *          skin signature
     * @throws  IllegalArgumentException
     *          if {@code value} is {@code null}
     */
    public Skin(String value, String signature) {
        Preconditions.checkNotNull(value, "value");
        this.value = value;
        this.signature = signature;
    }

    /**
     * Returns skin value
     *
     * @return  skin value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns skin signature
     *
     * @return  skin signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Returns user-friendly representation of this object
     *
     * @return  user-friendly representation of this object
     */
    @Override
    public String toString() {
        return String.format("Skin{value=%s,signature=%s}", value, signature);
    }
}
