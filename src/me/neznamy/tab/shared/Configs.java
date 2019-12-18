package me.neznamy.tab.shared;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.BossBar.BossBarLine;

public class Configs {

	public static ConfigurationFile config;
	public static HashMap<String, List<String>> configComments;
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
	public static List<String> disabledBelowname;
	public static SimpleDateFormat dateFormat;
	public static SimpleDateFormat timeFormat;
	public static double timeOffset;
	public static List<String> removeStrings = new ArrayList<String>();
	public static String noFaction;
	public static String yesFaction;
	public static String noTag;
	public static String yesTag;
	public static String noAfk;
	public static String yesAfk;
	public static double SECRET_NTX_space;
	public static int SECRET_relational_placeholders_refresh;
	public static boolean SECRET_invisible_nametags;
	public static boolean SECRET_safe_register;
	public static boolean SECRET_remove_ghost_players;
	public static boolean SECRET_log_errors_into_console;
	public static boolean SECRET_armorstands_always_visible;


	public static ConfigurationFile animation;
	public static HashMap<String, List<String>> animationComments;
	public static List<Animation> animations;


	public static ConfigurationFile bossbar;
	public static HashMap<String, List<String>> bossbarComments;


	public static ConfigurationFile translation;
	public static String no_perm;
	public static String unlimited_nametag_mode_not_enabled;
	public static String data_removed;
	public static String player_not_found;
	public static String reloaded;
	public static String value_assigned;
	public static String value_removed;
	public static String plugin_disabled = "&c[TAB] Plugin is disabled because one of your configuration files is broken. Check console for more info.";
	public static List<String> help_menu = new ArrayList<String>();
	public static String bossbar_off;
	public static String bossbar_on;
	public static String preview_off;
	public static String preview_on;

	public static ConfigurationFile advancedconfig;
	public static HashMap<String, List<String>> advancedconfigComments;
	public static boolean sortByPermissions = false;
	public static boolean fixPetNames = false;
	public static boolean usePrimaryGroup = true;
	public static List<String> primaryGroupFindingList = Arrays.asList("Owner", "Admin", "Helper", "default");


	public static File errorFile = new File(ConfigurationFile.dataFolder, "errors.txt");

