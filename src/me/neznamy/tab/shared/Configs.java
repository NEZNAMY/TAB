package me.neznamy.tab.shared;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.BossBar.BossBarFrame;
import me.neznamy.tab.shared.BossBar.BossBarLine;
import me.neznamy.tab.shared.Shared.ServerType;

public class Configs {

	public static ConfigurationFile config;
	public static boolean unlimitedTags;
	public static boolean modifyNPCnames;
	public static boolean collision;
	public static HashMap<String, String> sortedGroups;
	public static Map<String, Object> rankAliases;
	public static boolean doNotMoveSpectators;
	public static List<Object> disabledHeaderFooter;
	public static List<Object> disabledTablistNames;
	public static List<Object> disabledNametag;
	public static List<Object> disabledTablistObjective;
	public static List<Object> disabledBossbar;
	public static String dateFormat;
	public static String timeFormat;
	public static double timeOffset;
	
	
	public static ConfigurationFile animation;
	public static List<Animation> animations;
	
	
	public static ConfigurationFile bossbar;
	public static String bossbarToggleCommand;
	
	
	public static ConfigurationFile translation;
	public static String no_perm;
	public static String value_too_long;
	public static String unlimited_nametag_mode_not_enabled;
	public static String data_removed;
	public static String player_not_found;
	public static String reloaded;
	public static String value_assigned;
	public static String value_removed;
	public static String plugin_disabled = "§c[TAB] Plugin is disabled because one of your configuration files is broken. Check console for more info.";
	public static List<Object> help_menu = new ArrayList<Object>();
	public static String bossbar_off;
	public static String bossbar_on;
	
	public static ConfigurationFile advancedconfig;
	public static boolean sortByNickname = false;
	public static boolean sortByPermissions = false;
	public static boolean fixPetNames = false;
	public static boolean usePrimaryGroup = true;
	public static List<? extends Object> primaryGroupFindingList = Lists.newArrayList("Owner", "Admin", "Helper", "default");
	
	
	public static File errorFile = new File(ConfigurationFile.dataFolder, "errors.txt");

	public static void loadFiles() throws Exception {
		if (errorFile.exists()) {
			if (errorFile.length() > 10000000) {
				errorFile.delete();
			}
		}
		loadConfig();
		loadAnimations();
		loadBossbar();
		loadTranslation();
		if (Premium.is()) Premium.loadPremiumConfig();
	}
	
