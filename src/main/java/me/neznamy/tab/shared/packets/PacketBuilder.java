package me.neznamy.tab.shared.packets;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.placeholders.Placeholders;

public interface PacketBuilder {

	public Object build(PacketPlayOutBoss packet, ProtocolVersion clientVersion) throws Exception;
	public Object build(PacketPlayOutChat packet, ProtocolVersion clientVersion) throws Exception;
	public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) throws Exception;
	public Object build(PacketPlayOutPlayerListHeaderFooter packet, ProtocolVersion clientVersion) throws Exception;
	public Object build(PacketPlayOutScoreboardDisplayObjective packet, ProtocolVersion clientVersion) throws Exception;
	public Object build(PacketPlayOutScoreboardObjective packet, ProtocolVersion clientVersion) throws Exception;
	public Object build(PacketPlayOutScoreboardScore packet, ProtocolVersion clientVersion) throws Exception;
	public Object build(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) throws Exception;

	public default String cutTo(String string, int length) {
		if (string == null || string.length() <= length) return string;
		if (string.charAt(length-1) == Placeholders.colorChar) {
			return string.substring(0, length-1); //cutting one extra character to prevent prefix ending with "&"
		} else {
			return string.substring(0, length);
		}
	}
}