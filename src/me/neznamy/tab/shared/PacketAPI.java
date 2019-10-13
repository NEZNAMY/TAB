package me.neznamy.tab.shared;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import com.google.common.collect.Lists;

import me.neznamy.tab.platforms.bukkit.TabPlayer;
import me.neznamy.tab.platforms.bukkit.packets.DataWatcher;
import me.neznamy.tab.platforms.bukkit.packets.DataWatcherObject;
import me.neznamy.tab.platforms.bukkit.packets.DataWatcherSerializer;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOutEntityMetadata;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.BossBar.BossBarLine;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import me.neznamy.tab.shared.packets.PacketPlayOutChat.ChatMessageType;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore.Action;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;

public class PacketAPI{

	public static void debug(ITabPlayer p, String message) {
		System.out.println("[TAB DEBUG] [" + p.getName() + "] " + message);
	}

	//scoreboard team
	public static synchronized void registerScoreboardTeam(ITabPlayer to, String teamName, String prefix, String suffix, boolean enumNameTagVisibility, boolean enumTeamPush, Collection<String> players) {
		if (to.getVersion().getNetworkId() >= ProtocolVersion.v1_8.getNetworkId() && Configs.SECRET_safe_register) {
			unregisterScoreboardTeam(to, teamName);
		}
		sendScoreboardTeamPacket(to, teamName, prefix, suffix, enumNameTagVisibility, enumTeamPush, players, 0, 69);
	}
	public static void unregisterScoreboardTeam(ITabPlayer to, String teamName) {
		sendScoreboardTeamPacket(to, teamName, null, null, true, true, null, 1, 69);
	}
	public static void updateScoreboardTeamPrefixSuffix(ITabPlayer to, String teamName, String prefix, String suffix, boolean enumNameTagVisibility, boolean enumTeamPush) {
		sendScoreboardTeamPacket(to, teamName, prefix, suffix, enumNameTagVisibility, enumTeamPush, null, 2, 69);
	}
	private static void sendScoreboardTeamPacket(ITabPlayer to, String team, String prefix, String suffix, boolean enumNameTagVisibility, boolean enumTeamPush, Collection<String> players, int action, int signature) {
		to.sendCustomPacket(new PacketPlayOutScoreboardTeam(team, prefix, suffix, enumNameTagVisibility?"always":"never", enumTeamPush?"always":"never", players, action, signature, null));
	}

	//scoreboard objective
	public static void registerScoreboardObjective(ITabPlayer to, String objectiveName, String title, int position, EnumScoreboardHealthDisplay displayType) {
		if (to.getVersion().getNetworkId() >= ProtocolVersion.v1_8.getNetworkId()) {
			unregisterScoreboardObjective(to, objectiveName, title, displayType);
		}
		to.sendCustomPacket(new PacketPlayOutScoreboardObjective(objectiveName, title, displayType, 0));
		to.sendCustomPacket(new PacketPlayOutScoreboardDisplayObjective(position, objectiveName));
	}
	public static void unregisterScoreboardObjective(ITabPlayer to, String objectiveName, String title, EnumScoreboardHealthDisplay displayType) {
		to.sendCustomPacket(new PacketPlayOutScoreboardObjective(objectiveName, title, displayType, 1));
	}
	public static void changeScoreboardObjectiveTitle(ITabPlayer p, String objectiveName, String title, EnumScoreboardHealthDisplay displayType) {
		p.sendCustomPacket(new PacketPlayOutScoreboardObjective(objectiveName, title, displayType, 2));
	}

