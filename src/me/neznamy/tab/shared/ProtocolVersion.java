package me.neznamy.tab.shared;

import me.neznamy.tab.platforms.bukkit.Main;

public enum ProtocolVersion {

	UNKNOWN		(999, "Unknown",0x4D),
	v1_16_20w16a(712, "20w16a",	0x4D),
	v1_15_2		(578, "1.15.2",	0x4C),
	v1_15_1		(575, "1.15.1",	0x4C),
	v1_15		(573, "1.15",	0x4C),
	v1_14_4		(498, "1.14.4",	0x4B),
	v1_14_3		(490, "1.14.3",	0x4B),
	v1_14_2		(485, "1.14.2",	0x4B),
	v1_14_1		(480, "1.14.1",	0x4B),
	v1_14		(477, "1.14",	0x4B),
	v1_13_2		(404, "1.13.2", 0x47),
	v1_13_1		(401, "1.13.1", 0x47),
	v1_13		(393, "1.13", 	0x47),
	v1_12_2		(340, "1.12.2", 0x44),
	v1_12_1		(338, "1.12.1", 0x44),
	v1_12		(335, "1.12", 	0x43),
	v1_11_2		(316, "1.11.2",	0x41),
	v1_11_1		(316, "1.11.1",	0x41),
	v1_11		(315, "1.11",	0x41),
	v1_10_2		(210, "1.10.2", 0x41),
	v1_10_1		(210, "1.10.1", 0x41),
	v1_10		(210, "1.10", 	0x41),
	v1_9_4		(110, "1.9.4", 	0x41),
	v1_9_3		(110, "1.9.3", 	0x41),
	v1_9_2		(109, "1.9.2", 	0x41),
	v1_9_1		(108, "1.9.1", 	0x41),
	v1_9		(107, "1.9", 	0x41),
	v1_8		(47,  "1.8.x", 	0x3E),
	v1_7_10		(5,   "1.7.10", 0x3E),
	v1_7_9		(5,   "1.7.9", 	0x3E),
	v1_7_8		(5,   "1.7.8", 	0x3E),
	v1_7_7		(5,   "1.7.7", 	0x3E),
	v1_7_6		(5,   "1.7.6", 	0x3E),
	v1_7_5		(4,   "1.7.5", 	0x3E),
	v1_7_4		(4,   "1.7.4", 	0x3E),
	v1_7_2		(4,   "1.7.2", 	0x3E),
	v1_6_4		(78,  "1.6.4"),
	v1_6_2		(74,  "1.6.2"),
	v1_6_1		(73,  "1.6.1"),
	v1_5_2		(61,  "1.5.2"),
	v1_5_1		(60,  "1.5.1"),
	v1_5		(60,  "1.5"),
	v1_4_7		(51,  "1.4.7"),
	v1_4_6		(51,  "1.4.6"),
	v1_4_5		(49,  "1.4.5"),
	v1_4_4		(49,  "1.4.4"),
	v1_4_2		(47,  "1.4.2"),
	v1_3_2		(39,  "1.3.2"),
	v1_3_1		(39,  "1.3.1"),
	v1_2_5		(29,  "1.2.5"),
	v1_2_4		(29,  "1.2.4"),
	v1_2_3		(28,  "1.2.3"),
	v1_2_2		(28,  "1.2.2"),
	v1_2_1		(28,  "1.2.1"),
	v1_1		(23,  "1.1"),
	v1_0_1		(22,  "1.0.1"),
	v1_0		(22,  "1.0");
	
	public static ProtocolVersion SERVER_VERSION;
	
	private int networkId;
	private String friendlyName;
	private int minorVersion;
	private int PacketPlayOutScoreboardTeamId;
	
	private ProtocolVersion(int networkId, String friendlyName){
		this(networkId, friendlyName, 0);
	}
	private ProtocolVersion(int networkId, String friendlyName, int PacketPlayOutScoreboardTeamId){
		this.networkId = networkId;
		this.friendlyName = friendlyName;
		this.PacketPlayOutScoreboardTeamId = PacketPlayOutScoreboardTeamId;
		if (toString().equals("UNKNOWN")) {
			try {
				minorVersion = Integer.parseInt(Main.serverPackage.split("_")[1]);
			} catch (Throwable t) {
				minorVersion = 15;
			}
		} else {
			minorVersion = Integer.parseInt(toString().split("_")[1]);
		}
	}
	public int getNetworkId() {
		return networkId;
	}
	public String getFriendlyName() {
		return friendlyName;
	}
	public int getMinorVersion() {
		return minorVersion;
	}
	public int getPacketPlayOutScoreboardTeamId() {
		return PacketPlayOutScoreboardTeamId;
	}
	public static ProtocolVersion fromServerString(String s) {
		if (s.startsWith("1.8")) return v1_8;
		try {
			return valueOf("v" + s.replace(".", "_"));
		} catch (Throwable e) {
			return UNKNOWN;
		}
	}
	public static ProtocolVersion fromNumber(int number) {
		for (ProtocolVersion v : values()) {
			if (number == v.getNetworkId()) return v;
		}
		return UNKNOWN;
	}
}