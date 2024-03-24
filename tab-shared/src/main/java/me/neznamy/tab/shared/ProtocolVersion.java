package me.neznamy.tab.shared;

import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Enum class representing all possibly used protocol versions
 */
@Getter
public enum ProtocolVersion {

    UNKNOWN,
    V1_20_4 (765),
    V1_20_3 (765),
    V1_20_2 (764),
    V1_20_1 (763),
    V1_20   (763),
    V1_19_4 (762),
    V1_19_3 (761),
    V1_19_2 (760),
    V1_19_1 (760),
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

    /** Newest MC version this plugin jar knows */
    public static final ProtocolVersion LATEST_KNOWN_VERSION = V1_20_4;

    /** Version's network id found at <a href="https://wiki.vg/Protocol_version_numbers">wiki.vg</a> */
    private final int networkId;

    /** Version's minor version, such as 16 for 1.16.x. Allowing override to
     * set minor version of UNKNOWN_SERVER_VERSION value to value from package to fix compatibility
     * with server forks that set bukkit version field value to "Unknown".
     */
    private final int minorVersion;

    /** Version's friendly name displayed in %player-version% placeholder */
    @NotNull private final String friendlyName;

    /**
     * Constructs new instance with given network id
     *
     * @param   networkId
     *          network id of this version
     */
    ProtocolVersion(int networkId) {
        this.networkId = networkId;
        minorVersion = Integer.parseInt(toString().split("_")[1]);
        friendlyName = toString().substring(1).replace("_", ".");
    }

    /**
     * Constructs new instance.
     */
    ProtocolVersion() {
        networkId = 999;
        minorVersion = 99;
        friendlyName = "Unknown";
    }

    /**
     * Returns {@code true} if this version supports RGB codes,
     * {@code false} if not.
     *
     * @return {@code true} if supports, {@code false} if not
     */
    public boolean supportsRGB() {
        return minorVersion >= 16;
    }

    /**
     * Returns enum constant of entered version or UNKNOWN_SERVER_VERSION if unknown version
     *
     * @param   friendlyName
     *          friendly name of the version
     * @return  version or UNKNOWN_SERVER_VERSION if version is unknown
     */
    public static @NotNull ProtocolVersion fromFriendlyName(@NonNull String friendlyName) {
        if (friendlyName.startsWith("1.8")) return V1_8;
        try {
            return valueOf("V" + friendlyName.replace(".", "_"));
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    /**
     * Returns version from given network id
     *
     * @param   networkId
     *          network id of protocol version
     * @return  version from given network id
     */
    public static @NotNull ProtocolVersion fromNetworkId(int networkId) {
        for (ProtocolVersion v : values()) {
            if (networkId == v.networkId) return v;
        }
        return UNKNOWN;
    }
}