	//scoreboard score
	public static void registerScoreboardScore(ITabPlayer p, String team, String player, String prefix, String suffix, String objective, int score) {
		registerScoreboardTeam(p, team, prefix, suffix, false, false, Lists.newArrayList(player));
		setScoreboardScore(p, player, objective, score);
	}
	public static void removeScoreboardScore(ITabPlayer p, String score, String ID) {
		p.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.REMOVE, ID, score, 0));
		unregisterScoreboardTeam(p, ID);
	}
	public static void setScoreboardScore(ITabPlayer to, String scoreName, String scoreboard, int scoreValue) {
		to.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, scoreboard, scoreName, scoreValue));
	}
	
	
	
	public static void sendFancyMessage(ITabPlayer to, FancyMessage message) {
		to.sendCustomPacket(new PacketPlayOutChat(message.toString(), ChatMessageType.CHAT));
	}
	private static final int NAME_POSITION = ProtocolVersion.SERVER_VERSION.getMinorVersion() == 8 ? 2 : ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 6 ? 10 : 5;
	public static void createBossBar(ITabPlayer to, BossBarLine bar){
		to.setProperty("bossbar-text-"+bar.getName(), bar.text);
		to.setProperty("bossbar-progress-"+bar.getName(), bar.progress);
		to.setProperty("bossbar-color-"+bar.getName(), bar.color);
		to.setProperty("bossbar-style-"+bar.getName(), bar.style);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			to.sendCustomPacket(new PacketPlayOutBoss(bar.getUniqueId(), 
					to.properties.get("bossbar-text-"+bar.getName()).get(), 
					(float)bar.parseProgress(to.properties.get("bossbar-progress-"+bar.getName()).get())/100, 
					bar.parseColor(to.properties.get("bossbar-color-"+bar.getName()).get()), 
					bar.parseStyle(to.properties.get("bossbar-style-"+bar.getName()).get())));
		} else {
			Location l = (((TabPlayer)to).player).getEyeLocation().add(((TabPlayer)to).player.getEyeLocation().getDirection().normalize().multiply(25));
			if (l.getY() < 1) l.setY(1);
			PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(bar.getEntityId(), null, EntityType.WITHER, l);
			DataWatcher w = new DataWatcher(null);
			w.setValue(new DataWatcherObject(0, DataWatcherSerializer.Byte), (byte)32);
			w.setValue(new DataWatcherObject(NAME_POSITION, DataWatcherSerializer.String), to.properties.get("bossbar-text-"+bar.getName()).get());
			float health = (float)3*bar.parseProgress(to.properties.get("bossbar-progress-"+bar.getName()).get());
			if (health == 0) health = 1;
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 6) {
				w.setValue(new DataWatcherObject(6, DataWatcherSerializer.Float), health);
			} else {
				w.setValue(new DataWatcherObject(16, DataWatcherSerializer.Integer), (int)health);
			}
			packet.setDataWatcher(w);
			to.sendCustomPacket(packet);
		}
	}
	public static void removeBossBar(ITabPlayer to, BossBarLine bar) {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			to.sendCustomPacket(new PacketPlayOutBoss(bar.getUniqueId()));
		} else {
			to.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityDestroy(new int[] {bar.getEntityId()}));
		}
	}
	public static void updateBossBar(ITabPlayer to, BossBarLine bar) {
		Property progress = to.properties.get("bossbar-progress-"+bar.getName());
		Property text = to.properties.get("bossbar-text-"+bar.getName());
		if (text == null) return; //not registered yet
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			Property color = to.properties.get("bossbar-color-"+bar.getName());
			Property style = to.properties.get("bossbar-style-"+bar.getName());
			boolean colorUpdate = color.isUpdateNeeded();
			boolean styleUpdate = style.isUpdateNeeded();
			if (colorUpdate || styleUpdate) {
				to.sendCustomPacket(new PacketPlayOutBoss(bar.getUniqueId(), bar.parseColor(color.get()), bar.parseStyle(style.get())));
			}
			if (progress.isUpdateNeeded()) {
				to.sendCustomPacket(new PacketPlayOutBoss(bar.getUniqueId(), (float)bar.parseProgress(progress.get())/100));
			}
			if (text.isUpdateNeeded()) {
				to.sendCustomPacket(new PacketPlayOutBoss(bar.getUniqueId(), text.get()));
			}
		} else {
			DataWatcher w = new DataWatcher(null);
			if (text.isUpdateNeeded()) {
				w.setValue(new DataWatcherObject(NAME_POSITION, DataWatcherSerializer.String), text.get());
			}
			if (progress.isUpdateNeeded()) {
				float health = (float)3*bar.parseProgress(progress.get());
				if (health == 0) health = 1;
				if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 6) {
					w.setValue(new DataWatcherObject(6, DataWatcherSerializer.Float), health);
				} else {
					w.setValue(new DataWatcherObject(16, DataWatcherSerializer.Integer), (int)health);
				}
			}
			if (w.getAllObjects().isEmpty()) return;
			to.sendPacket(new PacketPlayOutEntityMetadata(bar.getEntityId(), w, true).toNMS(null));
		}
	}
}