package me.neznamy.tab.shared.packets;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import net.md_5.bungee.protocol.packet.BossBar;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class PacketPlayOutBoss extends UniversalPacketPlayOut{

	private UUID id;
	private Action operation;
	private String name;
	private float pct;
	private BarColor color;
	private BarStyle overlay;
	private boolean darkenScreen;
	private boolean playMusic;
	private boolean createWorldFog;

	public PacketPlayOutBoss(UUID id, String name, float pct, BarColor color, BarStyle overlay) {
		this(id, name, pct, color, overlay, false, false, false);
	}
	public PacketPlayOutBoss(UUID id, String name, float pct, BarColor color, BarStyle overlay, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
		this.operation = Action.ADD;
		this.id = id;
		this.name = name;
		this.pct = pct;
		this.color = color;
		this.overlay = overlay;
		this.darkenScreen = darkenScreen;
		this.playMusic = playMusic;
		this.createWorldFog = createWorldFog;
	}
	public PacketPlayOutBoss(UUID id) {
		this.operation = Action.REMOVE;
		this.id = id;
	}
	public PacketPlayOutBoss(UUID id, float pct) {
		this.operation = Action.UPDATE_PCT;
		this.id = id;
		this.pct = pct;
	}
	public PacketPlayOutBoss(UUID id, String name) {
		this.operation = Action.UPDATE_NAME;
		this.id = id;
		this.name = name;
	}
	public PacketPlayOutBoss(UUID id, BarColor color, BarStyle overlay) {
		this.operation = Action.UPDATE_STYLE;
		this.id = id;
		this.color = color;
		this.overlay = overlay;
	}
	public PacketPlayOutBoss(UUID id, boolean darkenScreen, boolean playMusic, boolean createWorldFog) {
		this.operation = Action.UPDATE_PROPERTIES;
		this.id = id;
		this.darkenScreen = darkenScreen;
		this.playMusic = playMusic;
		this.createWorldFog = createWorldFog;
	}

	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		Object packet = MethodAPI.getInstance().newPacketPlayOutBoss();
		UUID.set(packet, id);
		ACTION.set(packet, operation.toNMS());
		if (operation == Action.UPDATE_PCT || operation == Action.ADD) {
			PROGRESS.set(packet, pct);
		}
		if (operation == Action.UPDATE_NAME || operation == Action.ADD) {
			NAME.set(packet, MethodAPI.getInstance().ICBC_fromString(new IChatBaseComponent(name).toString()));
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
	public Object toBungee(ProtocolVersion clientVersion) {
		if (clientVersion.getMinorVersion() < 9) return null;
		BossBar packet = new BossBar(id, operation.toBungee());
		if (operation == Action.UPDATE_PCT || operation == Action.ADD) {
			packet.setHealth(pct);
		}
		if (operation == Action.UPDATE_NAME || operation == Action.ADD) {
			packet.setTitle(new IChatBaseComponent(name).toString());
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
	public Object toVelocity(ProtocolVersion clientVersion) {
		if (clientVersion.getMinorVersion() < 9) return null;
		com.velocitypowered.proxy.protocol.packet.BossBar packet = new com.velocitypowered.proxy.protocol.packet.BossBar();
		packet.setUuid(id);
		packet.setAction(operation.toBungee());
		if (operation == Action.UPDATE_PCT || operation == Action.ADD) {
			packet.setPercent(pct);
		}
		if (operation == Action.UPDATE_NAME || operation == Action.ADD) {
			packet.setName(new IChatBaseComponent(name).toString());
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
			if (MethodAPI.PacketPlayOutBoss_Action != null) {
				nmsEquivalent = Enum.valueOf((Class<Enum>)MethodAPI.PacketPlayOutBoss_Action, toString());
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
			if (MethodAPI.BarColor != null) {
				nmsEquivalent = Enum.valueOf((Class<Enum>)MethodAPI.BarColor, toString());
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
			if (MethodAPI.BarStyle != null) {
				nmsEquivalent = Enum.valueOf((Class<Enum>)MethodAPI.BarStyle, toString());
			}
		}
		public Object toNMS() {
			return nmsEquivalent;
		}
		public int toBungee() {
			return bungeeEquivalent;
		}
	}
	private static Map<String, Field> fields = getFields(MethodAPI.PacketPlayOutBoss);
	private static final Field UUID = fields.get("a");
	private static final Field ACTION = fields.get("b");
	private static final Field NAME = fields.get("c");
	private static final Field PROGRESS = fields.get("d");
	private static final Field COLOR = fields.get("e");
	private static final Field STYLE = fields.get("f");
	private static final Field DARKEN_SKY = fields.get("g");
	private static final Field PLAY_MUSIC = fields.get("h");
	private static final Field CREATE_FOG = fields.get("i");
}