	public static void loadConfig() throws Exception {
		Shared.mainClass.loadConfig();
		HeaderFooter.enable = config.getBoolean("enable-header-footer", true);
		collision = config.getBoolean("enable-collision", true);
		timeFormat = config.getString("placeholders.time-format", "[HH:mm:ss / h:mm a]");
		timeOffset = config.getDouble("placeholders.time-offset", 0);
		dateFormat = config.getString("placeholders.date-format", "dd.MM.yyyy");
		doNotMoveSpectators = config.getBoolean("do-not-move-spectators", false);
		sortedGroups = new LinkedHashMap<String, String>();
		int index = 1;
		for (Object group : config.getList("group-sorting-priority-list", Lists.newArrayList("Owner", "Admin", "Mod", "Helper", "Builder", "Premium", "Player", "default"))){
			String sort = index+"";
			while (sort.length()<4) {
				sort = "0" + sort;
			}
			sortedGroups.put(group+"", sort);
			index++;
		}
		Map<String, Object> cs = config.getConfigurationSection("rank-aliases");
		if (cs != null) {
			rankAliases = cs;
		} else {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("Admin", "&4&lADMIN");
			map.put("Mod", "&b&lMOD");
			map.put("Premium", "&6&lPREMIUM");
			map.put("Ultra", "&b&lULTRA");
			map.put("Legend", "&a&lLEGEND");
			map.put("Titan", "&c&lTITAN");
			map.put("Youtuber", "&c&lYOUTUBE");
			map.put("_OTHER_", "%vault-prefix%");
			config.set("rank-aliases", rankAliases = map);
			config.save();
		}
		disabledHeaderFooter = config.getList("disable-features-in-"+Shared.mainClass.getSeparatorType()+"s.header-footer", Lists.newArrayList("disabled" + Shared.mainClass.getSeparatorType()));
		disabledTablistNames = config.getList("disable-features-in-"+Shared.mainClass.getSeparatorType()+"s.tablist-names", Lists.newArrayList("disabled" + Shared.mainClass.getSeparatorType()));
		disabledNametag = config.getList("disable-features-in-"+Shared.mainClass.getSeparatorType()+"s.nametag", Lists.newArrayList("disabled" + Shared.mainClass.getSeparatorType()));
		disabledTablistObjective = config.getList("disable-features-in-"+Shared.mainClass.getSeparatorType()+"s.tablist-objective", Lists.newArrayList("disabled" + Shared.mainClass.getSeparatorType()));
		disabledBossbar = config.getList("disable-features-in-"+Shared.mainClass.getSeparatorType()+"s.bossbar", Lists.newArrayList("disabled" + Shared.mainClass.getSeparatorType()));
	}
	public static void loadAnimations() throws Exception {
		animation = new ConfigurationFile("animations.yml");
		animations = new ArrayList<Animation>();
		if (animation.getConfigurationSection("animations") != null) {
			for (String s : animation.getConfigurationSection("animations").keySet())
				animations.add(new Animation(s, animation.getList("animations." + s + ".texts"), animation.getInt("animations." + s + ".change-interval", 1000)));
		}
	}
	public static void loadBossbar() throws Exception {
		if (Shared.servertype == ServerType.BUKKIT) {
			bossbar = new ConfigurationFile("bukkitbossbar.yml", "bossbar.yml");
			BossBar.refresh = (bossbar.getInt("refresh-interval", 20)*50);
		} else {
			bossbar = new ConfigurationFile("bungeebossbar.yml", "bossbar.yml");
			BossBar.refresh = bossbar.getInt("refresh-interval", 1000);
		}
		BossBar.enable = bossbar.getBoolean("enabled", false);
		bossbarToggleCommand = bossbar.getString("bossbar-toggle-command", "/bossbar");
		BossBar.lines.clear();
		if (bossbar.getConfigurationSection("bars") != null) {
			for (String bar : bossbar.getConfigurationSection("bars").keySet()){
				List<BossBarFrame> frames = new ArrayList<BossBarFrame>();
				for (String frame : bossbar.getConfigurationSection("bars." + bar + ".frames").keySet()){
					String style = bossbar.getString("bars." + bar + ".frames." + frame + ".style");
					String color = bossbar.getString("bars." + bar + ".frames." + frame + ".color");
					String progress = bossbar.getString("bars." + bar + ".frames." + frame + ".progress");
					String message = bossbar.getString("bars." + bar + ".frames." + frame + ".text");
					frames.add(new BossBarFrame(style, color, progress, message));
				}
				if (!frames.isEmpty()) BossBar.lines.add(new BossBarLine(bossbar.getInt("bars." + bar + ".refresh", 1000), frames));
			}
		}
	}
	public static void loadTranslation() throws Exception {
		translation = new ConfigurationFile("translation.yml");
		no_perm = translation.getString("no_permission", "&cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.").replace("&", "§");
		value_too_long = translation.getString("value_too_long", "&c[TAB] Error! %type% cannot be longer than 16 characters! (You attempted to use %length% characters). If you with to completely remove the limit, enable unlimited-nametag-prefix-suffix-mode in config.").replace("&", "§");
		unlimited_nametag_mode_not_enabled = translation.getString("unlimited_nametag_mode_not_enabled", "&c[TAB] Warning! To make these work, you need to enable unlimited-nametag-prefix-suffix-mode in config !").replace("&", "§");
		data_removed = translation.getString("data_removed", "&3[TAB] All data has been successfully removed from %category% §e%value%").replace("&", "§");
		player_not_found = translation.getString("player_not_found", "&4[TAB] Player not found !").replace("&", "§");
		reloaded = translation.getString("reloaded", "&3[TAB] Reloaded").replace("&", "§");
		value_assigned = translation.getString("value_assigned", "&3[TAB] %type% &r'%value%'&r&3 has been successfully assigned to %category% &e%unit%").replace("&", "§");
		value_removed = translation.getString("value_removed", "&3[TAB] %type% has been successfully removed from %category% &e%unit%").replace("&", "§");
		help_menu = translation.getList("help_menu");
		bossbar_on = translation.getString("bossbar-toggle-on", "&2Bossbar is now visible").replace("&", "§");
		bossbar_off = translation.getString("bossbar-toggle-off", "&7Bossbar is no longer visible. Magic!").replace("&", "§");
	}
}