package me.neznamy.tab.shared;

/**
 * Enum class representing all possibly used protocol versions
 */
public enum ProtocolVersion {

	UNKNOWN	(999),
	v1_16_4	(754),
	v1_16_3	(753),
	v1_16_2	(751),
	v1_16_1	(736),
	v1_16	(735),
	v1_15_2	(578),
	v1_15_1	(575),
	v1_15	(573),
	v1_14_4	(498),
	v1_14_3	(490),
	v1_14_2	(485),
	v1_14_1	(480),
	v1_14	(477),
	v1_13_2	(404),
	v1_13_1	(401),
	v1_13	(393),
	v1_12_2	(340),
	v1_12_1	(338),
	v1_12	(335),
	v1_11_2	(316),
	v1_11_1	(316),
	v1_11	(315),
	v1_10_2	(210),
	v1_10_1	(210),
	v1_10	(210),
	v1_9_4	(110),
	v1_9_3	(110),
	v1_9_2	(109),
	v1_9_1	(108),
	v1_9	(107),
	v1_8	(47),
	v1_7_10	(5),
	v1_7_9	(5),
	v1_7_8	(5),
	v1_7_7	(5),
	v1_7_6	(5),
	v1_7_5	(4),
	v1_7_4	(4),
	v1_7_2	(4),
	v1_6_4	(78),
	v1_6_2	(74),
	v1_6_1	(73),
	v1_5_2	(61),
	v1_5_1	(60),
	v1_5	(60),
	v1_4_7	(51),
	v1_4_6	(51);

	//server version, always using latest on proxies
	public static ProtocolVersion SERVER_VERSION;

	//version's network id found at https://wiki.vg/Protocol_version_numbers
	private int networkId;

	private ProtocolVersion(int networkId){
		this.networkId = networkId;
	}

	/**
	 * Returns the vesion's network id
	 * @return version's network id
	 */
	public int getNetworkId() {
		return networkId;
	}

	/**
	 * Returns user-friendly name of the version (such as 1.16.2 instead of v1_16_2)
	 * @return
	 */
	public String getFriendlyName() {
		if (this == UNKNOWN) return "Unknown";
		return toString().substring(1).replace("_", ".");
	}

	/**
	 * Returns minor version of this release, such as 8 for 1.8.x or 13 for 1.13.x
	 * @return version's minor version
	 */
	public int getMinorVersion() {
		if (this == UNKNOWN) return 16;
		return Integer.parseInt(toString().split("_")[1]);
	}

	/**
	 * Returns enum constant of entered version or UNKNOWN if unknown version
	 * @param friendlyName - friendly name of the version
	 * @return version or UNKNOWN if version is unknown
	 */
	public static ProtocolVersion fromFriendlyName(String friendlyName) {
		if (friendlyName.startsWith("1.8")) return v1_8;
		try {
			return valueOf("v" + friendlyName.replace(".", "_"));
		} catch (Throwable e) {
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