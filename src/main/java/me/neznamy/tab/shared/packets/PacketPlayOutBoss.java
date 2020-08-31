package me.neznamy.tab.shared.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.UUID;

import me.neznamy.tab.platforms.bukkit.packets.NMSHook;
import me.neznamy.tab.shared.ProtocolVersion;
import net.md_5.bungee.protocol.packet.BossBar;

/**
 * A class representing platform specific packet class
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class PacketPlayOutBoss extends UniversalPacketPlayOut {

	private static Class<?> PacketPlayOutBoss;
	private static Class<?> BarColor;
	private static Class<?> BarStyle;
	private static Class<Enum> Action_;
	private static Constructor<?> newPacketPlayOutBoss;
	private static Field UUID;
	private static Field ACTION;
	private static Field NAME;
	private static Field PROGRESS;
	private static Field COLOR;
	private static Field STYLE;
	private static Field DARKEN_SKY;
	private static Field PLAY_MUSIC;
	private static Field CREATE_FOG;
	
	private UUID id;
	private Action operation;
	private String name;
	private float pct;
	private BarColor color;
	private BarStyle overlay;
	private boolean darkenScreen;
	private boolean playMusic;
	private boolean createWorldFog;

	public static void initializeClass() throws Exception {
		PacketPlayOutBoss = getNMSClass("PacketPlayOutBoss");
		BarColor = getNMSClass("BossBattle$BarColor");
		BarStyle = getNMSClass("BossBattle$BarStyle");
		Action_ = (Class<Enum>) getNMSClass("PacketPlayOutBoss$Action");
		newPacketPlayOutBoss = PacketPlayOutBoss.getConstructor();
		(UUID = PacketPlayOutBoss.getDeclaredField("a")).setAccessible(true);
		(ACTION = PacketPlayOutBoss.getDeclaredField("b")).setAccessible(true);
		(NAME = PacketPlayOutBoss.getDeclaredField("c")).setAccessible(true);
		(PROGRESS = PacketPlayOutBoss.getDeclaredField("d")).setAccessible(true);
		(COLOR = PacketPlayOutBoss.getDeclaredField("e")).setAccessible(true);
		(STYLE = PacketPlayOutBoss.getDeclaredField("f")).setAccessible(true);
		(DARKEN_SKY = PacketPlayOutBoss.getDeclaredField("g")).setAccessible(true);
		(PLAY_MUSIC = PacketPlayOutBoss.getDeclaredField("h")).setAccessible(true);
		(CREATE_FOG = PacketPlayOutBoss.getDeclaredField("i")).setAccessible(true);
	}
	
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

	@Override
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		Object packet = newPacketPlayOutBoss.newInstance();
		UUID.set(packet, id);
		ACTION.set(packet, operation.toNMS());
		if (operation == Action.UPDATE_PCT || operation == Action.ADD) {
			PROGRESS.set(packet, pct);
		}
		if (operation == Action.UPDATE_NAME || operation == Action.ADD) {
			NAME.set(packet, NMSHook.stringToComponent(IChatBaseComponent.optimizedComponent(name).toString(clientVersion)));
		}
		if (operation == Action.UPDATE_STYLE || operation == Action.ADD) {
			COLOR.set(packet, color.toNMS());
			STYLE.set(packet, overlay.toNMS());
		}
		if (operation == Action.UPDATE_PROPERTIES || operation == Action.ADD) {
			DARKEN_SKY.set(packet, darkenScreen);
			PLAY_MUSIC.set(packet, playMusic);
			CREATE_FOG.set(packet, createWorldFog);
		}
		return packet;
	}
	
	@Override
	public Object toBungee(ProtocolVersion clientVersion) {
		if (clientVersion.getMinorVersion() < 9) return null;
		BossBar packet = new BossBar(id, operation.toBungee());
		if (operation == Action.UPDATE_PCT || operation == Action.ADD) {
			packet.setHealth(pct);
		}
		if (operation == Action.UPDATE_NAME || operation == Action.ADD) {
			packet.setTitle(IChatBaseComponent.optimizedComponent(name).toString(clientVersion));
		}
		if (operation == Action.UPDATE_STYLE || operation == Action.ADD) {
			packet.setColor(color.toBungee());
			packet.setDivision(overlay.toBungee());
		}
		if (operation == Action.UPDATE_PROPERTIES || operation == Action.ADD) {
			packet.setFlags(getFlags());
		}
		return packet;
	}
	
	@Override
	public Object toVelocity(ProtocolVersion clientVersion) {
		if (clientVersion.getMinorVersion() < 9) return null;
		com.velocitypowered.proxy.protocol.packet.BossBar packet = new com.velocitypowered.proxy.protocol.packet.BossBar();
		packet.setUuid(id);
		packet.setAction(operation.toBungee());
		if (operation == Action.UPDATE_PCT || operation == Action.ADD) {
			packet.setPercent(pct);
		}
		if (operation == Action.UPDATE_NAME || operation == Action.ADD) {
			packet.setName(IChatBaseComponent.optimizedComponent(name).toString(clientVersion));
		}
		if (operation == Action.UPDATE_STYLE || operation == Action.ADD) {
			packet.setColor(color.toBungee());
			packet.setOverlay(overlay.toBungee());
		}
		if (operation == Action.UPDATE_PROPERTIES || operation == Action.ADD) {
			packet.setFlags(getFlags());
		}
		return packet;
	}
	
	private byte getFlags(){
		byte value = 0;
		if (darkenScreen) value += 1;
		if (playMusic) value += 2;
		if (createWorldFog) value += 4;
		return value;
	}

	public enum Action {

		ADD(0),
		REMOVE(1),
		UPDATE_PCT(2),
		UPDATE_NAME(3),
		UPDATE_STYLE(4),
		UPDATE_PROPERTIES(5);

		private int bungeeEquivalent;
		private Object nmsEquivalent;

		private Action(int bungeeEquivalent) {
			this.bungeeEquivalent = bungeeEquivalent;
			if (Action_ != null) {
				nmsEquivalent = Enum.valueOf(Action_, toString());
			}
		}
		public Object toNMS() {
			return nmsEquivalent;
		}
		public int toBungee() {
			return bungeeEquivalent;
		}
	}
	public enum BarColor {

		PINK(0),
		BLUE(1),
		RED(2),
		GREEN(3),
		YELLOW(4),
		PURPLE(5),
		WHITE(6);

		private int bungeeEquivalent;
		private Object nmsEquivalent;

		private BarColor(int bungeeEquivalent) {
			this.bungeeEquivalent = bungeeEquivalent;
			if (BarColor != null) {
				nmsEquivalent = Enum.valueOf((Class<Enum>)BarColor, toString());
			}
		}
		public Object toNMS() {
			return nmsEquivalent;
		}
		public int toBungee() {
			return bungeeEquivalent;
		}
	}
	public enum BarStyle {

		PROGRESS(0),
		NOTCHED_6(1),
		NOTCHED_10(2),
		NOTCHED_12(3),
		NOTCHED_20(4);

		private int bungeeEquivalent;
		private Object nmsEquivalent;

		private BarStyle(int bungeeEquivalent) {
			this.bungeeEquivalent = bungeeEquivalent;
			if (BarStyle != null) {
				nmsEquivalent = Enum.valueOf((Class<Enum>)BarStyle, toString());
			}
		}
		public Object toNMS() {
			return nmsEquivalent;
		}
		public int toBungee() {
			return bungeeEquivalent;
		}
	}
}