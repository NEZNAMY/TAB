package me.neznamy.tab.shared.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.api.config.YamlConfigurationFile;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.YamlPropertyConfigurationFile;
import me.neznamy.tab.shared.config.mysql.MySQLGroupConfiguration;
import me.neznamy.tab.shared.config.mysql.MySQLUserConfiguration;

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
	private boolean unregisterBeforeRegister;
	private boolean armorStandsAlwaysVisible; //paid private addition
	private boolean removeGhostPlayers;
	private boolean pipelineInjection;

	//animations.yml file
	private ConfigurationFile animation;

	//translation.yml file
	private ConfigurationFile translation;

	//default reload message in case plugin did not load translation file due to an error
	private String reloadFailed = "&4Failed to reload, file %file% has broken syntax. Check console for more info.";

	//playerdata.yml, used for bossbar & scoreboard toggle saving
	private ConfigurationFile playerdata;
	
	private ConfigurationFile layout;
	
	private PropertyConfiguration groupFile;
	
	private PropertyConfiguration userFile;
	
	private MySQL mysql;

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
		translation = new YamlConfigurationFile(loader.getResourceAsStream("translation.yml"), new File(tab.getPlatform().getDataFolder(), "translation.yml"));
		layout = new YamlConfigurationFile(loader.getResourceAsStream("layout.yml"), new File(tab.getPlatform().getDataFolder(), "layout.yml"));
		reloadFailed = getTranslation().getString("reload-failed", "&4Failed to reload, file %file% has broken syntax. Check console for more info.");
	}

	/**
	 * Loads config.yml and some of it's values
	 * @throws IOException 
	 * @throws YAMLException 
	 */
	public void loadConfig() throws YAMLException, IOException {
		config = new YamlConfigurationFile(Configs.class.getClassLoader().getResourceAsStream("bukkitconfig.yml"), new File(tab.getPlatform().getDataFolder(), "config.yml"));
		removeStrings = new ArrayList<>();
		for (String s : getConfig().getStringList("placeholders.remove-strings", Arrays.asList("[] ", "< > "))) {
			getRemoveStrings().add(s.replace('&', '\u00a7'));
		}
		tab.setDebugMode(getConfig().getBoolean("debug", false));
		unregisterBeforeRegister = (boolean) getSecretOption("unregister-before-register", true);
		armorStandsAlwaysVisible = (boolean) getSecretOption("unlimited-nametag-prefix-suffix-mode.always-visible", false);
		removeGhostPlayers = (boolean) getSecretOption("remove-ghost-players", false);
		pipelineInjection = (boolean) getSecretOption("pipeline-injection", true);
		if (tab.getPlatform().getSeparatorType().equals("server")) {
			bukkitPermissions = getConfig().getBoolean("use-bukkit-permissions-manager", false);
		}
		if (config.getBoolean("mysql.enabled", false)) {
			try {
				mysql = new MySQL(config.getString("mysql.host", "127.0.0.1"), config.getInt("mysql.port", 3306),
						config.getString("mysql.database", "tab"), config.getString("mysql.username", "user"), config.getString("mysql.password", "password"));
				groupFile = new MySQLGroupConfiguration(mysql);
				userFile = new MySQLUserConfiguration(mysql);
				return;
			} catch (SQLException e) {
				TAB.getInstance().getErrorManager().criticalError("Failed to connect to MySQL", e);
			}
		}
		groupFile = new YamlPropertyConfigurationFile(Configs.class.getClassLoader().getResourceAsStream("groups.yml"), new File(tab.getPlatform().getDataFolder(), "groups.yml"));
		userFile = new YamlPropertyConfigurationFile(Configs.class.getClassLoader().getResourceAsStream("users.yml"), new File(tab.getPlatform().getDataFolder(), "users.yml"));
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

	public ConfigurationFile getLayout() {
		return layout;
	}

	public ConfigurationFile getAnimationFile() {
		return animation;
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

	public String getReloadFailedMessage() {
		return reloadFailed;
	}

	public ConfigurationFile getPlayerDataFile() {
		return playerdata;
	}

	public PropertyConfiguration getGroups() {
		return groupFile;
	}

	public PropertyConfiguration getUsers() {
		return userFile;
	}

	public MySQL getMysql() {
		return mysql;
	}
}