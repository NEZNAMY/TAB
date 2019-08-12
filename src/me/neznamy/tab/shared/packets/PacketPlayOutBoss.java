package me.neznamy.tab.shared.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.UUID;

import me.neznamy.tab.bukkit.packets.EnumConstant;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import net.md_5.bungee.protocol.packet.BossBar;

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
		Object packet = newPacketPlayOutBoss.newInstance();
		PacketPlayOutBoss_UUID.set(packet, uuid);
		PacketPlayOutBoss_ACTION.set(packet, action.toNMS());
		if (action == Action.ADD) {
			PacketPlayOutBoss_NAME.set(packet, Shared.mainClass.createComponent(title));
			PacketPlayOutBoss_PROGRESS.set(packet, progress);
			PacketPlayOutBoss_COLOR.set(packet, color.toNMS());
			PacketPlayOutBoss_STYLE.set(packet, style.toNMS());
			PacketPlayOutBoss_DARKEN_SKY.set(packet, darkenSky);
			PacketPlayOutBoss_PLAY_MUSIC.set(packet, playMusic);
			PacketPlayOutBoss_CREATE_FOG.set(packet, createFog);
		}
		if (action == Action.UPDATE_PCT) {
			PacketPlayOutBoss_PROGRESS.set(packet, progress);
		}
		if (action == Action.UPDATE_NAME) {
			PacketPlayOutBoss_NAME.set(packet, Shared.mainClass.createComponent(title));
		}
		if (action == Action.UPDATE_STYLE) {
			PacketPlayOutBoss_COLOR.set(packet, color.toNMS());
			PacketPlayOutBoss_STYLE.set(packet, style.toNMS());
		}
		if (action == Action.UPDATE_PROPERTIES) {
			PacketPlayOutBoss_DARKEN_SKY.set(packet, darkenSky);
			PacketPlayOutBoss_PLAY_MUSIC.set(packet, playMusic);
			PacketPlayOutBoss_CREATE_FOG.set(packet, createFog);
		}
		return packet;
	}
	public Object toBungee(ProtocolVersion clientVersion) {
		if (!clientVersion.is1_9orNewer()) return null;
		BossBar packet = new BossBar(uuid, action.toBungee());
		if (action == Action.ADD) {
			packet.setTitle((String) Shared.mainClass.createComponent(title));
			packet.setHealth(progress);
			packet.setColor(color.toBungee());
			packet.setDivision(style.toBungee());
			packet.setFlags(getFlags());
		}
		if (action == Action.UPDATE_PCT) {
			packet.setHealth(progress);
		}
		if (action == Action.UPDATE_NAME) {
			packet.setTitle((String) Shared.mainClass.createComponent(title));
		}
		if (action == Action.UPDATE_STYLE) {
			packet.setColor(color.toBungee());
			packet.setDivision(style.toBungee());
		}
		if (action == Action.UPDATE_PROPERTIES) {
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

		ADD(0, EnumConstant.PacketPlayOutBoss_Action_since_1_9_R1_ADD),
		REMOVE(1, EnumConstant.PacketPlayOutBoss_Action_since_1_9_R1_REMOVE),
		UPDATE_PCT(2, EnumConstant.PacketPlayOutBoss_Action_since_1_9_R1_UPDATE_PCT),
		UPDATE_NAME(3, EnumConstant.PacketPlayOutBoss_Action_since_1_9_R1_UPDATE_NAME),
		UPDATE_STYLE(4, EnumConstant.PacketPlayOutBoss_Action_since_1_9_R1_UPDATE_STYLE),
		UPDATE_PROPERTIES(5, EnumConstant.PacketPlayOutBoss_Action_since_1_9_R1_UPDATE_PROPERTIES);

		private int bungeeEquivalent;
		private Object nmsEquivalent;

		private Action(int bungeeEquivalent, Object nmsEquivalent) {
			this.bungeeEquivalent = bungeeEquivalent;
			this.nmsEquivalent = nmsEquivalent;
		}
		public Object toNMS() {
			return nmsEquivalent;
		}
		public int toBungee() {
			return bungeeEquivalent;
		}
	}
	public enum BarColor {

		PINK(0, EnumConstant.BarColor_since_1_9_R1_PINK),
		BLUE(1, EnumConstant.BarColor_since_1_9_R1_BLUE),
		RED(2, EnumConstant.BarColor_since_1_9_R1_RED),
		GREEN(3, EnumConstant.BarColor_since_1_9_R1_GREEN),
		YELLOW(4, EnumConstant.BarColor_since_1_9_R1_YELLOW),
		PURPLE(5, EnumConstant.BarColor_since_1_9_R1_PURPLE),
		WHITE(6, EnumConstant.BarColor_since_1_9_R1_WHITE);

		private int bungeeEquivalent;
		private Object nmsEquivalent;

		private BarColor(int bungeeEquivalent, Object nmsEquivalent) {
			this.bungeeEquivalent = bungeeEquivalent;
			this.nmsEquivalent = nmsEquivalent;
		}
		public Object toNMS() {
			return nmsEquivalent;
		}
		public int toBungee() {
			return bungeeEquivalent;
		}
	}
	public enum BarStyle {

		PROGRESS(0, EnumConstant.BarStyle_since_1_9_R1_PROGRESS),
		NOTCHED_6(1, EnumConstant.BarStyle_since_1_9_R1_NOTCHED_6),
		NOTCHED_10(2, EnumConstant.BarStyle_since_1_9_R1_NOTCHED_10),
		NOTCHED_12(3, EnumConstant.BarStyle_since_1_9_R1_NOTCHED_12),
		NOTCHED_20(4, EnumConstant.BarStyle_since_1_9_R1_NOTCHED_20);

		private int bungeeEquivalent;
		private Object nmsEquivalent;

		private BarStyle(int bungeeEquivalent, Object nmsEquivalent) {
			this.bungeeEquivalent = bungeeEquivalent;
			this.nmsEquivalent = nmsEquivalent;
		}
		public Object toNMS() {
			return nmsEquivalent;
		}
		public int toBungee() {
			return bungeeEquivalent;
		}
	}

	private static Class<?> PacketPlayOutBoss;
	private static Constructor<?> newPacketPlayOutBoss;
	private static Field PacketPlayOutBoss_UUID;
	private static Field PacketPlayOutBoss_ACTION;
	private static Field PacketPlayOutBoss_NAME;
	private static Field PacketPlayOutBoss_PROGRESS;
	private static Field PacketPlayOutBoss_COLOR;
	private static Field PacketPlayOutBoss_STYLE;
	private static Field PacketPlayOutBoss_DARKEN_SKY;
	private static Field PacketPlayOutBoss_PLAY_MUSIC;
	private static Field PacketPlayOutBoss_CREATE_FOG;

	static {
		try {
			if (versionNumber >= 9) {
				PacketPlayOutBoss = getNMSClass("PacketPlayOutBoss");
				newPacketPlayOutBoss = PacketPlayOutBoss.getConstructor();
				(PacketPlayOutBoss_UUID = PacketPlayOutBoss.getDeclaredField("a")).setAccessible(true);
				(PacketPlayOutBoss_ACTION = PacketPlayOutBoss.getDeclaredField("b")).setAccessible(true);
				(PacketPlayOutBoss_NAME = PacketPlayOutBoss.getDeclaredField("c")).setAccessible(true);
				(PacketPlayOutBoss_PROGRESS = PacketPlayOutBoss.getDeclaredField("d")).setAccessible(true);
				(PacketPlayOutBoss_COLOR = PacketPlayOutBoss.getDeclaredField("e")).setAccessible(true);
				(PacketPlayOutBoss_STYLE = PacketPlayOutBoss.getDeclaredField("f")).setAccessible(true);
				(PacketPlayOutBoss_DARKEN_SKY = PacketPlayOutBoss.getDeclaredField("g")).setAccessible(true);
				(PacketPlayOutBoss_PLAY_MUSIC = PacketPlayOutBoss.getDeclaredField("h")).setAccessible(true);
				(PacketPlayOutBoss_CREATE_FOG = PacketPlayOutBoss.getDeclaredField("i")).setAccessible(true);
			}
		} catch (Exception e) {
			Shared.error("Failed to initialize PacketPlayOutBoss", e);
		}
	}
}