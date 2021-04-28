package me.neznamy.tab.shared.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import me.neznamy.tab.shared.TAB;

/**
 * Core of loading configuration files
 */
public class Configs {

	private TAB tab;
	
	//config.yml file
	public ConfigurationFile config;

	//remove-strings option
	public List<String> removeStrings;
	
	//hidden config options
	public boolean rgbSupport;
	public boolean unregisterBeforeRegister;
	public String multiWorldSeparator;
	public String essentialsNickPrefix;
	public boolean armorStandsAlwaysVisible; //paid private addition
	public boolean removeGhostPlayers;
	public boolean layout;
	public boolean pipelineInjection;

	//animations.yml file
	public ConfigurationFile animation;

	//bossbar.yml file
	public ConfigurationFile bossbar;
	
	//used for check & warn in case of old bossbar file format
	public boolean BossBarEnabled;

	//translation.yml file
	public ConfigurationFile translation;
	
	//default reload message in case plugin did not load translation file due to an error
	public String reloadFailed = "&4Failed to reload, file %file% has broken syntax. Check console for more info.";
	
	//premiumconfig.yml
	public ConfigurationFile premiumconfig;

	//playerdata.yml, used for bossbar & scoreboard toggle saving
	public ConfigurationFile playerdata; 

	/**
	 * Constructs new instance with given parameter
	 * @param tab - tab instance
	 */
	public Configs(TAB tab) {
		this.tab = tab;
	}
	
	/**
	 * Loads all configuration files and converts files to latest version
	 * @throws Exception - if I/O file opration fails or some file has broken yaml syntax
	 */
	public void loadFiles() throws Exception {
		ClassLoader loader = Configs.class.getClassLoader();
		removeAdvancedConfig();
		loadConfig();
		animation = new YamlConfigurationFile(loader.getResourceAsStream("animations.yml"), new File(tab.getPlatform().getDataFolder(), "animations.yml"));
		tab.getPlatform().convertConfig(animation);
		loadBossbar();
		translation = new YamlConfigurationFile(loader.getResourceAsStream("translation.yml"), new File(tab.getPlatform().getDataFolder(), "translation.yml"));
		reloadFailed = translation.getString("reload-failed", "&4Failed to reload, file %file% has broken syntax. Check console for more info.");
		if (tab.isPremium()) {
			premiumconfig = new YamlConfigurationFile(loader.getResourceAsStream("premiumconfig.yml"), new File(tab.getPlatform().getDataFolder(), "premiumconfig.yml"));
			tab.getPlatform().convertConfig(premiumconfig);
		}
	}

	/**
	 * Conversion from old advancedconfig.yml file
	 */
	private void removeAdvancedConfig() {
		File f = new File(tab.getPlatform().getDataFolder(), "advancedconfig.yml");
		if (f.exists()) {
			List<String> fileLines = readAllLines(f);
			File config = new File(tab.getPlatform().getDataFolder(), "config.yml");
			for (String line : fileLines) {
				if (!line.startsWith("#")) {
					write(config, line);
				}
			}
			f.delete();
		}
	}
	/**
	 * Loads config.yml and some of it's values
	 * @throws Exception - if file has broken syntax or I/O file operation fails
	 */
	@SuppressWarnings("unchecked")
	public void loadConfig() throws Exception {
		String source = tab.getPlatform().getSeparatorType().equals("world") ? "bukkitconfig.yml" : "bungeeconfig.yml";
		config = new YamlConfigurationFile(Configs.class.getClassLoader().getResourceAsStream(source), new File(tab.getPlatform().getDataFolder(), "config.yml"), Arrays.asList("# Detailed explanation of all options available at https://github.com/NEZNAMY/TAB/wiki/config.yml", ""));
		tab.getPlatform().convertConfig(config);
		removeStrings = new ArrayList<>();
		for (String s : config.getStringList("placeholders.remove-strings", Arrays.asList("[] ", "< > "))) {
			removeStrings.add(s.replace('&', '\u00a7'));
		}
		tab.debugMode = (boolean) getSecretOption("debug", false);
		rgbSupport = (boolean) getSecretOption("rgb-support", true);
		unregisterBeforeRegister = (boolean) getSecretOption("unregister-before-register", true);
		multiWorldSeparator = (String) getSecretOption("multi-world-separator", "-");
		essentialsNickPrefix = (String) getSecretOption("essentials-nickname-prefix", "");
		armorStandsAlwaysVisible = (boolean) getSecretOption("unlimited-nametag-prefix-suffix-mode.always-visible", false);
		removeGhostPlayers = (boolean) getSecretOption("remove-ghost-players", false);
		layout = (boolean) getSecretOption("layout", false);
		pipelineInjection = (boolean) getSecretOption("pipeline-injection", true);
		
		//checking for unnecessary copypaste in config
		Set<Object> groups = config.getConfigurationSection("Groups").keySet();
		if (groups.size() < 2) return;
		Map<Object, Object> sharedProperties = new HashMap<>(config.getConfigurationSection("Groups." + groups.toArray()[0])); //cloning to not delete from original one
		for (Object groupSettings : config.getConfigurationSection("Groups").values()) {
			if (!(groupSettings instanceof Map)) continue;
			Map<String, Object> group = (Map<String, Object>) groupSettings;
			for (Entry<Object, Object> sharedProperty : new HashSet<>(sharedProperties.entrySet())) {
				String property = sharedProperty.getKey().toString();
				if (!group.containsKey(property) || !String.valueOf(group.get(property)).equals(sharedProperty.getValue())) {
					sharedProperties.remove(property);
				}
			}
		}
		for (Object property : sharedProperties.keySet()) {
			tab.print('9', "Hint: All of your groups have the same value of \"&d" + property + "&9\" set. Delete it from all groups and add it only to _OTHER_ for cleaner and smaller config.");
		}
	}

