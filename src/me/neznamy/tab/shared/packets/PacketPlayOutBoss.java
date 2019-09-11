package me.neznamy.tab.shared.packets;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import net.kyori.text.Component;
import net.kyori.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.protocol.packet.BossBar;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class PacketPlayOutBoss extends UniversalPacketPlayOut{

	private UUID uuid;
	private Action action;
	private String title;
	private float progress;
	private BarColor color;
	private BarStyle style;
	private boolean darkenSky;
	private boolean playMusic;
	private boolean createFog;

	public PacketPlayOutBoss(UUID uuid, String title, float progress, BarColor color, BarStyle style) {
		this(uuid, title, progress, color, style, false, false, false);
	}
	public PacketPlayOutBoss(UUID uuid, String title, float progress, BarColor color, BarStyle style, boolean darkenSky, boolean playMusic, boolean createFog) {
		this.action = Action.ADD;
		this.uuid = uuid;
		this.title = title;
		this.progress = progress;
		this.color = color;
		this.style = style;
		this.darkenSky = darkenSky;
		this.playMusic = playMusic;
		this.createFog = createFog;
	}
	public PacketPlayOutBoss(UUID uuid) {
		this.action = Action.REMOVE;
		this.uuid = uuid;
	}
	public PacketPlayOutBoss(UUID uuid, float progress) {
		this.action = Action.UPDATE_PCT;
		this.uuid = uuid;
		this.progress = progress;
	}
	public PacketPlayOutBoss(UUID uuid, String title) {
		this.action = Action.UPDATE_NAME;
		this.uuid = uuid;
		this.title = title;
	}
	public PacketPlayOutBoss(UUID uuid, BarColor color, BarStyle style) {
		this.action = Action.UPDATE_STYLE;
		this.uuid = uuid;
		this.color = color;
		this.style = style;
	}
	public PacketPlayOutBoss(UUID uuid, boolean darkenSky, boolean playMusic, boolean createFog) {
		this.action = Action.UPDATE_PROPERTIES;
		this.uuid = uuid;
		this.darkenSky = darkenSky;
		this.playMusic = playMusic;
		this.createFog = createFog;
	}

	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		Object packet = MethodAPI.getInstance().newPacketPlayOutBoss();
		UUID.set(packet, uuid);
		ACTION.set(packet, action.toNMS());
		if (action == Action.UPDATE_PCT || action == Action.ADD) {
			PROGRESS.set(packet, progress);
		}
		if (action == Action.UPDATE_NAME || action == Action.ADD) {
			NAME.set(packet, Shared.mainClass.createComponent(title));
		}
		if (action == Action.UPDATE_STYLE || action == Action.ADD) {
			COLOR.set(packet, color.toNMS());
			STYLE.set(packet, style.toNMS());
		}
		if (action == Action.UPDATE_PROPERTIES || action == Action.ADD) {
			DARKEN_SKY.set(packet, darkenSky);
			PLAY_MUSIC.set(packet, playMusic);
			CREATE_FOG.set(packet, createFog);
		}
		return packet;
	}
	public Object toBungee(ProtocolVersion clientVersion) {
		if (clientVersion.getMinorVersion() < 9) return null;
		BossBar packet = new BossBar(uuid, action.toBungee());
		if (action == Action.UPDATE_PCT || action == Action.ADD) {
			packet.setHealth(progress);
		}
		if (action == Action.UPDATE_NAME || action == Action.ADD) {
			packet.setTitle((String) Shared.mainClass.createComponent(title));
		}
		if (action == Action.UPDATE_STYLE || action == Action.ADD) {
			packet.setColor(color.toBungee());
			packet.setDivision(style.toBungee());
		}
		if (action == Action.UPDATE_PROPERTIES || action == Action.ADD) {
			packet.setFlags(getFlags());
		}
		return packet;
	}
	public Object toVelocity(ProtocolVersion clientVersion) {
		if (clientVersion.getMinorVersion() < 9) return null;
		com.velocitypowered.proxy.protocol.packet.BossBar packet = new com.velocitypowered.proxy.protocol.packet.BossBar();
		packet.setUuid(uuid);
		packet.setAction(action.toBungee());
		if (action == Action.UPDATE_PCT || action == Action.ADD) {
			packet.setPercent(progress);
		}
		if (action == Action.UPDATE_NAME || action == Action.ADD) {
			packet.setName(GsonComponentSerializer.INSTANCE.serialize((Component) Shared.mainClass.createComponent(title)));
		}
		if (action == Action.UPDATE_STYLE || action == Action.ADD) {
			packet.setColor(color.toBungee());
			packet.setOverlay(style.toBungee());
		}
		if (action == Action.UPDATE_PROPERTIES || action == Action.ADD) {
			packet.setFlags(getFlags());
		}
		return packet;
	}
	private byte getFlags(){
		byte value = 0;
		if (darkenSky) value += 1;
		if (playMusic) value += 2;
		if (createFog) value += 4;
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
			if (MethodAPI.getInstance() != null) nmsEquivalent = Enum.valueOf((Class<Enum>)MethodAPI.PacketPlayOutBoss_Action, toString());
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
			if (MethodAPI.getInstance() != null) nmsEquivalent = Enum.valueOf((Class<Enum>)MethodAPI.BarColor, toString());
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
			if (MethodAPI.getInstance() != null) nmsEquivalent = Enum.valueOf((Class<Enum>)MethodAPI.BarStyle, toString());
		}
		public Object toNMS() {
			return nmsEquivalent;
		}
		public int toBungee() {
			return bungeeEquivalent;
		}
	}
	private static Map<String, Field> fields = getFields(MethodAPI.PacketPlayOutBoss);
	private static Field UUID = fields.get("a");
	private static Field ACTION = fields.get("b");
	private static Field NAME = fields.get("c");
	private static Field PROGRESS = fields.get("d");
	private static Field COLOR = fields.get("e");
	private static Field STYLE = fields.get("f");
	private static Field DARKEN_SKY = fields.get("g");
	private static Field PLAY_MUSIC = fields.get("h");
	private static Field CREATE_FOG = fields.get("i");
}