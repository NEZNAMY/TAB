package me.neznamy.tab.api.protocol;

/**
 * Class representing a minecraft skin as a value - signature pair.
 */
public class Skin {

    /** Skin value */
    private final String value;

    /** Skin signature */
    private final String signature;

    /**
     * Constructs new instance with given parameters
     * @param   value
     *          skin value
     * @param   signature
     *          skin signature
     */
    public Skin(String value, String signature) {
        this.value = value;
        this.signature = signature;
    }

    /**
     * Returns skin value
     * @return  skin value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns skin signature
     * @return  skin signature
     */
    public String getSignature() {
        return signature;
    }
}