	/**
	 * Loads bossbar.yml and sends a warn if file is old
	 * @throws Exception - if file has broken syntax or I/O file operation fails
	 */
	public void loadBossbar() throws Exception {
		bossbar = new YamlConfigurationFile(Configs.class.getClassLoader().getResourceAsStream("bossbar.yml"), new File(tab.getPlatform().getDataFolder(), "bossbar.yml"));
		tab.getPlatform().convertConfig(bossbar);
		if (bossbar.hasConfigOption("enabled")) {
			tab.getErrorManager().startupWarn("You are using old bossbar config, please make a backup of the file and delete it to get new file.");
			BossBarEnabled = false;
			return;
		}
		BossBarEnabled = bossbar.getBoolean("bossbar-enabled", false);
	}

	/**
	 * Returns value of hidden config option with specified path if it exists, defaultValue otherwise
	 * @param path - path to value
	 * @param defaultValue - value to return if option is not present in file
	 * @return value with specified path or default value if not present
	 */
	private Object getSecretOption(String path, Object defaultValue) {
		if (config == null) return defaultValue;
		Object value = config.getObject(path);
		return value == null ? defaultValue : value;
	}
	
	/**
	 * Returns player data with specified key
	 * @param key - data key
	 * @return list of players logged in this data key
	 */
	public List<String> getPlayerData(String key) {
		if (playerdata == null) {
			File file = new File(tab.getPlatform().getDataFolder(), "playerdata.yml");
			try {
				if (!file.exists()) file.createNewFile();
				playerdata = new YamlConfigurationFile(null, file);
			} catch (Exception e) {
				tab.getErrorManager().criticalError("Failed to load playerdata.yml", e);
				return new ArrayList<String>();
			}
		}
		return playerdata.getStringList(key, new ArrayList<String>());
	}
	
	/**
	 * Returns name of world group of specified world in per-server-settings that may consist of multiple
	 * world separated with "-" or something else defined in config
	 * @param world - name of world to find group of
	 * @return name of world group
	 */
	public String getWorldGroupOf(String world) {
		Map<String, Object> worlds = config.getConfigurationSection("per-" + tab.getPlatform().getSeparatorType() + "-settings");
		if (worlds.isEmpty()) return world;
		for (String worldGroup : worlds.keySet()) {
			for (String localWorld : worldGroup.split(multiWorldSeparator)) {
				if (localWorld.equalsIgnoreCase(world)) return worldGroup;
			}
		}
		return world;
	}
	
	/**
	 * Reads all lines in file and returns them as List
	 * @return list of lines in file
	 */
	public List<String> readAllLines(File file) {
		List<String> list = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
			String line;
			while ((line = br.readLine()) != null) {
				list.add(line);
			}
			br.close();
		} catch (Exception ex) {
			tab.getErrorManager().criticalError("Failed to read file " + file, ex);
		}
		return list;
	}
	
	/**
	 * Writes defined line of text to file
	 * @param f - file to write to
	 * @param line - line to write
	 */
	public void write(File f, String line){
		try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(f, true));
            buf.write(line + System.getProperty("line.separator"));
            buf.close();
        } catch (Exception ex) {
            tab.getErrorManager().criticalError("Failed to write to file " + f.getName(), ex);
        }
	}
}