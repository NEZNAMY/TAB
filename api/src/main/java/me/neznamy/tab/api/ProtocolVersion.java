package me.neznamy.tab.api;

/**
 * Enum class representing all possibly used protocol versions
 */
public enum ProtocolVersion {

    UNKNOWN_SERVER_VERSION ("Unknown"),
    UNKNOWN_CLIENT_VERSION ("Unknown"),
    PROXY   ("Proxy"),
    V1_19   (759),
    V1_18_2 (758),
    V1_18_1 (757),
    V1_18   (757),
    V1_17_1 (756),
    V1_17   (755),
    V1_16_5 (754),
    V1_16_4 (754),
    V1_16_3 (753),
    V1_16_2 (751),
    V1_16_1 (736),
    V1_16   (735),
    V1_15_2 (578),
    V1_15_1 (575),
    V1_15   (573),
    V1_14_4 (498),
    V1_14_3 (490),
    V1_14_2 (485),
    V1_14_1 (480),
    V1_14   (477),
    V1_13_2 (404),
    V1_13_1 (401),
    V1_13   (393),
    V1_12_2 (340),
    V1_12_1 (338),
    V1_12   (335),
    V1_11_2 (316),
    V1_11_1 (316),
    V1_11   (315),
    V1_10_2 (210),
    V1_10_1 (210),
    V1_10   (210),
    V1_9_4  (110),
    V1_9_3  (110),
    V1_9_2  (109),
    V1_9_1  (108),
    V1_9    (107),
    V1_8    (47),
    V1_7_10 (5),
    V1_7_9  (5),
    V1_7_8  (5),
    V1_7_7  (5),
    V1_7_6  (5),
    V1_7_5  (4),
    V1_7_4  (4),
    V1_7_2  (4),
    V1_6_4  (78),
    V1_6_2  (74),
    V1_6_1  (73),
    V1_5_2  (61),
    V1_5_1  (60),
    V1_5    (60),
    V1_4_7  (51),
    V1_4_6  (51);

    /** Value array to iterate over to avoid array creations on each call */
    public static final ProtocolVersion[] VALUES = values();

    /** Version's network id found at https://wiki.vg/Protocol_version_numbers */
    private final int networkId;

    /** Version's minor version, such as 16 for 1.16.x */
    private int minorVersion;

    /** Version's friendly name displayed in %player-version% placeholder */
    private final String friendlyName;

    /**
     * Constructs new instance with given network id
     *
     * @param   networkId
     *          network id of this version
     */
    ProtocolVersion(int networkId){
        this.networkId = networkId;
        this.minorVersion = Integer.parseInt(toString().split("_")[1]);
        this.friendlyName = toString().substring(1).replace("_", ".");
    }

    /**
     * Constructs new instance with given friendly name
     *
     * @param   friendlyName
     *          friendly name to display
     */
    ProtocolVersion(String friendlyName){
        this.networkId = 999;
        this.minorVersion = 18;
        this.friendlyName = friendlyName;
    }

    /**
     * Returns the version's network id
     *
     * @return  version's network id
     */
    public int getNetworkId() {
        return networkId;
    }

    /**
     * Returns user-friendly name of the version (such as 1.16.2 instead of V1_16_2)
     *
     * @return  user-friendly name of the version
     */
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * Returns minor version of this release, such as 8 for 1.8.x or 13 for 1.13.x
     *
     * @return  version's minor version
     */
    public int getMinorVersion() {
        return minorVersion;
    }

    /**
     * Overrides minor version of the version instance. This is used internally to
     * set minor version of UNKNOWN_SERVER_VERSION value to value from package to fix compatibility
     * with server forks that set bukkit version field value to "Unknown".
     *
     * @param   minorVersion
     *          minor version to change value to
     */
    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    /**
     * Returns enum constant of entered version or UNKNOWN_SERVER_VERSION if unknown version
     *
     * @param   friendlyName
     *          friendly name of the version
     * @return  version or UNKNOWN_SERVER_VERSION if version is unknown
     */
    public static ProtocolVersion fromFriendlyName(String friendlyName) {
        if (friendlyName.startsWith("1.8")) return V1_8;
        try {
            return valueOf("V" + friendlyName.replace(".", "_"));
        } catch (IllegalArgumentException e) {
            return UNKNOWN_SERVER_VERSION;
        }
    }

    /**
     * Returns version from given network id
     *
     * @param   networkId
     *          network id of protocol version
     * @return  version from given network id
     */
    public static ProtocolVersion fromNetworkId(int networkId) {
        for (ProtocolVersion v : VALUES) {
            if (networkId == v.getNetworkId()) return v;
        }
        return UNKNOWN_CLIENT_VERSION;
    }
}