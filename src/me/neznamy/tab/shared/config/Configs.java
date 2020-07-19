package me.neznamy.tab.shared.config;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;

import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.premium.SortingType;
import me.neznamy.tab.shared.Animation;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class Configs {

	public static ConfigurationFile config;
	public static boolean collisionRule;
	public static List<String> revertedCollision;
	public static Map<String, String> sortedGroups;
	public static Map<Object, Object> rankAliases;
	public static List<String> disabledHeaderFooter;
	public static List<String> disabledTablistNames;
	public static List<String> disabledNametag;
	public static List<String> disabledTablistObjective;
	public static List<String> disabledBossbar;
	public static List<String> disabledBelowname;
	public static SimpleDateFormat dateFormat;
	public static SimpleDateFormat timeFormat;
	public static double timeOffset;
	public static List<String> removeStrings;
	public static String noAfk;
	public static String yesAfk;
	public static Map<String, Object> serverAliases;
	public static double SECRET_NTX_space;
	public static boolean SECRET_invisible_nametags;
	public static boolean SECRET_safe_register;
	public static boolean SECRET_remove_ghost_players;
	public static boolean SECRET_armorstands_always_visible;
	public static boolean SECRET_debugMode;
	public static String SECRET_multiWorldSeparator;
	public static String SECRET_essentials_nickname_prefix;
	public static boolean SECRET_rgb_support;


	public static ConfigurationFile animation;
	public static List<Animation> animations;


	public static ConfigurationFile bossbar;
	public static boolean BossBarEnabled;


	public static ConfigurationFile translation;
	public static String no_perm;
	public static String unlimited_nametag_mode_not_enabled;
	public static String data_removed;
	public static String player_not_found;
	public static String reloaded;
	public static String value_assigned;
	public static String value_removed;
	public static String bossbar_off;
	public static String bossbar_on;
	public static String preview_off;
	public static String preview_on;
	public static String reloadFailed = "&4Failed to reload, file %file% has broken syntax. Check console for more info.";

	public static ConfigurationFile advancedconfig;
	public static boolean usePrimaryGroup = true;
	public static List<String> primaryGroupFindingList = Arrays.asList("Owner", "Admin", "Helper", "default");
	public static boolean bukkitBridgeMode;
	public static boolean groupsByPermissions;

	public static ConfigurationFile playerdata; 

	public static File dataFolder;
	public static File errorFile;
	public static File papiErrorFile;

	public static void loadFiles() throws Exception {
		errorFile = new File(dataFolder, "errors.txt");
		papiErrorFile = new File(dataFolder, "PlaceholderAPI.errors.txt");
		if (errorFile.exists() && errorFile.length() > 10) {
			Shared.errorManager.startupWarn("File &e" + errorFile.getPath() + "&c exists and is not empty. Please take a look at the errors and try to correct them. You can also join our discord for assistance. After you resolve them, delete the file.");
		}
		Placeholders.allUsedPlaceholderIdentifiers.clear();
		Placeholders.usedPlaceholders = new HashSet<>();
		Placeholders.registeredPlaceholders.clear();
		Placeholders.registeredRelationalPlaceholders.clear();
		loadConfig();
		loadAnimations();
		loadBossbar();
		loadTranslation();
		if (Premium.is()) {
			Premium.loadPremiumConfig();
		}
		SortingType.initialize();
		Shared.mainClass.suggestPlaceholders();
	}
	@SuppressWarnings("unchecked")
	public static void loadConfig() throws Exception {
		Shared.mainClass.loadConfig();
		collisionRule = config.getBoolean("enable-collision", true);
		timeFormat = Shared.errorManager.createDateFormat(config.getString("placeholders.time-format", "[HH:mm:ss / h:mm a]"), "[HH:mm:ss / h:mm a]");
		timeOffset = config.getDouble("placeholders.time-offset", 0);
		dateFormat = Shared.errorManager.createDateFormat(config.getString("placeholders.date-format", "dd.MM.yyyy"), "dd.MM.yyyy");
		removeStrings = new ArrayList<>();
		for (String s : config.getStringList("placeholders.remove-strings", Arrays.asList("[] ", "< > "))) {
			removeStrings.add(Placeholders.color(s));
		}
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
		rankAliases = config.getConfigurationSection("rank-aliases");
		revertedCollision = config.getStringList("revert-collision-rule-in-" + Shared.separatorType+"s", Arrays.asList("reverted" + Shared.separatorType));
		disabledHeaderFooter = config.getStringList("disable-features-in-"+Shared.separatorType+"s.header-footer", Arrays.asList("disabled" + Shared.separatorType));
		disabledTablistNames = config.getStringList("disable-features-in-"+Shared.separatorType+"s.tablist-names", Arrays.asList("disabled" + Shared.separatorType));
		disabledNametag = config.getStringList("disable-features-in-"+Shared.separatorType+"s.nametag", Arrays.asList("disabled" + Shared.separatorType));
		disabledTablistObjective = config.getStringList("disable-features-in-"+Shared.separatorType+"s.tablist-objective", Arrays.asList("disabled" + Shared.separatorType));
		disabledBossbar = config.getStringList("disable-features-in-"+Shared.separatorType+"s.bossbar", Arrays.asList("disabled" + Shared.separatorType));
		disabledBelowname = config.getStringList("disable-features-in-"+Shared.separatorType+"s.belowname", Arrays.asList("disabled" + Shared.separatorType));
		
		SECRET_NTX_space = getSecretOption("ntx-space", 0.22F);
		SECRET_invisible_nametags = getSecretOption("invisible-nametags", false);
		SECRET_safe_register = getSecretOption("safe-team-register", true);
		SECRET_remove_ghost_players = getSecretOption("remove-ghost-players", false);
		SECRET_armorstands_always_visible = getSecretOption("unlimited-nametag-prefix-suffix-mode.always-visible", false);
		SECRET_debugMode = getSecretOption("debug", false);
		SECRET_multiWorldSeparator = getSecretOption("multi-world-separator", "-");
		SECRET_essentials_nickname_prefix = getSecretOption("essentials-nickname-prefix", "");
		SECRET_rgb_support = getSecretOption("rgb-support", true);
	}
	public static void loadAnimations() throws Exception {
		animation = new YamlConfigurationFile(dataFolder, "animations.yml", null);
		animations = new ArrayList<Animation>();
		for (Object s : animation.getConfigurationSection("animations").keySet()) {
			animations.add(new Animation(s+"", animation.getStringList("animations." + s + ".texts"), animation.getInt("animations." + s + ".change-interval", 0)));
		}
	}
	public static void loadBossbar() throws Exception {
		bossbar = new YamlConfigurationFile(dataFolder, "bossbar.yml", null);
		if (bossbar.hasConfigOption("enabled")) {
			Shared.errorManager.startupWarn("You are using old bossbar config, please make a backup of the file and delete it to get new file.");
			BossBarEnabled = false;
			return;
		}
		BossBarEnabled = bossbar.getBoolean("bossbar-enabled", false);
	}
	public static void loadTranslation() throws Exception {
		translation = new YamlConfigurationFile(dataFolder, "translation.yml", null);
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
		reloadFailed = translation.getString("reload-failed", "&4Failed to reload, file %file% has broken syntax. Check console for more info.");
	}
	@SuppressWarnings("unchecked")
	public static <T> T getSecretOption(String path, T defaultValue) {
		Object value = config.getObject(path);
		if (value == null) return defaultValue;
		if (defaultValue instanceof Integer) return (T) (Object) Integer.parseInt(value+"");
		if (defaultValue instanceof Float) return (T) (Object) Float.parseFloat(value+"");
		if (defaultValue instanceof Double) return (T) (Object) Double.parseDouble(value+"");
		if (defaultValue instanceof Long) return (T) (Object) Long.parseLong(value+"");
		if (defaultValue instanceof String) return (T) (value+"");
		return (T) value;
	}
	public static List<String> getPlayerData(String key) {
		if (playerdata == null) {
			File file = new File("plugins" + File.separatorChar + "TAB" + File.separatorChar + "playerdata.yml");
			try {
				if (!file.exists()) file.createNewFile();
				playerdata = new YamlConfigurationFile(dataFolder, "playerdata.yml", null);
			} catch (Exception e) {
				Shared.errorManager.criticalError("Failed to load playerdata.yml", e);
				return Lists.newArrayList();
			}
		}
		return playerdata.getStringList(key);
	}
	public static boolean getCollisionRule(String world) {
		return revertedCollision.contains(world) ? !collisionRule : collisionRule;
	}
}