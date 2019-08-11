package me.neznamy.tab.shared.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import me.neznamy.tab.bukkit.packets.EnumConstant;
import me.neznamy.tab.shared.Shared;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardObjective.HealthDisplay;

public class PacketPlayOutScoreboardObjective extends UniversalPacketPlayOut{

	private String objectiveName;
	private String title;
	private EnumScoreboardHealthDisplay displayType;
	private int action;

	public PacketPlayOutScoreboardObjective() {
	}
	public PacketPlayOutScoreboardObjective(String objectiveName, String title, EnumScoreboardHealthDisplay displayType, int action) {
		this.objectiveName = objectiveName;
		this.title = title;
		this.displayType = displayType;
		this.action = action;
	}
	public Object toNMS() throws Exception {
		Object packet = newPacketPlayOutScoreboardObjective.newInstance();
		PacketPlayOutScoreboardObjective_OBJECTIVENAME.set(packet, objectiveName);
		if (versionNumber >= 13) {
			PacketPlayOutScoreboardObjective_TITLE.set(packet, Shared.mainClass.createComponent(title));
		} else {
			PacketPlayOutScoreboardObjective_TITLE.set(packet, title);
		}
		if (displayType != null) PacketPlayOutScoreboardObjective_DISPLAYTYPE.set(packet, displayType.toNMS());
		if (action != 0) PacketPlayOutScoreboardObjective_ACTION.set(packet, action);
		return packet;
	}
	public Object toBungee(int clientVersion) {
		if (clientVersion > 340) title = (String) Shared.mainClass.createComponent(title);
		return new ScoreboardObjective(objectiveName, title, displayType.toBungee(), (byte)action);
	}
	public enum EnumScoreboardHealthDisplay{
		
		INTEGER("INTEGER", EnumConstant.EnumScoreboardHealthDisplay_INTEGER),
		HEARTS("HEARTS", EnumConstant.EnumScoreboardHealthDisplay_HEARTS);

		private String name;
		private Object nmsEquivalent;

		private EnumScoreboardHealthDisplay(String name, Object nmsEquivalent) {
			this.name = name;
			this.nmsEquivalent = nmsEquivalent;
		}
		public Object toNMS() {
			return nmsEquivalent;
		}
		public HealthDisplay toBungee() {
			return HealthDisplay.valueOf(name);
		}
	}

	private static Class<?> PacketPlayOutScoreboardObjective;
	private static Constructor<?> newPacketPlayOutScoreboardObjective;
	private static Field PacketPlayOutScoreboardObjective_OBJECTIVENAME;
	private static Field PacketPlayOutScoreboardObjective_TITLE;
	private static Field PacketPlayOutScoreboardObjective_DISPLAYTYPE;
	private static Field PacketPlayOutScoreboardObjective_ACTION;
	
	static {
		try {
			if (versionNumber >= 8) {
				PacketPlayOutScoreboardObjective = getNMSClass("PacketPlayOutScoreboardObjective");
				newPacketPlayOutScoreboardObjective = PacketPlayOutScoreboardObjective.getConstructor();
				(PacketPlayOutScoreboardObjective_OBJECTIVENAME = PacketPlayOutScoreboardObjective.getDeclaredField("a")).setAccessible(true);
				(PacketPlayOutScoreboardObjective_TITLE = PacketPlayOutScoreboardObjective.getDeclaredField("b")).setAccessible(true);
				(PacketPlayOutScoreboardObjective_DISPLAYTYPE = PacketPlayOutScoreboardObjective.getDeclaredField("c")).setAccessible(true);
				(PacketPlayOutScoreboardObjective_ACTION = PacketPlayOutScoreboardObjective.getDeclaredField("d")).setAccessible(true);
			}
		} catch (Exception e) {
			Shared.error("Failed to initialize PacketPlayOutScoreboardDisplayObjective", e);
		}
	}
}