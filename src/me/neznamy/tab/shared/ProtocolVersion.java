package me.neznamy.tab.shared;

public enum ProtocolVersion {

	UNKNOWN(-1, "Unknown", 0),
	v1_7_2to5(4, "1.7.2-5", 7),
	v1_7_6to10(5, "1.7.6-10", 7),
	v1_8_x(47, "1.8.x", 8),
	v1_9(107, "1.9", 9),
	v1_9_1(108, "1.9.1", 9),
	v1_9_2(109, "1.9.2", 9),
	v1_9_3and4(110, "1.9.3/4", 9),
	v1_10_x(210, "1.10.x", 10),
	v1_11(315, "1.11", 11),
	v1_11_1and2(316, "1.11.1/2", 11),
	v1_12(335, "1.12", 12),
	v1_12_1(338, "1.12.1", 12),
	v1_12_2(340, "1.12.2", 12),
	v1_13(393, "1.13", 13),
	v1_13_1(401, "1.13.1", 13),
	v1_13_2(404, "1.13.2", 13),
	v1_14(477, "1.14", 14),
	v1_14_1(480, "1.14.1", 14),
	v1_14_2(485, "1.14.2", 14),
	v1_14_3(490, "1.14.3", 14),
	v1_14_4(498, "1.14.4", 14);
	
	public static ProtocolVersion SERVER_VERSION;
	public static String packageName;
	
	private int number;
	private String friendlyName;
	private int minorVersion;
	
	ProtocolVersion(int number, String friendlyName, int minorVersion){
		this.number = number;
		this.friendlyName = friendlyName;
		this.minorVersion = minorVersion;
	}
	public int getNumber() {
		return number;
	}
	public String getFriendlyName() {
		return friendlyName;
	}
	public int getMinorVersion() {
		return minorVersion;
	}
	public boolean isSupported() {
		return minorVersion >= 8;
	}
	public ProtocolVersion friendlyName(String name) {
		friendlyName = name;
		return this;
	}
	public ProtocolVersion minorVersion(int version) {
		minorVersion = version;
		return this;
	}
	public static ProtocolVersion fromServerString(String s) {
		if (s.startsWith("1.8")) return v1_8_x;
		if (s.startsWith("1.10")) return v1_10_x;
		if (s.equals("1.9.3") || s.equals("1.9.4")) return v1_9_3and4;
		if (s.equals("1.11.1") || s.equals("1.11.2")) return v1_11_1and2;
		try {
			return valueOf("v" + s.replace(".", "_"));
		} catch (Exception e) {
			return UNKNOWN.friendlyName(s).minorVersion(Integer.parseInt(s.split("\\.")[1]));
		}
	}
	public static ProtocolVersion fromNumber(int number) {
		for (ProtocolVersion v : values()) {
			if (number == v.getNumber()) return v;
		}
		return UNKNOWN;
	}
}