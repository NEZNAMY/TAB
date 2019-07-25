package me.neznamy.tab.shared;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import me.neznamy.tab.shared.Shared.ServerType;

public class Configs {

	public static ConfigurationFile config;
	public static boolean unlimitedTags;
	public static boolean modifyNPCnames;
	public static boolean collision;
	public static HashMap<String, String> sortedGroups;
	public static Map<String, Object> rankAliases;
	public static boolean doNotMoveSpectators;
	public static List<String> disabledHeaderFooter;
	public static List<String> disabledTablistNames;
	public static List<String> disabledNametag;
	public static List<String> disabledTablistObjective;
	public static List<String> disabledBossbar;
	public static String dateFormat;
	public static String timeFormat;
	public static double timeOffset;
	public static List<String> staffGroups = new ArrayList<String>();
	
	
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
	public static List<String> help_menu = new ArrayList<String>();
	public static String bossbar_off;
	public static String bossbar_on;
	
	
	public static ConfigurationFile advancedconfig;
	public static boolean sortByNickname = false;
	public static boolean sortByPermissions = false;
	public static boolean fixPetNames = false;
	public static boolean usePrimaryGroup = true;
	public static List<String> primaryGroupFindingList = Lists.newArrayList("Owner", "Admin", "Helper", "default");
	
	
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
		loadAdvancedConfig();
	}
	public static void loadConfig() throws Exception {
		if (Shared.servertype == ServerType.BUKKIT) {
			me.neznamy.tab.bukkit.IConfigs.loadConfig();
		}
		if (Shared.servertype == ServerType.BUNGEE) {
			me.neznamy.tab.bungee.IConfigs.loadConfig();
		}
		HeaderFooter.enable = config.getBoolean("enable-header-footer", true);
		collision = config.getBoolean("enable-collision", true);
		timeFormat = config.getString("placeholders.time-format", "[HH:mm:ss / h:mm a]");
		timeOffset = config.getDouble("placeholders.time-offset", 0);
		dateFormat = config.getString("placeholders.date-format", "dd.MM.yyyy");
		doNotMoveSpectators = config.getBoolean("do-not-move-spectators", false);
		sortedGroups = new LinkedHashMap<String, String>();
		int index = 1;
		for (String group : config.getStringList("group-sorting-priority-list", Lists.newArrayList("Owner", "Admin", "Mod", "Helper", "Builder", "Premium", "Player", "default"))){
			String sort = index+"";
			while (sort.length()<4) {
				sort = "0" + sort;
			}
			sortedGroups.put(group, sort);
			index++;
		}
		staffGroups = config.getStringList("staff-groups", Lists.newArrayList("Admin", "Mod", "Owner", "Moderator", "Helper"));
		Map<String, Object> cs = Configs.config.getConfigurationSection("rank-aliases");
		if (cs != null) {
			Configs.rankAliases = cs;
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
			Configs.config.set("rank-aliases", Configs.rankAliases = map);
			Configs.config.save();
		}
	}
	public static void loadAnimations() throws Exception {
		animation = new ConfigurationFile("animations.yml");
		animations = new ArrayList<Animation>();
		if (animation.getConfigurationSection("animations") != null) {
			for (String s : animation.getConfigurationSection("animations").keySet())
				animations.add(new Animation(s, animation.getStringList("animations." + s + ".texts"), animation.getInt("animations." + s + ".change-interval", 1000)));
		}
	}
	public static void loadBossbar() throws Exception {
		if (Shared.servertype == ServerType.BUKKIT) {
			me.neznamy.tab.bukkit.IConfigs.loadBossbar();
		} else {
			me.neznamy.tab.bungee.IConfigs.loadBossbar();
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
		help_menu = translation.getStringList("help_menu");
		bossbar_on = translation.getString("bossbar-toggle-on", "&2Bossbar is now visible").replace("&", "§");
		bossbar_off = translation.getString("bossbar-toggle-off", "&7Bossbar is no longer visible. Magic!").replace("&", "§");
	}
	public static void loadAdvancedConfig() throws Exception {
		if (Shared.servertype == ServerType.BUKKIT) {
			me.neznamy.tab.bukkit.IConfigs.loadAdvancedConfig();
		}
	}
}