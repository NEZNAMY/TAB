package me.neznamy.tab.shared.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.shared.TAB;

/**
 * Core of loading configuration files
 */
public class Configs {

	private TAB tab;

	//config.yml file
	private ConfigurationFile config;

	private List<String> removeStrings;
	private boolean bukkitPermissions;

	//hidden config options
	private boolean rgbSupport;
	private boolean unregisterBeforeRegister;
	private String multiWorldSeparator;
	private String essentialsNickPrefix;
	private boolean armorStandsAlwaysVisible; //paid private addition
	private boolean removeGhostPlayers;
	private boolean layout;
	private boolean pipelineInjection;

	//animations.yml file
	private ConfigurationFile animation;

	//bossbar.yml file
	private ConfigurationFile bossbar;

	//translation.yml file
	private ConfigurationFile translation;

	//default reload message in case plugin did not load translation file due to an error
	private String reloadFailed = "&4Failed to reload, file %file% has broken syntax. Check console for more info.";

	//premiumconfig.yml
	private ConfigurationFile premiumconfig;

	//playerdata.yml, used for bossbar & scoreboard toggle saving
	private ConfigurationFile playerdata; 

	/**
	 * Constructs new instance with given parameter
	 * @param tab - tab instance
	 */
	public Configs(TAB tab) {
		this.tab = tab;
	}

	/**
	 * Loads all configuration files and converts files to latest version
	 * @throws IOException 
	 * @throws YAMLException 
	 */
	public void loadFiles() throws YAMLException, IOException {
		ClassLoader loader = Configs.class.getClassLoader();
		loadConfig();
		animation = new YamlConfigurationFile(loader.getResourceAsStream("animations.yml"), new File(tab.getPlatform().getDataFolder(), "animations.yml"));
		Map<String, Object> values = getAnimationFile().getValues();
		if (values.size() == 1 && values.containsKey("animations")) {
			getAnimationFile().setValues(getAnimationFile().getConfigurationSection("animations"));
			getAnimationFile().save();
			TAB.getInstance().print('2', "Converted animations.yml to new format.");
		}
		bossbar = new YamlConfigurationFile(loader.getResourceAsStream("bossbar.yml"), new File(tab.getPlatform().getDataFolder(), "bossbar.yml"));
		translation = new YamlConfigurationFile(loader.getResourceAsStream("translation.yml"), new File(tab.getPlatform().getDataFolder(), "translation.yml"));
		reloadFailed = getTranslation().getString("reload-failed", "&4Failed to reload, file %file% has broken syntax. Check console for more info.");
		if (tab.isPremium()) premiumconfig = new YamlConfigurationFile(loader.getResourceAsStream("premiumconfig.yml"), new File(tab.getPlatform().getDataFolder(), "premiumconfig.yml"));
	}

	/**
	 * Loads config.yml and some of it's values
	 * @throws IOException 
	 * @throws YAMLException 
	 */
	@SuppressWarnings("unchecked")
	public void loadConfig() throws YAMLException, IOException {
		config = new YamlConfigurationFile(Configs.class.getClassLoader().getResourceAsStream(tab.getPlatform().getConfigName()), new File(tab.getPlatform().getDataFolder(), "config.yml"), Arrays.asList("# Detailed explanation of all options available at https://github.com/NEZNAMY/TAB/wiki/config.yml", ""));
		removeStrings = new ArrayList<>();
		for (String s : getConfig().getStringList("placeholders.remove-strings", Arrays.asList("[] ", "< > "))) {
			getRemoveStrings().add(s.replace('&', '\u00a7'));
		}
		tab.setDebugMode(getConfig().getBoolean("debug", false));
		rgbSupport = (boolean) getSecretOption("rgb-support", true);
		unregisterBeforeRegister = (boolean) getSecretOption("unregister-before-register", true);
		multiWorldSeparator = (String) getSecretOption("multi-world-separator", "-");
		essentialsNickPrefix = (String) getSecretOption("essentials-nickname-prefix", "");
		armorStandsAlwaysVisible = (boolean) getSecretOption("unlimited-nametag-prefix-suffix-mode.always-visible", false);
		removeGhostPlayers = (boolean) getSecretOption("remove-ghost-players", false);
		layout = (boolean) getSecretOption("layout", false);
		pipelineInjection = (boolean) getSecretOption("pipeline-injection", true);
		if (tab.getPlatform().getSeparatorType().equals("server")) {
			bukkitPermissions = getConfig().getBoolean("use-bukkit-permissions-manager", false);
		}

		//checking for unnecessary copypaste in config
		Set<Object> groups = getConfig().getConfigurationSection("Groups").keySet();
		if (groups.size() < 2) return;
		Map<Object, Object> sharedProperties = new HashMap<>(getConfig().getConfigurationSection("Groups." + groups.toArray()[0])); //cloning to not delete from original one
		for (Object groupSettings : getConfig().getConfigurationSection("Groups").values()) {
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
	 * Returns value of hidden config option with specified path if it exists, defaultValue otherwise
	 * @param path - path to value
	 * @param defaultValue - value to return if option is not present in file
	 * @return value with specified path or default value if not present
	 */
	private Object getSecretOption(String path, Object defaultValue) {
		if (getConfig() == null) return defaultValue;
		Object value = getConfig().getObject(path);
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
				if (file.exists() || file.createNewFile()) {
					playerdata = new YamlConfigurationFile(null, file);
					return playerdata.getStringList(key, new ArrayList<>());
				}
			} catch (Exception e) {
				tab.getErrorManager().criticalError("Failed to load playerdata.yml", e);
			}
		} else {
			return playerdata.getStringList(key, new ArrayList<>());
		}
		return new ArrayList<>();
	}

