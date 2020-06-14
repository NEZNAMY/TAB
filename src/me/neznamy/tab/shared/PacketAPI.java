package me.neznamy.tab.shared;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.entity.EntityType;

import me.neznamy.tab.platforms.bukkit.features.BossBar_legacy;
import me.neznamy.tab.platforms.bukkit.packets.DataWatcher;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.features.BossBar.BossBarLine;
import me.neznamy.tab.shared.packets.EnumChatFormat;
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
	public static void registerScoreboardTeam(ITabPlayer to, String teamName, String prefix, String suffix, boolean enumNameTagVisibility, boolean enumTeamPush, Collection<String> players, EnumChatFormat color) {
		if (to.getVersion().getMinorVersion() >= 8 && Configs.SECRET_safe_register && Shared.separatorType.equals("world")) {
			to.sendCustomPacket(PacketPlayOutScoreboardTeam.REMOVE_TEAM(teamName).setTeamOptions(69));
		}
		to.sendCustomPacket(PacketPlayOutScoreboardTeam.CREATE_TEAM(teamName, prefix, suffix, enumNameTagVisibility?"always":"never", enumTeamPush?"always":"never", players, 69).setColor(color));
	}
	public static void updateScoreboardTeamPrefixSuffix(ITabPlayer to, String teamName, String prefix, String suffix, boolean enumNameTagVisibility, boolean enumTeamPush) {
		to.sendCustomPacket(PacketPlayOutScoreboardTeam.UPDATE_TEAM_INFO(teamName, prefix, suffix, enumNameTagVisibility?"always":"never", enumTeamPush?"always":"never", 69));
	}

	//scoreboard objective
	public static void registerScoreboardObjective(ITabPlayer to, String objectiveName, String title, int position, EnumScoreboardHealthDisplay displayType) {
		if (to.getVersion().getMinorVersion() >= 8 && Configs.SECRET_safe_register) {
			to.sendCustomPacket(PacketPlayOutScoreboardObjective.UNREGISTER(objectiveName));
		}
		to.sendCustomPacket(PacketPlayOutScoreboardObjective.REGISTER(objectiveName, title, displayType));
		to.sendCustomPacket(new PacketPlayOutScoreboardDisplayObjective(position, objectiveName));
	}

	//scoreboard score
	public static void registerScoreboardScore(ITabPlayer p, String team, String fakeplayer, String prefix, String suffix, String objective, int score) {
		registerScoreboardTeam(p, team, prefix, suffix, false, false, Arrays.asList(fakeplayer), EnumChatFormat.RESET);
		setScoreboardScore(p, fakeplayer, objective, score);
	}
	public static void removeScoreboardScore(ITabPlayer p, String fakeplayer, String objective) {
		p.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.REMOVE, objective, fakeplayer, 0));
		p.sendCustomPacket(PacketPlayOutScoreboardTeam.REMOVE_TEAM(objective).setTeamOptions(69));
	}
	public static void setScoreboardScore(ITabPlayer to, String fakeplayer, String objective, int score) {
		to.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, objective, fakeplayer, score));
	}
	
	
	
	public static void createBossBar(ITabPlayer to, BossBarLine bar){
		to.setProperty("bossbar-text-"+bar.getName(), bar.text, null);
		to.setProperty("bossbar-progress-"+bar.getName(), bar.progress, null);
		to.setProperty("bossbar-color-"+bar.getName(), bar.color, null);
		to.setProperty("bossbar-style-"+bar.getName(), bar.style, null);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			to.sendCustomPacket(PacketPlayOutBoss.CREATE(bar.getUniqueId(), 
					to.properties.get("bossbar-text-"+bar.getName()).get(), 
					(float)bar.parseProgress(to.properties.get("bossbar-progress-"+bar.getName()).get())/100, 
					bar.parseColor(to.properties.get("bossbar-color-"+bar.getName()).get()), 
					bar.parseStyle(to.properties.get("bossbar-style-"+bar.getName()).get())));
		} else {
			PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(bar.getEntityId(), null, EntityType.WITHER, ((BossBar_legacy)Shared.features.get("bossbar1.8")).getWitherLocation(to));
			DataWatcher w = new DataWatcher(null);
			DataWatcher.Helper.setEntityFlags(w, (byte) 32);
			DataWatcher.Helper.setCustomName(w, to.properties.get("bossbar-text-"+bar.getName()).get(), to.getVersion());
			float health = (float)3*bar.parseProgress(to.properties.get("bossbar-progress-"+bar.getName()).get());
			if (health == 0) health = 1;
			DataWatcher.Helper.setHealth(w, health);
			packet.setDataWatcher(w);
			to.sendCustomBukkitPacket(packet);
		}
	}
	public static void removeBossBar(ITabPlayer to, BossBarLine bar) {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			to.sendCustomPacket(PacketPlayOutBoss.REMOVE(bar.getUniqueId()));
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
				to.sendCustomPacket(PacketPlayOutBoss.UPDATE_STYLE(bar.getUniqueId(), bar.parseColor(color.get()), bar.parseStyle(style.get())));
			}
			if (progress.isUpdateNeeded()) {
				to.sendCustomPacket(PacketPlayOutBoss.UPDATE_PCT(bar.getUniqueId(), (float)bar.parseProgress(progress.get())/100));
			}
			if (text.isUpdateNeeded()) {
				to.sendCustomPacket(PacketPlayOutBoss.UPDATE_NAME(bar.getUniqueId(), text.get()));
			}
		} else {
			DataWatcher w = new DataWatcher(null);
			boolean update = false;
			if (text.isUpdateNeeded()) {
				DataWatcher.Helper.setCustomName(w, text.get(), to.getVersion());
				update = true;
			}
			if (progress.isUpdateNeeded()) {
				float health = (float)3*bar.parseProgress(progress.get());
				if (health == 0) health = 1;
				DataWatcher.Helper.setHealth(w, health);
				update = true;
			}
			if (update) to.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityMetadata(bar.getEntityId(), w.toNMS(), true));
		}
	}
}