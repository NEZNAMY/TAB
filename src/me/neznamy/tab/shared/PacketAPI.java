package me.neznamy.tab.shared;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import me.neznamy.tab.platforms.bukkit.packets.DataWatcher;
import me.neznamy.tab.platforms.bukkit.packets.DataWatcher.DataWatcherObject;
import me.neznamy.tab.platforms.bukkit.packets.DataWatcherSerializer;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.features.BossBar.BossBarLine;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore.Action;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;

public class PacketAPI{

	public static Object buildPacket(UniversalPacketPlayOut packet, ProtocolVersion version) {
		try {
			return Shared.mainClass.buildPacket(packet, version);
		} catch (Throwable e) {
			return Shared.errorManager.printError(null, "An error occurred when creating " + packet.getClass().getSimpleName(), e);
		}
	}
	
	//scoreboard team
	public static synchronized void registerScoreboardTeam(ITabPlayer to, String teamName, String prefix, String suffix, boolean enumNameTagVisibility, boolean enumTeamPush, Collection<String> players) {
		if (to.getVersion().getMinorVersion() >= 8 && Configs.SECRET_safe_register && Shared.separatorType.equals("world")) {
			unregisterScoreboardTeam(to, teamName);
		}
		to.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName, prefix, suffix, enumNameTagVisibility?"always":"never", enumTeamPush?"always":"never", players, 69));
	}
	public static void unregisterScoreboardTeam(ITabPlayer to, String teamName) {
		to.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName).setTeamOptions(69));
	}
	public static void updateScoreboardTeamPrefixSuffix(ITabPlayer to, String teamName, String prefix, String suffix, boolean enumNameTagVisibility, boolean enumTeamPush) {
		to.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName, prefix, suffix, enumNameTagVisibility?"always":"never", enumTeamPush?"always":"never", 69));
	}

	//scoreboard objective
	public static synchronized void registerScoreboardObjective(ITabPlayer to, String objectiveName, String title, int position, EnumScoreboardHealthDisplay displayType) {
		if (to.getVersion().getMinorVersion() >= 8 && Configs.SECRET_safe_register) {
			unregisterScoreboardObjective(to, objectiveName);
		}
		to.sendCustomPacket(new PacketPlayOutScoreboardObjective(objectiveName, title, displayType, 0));
		to.sendCustomPacket(new PacketPlayOutScoreboardDisplayObjective(position, objectiveName));
	}
	public static void unregisterScoreboardObjective(ITabPlayer to, String objectiveName) {
		to.sendCustomPacket(new PacketPlayOutScoreboardObjective(objectiveName));
	}
	public static void changeScoreboardObjectiveTitle(ITabPlayer p, String objectiveName, String title, EnumScoreboardHealthDisplay displayType) {
		p.sendCustomPacket(new PacketPlayOutScoreboardObjective(objectiveName, title, displayType, 2));
	}

	//scoreboard score
	public static void registerScoreboardScore(ITabPlayer p, String team, String fakeplayer, String prefix, String suffix, String objective, int score) {
		registerScoreboardTeam(p, team, prefix, suffix, false, false, Arrays.asList(fakeplayer));
		setScoreboardScore(p, fakeplayer, objective, score);
	}
	public static void removeScoreboardScore(ITabPlayer p, String fakeplayer, String objective) {
		p.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.REMOVE, objective, fakeplayer, 0));
		unregisterScoreboardTeam(p, objective);
	}
	public static void setScoreboardScore(ITabPlayer to, String fakeplayer, String objective, int score) {
		to.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, objective, fakeplayer, score));
	}
	
	
	
	private static final int NAME_POSITION = ProtocolVersion.SERVER_VERSION.getMinorVersion() == 8 ? 2 : ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 6 ? 10 : 5;
	public static void createBossBar(ITabPlayer to, BossBarLine bar){
		to.setProperty("bossbar-text-"+bar.getName(), bar.text, null);
		to.setProperty("bossbar-progress-"+bar.getName(), bar.progress, null);
		to.setProperty("bossbar-color-"+bar.getName(), bar.color, null);
		to.setProperty("bossbar-style-"+bar.getName(), bar.style, null);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			to.sendCustomPacket(new PacketPlayOutBoss(bar.getUniqueId(), 
					to.properties.get("bossbar-text-"+bar.getName()).get(), 
					(float)bar.parseProgress(to.properties.get("bossbar-progress-"+bar.getName()).get())/100, 
					bar.parseColor(to.properties.get("bossbar-color-"+bar.getName()).get()), 
					bar.parseStyle(to.properties.get("bossbar-style-"+bar.getName()).get())));
		} else {
			Location l = to.getBukkitEntity().getEyeLocation().add(to.getBukkitEntity().getEyeLocation().getDirection().normalize().multiply(25));
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
			to.sendCustomBukkitPacket(packet);
		}
	}
	public static void removeBossBar(ITabPlayer to, BossBarLine bar) {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			to.sendCustomPacket(new PacketPlayOutBoss(bar.getUniqueId()));
		} else {
			to.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityDestroy(bar.getEntityId()));
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
			boolean update = false;
			if (text.isUpdateNeeded()) {
				w.setValue(new DataWatcherObject(NAME_POSITION, DataWatcherSerializer.String), text.get());
				update = true;
			}
			if (progress.isUpdateNeeded()) {
				float health = (float)3*bar.parseProgress(progress.get());
				if (health == 0) health = 1;
				if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 6) {
					w.setValue(new DataWatcherObject(6, DataWatcherSerializer.Float), health);
				} else {
					w.setValue(new DataWatcherObject(16, DataWatcherSerializer.Integer), (int)health);
				}
				update = true;
			}
			if (update) to.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityMetadata(bar.getEntityId(), w.toNMS(), true));
		}
	}
}