	/**
	 * Returns name of world group in specified set that may consist of multiple
	 * worlds separated with "-" or something else defined in config
	 * @param world - name of world to find group of
	 * @return name of world group
	 */
	public String getWorldGroupOf(Set<?> groups, String world) {
		if (groups.isEmpty()) return world;
		for (Object worldGroup : groups) {
			for (String definedWorld : worldGroup.toString().split(multiWorldSeparator)) {
				if (definedWorld.endsWith("*")) {
					if (world.toLowerCase().startsWith(definedWorld.substring(0, definedWorld.length()-1).toLowerCase())) return worldGroup.toString();
				} else {
					if (world.equalsIgnoreCase(definedWorld)) return worldGroup.toString();
				}
			}
		}
		return world;
	}

	/**
	 * Reads all lines in file and returns them as List
	 * @return list of lines in file
	 */
	public static List<String> readAllLines(File file) {
		List<String> list = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))){
			String line;
			while ((line = br.readLine()) != null) {
				list.add(line);
			}
		} catch (Exception ex) {
			TAB.getInstance().getErrorManager().criticalError("Failed to read file " + file, ex);
		}
		return list;
	}

	/**
	 * Writes defined line of text to file
	 * @param f - file to write to
	 * @param line - line to write
	 */
	public static void write(File file, List<String> lines){
		try (BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))){
			for (String line : lines) {
				buf.write(line + System.getProperty("line.separator"));
			}
		} catch (Exception ex) {
			TAB.getInstance().getErrorManager().criticalError("Failed to write to file " + file.getName(), ex);
		}
	}

	public List<String> getRemoveStrings() {
		return removeStrings;
	}

	public boolean isUnregisterBeforeRegister() {
		return unregisterBeforeRegister;
	}

	public ConfigurationFile getTranslation() {
		return translation;
	}

	public ConfigurationFile getConfig() {
		return config;
	}

	public boolean isRemoveGhostPlayers() {
		return removeGhostPlayers;
	}

	public ConfigurationFile getPremiumConfig() {
		return premiumconfig;
	}

	public ConfigurationFile getBossbarConfig() {
		return bossbar;
	}

	public boolean isLayout() {
		return layout;
	}

	public ConfigurationFile getAnimationFile() {
		return animation;
	}

	public boolean isRgbSupport() {
		return rgbSupport;
	}

	public boolean isBukkitPermissions() {
		return bukkitPermissions;
	}

	public boolean isPipelineInjection() {
		return pipelineInjection;
	}

	public boolean isArmorStandsAlwaysVisible() {
		return armorStandsAlwaysVisible;
	}

	public String getEssentialsNickPrefix() {
		return essentialsNickPrefix;
	}

	public String getReloadFailedMessage() {
		return reloadFailed;
	}

	public ConfigurationFile getPlayerDataFile() {
		return playerdata;
	}
}