	static {
		configComments = new HashMap<String, List<String>>();
		configComments.put("nametag-refresh-interval-ticks", Arrays.asList("", "#20 ticks = 1 second"));
		configComments.put("tablist-objective:", Arrays.asList("#the yellow number in tablist", "#options: PING, HEARTS, NONE (to disable it), CUSTOM (any placeholder defined below)"));
		configComments.put("tablist-objective-value:", Arrays.asList("#the yellow number in tablist", "#set to \"\" to disable"));
		configComments.put("group-sorting-priority-list", Arrays.asList("#NOT case sensitive"));
		configComments.put("Groups:", Arrays.asList("#properties: tabprefix, tabsuffix, tagprefix (in name tag), tagsuffix, customtabname (modifying the name itself), header, footer", "#extra ones which need unlimited nametag mode enabled: abovename (line of text above name tag), belowname (below name tag), customtagname"));
		configComments.put("  _OTHER_:", Arrays.asList("  #any other group not defined above"));
		configComments.put("Users:", Arrays.asList("", "#personal settings, override group settings"));
		configComments.put("enable-collision:", Arrays.asList("", "#servers and clients 1.9+"));
		configComments.put("do-not-move-spectators", Arrays.asList("", "#preventing players in spectator gamemode from appearing at the bottom of tablist with transparent name FOR OTHER PLAYERS"));
		configComments.put("unlimited-nametag-prefix-suffix-mode:", Arrays.asList("#VERY EXPERIMENTAL !", "#IF YOU EXPERIENCE ANY ISSUES CONTACT ME"));
		configComments.put("  modify-npc-names:", Arrays.asList("  #modifying names of NPCs to avoid empty names on NPCs of online players", "  #this blocks any other attemps to change the name visibility or anything, so you need to disable it if you are using some citizens addon that changes their name", "  #needs relog to see the change"));
		configComments.put("per-world-settings:", Arrays.asList("", "#list of worlds with different settings than default, other worlds will use settings from above"));
		configComments.put("per-server-settings:", Arrays.asList("", "#list of servers with different settings than default, other servers will use settings from above"));
		configComments.put("placeholders:", Arrays.asList("#setting output of some placeholders"));
		configComments.put("  time-offset:", Arrays.asList("  #if time doesn't show correctly, you can change the time it shows", "  #setting to -1 will make it show 1 hour less than it does currently, 1 makes it show one hour more than currently, 0 does nothing"));
		configComments.put("  remove-strings:", Arrays.asList("  #remove these strings from everywhere. Typically an empty output when using some clan/faction related plugin but player isn't in any", "  #so players won't have empty brackets before their names"));
		configComments.put("rank-aliases:", Arrays.asList("", "#better showing of %rank% placeholder"));
		configComments.put("disable-features", Arrays.asList(""));
		configComments.put("belowname:", Arrays.asList("", "#the vanilla belowname feature", "#doesn't support player placeholders (faction, prefix), only server placeholders (time, date, animations) and static text"));
		bossbarComments = new HashMap<String, List<String>>();
		bossbarComments.put("bossbar-enabled:", Arrays.asList("#styles (SERVER 1.9+)", "#NOTCHED_6, NOTCHED_10, NOTCHED_12, NOTCHED_20, PROGRESS", "", "#colors (SERVER 1.9+)", "#BLUE, GREEN, PINK, PURPLE, RED, WHITE, YELLOW", "", "#IF YOU ARE USING SERVER 1.8.X", "#the entity will be slightly visible when progress is less than ~50% (client-sided bug)", "#only 1 line can be displayed at a time", "", "#you can also announce a message using /tab announce bar <bar name> <duration in seconds> (does not support animations yet)", ""));
		bossbarComments.put("default-bars:", Arrays.asList("#to have no default bars, set it to ", "#default-bars: []"));
		animationComments = new HashMap<String, List<String>>();
		animationComments.put("animations:", Arrays.asList("#usage: %animation:NAME%  or  {animation:NAME}"));
		advancedconfigComments = new HashMap<String, List<String>>();
		advancedconfigComments.put("per-world-playerlist:", Arrays.asList("#players will only see in tablist those who are in the same world"));
		advancedconfigComments.put("allow-pwp-bypass-permission:", Arrays.asList("", "#allow players with tab.bypass permission to see every player in tablist even if the setting above is enabled"));
		advancedconfigComments.put("ignore-pwp-in-worlds:", Arrays.asList("", "#even if per-world-playerlist is enabled, people in these worlds will see everyone on server in tablist"));
		advancedconfigComments.put("sort-players-by-permissions:", Arrays.asList("", "#sorting by permissions. Permission node is \"tab.sort.<group>\" and priorities can be set in config"));
		advancedconfigComments.put("fix-pet-names:", Arrays.asList("", "#an option to remove minecraft vanilla feature since 1.9 making named pets have same prefix as their owner (and being invisible when unlimited nametag mode is enabled)", "#needs relog to see the change"));
		advancedconfigComments.put("use-primary-group:", Arrays.asList("", "#asking permission plugin for primary group only"));
		advancedconfigComments.put("primary-group-finding-list:", Arrays.asList("", "#if the option above is disabled, full group list will be asked for and group higher in this list will be used as primary"));
	}
	public static void loadFiles() throws Exception {
		if (errorFile.exists()) {
			if (errorFile.length() > 10000000) {
				errorFile.delete();
			}
		}
		loadConfig();
		SECRET_relational_placeholders_refresh = getSecretOption("relational-placeholders-refresh", 30);
		SECRET_NTX_space = getSecretOption("ntx-space", 0.22F);
		SECRET_invisible_nametags = getSecretOption("invisible-nametags", false);
		SECRET_safe_register = getSecretOption("safe-team-register", true);
		SECRET_remove_ghost_players = getSecretOption("remove-ghost-players", false);
		SECRET_log_errors_into_console = getSecretOption("log-errors-into-console", false);
		SECRET_armorstands_always_visible = getSecretOption("unlimited-nametag-prefix-suffix-mode.always-visible", false);
		loadAnimations();
		loadBossbar();
		loadTranslation();
		if (Premium.is()) Premium.loadPremiumConfig();
	}

