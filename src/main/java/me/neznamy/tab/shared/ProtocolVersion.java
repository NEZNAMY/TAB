package me.neznamy.tab.shared;

import me.neznamy.tab.platforms.bukkit.Main;

/**
 * Enum class representing all possibly used protocol versions
 */
public enum ProtocolVersion {

	UNKNOWN		(999, "Unknown"),
	v1_16_2		(751, "1.16.2"),
	v1_16_1		(736, "1.16.1"),
	v1_16		(735, "1.16"),
	v1_15_2		(578, "1.15.2"),
	v1_15_1		(575, "1.15.1"),
	v1_15		(573, "1.15"),
	v1_14_4		(498, "1.14.4"),
	v1_14_3		(490, "1.14.3"),
	v1_14_2		(485, "1.14.2"),
	v1_14_1		(480, "1.14.1"),
	v1_14		(477, "1.14"),
	v1_13_2		(404, "1.13.2"),
	v1_13_1		(401, "1.13.1"),
	v1_13		(393, "1.13"),
	v1_12_2		(340, "1.12.2"),
	v1_12_1		(338, "1.12.1"),
	v1_12		(335, "1.12"),
	v1_11_2		(316, "1.11.2"),
	v1_11_1		(316, "1.11.1"),
	v1_11		(315, "1.11"),
	v1_10_2		(210, "1.10.2"),
	v1_10_1		(210, "1.10.1"),
	v1_10		(210, "1.10"),
	v1_9_4		(110, "1.9.4"),
	v1_9_3		(110, "1.9.3"),
	v1_9_2		(109, "1.9.2"),
	v1_9_1		(108, "1.9.1"),
	v1_9		(107, "1.9"),
	v1_8		(47,  "1.8.x"),
	v1_7_10		(5,   "1.7.10"),
	v1_7_9		(5,   "1.7.9"),
	v1_7_8		(5,   "1.7.8"),
	v1_7_7		(5,   "1.7.7"),
	v1_7_6		(5,   "1.7.6"),
	v1_7_5		(4,   "1.7.5"),
	v1_7_4		(4,   "1.7.4"),
	v1_7_2		(4,   "1.7.2"),
	v1_6_4		(78,  "1.6.4"),
	v1_6_2		(74,  "1.6.2"),
	v1_6_1		(73,  "1.6.1"),
	v1_5_2		(61,  "1.5.2"),
	v1_5_1		(60,  "1.5.1"),
	v1_5		(60,  "1.5"),
	v1_4_7		(51,  "1.4.7"),
	v1_4_6		(51,  "1.4.6");

	public static ProtocolVersion SERVER_VERSION;

	//version's network id found at https://wiki.vg/Protocol_version_numbers
	private int networkId;

	//user-friendly name of the version
	private String friendlyName;

	//minor version of the release
	private int minorVersion;

	private ProtocolVersion(int networkId, String friendlyName){
		this.networkId = networkId;
		this.friendlyName = friendlyName;
		if (toString().equals("UNKNOWN")) {
			try {
				minorVersion = Integer.parseInt(Main.serverPackage.split("_")[1]);
			} catch (Throwable t) {
				minorVersion = 16;
			}
		} else {
			minorVersion = Integer.parseInt(toString().split("_")[1]);
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
	 * Returns user-friendly name of the version (such as 1.16.2 instead of v1_16_2)
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
	 * Returns enum constant of entered version or UNKNOWN if unknown server version
	 * @param serverString - friendly name of the version
	 * @return version or UNKNOWN if version is unknown
	 */
	public static ProtocolVersion fromServerString(String serverString) {
		if (serverString.startsWith("1.8")) return v1_8;
		try {
			return valueOf("v" + serverString.replace(".", "_"));
		} catch (Throwable e) {
			return UNKNOWN;
		}
	}

	/**
	 * Returns version from given network id
	 * @param number - network id of protocol version
	 * @return version from given network id
	 */
	public static ProtocolVersion fromNumber(int number) {
		for (ProtocolVersion v : values()) {
			if (number == v.getNetworkId()) return v;
		}
		return UNKNOWN;
	}
}