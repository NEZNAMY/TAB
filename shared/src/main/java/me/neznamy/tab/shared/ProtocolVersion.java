package me.neznamy.tab.shared;

/**
 * Enum class representing all possibly used protocol versions
 */
public enum ProtocolVersion {

	UNKNOWN	(999),
	V1_17_1	(756),
	V1_17	(755),
	V1_16_5 (754),
	V1_16_4	(754),
	V1_16_3	(753),
	V1_16_2	(751),
	V1_16_1	(736),
	V1_16	(735),
	V1_15_2	(578),
	V1_15_1	(575),
	V1_15	(573),
	V1_14_4	(498),
	V1_14_3	(490),
	V1_14_2	(485),
	V1_14_1	(480),
	V1_14	(477),
	V1_13_2	(404),
	V1_13_1	(401),
	V1_13	(393),
	V1_12_2	(340),
	V1_12_1	(338),
	V1_12	(335),
	V1_11_2	(316),
	V1_11_1	(316),
	V1_11	(315),
	V1_10_2	(210),
	V1_10_1	(210),
	V1_10	(210),
	V1_9_4	(110),
	V1_9_3	(110),
	V1_9_2	(109),
	V1_9_1	(108),
	V1_9	(107),
	V1_8	(47),
	V1_7_10	(5),
	V1_7_9	(5),
	V1_7_8	(5),
	V1_7_7	(5),
	V1_7_6	(5),
	V1_7_5	(4),
	V1_7_4	(4),
	V1_7_2	(4),
	V1_6_4	(78),
	V1_6_2	(74),
	V1_6_1	(73),
	V1_5_2	(61),
	V1_5_1	(60),
	V1_5	(60),
	V1_4_7	(51),
	V1_4_6	(51);

	//version's network id found at https://wiki.vg/Protocol_version_numbers
	private int networkId;
	
	//minor version, such as 16
	private int minorVersion;
	
	//friendly name displayed in %player-version% placeholder
	private String friendlyName;

	/**
	 * Constructs new instance with given network id
	 * @param networkId - network id of this version
	 */
	private ProtocolVersion(int networkId){
		this.networkId = networkId;
		if (toString().equals("UNKNOWN")) {
			this.minorVersion = 17;
			this.friendlyName = "Unknown";
		} else {
			this.minorVersion = Integer.parseInt(toString().split("_")[1]);
			this.friendlyName = toString().substring(1).replace("_", ".");
		}
	}

	/**
	 * Returns the vesion's network id
	 * @return version's network id
	 */
	public int getNetworkId() {
		return networkId;
	}

	/**
	 * Returns user-friendly name of the version (such as 1.16.2 instead of V1_16_2)
	 * @return
	 */
	public String getFriendlyName() {
		return friendlyName;
	}

	/**
	 * Returns minor version of this release, such as 8 for 1.8.x or 13 for 1.13.x
	 * @return version's minor version
	 */
	public int getMinorVersion() {
		return minorVersion;
	}

	/**
	 * Returns enum constant of entered version or UNKNOWN if unknown version
	 * @param friendlyName - friendly name of the version
	 * @return version or UNKNOWN if version is unknown
	 */
	public static ProtocolVersion fromFriendlyName(String friendlyName) {
		if (friendlyName.startsWith("1.8")) return V1_8;
		try {
			return valueOf("V" + friendlyName.replace(".", "_"));
		} catch (Exception e) {
			return UNKNOWN;
		}
	}

	/**
	 * Returns version from given network id
	 * @param networkId - network id of protocol version
	 * @return version from given network id
	 */
	public static ProtocolVersion fromNetworkId(int networkId) {
		for (ProtocolVersion v : values()) {
			if (networkId == v.getNetworkId()) return v;
		}
		return UNKNOWN;
	}
}