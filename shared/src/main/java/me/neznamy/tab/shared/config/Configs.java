package me.neznamy.tab.shared.config;

import me.neznamy.tab.api.PropertyConfiguration;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.api.config.YamlConfigurationFile;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.config.file.YamlPropertyConfigurationFile;
import me.neznamy.tab.shared.config.mysql.MySQLGroupConfiguration;
import me.neznamy.tab.shared.config.mysql.MySQLUserConfiguration;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Core of loading configuration files
 */
public class Configs {

	private final TAB tab;

	private final Converter converter = new Converter();

	//config.yml file
	private ConfigurationFile config;

	private boolean bukkitPermissions;

	//hidden config options
	private boolean unregisterBeforeRegister;
	private boolean armorStandsAlwaysVisible; //paid private addition
	private boolean removeGhostPlayers;
	private boolean pipelineInjection;

	//animations.yml file
	private ConfigurationFile animation;

	//messages.yml file
	private MessageFile messages;

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
	 * @throws	IOException
	 * 			if File I/O operation fails
	 * @throws	YAMLException
	 * 			if files contain syntax errors
	 */
	public void loadFiles() throws YAMLException, IOException {
		ClassLoader loader = Configs.class.getClassLoader();
		loadConfig();
		animation = new YamlConfigurationFile(loader.getResourceAsStream("animations.yml"), new File(tab.getPlatform().getDataFolder(), "animations.yml"));
		converter.convertAnimationFile(animation);
		messages = new MessageFile();
		layout = new YamlConfigurationFile(loader.getResourceAsStream("layout.yml"), new File(tab.getPlatform().getDataFolder(), "layout.yml"));
		reloadFailed = messages.getReloadFailBrokenFile();
	}

	/**
	 * Loads config.yml and some of its values
	 * @throws	IOException
	 * 			if File I/O operation fails
	 * @throws	YAMLException
	 * 			if files contain syntax errors
	 */
	public void loadConfig() throws YAMLException, IOException {
		config = new YamlConfigurationFile(Configs.class.getClassLoader().getResourceAsStream(tab.getPlatform().getConfigName()), new File(tab.getPlatform().getDataFolder(), "config.yml"));
		converter.convertToV3(config);
		converter.removeOldOptions(config);
		tab.setDebugMode(getConfig().getBoolean("debug", false));
		if (tab.getPlatform().isProxy()) {
			bukkitPermissions = getConfig().getBoolean("use-bukkit-permissions-manager", false);
		} else {
			unregisterBeforeRegister = (boolean) getSecretOption("unregister-before-register", true);
			armorStandsAlwaysVisible = (boolean) getSecretOption("scoreboard-teams.unlimited-nametag-mode.always-visible", false);
		}
		removeGhostPlayers = (boolean) getSecretOption("remove-ghost-players", false);
		pipelineInjection = (boolean) getSecretOption("pipeline-injection", true) && tab.getServerVersion().getMinorVersion() >= 8;
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

	public boolean isUnregisterBeforeRegister() {
		return unregisterBeforeRegister;
	}

	public MessageFile getMessages() {
		return messages;
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
		if (playerdata == null) {
			File file = new File(tab.getPlatform().getDataFolder(), "playerdata.yml");
			try {
				if (file.exists() || file.createNewFile()) {
					playerdata = new YamlConfigurationFile(null, file);
				}
			} catch (IOException e) {
				tab.getErrorManager().criticalError("Failed to load playerdata.yml", e);
			}
		}
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
	
	public String getGroup(List<Object> serverGroups, String element) {
		if (serverGroups.isEmpty() || element == null) return element;
		for (Object worldGroup : serverGroups) {
			for (String definedWorld : worldGroup.toString().split(";")) {
				if (definedWorld.endsWith("*")) {
					if (element.toLowerCase().startsWith(definedWorld.substring(0, definedWorld.length()-1).toLowerCase())) return worldGroup.toString();
				} else {
					if (element.equalsIgnoreCase(definedWorld)) return worldGroup.toString();
				}
			}
		}
		return element;
	}
}