	public static void loadConfig() throws Exception {
		Shared.mainClass.loadConfig();
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			HeaderFooter.enable = config.getBoolean("enable-header-footer", true);
			Playerlist.enable = config.getBoolean("change-tablist-prefix-suffix", true);
		}
		collision = config.getBoolean("enable-collision", true);
		timeFormat = new SimpleDateFormat(config.getString("placeholders.time-format", "[HH:mm:ss / h:mm a]"));
		timeOffset = config.getDouble("placeholders.time-offset", 0);
		dateFormat = new SimpleDateFormat(config.getString("placeholders.date-format", "dd.MM.yyyy"));
		doNotMoveSpectators = config.getBoolean("do-not-move-spectators", false);
		sortedGroups = new LinkedHashMap<String, String>();
		int index = 1;
		for (String group : config.getStringList("group-sorting-priority-list", Arrays.asList("Owner", "Admin", "Mod", "Helper", "Builder", "Premium", "Player", "default"))){
			String sort = index+"";
			while (sort.length()<4) {
				sort = "0" + sort;
			}
			sortedGroups.put(group.toLowerCase()+"", sort);
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
		disabledHeaderFooter = config.getStringList("disable-features-in-"+Shared.separatorType+"s.header-footer", Arrays.asList("disabled" + Shared.separatorType));
		disabledTablistNames = config.getStringList("disable-features-in-"+Shared.separatorType+"s.tablist-names", Arrays.asList("disabled" + Shared.separatorType));
		disabledNametag = config.getStringList("disable-features-in-"+Shared.separatorType+"s.nametag", Arrays.asList("disabled" + Shared.separatorType));
		disabledTablistObjective = config.getStringList("disable-features-in-"+Shared.separatorType+"s.tablist-objective", Arrays.asList("disabled" + Shared.separatorType));
		disabledBossbar = config.getStringList("disable-features-in-"+Shared.separatorType+"s.bossbar", Arrays.asList("disabled" + Shared.separatorType));
		disabledBelowname = config.getStringList("disable-features-in-"+Shared.separatorType+"s.belowname", Arrays.asList("disabled" + Shared.separatorType));
	}
	public static void loadAnimations() throws Exception {
		animation = new ConfigurationFile("animations.yml", animationComments);
		animations = new ArrayList<Animation>();
		if (animation.getConfigurationSection("animations") != null) {
			for (String s : animation.getConfigurationSection("animations").keySet())
				animations.add(new Animation(s, animation.getStringList("animations." + s + ".texts"), animation.getInt("animations." + s + ".change-interval")));
		}
	}
	@SuppressWarnings("unchecked")
	public static void loadBossbar() throws Exception {
		bossbar = new ConfigurationFile("bossbar.yml", bossbarComments);
		if (bossbar.get("enabled") != null) {
			Shared.startupWarn("You are using old bossbar config, please make a backup of the file and delete it to get new file.");
			return;
		}
		BossBar.enabled = bossbar.getBoolean("bossbar-enabled", false);
		BossBar.refresh = bossbar.getInt("refresh-interval-milliseconds", 1000);
		BossBar.toggleCommand = bossbar.getString("bossbar-toggle-command", "/bossbar");
		BossBar.defaultBars = bossbar.getStringList("default-bars");
		BossBar.perWorld = (Map<String, List<String>>) bossbar.get("per-world");
		if (BossBar.perWorld == null) BossBar.perWorld = new HashMap<String, List<String>>();
		BossBar.lines.clear();
		if (bossbar.getConfigurationSection("bars") != null) {
			for (String bar : bossbar.getConfigurationSection("bars").keySet()){
				boolean permissionRequired = bossbar.getBoolean("bars." + bar + ".permission-required");
				int refresh = bossbar.getInt("bars." + bar + ".refresh");
				String style = bossbar.getString("bars." + bar + ".style");
				String color = bossbar.getString("bars." + bar + ".color");
				Object progress = bossbar.get("bars." + bar + ".progress");
				String text = bossbar.getString("bars." + bar + ".text");
				if (progress == null) {
					Shared.startupWarn("BossBar \"&e" + bar + "&c\" is missing \"&eprogress&c\" attribute! &bUsing 100");
					progress = 100;
				}
				BossBar.lines.add(new BossBarLine(bar, permissionRequired, refresh, color, style, text, progress+""));
			}
		}
		List<String> toRemove = new ArrayList<String>();
		for (String bar : BossBar.defaultBars) {
			if (BossBar.getLine(bar) == null) {
				Shared.startupWarn("BossBar \"&e" + bar + "&c\" is defined as default bar, but does not exist! &bIgnoring.");
				toRemove.add(bar);
			}
		}
		BossBar.defaultBars.removeAll(toRemove);
	}
	public static void loadTranslation() throws Exception {
		translation = new ConfigurationFile("translation.yml", new HashMap<String, List<String>>());
		no_perm = translation.getString("no_permission", "&cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
		unlimited_nametag_mode_not_enabled = translation.getString("unlimited_nametag_mode_not_enabled", "&c[TAB] Warning! To make these work, you need to enable unlimited-nametag-prefix-suffix-mode in config !");
		data_removed = translation.getString("data_removed", "&3[TAB] All data has been successfully removed from %category% &e%value%");
		player_not_found = translation.getString("player_not_found", "&4[TAB] Player not found !");
		reloaded = translation.getString("reloaded", "&3[TAB] Reloaded");
		value_assigned = translation.getString("value_assigned", "&3[TAB] %type% &r'%value%'&r&3 has been successfully assigned to %category% &e%unit%");
		value_removed = translation.getString("value_removed", "&3[TAB] %type% has been successfully removed from %category% &e%unit%");
		help_menu = translation.getStringList("help_menu");
		bossbar_on = translation.getString("bossbar-toggle-on", "&2Bossbar is now visible");
		bossbar_off = translation.getString("bossbar-toggle-off", "&7Bossbar is no longer visible. Magic!");
		preview_on = translation.getString("preview-on", "&7Preview mode &aactivated.");
		preview_off = translation.getString("preview-off", "&7Preview mode &3deactivated.");
	}
	@SuppressWarnings("unchecked")
	public static <T> T getSecretOption(String path, T defaultValue) {
		Object value = config.get(path);
		if (value == null) return defaultValue;
		if (defaultValue instanceof Integer) return (T) (Object) Integer.parseInt(value+"");
		if (defaultValue instanceof Float) return (T) (Object) Float.parseFloat(value+"");
		if (defaultValue instanceof Double) return (T) (Object) Double.parseDouble(value+"");
		if (defaultValue instanceof Long) return (T) (Object) Long.parseLong(value+"");
		if (defaultValue instanceof String) return (T) (value+"");
		return (T) value;
	}
}