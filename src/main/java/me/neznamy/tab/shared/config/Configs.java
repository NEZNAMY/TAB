package me.neznamy.tab.shared.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;

import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.placeholders.Animation;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

/**
 * A static mess of config options
 * Looking to clean this up in the future
 */
public class Configs {

	public static ConfigurationFile config;
	public static boolean collisionRule;
	public static List<String> revertedCollision;
	public static LinkedHashMap<String, String> sortedGroups;
	public static Map<Object, Object> rankAliases;
	public static SimpleDateFormat dateFormat;
	public static SimpleDateFormat timeFormat;
	public static double timeOffset;
	public static List<String> removeStrings;
	public static String noAfk;
	public static String yesAfk;
	public static Map<String, Object> serverAliases;
	public static boolean SECRET_debugMode;


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
	
	public static ConfigurationFile premiumconfig;
	public static Map<String, Condition> conditions = new HashMap<String, Condition>();

	public static ConfigurationFile playerdata; 

	public static File errorFile;
	public static File papiErrorFile;

	public static void loadFiles() throws Exception {
		errorFile = new File(Shared.platform.getDataFolder(), "errors.txt");
		papiErrorFile = new File(Shared.platform.getDataFolder(), "PlaceholderAPI.errors.txt");
		if (errorFile.exists() && errorFile.length() > 10) {
			Shared.errorManager.startupWarn("File &e" + errorFile.getPath() + "&c exists and is not empty. Take a look at the error messages and try to resolve them. After you do, delete the file.");
		}
		PlaceholderManager.allUsedPlaceholderIdentifiers = new HashSet<String>();
		removeAdvancedConfig();
		loadConfig();
		loadAnimations();
		loadBossbar();
		loadTranslation();
		if (Shared.isPremium()) {
			loadPremiumConfig();
		}
		Shared.platform.suggestPlaceholders();
	}

