package me.neznamy.tab.shared;

public enum ProtocolVersion {

	FUTURE		(999, "Future",		-1,	-1,	-1),
	BUNGEE		(999, "Bungee",		14,	0,	0),
	v1_14_4		(498, "1.14.4",		14, 16, 13),
	v1_14_3		(490, "1.14.3",		14, 16, 13),
	v1_14_2		(485, "1.14.2",		14, 16, 13),
	v1_14_1		(480, "1.14.1",		14, 16, 13),
	v1_14		(477, "1.14",		14, 16, 13),
	v1_13_2		(404, "1.13.2", 	13, 14, 11),
	v1_13_1		(401, "1.13.1", 	13, 14, 11),
	v1_13		(393, "1.13", 		13, 14, 11),
	v1_12_2		(340, "1.12.2", 	12, 14, 11),
	v1_12_1		(338, "1.12.1", 	12, 14, 11),
	v1_12		(335, "1.12", 		12, 14, 11),
	v1_11_2		(316, "1.11.2",		11, 14, 11),
	v1_11_1		(316, "1.11.1",		11, 14, 11),
	v1_11		(315, "1.11",		11, 14, 11),
	v1_10_2		(210, "1.10.2", 	10, 14, 11),
	v1_10_1		(210, "1.10.1", 	10, 14, 11),
	v1_10		(210, "1.10", 		10, 14, 11),
	v1_9_4		(110, "1.9.4", 		9,  14, 10),
	v1_9_3		(110, "1.9.3", 		9,  14, 10),
	v1_9_2		(109, "1.9.2", 		9,  14, 10),
	v1_9_1		(108, "1.9.1", 		9,  14, 10),
	v1_9		(107, "1.9", 		9,  14, 10),
	v1_8		(47,  "1.8.x", 		8,  0, 	10),
	v1_7_10		(5,   "1.7.10", 	7,  0, 	0),
	v1_7_9		(5,   "1.7.9", 		7,  0, 	0),
	v1_7_8		(5,   "1.7.8", 		7,  0, 	0),
	v1_7_7		(5,   "1.7.7", 		7,  0, 	0),
	v1_7_6		(5,   "1.7.6", 		7,  0, 	0),
	v1_7_5		(4,   "1.7.5", 		7,  0, 	0),
	v1_7_4		(4,   "1.7.4", 		7,  0, 	0),
	v1_7_2		(4,   "1.7.2", 		7,  0, 	0),
	v1_6_4		(78,  "1.6.4",		6,	0,	0),
	v1_6_2		(74,  "1.6.2",		6,	0,	0),
	v1_6_1		(73,  "1.6.1",		6,	0,	0),
	v1_5_2		(61,  "1.5.2",		5,	0,	0),
	v1_5_1		(60,  "1.5.1",		5,	0,	0),
	v1_5		(60,  "1.5",		5,	0,	0),
	v1_4_7		(51,  "1.4.7",		4,	0,	0),
	v1_4_6		(51,  "1.4.6",		4,	0,	0),
	v1_4_5		(49,  "1.4.5",		4,	0,	0),
	v1_4_4		(49,  "1.4.4",		4,	0,	0),
	v1_4_2		(47,  "1.4.2",		4,	0,	0),
	v1_3_2		(39,  "1.3.2",		3,	0,	0),
	v1_3_1		(39,  "1.3.1",		3,	0,	0),
	v1_2_5		(29,  "1.2.5",		2,	0,	0),
	v1_2_4		(29,  "1.2.4",		2,	0,	0),
	v1_2_3		(28,  "1.2.3",		2,	0,	0),
	v1_2_2		(28,  "1.2.2",		2,	0,	0),
	v1_2_1		(28,  "1.2.1",		2,	0,	0),
	v1_1		(23,  "1.1",		1,	0,	0),
	v1_0_1		(22,  "1.0.1",		0,	0,	0),
	v1_0_0		(22,  "1.0.0",		0,	0,	0),
	UNKNOWN		(-1,  "Unknown",   -1,	0,  0);
	
	public static ProtocolVersion SERVER_VERSION;
	
	private int networkId;
	private String friendlyName;
	private int minorVersion;
	private int petOwnerPosition;
	private int markerPosition;
	
	ProtocolVersion(int networkId, String friendlyName, int minorVersion, int petOwnerPosition, int markerPosition){
		this.networkId = networkId;
		this.friendlyName = friendlyName;
		this.minorVersion = minorVersion;
		this.petOwnerPosition = petOwnerPosition;
		this.markerPosition = markerPosition;
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
	public ProtocolVersion friendlyName(String name) {
		friendlyName = name;
		return this;
	}
	public ProtocolVersion minorVersion(int version) {
		minorVersion = version;
		return this;
	}
	public int getPetOwnerPosition() {
		return petOwnerPosition;
	}
	public int getMarkerPosition() {
		return markerPosition;
	}
	public static ProtocolVersion fromServerString(String s) {
		if (s.startsWith("1.8")) return v1_8;
		try {
			return valueOf("v" + s.replace(".", "_"));
		} catch (Throwable e) {
			return UNKNOWN.friendlyName(s).minorVersion(Integer.parseInt(s.split("\\.")[1]));
		}
	}
	public static ProtocolVersion fromNumber(int number) {
		for (ProtocolVersion v : values()) {
			if (number == v.getNetworkId()) return v;
		}
		if (number > v1_14_4.getNetworkId()) {
			return FUTURE;
		}
		return UNKNOWN;
	}
}