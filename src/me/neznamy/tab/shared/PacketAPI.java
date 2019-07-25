package me.neznamy.tab.shared;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import me.neznamy.tab.bukkit.packets.DataWatcher;
import me.neznamy.tab.bukkit.packets.DataWatcherObject;
import me.neznamy.tab.bukkit.packets.DataWatcherSerializer;
import me.neznamy.tab.bukkit.packets.PacketPlayOutEntityDestroy;
import me.neznamy.tab.bukkit.packets.PacketPlayOutEntityMetadata;
import me.neznamy.tab.bukkit.packets.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss.BarColor;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss.BarStyle;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore.Action;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;

public abstract class PacketAPI{

	public static Object getField(Object object, String name) throws Exception {
		Field field = object.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return field.get(object);
	}
	public static void changeScoreboardScore(ITabPlayer to, String scoreName, String scoreboard, int scoreValue) {
		new PacketPlayOutScoreboardScore(Action.CHANGE, scoreboard, scoreName, scoreValue).send(to);
	}
	public static void registerScoreboardTeam(ITabPlayer to, String teamName, String prefix, String suffix, boolean enumNameTagVisibility, boolean enumTeamPush, Collection<String> players) {
		unregisterScoreboardTeam(to, teamName);
		sendScoreboardTeamPacket(to, teamName, prefix, suffix, enumNameTagVisibility, enumTeamPush, players, 0, 69);
	}
	public static void unregisterScoreboardTeam(ITabPlayer to, String teamName) {
		sendScoreboardTeamPacket(to, teamName, null, null, true, true, null, 1, 69);
	}
	public static void updateScoreboardTeamPrefixSuffix(ITabPlayer to, String teamName, String prefix, String suffix, boolean enumNameTagVisibility, boolean enumTeamPush) {
		sendScoreboardTeamPacket(to, teamName, prefix, suffix, enumNameTagVisibility, enumTeamPush, null, 2, 69);
	}
	public static void sendFancyMessage(ITabPlayer to, FancyMessage message) {
		new PacketPlayOutChat(message.toString()).send(to);
	}
	public static void registerScoreboardObjective(ITabPlayer to, String objectiveName, String title, int position, EnumScoreboardHealthDisplay displayType) {
		new PacketPlayOutScoreboardObjective(objectiveName, title, displayType, 0).send(to);
		new PacketPlayOutScoreboardDisplayObjective(position, objectiveName).send(to);
	}
	public static void unregisterScoreboardObjective(ITabPlayer to, String objectiveName, String title, EnumScoreboardHealthDisplay displayType) {
		new PacketPlayOutScoreboardObjective(objectiveName, title, displayType, 1).send(to);
	}
	public static void sendScoreboardTeamPacket(ITabPlayer to, String team, String prefix, String suffix, boolean enumNameTagVisibility, boolean enumTeamPush, Collection<String> players, int action, int signature) {
		new PacketPlayOutScoreboardTeam(team, prefix, suffix, enumNameTagVisibility?"always":"never", enumTeamPush?"always":"never", players, action, signature, null).send(to);
	}

	public static void sendBossBar(ITabPlayer to, BossBAR bar, float progress, String message) {
		if (UniversalPacketPlayOut.versionNumber != 8) {
			new PacketPlayOutBoss(bar.getUniqueId(), message, progress, bar.getColor(), bar.getStyle()).send(to);
		} else {
			Location l = ((Player) to.getPlayer()).getEyeLocation().add(((Player) to.getPlayer()).getEyeLocation().getDirection().normalize().multiply(25));
			if (l.getY() < 1) l.setY(1);
			PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(bar.getEntityId(), null, EntityType.WITHER, l);
			DataWatcher w = new DataWatcher(null);
			w.setValue(new DataWatcherObject(0, DataWatcherSerializer.Byte), (byte)32);
			w.setValue(new DataWatcherObject(2, DataWatcherSerializer.String), message);
			w.setValue(new DataWatcherObject(6, DataWatcherSerializer.Float), (float)300*progress);
			packet.setDataWatcher(w);
			packet.send(to);
		}
	}
	public static void removeBossBar(ITabPlayer to, BossBAR bar) {
		if (UniversalPacketPlayOut.versionNumber != 8) {
			new PacketPlayOutBoss(bar.getUniqueId()).send(to);
		} else {
			new PacketPlayOutEntityDestroy(bar.getEntityId()).send(to);
		}
	}
	public static void updateBossBar(ITabPlayer to, BossBAR bar, BarColor color, BarStyle style, float progress, String message) {
		if (UniversalPacketPlayOut.versionNumber != 8) {
			boolean updateStyle = false;
			if (bar.getColor() != color) {
				bar.setColor(color);
				updateStyle = true;
			}
			if (bar.getStyle() != style) {
				bar.setStyle(style);
				updateStyle = true;
			}
			if (updateStyle) {
				PacketPlayOutBoss styleColorPacket = new PacketPlayOutBoss(bar.getUniqueId(), bar.getColor(), bar.getStyle());
				for (ITabPlayer all : Shared.getPlayers()) {
					styleColorPacket.send(all);
				}
			}
			new PacketPlayOutBoss(bar.getUniqueId(), progress).send(to);
			new PacketPlayOutBoss(bar.getUniqueId(), message).send(to);
		} else {
			DataWatcher w = new DataWatcher(null);
			w.setValue(new DataWatcherObject(2, DataWatcherSerializer.String), message);
			w.setValue(new DataWatcherObject(6, DataWatcherSerializer.Float), (float)300*progress);
			new PacketPlayOutEntityMetadata(bar.getEntityId(), w, true).send(to);
		}
	}
	public static class BossBAR{

		private UUID uuid;
		private int entityId; //1.8.x
		private BarColor color;
		private BarStyle style;

		public BossBAR(BarStyle style, BarColor color) {
			this.uuid = UUID.randomUUID();
			this.entityId = Shared.getNextEntityId();
			this.color = color;
			this.style = style;
		}
		public UUID getUniqueId() {
			return uuid;
		}
		public int getEntityId() {
			return entityId;
		}
		public BarColor getColor() {
			return color;
		}
		public BarStyle getStyle() {
			return style;
		}
		public void setColor(BarColor color) {
			this.color = color;
		}
		public void setStyle(BarStyle style) {
			this.style = style;
		}
	}
}