	private static void removeAdvancedConfig() {
		File f = new File(Shared.platform.getDataFolder(), "advancedconfig.yml");
		if (f.exists()) {
			List<String> fileLines = readAllLines(f);
			File config = new File(Shared.platform.getDataFolder(), "config.yml");
			for (String line : fileLines) {
				if (!line.startsWith("#")) {
					write(config, line);
				}
			}
			f.delete();
		}
	}
	@SuppressWarnings("unchecked")
	public static void loadConfig() throws Exception {
		Shared.platform.loadConfig();
		Shared.platform.convertConfig(config);
		collisionRule = config.getBoolean("enable-collision", true);
		timeFormat = Shared.errorManager.createDateFormat(config.getString("placeholders.time-format", "[HH:mm:ss / h:mm a]"), "[HH:mm:ss / h:mm a]");
		timeOffset = config.getDouble("placeholders.time-offset", 0);
		dateFormat = Shared.errorManager.createDateFormat(config.getString("placeholders.date-format", "dd.MM.yyyy"), "dd.MM.yyyy");
		removeStrings = new ArrayList<>();
		for (String s : config.getStringList("placeholders.remove-strings", Arrays.asList("[] ", "< > "))) {
			removeStrings.add(PlaceholderManager.color(s));
		}
		sortedGroups = new LinkedHashMap<String, String>();
		int index = 1;
		for (Object group : config.getStringList("group-sorting-priority-list", Arrays.asList("Owner", "Admin", "Mod", "Helper", "Builder", "Premium", "Player", "default"))){
			String sort = index+"";
			while (sort.length() < 3) {
				sort = "0" + sort;
			}
			for (String group0 : String.valueOf(group).toLowerCase().split(" ")) {
				sortedGroups.put(group0, sort);
			}
			index++;
		}
		rankAliases = config.getConfigurationSection("rank-aliases");
		revertedCollision = config.getStringList("revert-collision-rule-in-" + Shared.platform.getSeparatorType()+"s", Arrays.asList("reverted" + Shared.platform.getSeparatorType()));
		SECRET_debugMode = getSecretOption("debug", false);
		PlaceholderManager.findAllUsed(config.getValues());
		Set<Object> groups = config.getConfigurationSection("Groups").keySet();
		if (groups.size() < 2) return;
		Map<Object, Object> sharedProperties = new HashMap<>(config.getConfigurationSection("Groups." + groups.toArray()[0])); //cloning to not delete from original one
		for (Object groupSettings : config.getConfigurationSection("Groups").values()) {
			Map<String, Object> group = (Map<String, Object>) groupSettings;
			for (Entry<Object, Object> sharedProperty : new HashSet<>(sharedProperties.entrySet())) {
				String property = sharedProperty.getKey().toString();
				if (!group.containsKey(property) || !String.valueOf(group.get(property)).equals(sharedProperty.getValue())) {
					sharedProperties.remove(property);
				}
			}
		}
		for (Object property : sharedProperties.keySet()) {
			Shared.print('9', "Hint: All of your groups have the same value of \"&d" + property + "&9\" set. Delete it from all groups and add it only to _OTHER_ for cleaner and smaller config.");
		}
	}
	public static void loadAnimations() throws Exception {
		animation = new YamlConfigurationFile(Configs.class.getClassLoader().getResourceAsStream("animations.yml"), new File(Shared.platform.getDataFolder(), "animations.yml"));
		animations = new ArrayList<Animation>();
		for (Object s : animation.getConfigurationSection("animations").keySet()) {
			animations.add(new Animation(s+"", animation.getStringList("animations." + s + ".texts"), animation.getInt("animations." + s + ".change-interval", 0)));
		}
		PlaceholderManager.findAllUsed(animation.getValues());
	}
	public static void loadBossbar() throws Exception {
		bossbar = new YamlConfigurationFile(Configs.class.getClassLoader().getResourceAsStream("bossbar.yml"), new File(Shared.platform.getDataFolder(), "bossbar.yml"));
		Shared.platform.convertConfig(bossbar);
		if (bossbar.hasConfigOption("enabled")) {
			Shared.errorManager.startupWarn("You are using old bossbar config, please make a backup of the file and delete it to get new file.");
			BossBarEnabled = false;
			return;
		}
		BossBarEnabled = bossbar.getBoolean("bossbar-enabled", false);
		PlaceholderManager.findAllUsed(bossbar.getValues());
	}
	public static void loadTranslation() throws Exception {
		translation = new YamlConfigurationFile(Configs.class.getClassLoader().getResourceAsStream("translation.yml"), new File(Shared.platform.getDataFolder(), "translation.yml"));
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
	
	public static void loadPremiumConfig() throws Exception {
		premiumconfig = new YamlConfigurationFile(Configs.class.getClassLoader().getResourceAsStream("premiumconfig.yml"), new File(Shared.platform.getDataFolder(), "premiumconfig.yml"));
		Shared.platform.convertConfig(premiumconfig);
		conditions = new HashMap<String, Condition>();
		for (Object condition : premiumconfig.getConfigurationSection("conditions").keySet()) {
			List<String> list = premiumconfig.getStringList("conditions." + condition + ".conditions"); //lol
			String type = premiumconfig.getString("conditions." + condition + ".type");
			String yes = premiumconfig.getString("conditions." + condition + ".true");
			String no = premiumconfig.getString("conditions." + condition + ".false");
			conditions.put(condition+"", Condition.compile(condition+"", list, type, yes, no));
		}
		PlaceholderManager.findAllUsed(premiumconfig.getValues());
	}
	@SuppressWarnings("unchecked")
	public static <T> T getSecretOption(String path, T defaultValue) {
		if (config == null) return defaultValue;
		Object value = config.getObject(path);
		if (value == null) return defaultValue;
		if (defaultValue instanceof String) return (T) value.toString();
		if (defaultValue instanceof Boolean) return (T) (Object) Boolean.parseBoolean(value.toString());
		return (T) (Object) Double.parseDouble(value.toString());
	}
	public static List<String> getPlayerData(String key) {
		if (playerdata == null) {
			File file = new File(Shared.platform.getDataFolder(), "playerdata.yml");
			try {
				if (!file.exists()) file.createNewFile();
				playerdata = new YamlConfigurationFile(null, file);
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
	
	public static String getWorldGroupOf(String world) {
		Map<String, Object> worlds = Configs.config.getConfigurationSection("per-" + Shared.platform.getSeparatorType() + "-settings");
		if (worlds.isEmpty()) return world;
		for (String worldGroup : worlds.keySet()) {
			for (String localWorld : worldGroup.split(Configs.getSecretOption("multi-world-separator", "-"))) {
				if (localWorld.equalsIgnoreCase(world)) return worldGroup;
			}
		}
		return world;
	}
	
	/**
	 * Reads all lines in file and returns them as List
	 * @return list of lines in file
	 */
	public static List<String> readAllLines(File file) {
		List<String> list = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
			String line;
			while ((line = br.readLine()) != null) {
				list.add(line);
			}
			br.close();
		} catch (Exception ex) {
			Shared.errorManager.criticalError("Failed to read file " + file, ex);
		}
		return list;
	}
	
	public static void write(File f, String line){
		try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(f, true));
            buf.write(line + System.getProperty("line.separator"));
            buf.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
}