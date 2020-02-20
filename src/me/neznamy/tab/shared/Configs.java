package me.neznamy.tab.shared;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.premium.ScoreboardManager;
import me.neznamy.tab.shared.features.BelowName;
import me.neznamy.tab.shared.features.BossBar;
import me.neznamy.tab.shared.features.HeaderFooter;
import me.neznamy.tab.shared.features.NameTag16;
import me.neznamy.tab.shared.features.Playerlist;
import me.neznamy.tab.shared.features.BossBar.BossBarLine;
import me.neznamy.tab.shared.placeholders.Placeholders;

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
	public static List<String> disabledBelowname;
	public static SimpleDateFormat dateFormat;
	public static SimpleDateFormat timeFormat;
	public static double timeOffset;
	public static List<String> removeStrings = new ArrayList<String>();
	public static String noAfk;
	public static String yesAfk;
	public static Map<String, Object> serverAliases;
	public static double SECRET_NTX_space;
	public static int SECRET_relational_placeholders_refresh;
	public static boolean SECRET_invisible_nametags;
	public static boolean SECRET_safe_register;
	public static boolean SECRET_remove_ghost_players;
	public static boolean SECRET_armorstands_always_visible;
	public static boolean SECRET_debugMode;
	public static String SECRET_multiWorldSeparator;
	public static String SECRET_essentials_nickname_prefix;


	public static ConfigurationFile animation;
	public static List<Animation> animations;


	public static ConfigurationFile bossbar;


	public static ConfigurationFile translation;
	public static String no_perm;
	public static String unlimited_nametag_mode_not_enabled;
	public static String data_removed;
	public static String player_not_found;
	public static String reloaded;
	public static String value_assigned;
	public static String value_removed;
	public static String plugin_disabled = "&c[TAB] Plugin is disabled because one of your configuration files is broken. Check console for more info.";
	public static String bossbar_off;
	public static String bossbar_on;
	public static String preview_off;
	public static String preview_on;

	public static ConfigurationFile advancedconfig;
	public static boolean sortByPermissions = false;
	public static boolean fixPetNames = false;
	public static boolean usePrimaryGroup = true;
	public static List<String> primaryGroupFindingList = Arrays.asList("Owner", "Admin", "Helper", "default");
	public static boolean bukkitBridgeMode;
	public static boolean groupsByPermissions;

	public static ConfigurationFile playerdata; 

	public static File errorFile = new File(ConfigurationFile.dataFolder, "errors.txt");

	public static void loadFiles() throws Exception {
		if (errorFile.exists()) {
			if (errorFile.length() > 10) {
				Shared.errorManager.startupWarn("File &e" + errorFile.getPath() + "&c exists and is not empty. Please take a look at the errors and try to correct them. You can also join our discord for assistance. After you resolve them, delete the file.");
			}
		}
		Placeholders.clearAll();
		loadConfig();
		SECRET_relational_placeholders_refresh = getSecretOption("relational-placeholders-refresh", 30);
		SECRET_NTX_space = getSecretOption("ntx-space", 0.22F);
		SECRET_invisible_nametags = getSecretOption("invisible-nametags", false);
		SECRET_safe_register = getSecretOption("safe-team-register", true);
		SECRET_remove_ghost_players = getSecretOption("remove-ghost-players", false);
		SECRET_armorstands_always_visible = getSecretOption("unlimited-nametag-prefix-suffix-mode.always-visible", false);
		SECRET_debugMode = getSecretOption("debug", false);
		SECRET_multiWorldSeparator = getSecretOption("multi-world-separator", "-");
		SECRET_essentials_nickname_prefix = getSecretOption("essentials-nickname-prefix", "");
		loadAnimations();
		loadBossbar();
		loadTranslation();
		checkAnimations(config.getValues());
		checkAnimations(bossbar.getValues());
		if (Premium.is()) {
			Premium.loadPremiumConfig();
			checkAnimations(Premium.premiumconfig.getValues());
		}

	}
	@SuppressWarnings("unchecked")
	private static void checkAnimations(Map<String, Object> values) {
		for (Entry<String, Object> entry : values.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof String || value instanceof List) {
				if (HeaderFooter.enable) {
					checkAnimation(key, "header", value, "header", HeaderFooter.refresh);
					checkAnimation(key, "footer", value, "footer", HeaderFooter.refresh);
				}
				if (NameTag16.enable || unlimitedTags) {
					checkAnimation(key, "tagprefix", value, "tagprefix", NameTag16.refresh);
					checkAnimation(key, "tagsuffix", value, "tagsuffix", NameTag16.refresh);
				}
				if (Playerlist.enable) {
					checkAnimation(key, "tabprefix", value, "tabprefix", Playerlist.refresh);
					checkAnimation(key, "tabsuffix", value, "tabsuffix", Playerlist.refresh);
				}
				if (ScoreboardManager.enabled) {
					checkAnimation(key, "title", value, "scoreboard title", ScoreboardManager.refresh);
					checkAnimation(key, "lines", value, "scoreboard", ScoreboardManager.refresh);
				}
				if (BossBar.enabled) {
					checkAnimation(key, "style", value, "bossbar style", BossBar.refresh);
					checkAnimation(key, "color", value, "bossbar color", BossBar.refresh);
					checkAnimation(key, "progress", value, "bossbar progress", BossBar.refresh);
					checkAnimation(key, "text", value, "bossbar text", BossBar.refresh);
				}
			}
			if (value instanceof Map) checkAnimations((Map<String, Object>) value);
		}
	}
	private static void checkAnimation(String key, String searching, Object value, String position, int refresh) {
		if (key.contains(searching)) {
			for (Animation a : animations) {
				if (value.toString().contains("%animation:" + a.getName() + "%")){
					if (a.getInterval() < refresh) {
						Shared.errorManager.startupWarn("Animation &e" + a.getName() + " &cused in " + position + " is refreshing faster (every &e" + a.getInterval() + "ms&c) than " + position + " (every &e" + refresh + "ms&c). This will result in animation skipping frames !");
					} else if (a.getInterval() % refresh != 0) {
						Shared.errorManager.startupWarn("Animation &e" + a.getName() + " &cused in " + position + " has refresh (every &e" + a.getInterval() + "ms&c) not divisible by refresh of " + position + " (every &e" + refresh + "ms&c). This will result in animation skipping frames !");
					}
				}
			}
		}
	}
	public static void assignPlaceholder(String placeholder) {
		if (placeholder.contains("%rel_")) return; //relational placeholders are something else
		if (!Placeholders.usedPlaceholders.contains(placeholder)) return;

		//filtering though placeholder types
		if (Placeholders.myPlayerPlaceholders.containsKey(placeholder)) {
			Placeholders.usedPlayerPlaceholders.put(placeholder, Placeholders.myPlayerPlaceholders.get(placeholder));
			return;
		}
		if (Placeholders.myServerPlaceholders.containsKey(placeholder)) {
			Placeholders.usedServerPlaceholders.put(placeholder, Placeholders.myServerPlaceholders.get(placeholder));
			return;
		}
		if (Placeholders.myServerConstants.containsKey(placeholder)) {
			Placeholders.usedServerConstants.put(placeholder, Placeholders.myServerConstants.get(placeholder));
			return;
		}
		if (placeholder.contains("animation:")) {
			String animation = placeholder.substring(11, placeholder.length()-1);
			Shared.errorManager.startupWarn("Unknown animation &e\"" + animation + "\"&c used in configuration. You need to define it in animations.yml");
			return;
		}
		Shared.mainClass.registerUnknownPlaceholder(placeholder);
	}
	public static void loadConfig() throws Exception {
		Shared.mainClass.loadConfig();
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			HeaderFooter.enable = config.getBoolean("enable-header-footer", true);
			Playerlist.enable = config.getBoolean("change-tablist-prefix-suffix", true);
		}
		NameTag16.refresh = config.getInt("nametag-refresh-interval-milliseconds", 1000);
		Playerlist.refresh = config.getInt("tablist-refresh-interval-milliseconds", 1000);
		HeaderFooter.refresh = config.getInt("header-footer-refresh-interval-milliseconds", 100);
		BelowName.enable = Configs.config.getBoolean("belowname.enabled", true);
		BelowName.refresh = Configs.config.getInt("belowname.refresh-interval-milliseconds", 200);
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
		animation = new ConfigurationFile("animations.yml", null);
		animations = new ArrayList<Animation>();
		if (animation.getConfigurationSection("animations") != null) {
			for (String s : animation.getConfigurationSection("animations").keySet())
				animations.add(new Animation(s, animation.getStringList("animations." + s + ".texts"), animation.getInt("animations." + s + ".change-interval", 0)));
		}
	}
	@SuppressWarnings("unchecked")
	public static void loadBossbar() throws Exception {
		bossbar = new ConfigurationFile("bossbar.yml", null);
		if (bossbar.get("enabled") != null) {
			Shared.errorManager.startupWarn("You are using old bossbar config, please make a backup of the file and delete it to get new file.");
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
				boolean permissionRequired = bossbar.getBoolean("bars." + bar + ".permission-required", false);
				int refresh = bossbar.getInt("bars." + bar + ".refresh", 0);
				String style = bossbar.getString("bars." + bar + ".style");
				String color = bossbar.getString("bars." + bar + ".color");
				Object progress = bossbar.get("bars." + bar + ".progress");
				String text = bossbar.getString("bars." + bar + ".text");
				if (progress == null) {
					Shared.errorManager.startupWarn("BossBar \"&e" + bar + "&c\" is missing \"&eprogress&c\" attribute! &bUsing 100");
					progress = 100;
				}
				BossBar.lines.add(new BossBarLine(bar, permissionRequired, refresh, color, style, text, progress+""));
			}
		}
		List<String> toRemove = new ArrayList<String>();
		for (String bar : BossBar.defaultBars) {
			if (BossBar.getLine(bar) == null) {
				Shared.errorManager.startupWarn("BossBar \"&e" + bar + "&c\" is defined as default bar, but does not exist! &bIgnoring.");
				toRemove.add(bar);
			}
		}
		BossBar.defaultBars.removeAll(toRemove);
		BossBar.remember_toggle_choice = bossbar.getBoolean("remember-toggle-choice", false);
		if (BossBar.remember_toggle_choice) {
			File file = new File("plugins" + File.separatorChar + "TAB" + File.separatorChar + "playerdata.yml");
			if (!file.exists()) file.createNewFile();
			playerdata = new ConfigurationFile("playerdata.yml", null);
			BossBar.bossbar_off_players = playerdata.getStringList("bossbar-off");
		}
		if (BossBar.bossbar_off_players == null) BossBar.bossbar_off_players = new ArrayList<String>();
	}
	public static void loadTranslation() throws Exception {
		translation = new ConfigurationFile("translation.yml", null);
		no_perm = translation.getString("no_permission", "&cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
		unlimited_nametag_mode_not_enabled = translation.getString("unlimited_nametag_mode_not_enabled", "&c[TAB] Warning! To make these work, you need to enable unlimited-nametag-prefix-suffix-mode in config !");
		data_removed = translation.getString("data_removed", "&3[TAB] All data has been successfully removed from %category% &e%value%");
		player_not_found = translation.getString("player_not_found", "&4[TAB] Player not found !");
		reloaded = translation.getString("reloaded", "&3[TAB] Reloaded");
		value_assigned = translation.getString("value_assigned", "&3[TAB] %type% &r'%value%'&r&3 has been successfully assigned to %category% &e%unit%");
		value_removed = translation.getString("value_removed", "&3[TAB] %type% has been successfully removed from %category% &e%unit%");
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