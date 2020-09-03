package me.neznamy.tab.shared.packets;

import java.util.UUID;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutBoss extends UniversalPacketPlayOut {

	public UUID id;
	public Action operation;
	public String name;
	public float pct;
	public BarColor color;
	public BarStyle overlay;
	public boolean darkenScreen;
	public boolean playMusic;
	public boolean createWorldFog;

	private PacketPlayOutBoss() {
	}

	public static PacketPlayOutBoss CREATE(UUID id, String name, float pct, BarColor color, BarStyle overlay, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
		PacketPlayOutBoss packet = new PacketPlayOutBoss();
		packet.operation = Action.ADD;
		packet.id = id;
		packet.name = name;
		packet.pct = pct;
		packet.color = color;
		packet.overlay = overlay;
		packet.darkenScreen = darkenScreen;
		packet.playMusic = playMusic;
		packet.createWorldFog = createWorldFog;
		return packet;
	}

	public static PacketPlayOutBoss CREATE(UUID id, String name, float pct, BarColor color, BarStyle overlay) {
		PacketPlayOutBoss packet = new PacketPlayOutBoss();
		packet.operation = Action.ADD;
		packet.id = id;
		packet.name = name;
		packet.pct = pct;
		packet.color = color;
		packet.overlay = overlay;
		return packet;
	}

	public static PacketPlayOutBoss REMOVE(UUID id) {
		PacketPlayOutBoss packet = new PacketPlayOutBoss();
		packet.operation = Action.REMOVE;
		packet.id = id;
		return packet;
	}

	public static PacketPlayOutBoss UPDATE_PCT(UUID id, float pct) {
		PacketPlayOutBoss packet = new PacketPlayOutBoss();
		packet.operation = Action.UPDATE_PCT;
		packet.id = id;
		packet.pct = pct;
		return packet;
	}

	public static PacketPlayOutBoss UPDATE_NAME(UUID id, String name) {
		PacketPlayOutBoss packet = new PacketPlayOutBoss();
		packet.operation = Action.UPDATE_NAME;
		packet.id = id;
		packet.name = name;
		return packet;
	}

	public static PacketPlayOutBoss UPDATE_STYLE(UUID id, BarColor color, BarStyle overlay) {
		PacketPlayOutBoss packet = new PacketPlayOutBoss();
		packet.operation = Action.UPDATE_STYLE;
		packet.id = id;
		packet.color = color;
		packet.overlay = overlay;
		return packet;
	}

	public static PacketPlayOutBoss UPDATE_PROPERTIES(UUID id, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
		PacketPlayOutBoss packet = new PacketPlayOutBoss();
		packet.operation = Action.UPDATE_PROPERTIES;
		packet.id = id;
		packet.darkenScreen = darkenScreen;
		packet.playMusic = playMusic;
		packet.createWorldFog = createWorldFog;
		return packet;
	}

	public byte getFlags(){
		byte value = 0;
		if (darkenScreen) value += 1;
		if (playMusic) value += 2;
		if (createWorldFog) value += 4;
		return value;
	}

	@Override
	protected Object build(ProtocolVersion clientVersion) throws Exception {
		return builder.build(this, clientVersion);
	}

	public enum Action {

		ADD,
		REMOVE,
		UPDATE_PCT,
		UPDATE_NAME,
		UPDATE_STYLE,
		UPDATE_PROPERTIES;
	}

	public enum BarColor {

		PINK,
		BLUE,
		RED,
		GREEN,
		YELLOW,
		PURPLE,
		WHITE;
	}

	public enum BarStyle {

		PROGRESS,
		NOTCHED_6,
		NOTCHED_10,
		NOTCHED_12,
		NOTCHED_20;
	}
}