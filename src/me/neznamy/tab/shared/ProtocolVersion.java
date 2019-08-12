package me.neznamy.tab.shared;

public enum ProtocolVersion {

	UNKNOWN(-1, "Unknown"),
	v1_8_x(47, "1.8.x"),
	v1_9(107, "1.9"),
	v1_9_1(108, "1.9.1"),
	v1_9_2(109, "1.9.2"),
	v1_9_3and4(110, "1.9.3/4"),
	v1_10_x(210, "1.10.x"),
	v1_11(315, "1.11"),
	v1_11_1and2(316, "1.11.1/2"),
	v1_12(335, "1.12"),
	v1_12_1(338, "1.12.1"),
	v1_12_2(340, "1.12.2"),
	v1_13(393, "1.13"),
	v1_13_1(401, "1.13.1"),
	v1_13_2(404, "1.13.2"),
	v1_14(477, "1.14"),
	v1_14_1(480, "1.14.1"),
	v1_14_2(485, "1.14.2"),
	v1_14_3(490, "1.14.3"),
	v1_14_4(498, "1.14.4");
	
	private int number;
	private String friendly;
	
	ProtocolVersion(int number, String friendly){
		this.number = number;
		this.friendly = friendly;
	}
	public boolean is1_9orNewer() {
		return number >= 107;
	}
	public boolean is1_13orNewer() {
		return number >= 393;
	}
	public boolean is1_14orNewer() {
		return number >= 477;
	}
	public int getNumber() {
		return number;
	}
	public String getFriendlyName() {
		return friendly;
	}
	public static ProtocolVersion fromString(String s) {
		if (s.startsWith("1.8")) return v1_8_x;
		if (s.equals("1.9.3") || s.equals("1.9.4")) return v1_9_3and4;
		if (s.equals("1.11.1") || s.equals("1.11.2")) return v1_11_1and2;
		return ProtocolVersion.valueOf("v" + s.replace(".", "_"));
	}
	public static ProtocolVersion fromNumber(int number) {
		for (ProtocolVersion v : ProtocolVersion.values()) {
			if (number == v.getNumber()) return v;
		}
		return null;
	}
}