package me.neznamy.tab.shared.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

import org.yaml.snakeyaml.error.YAMLException;

import me.neznamy.tab.api.PropertyConfiguration;
import me.neznamy.tab.api.chat.EnumChatFormat;
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

	private String[] removeStrings;
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
	
	private final List<String> disabledWorld = Collections.singletonList("disabledworld");
	private final List<String> disabledServer = Collections.singletonList("disabledServer");

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
		config = new YamlConfigurationFile(Configs.class.getClassLoader().getResourceAsStream(tab.getPlatform().isProxy() ? "proxyconfig.yml" : "bukkitconfig.yml"), new File(tab.getPlatform().getDataFolder(), "config.yml"));
		if (!config.hasConfigOption("mysql"))
			convertToV3();
		List<String> list = config.getStringList("placeholders.remove-strings", Arrays.asList("[] ", "< > "));
		removeStrings = new String[list.size()];
		for (int i=0; i<list.size(); i++) {
			removeStrings[i] = EnumChatFormat.color(list.get(i));
		}
		tab.setDebugMode(getConfig().getBoolean("debug", false));
		if (!tab.getPlatform().isProxy()) {
			unregisterBeforeRegister = (boolean) getSecretOption("unregister-before-register", true);
		} else {
			bukkitPermissions = getConfig().getBoolean("use-bukkit-permissions-manager", false);
		}
		armorStandsAlwaysVisible = (boolean) getSecretOption("unlimited-nametag-prefix-suffix-mode.always-visible", false);
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

	public String[] getRemoveStrings() {
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

	public void convertToV3() throws YAMLException, IOException {
		config.setValues(new HashMap<>());
		Map<String, ConfigurationFile> yamls = new HashMap<>();
		File folder = tab.getPlatform().getDataFolder();
		String path = folder.getPath();
		File premiumcfg = new File(tab.getPlatform().getDataFolder(), "premiumconfig.yml");
		if (!premiumcfg.exists()) {
			Files.copy(Configs.class.getClassLoader().getResourceAsStream("premiumconfig.yml"), premiumcfg.toPath());
		}
		for (File file : folder.listFiles()) if (!checkFiles(file,yamls)) return;

		Files.createFile(Paths.get(path+File.separator+"config.yml"));
		if (!Files.exists(Paths.get(path+File.separator+"groups.yml")))
			Files.createFile(Paths.get(path+File.separator+"groups.yml"));
		if (!Files.exists(Paths.get(path+File.separator+"users.yml")))
			Files.createFile(Paths.get(path+File.separator+"users.yml"));
		yamls.put("finalConfig", new YamlConfigurationFile(null, new File(path+File.separator+"config.yml")));
		yamls.put("groups.yml", new YamlConfigurationFile(null, new File(path+File.separator+"groups.yml")));
		yamls.put("users.yml", new YamlConfigurationFile(null, new File(path+File.separator+"users.yml")));
		config = yamls.get("finalConfig");

		createConfigYml(yamls);
	}

	public boolean checkFiles(File file, Map<String,ConfigurationFile> yamls) {
		if (!file.getName().equals("config.yml") && !file.getName().equals("premiumconfig.yml") && !file.getName().equals("bossbar.yml"))
			return true;

		try {
			File oldfolder = new File(file.getParent()+File.separator+"old_configs");
			if (!oldfolder.exists()) oldfolder.mkdir();

			Path oldconfig = Paths.get(file.getParent()+File.separator+"old_configs"+File.separator+file.getName());
			Files.copy(file.toPath(), oldconfig, StandardCopyOption.REPLACE_EXISTING);

			yamls.put(file.getName(), new YamlConfigurationFile(null,file));

			Files.delete(file.toPath());
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	private void createConfigYml(Map<String, ConfigurationFile> yamls) throws YAMLException {
		tab.sendConsoleMessage("&e[TAB] --------------------------------------------------------------",true);
		tab.sendConsoleMessage("&e[TAB] Performing configuration conversion from 2.9.2 to 3.0.0",true);
		tab.sendConsoleMessage("&e[TAB] Please note that this may not be 100% accurate",true);
		tab.sendConsoleMessage("&e[TAB] Review your configuration and verify everything is as you want it to be",true);
		tab.sendConsoleMessage("&e[TAB] --------------------------------------------------------------",true);
		String worldOrServer = tab.getPlatform().isProxy() ? "server" : "world";
		boolean isProxy = "server".equals(worldOrServer);

		ConfigurationFile premium = yamls.get("premiumconfig.yml");
		ConfigurationFile bossbar = yamls.get("bossbar.yml");
		ConfigurationFile config = yamls.get("config.yml");
		ConfigurationFile finalCfg = yamls.get("finalConfig");

		finalCfg.set("scoreboard-teams.enabled",config.getBoolean("change-nametag-prefix-suffix",true));
		finalCfg.set("scoreboard-teams.invisible-nametags",config.getBoolean("invisible-nametags",false));
		finalCfg.set("scoreboard-teams.anti-override",config.getBoolean("anti-override.scoreboard-teams",true));
		finalCfg.set("scoreboard-teams.enable-collision",config.getBoolean("enable-collision",true));

		List<String> sortingtypes = new ArrayList<>();
		String sortingtype = premium.getString("sorting-type");
		if (sortingtype == null)
			sortingtypes = Arrays.asList("GROUPS:owner,admin,mod,helper,builder,vip,default","PLACEHOLDER_A_TO_Z:%player%");
		else {
			for (String type : sortingtype.split("_THEN_")) {
				if (type.equalsIgnoreCase("GROUPS") || type.equalsIgnoreCase("GROUP_PERMISSIONS")) {
					List<String> sortinglist = config.getStringList("group-sorting-priority-list", Arrays.asList("owner", "admin", "mod", "helper", "builder", "premium", "player", "default"));

					StringBuilder groups = new StringBuilder(("GROUP_PERMISSIONS".equals(type) ? "PERMISSIONS" : "GROUPS") + ":");
					for (String group : sortinglist) {
						groups.append(("GROUP_PERMISSIONS".equals(type) ? "tab.sort." : ""));
						groups.append(group);
						if (sortinglist.indexOf(group) != sortinglist.size() - 1)
							groups.append(",");
					}
					sortingtypes.add(groups.toString());
				}
				else if (type.equalsIgnoreCase("PLACEHOLDER")) {
					List<String> sortinglist = config.getStringList("placeholder-order", Arrays.asList("value1", "value2"));
					String placeholder = premium.getString("sorting-placeholder", "%some_level_maybe?%");

					StringBuilder groups = new StringBuilder("PLACEHOLDER:"+placeholder+":");
					for (String group : sortinglist) {
						groups.append(group);
						if (sortinglist.indexOf(group) != sortinglist.size() - 1)
							groups.append(",");
					}
					sortingtypes.add(groups.toString());
				}
				else {
					String placeholder = premium.getString("sorting-placeholder", "%some_level_maybe?%");
					if (!placeholder.equalsIgnoreCase("%some_level_maybe?%"))
						sortingtypes.add(type+":"+placeholder);
				}
			}
		}
		finalCfg.set("scoreboard-teams.sorting-types",sortingtypes);

		finalCfg.set("scoreboard-teams.case-sensitive-sorting",premium.getBoolean("case-sensitive-sorting",true));
		finalCfg.set("scoreboard-teams.disable-in-worlds",config.getStringList("disable-features-in-worlds.nametag",disabledWorld));
		if (isProxy)
			finalCfg.set("scoreboard-teams.case-disable-in-servers",config.getStringList("disable-features-in-servers.nametag",disabledServer));
		finalCfg.set("scoreboard-teams.unlimited-nametag-mode.enabled",config.getBoolean("unlimited-nametag-prefix-suffix-mode.enabled",false));
		finalCfg.set("scoreboard-teams.unlimited-nametag-mode.use-marker-tag-for-1-8-x-clients",config.getBoolean("unlimited-nametag-prefix-suffix-mode.use-marker-tag-for-1-8-x-clients",false));
		finalCfg.set("scoreboard-teams.unlimited-nametag-mode.disable-on-boats",config.getBoolean("unlimited-nametag-prefix-suffix-mode.disable-on-boats",true));
		finalCfg.set("scoreboard-teams.unlimited-nametag-mode.space-between-lines",config.getBoolean("unlimited-nametag-prefix-suffix-mode.space-between-lines",true));
		finalCfg.set("scoreboard-teams.unlimited-nametag-mode.disable-in-worlds",config.getStringList("disable-features-in-worlds.unlimited-nametags",disabledWorld));
		finalCfg.set("scoreboard-teams.unlimited-nametag-mode.dynamic-lines",premium.getStringList("unlimited-nametag-mode-dynamic-lines",Arrays.asList("abovename","nametag","belowname","another")));
		finalCfg.set("scoreboard-teams.unlimited-nametag-mode.static-lines",premium.getConfigurationSection("unlimited-nametag-mode-static-lines"));

		finalCfg.set("tablist-name-formatting.enabled", config.getBoolean("change-tablist-prefix-suffix",true));
		finalCfg.set("tablist-name-formatting.align-tabsuffix-on-the-right", premium.getBoolean("align-tabsuffix-on-the-right",false));
		finalCfg.set("tablist-name-formatting.character-width-overrides", premium.getConfigurationSection("character-width-overrides"));
		finalCfg.set("tablist-name-formatting.anti-override", config.getBoolean("anti-override.tablist-names",true));
		finalCfg.set("tablist-name-formatting.disable-in-worlds", config.getStringList("disable-features-in-worlds.tablist-names",disabledWorld));
		if (isProxy)
			finalCfg.set("tablist-name-formatting.disable-in-servers",config.getStringList("disable-features-in-servers.tablist-names",disabledServer));

		finalCfg.set("header-footer.enabled", config.getBoolean("enable-header-footer",true));
		finalCfg.set("header-footer.disable-in-worlds", config.getStringList("disable-features-in-worlds.header-footer",disabledWorld));
		if (isProxy)
			finalCfg.set("header-footer.disable-in-servers",premium.getStringList("disable-features-in-servers.header-footer",disabledServer));
		finalCfg.set("header-footer.header", config.getStringList("header"));
		finalCfg.set("header-footer.footer", config.getStringList("footer"));

		finalCfg.set("yellow-number-in-tablist.enabled", !config.getString("yellow-number-in-tablist","%ping%").equals(""));
		finalCfg.set("yellow-number-in-tablist.value", config.getString("yellow-number-in-tablist","%ping%"));
		finalCfg.set("yellow-number-in-tablist.anti-override", config.getBoolean("anti-override.scoreboard-objectives",true));
		finalCfg.set("yellow-number-in-tablist.disable-in-worlds", config.getStringList("disable-features-in-worlds.yellow-number",disabledWorld));
		if (isProxy)
			finalCfg.set("yellow-number.disable-in-servers",config.getStringList("disable-features-in-servers.yellow-number",disabledServer));

		finalCfg.set("belowname-objective", config.getConfigurationSection("classic-vanilla-belowname"));
		finalCfg.set("belowname-objective.anti-override", config.getBoolean("anti-override.scoreboard-objectives",true));
		finalCfg.set("belowname-objective.disable-in-worlds", config.getStringList("disable-features-in-worlds.belowname",disabledWorld));
		if (isProxy)
			finalCfg.set("belowname-objective.disable-in-servers",config.getStringList("disable-features-in-servers.belowname",disabledServer));

		finalCfg.set("prevent-spectator-effect.enabled", config.getBoolean("do-not-move-spectators",false));

		finalCfg.set("bossbar.enabled", bossbar.getBoolean("bossbar-enabled",false));
		finalCfg.set("bossbar.toggle-command", bossbar.getString("bossbar-toggle-command","/bossbar"));
		finalCfg.set("bossbar.remember-toggle-choice", bossbar.getBoolean("remember-toggle-choice",false));
		finalCfg.set("bossbar.hidden-by-default", bossbar.getBoolean("hidden-by-default",false));
		finalCfg.set("bossbar.default-bars", bossbar.getStringList("default-bars",Arrays.asList("ServerInfo","tpsbar")));
		finalCfg.set("bossbar.disable-in-worlds", bossbar.getObject("disable-features-in-worlds.bossbar"));
		if (isProxy)
			finalCfg.set("bossbar.disable-in-servers",config.getStringList("disable-features-in-servers.bossbar",disabledServer));

		Map<String,Map<String,String>> bossbars = bossbar.getConfigurationSection("bars");
		Map<String,List<String>> perworldBossbars = bossbar.getConfigurationSection("per-world");
		if (perworldBossbars != null) {
			for (Entry<String, List<String>> entry : perworldBossbars.entrySet()) {
				String world = entry.getKey();
				for (String bar : entry.getValue()) {
					if (!bossbars.containsKey(bar)) continue;
					if (bossbars.get(bar).containsKey("display-condition"))
						bossbars.get(bar).put("display-condition", bossbars.get(bar).get("display-condition")+";%"+worldOrServer+"%="+world);
					else bossbars.get(bar).put("display-condition", "%"+worldOrServer+"%="+world);
				}
			}
		}
		finalCfg.set("bossbar.bars", bossbars);

		if (!isProxy)
			finalCfg.set("per-world-playerlist", config.getConfigurationSection("per-world-playerlist"));

		finalCfg.set("scoreboard", premium.getObject("scoreboard"));
		finalCfg.set("scoreboard.permission-required-to-toggle", null);
		Map<String,Map<String,Object>> scoreboards = premium.getConfigurationSection("scoreboards");
		Map<String,String> perworldScoreboards = premium.getConfigurationSection("scoreboard.per-world");
		finalCfg.set("scoreboard.per-world", null);
		if (perworldScoreboards != null)
			perworldScoreboards.forEach((world,sb)->{
				if (!scoreboards.containsKey(sb)) return;
				if (scoreboards.get(sb).containsKey("display-condition"))
					scoreboards.get(sb).put("display-condition", scoreboards.get(sb).get("display-condition")+";%"+worldOrServer+"%="+world);
				else scoreboards.get(sb).put("display-condition", "%"+worldOrServer+"%="+world);
			});
		finalCfg.set("scoreboard.scoreboards", scoreboards);

		finalCfg.set("ping-spoof", config.getConfigurationSection("ping-spoof"));

		finalCfg.set("fix-pet-names.enabled", config.getBoolean("fix-pet-names",false));

		Map<String,Object> placeholders = config.getConfigurationSection("placeholders");
		String afkyes = placeholders.remove("afk-yes")+"";
		String afkno = placeholders.remove("afk-no")+"";
		finalCfg.set("placeholders", placeholders);

		finalCfg.set("placeholder-output-replacements", premium.getConfigurationSection("placeholder-output-replacements"));
		finalCfg.set("placeholder-output-replacements.%afk%.yes", afkyes);
		finalCfg.set("placeholder-output-replacements.%afk%.no", afkno);

		finalCfg.set("conditions",premium.getConfigurationSection("conditions"));

		finalCfg.set("placeholderapi-refresh-intervals", config.getConfigurationSection("placeholderapi-refresh-intervals"));
		finalCfg.set("assign-groups-by-permissions", config.getBoolean("assign-groups-by-permissions",false));
		finalCfg.set("primary-group-finding-list", config.getStringList("primary-group-finding-list",Arrays.asList("Owner","Admin","Mod","Helper","default")));

		finalCfg.set("debug", config.getBoolean("debug", false));

		finalCfg.set("mysql.enabled", false);
		finalCfg.set("mysql.host", "127.0.0.1");
		finalCfg.set("mysql.port", 3306);
		finalCfg.set("mysql.database", "tab");
		finalCfg.set("mysql.username", "user");
		finalCfg.set("mysql.password", "password");

		ConfigurationFile groups = yamls.get("groups.yml");
		ConfigurationFile users = yamls.get("users.yml");
		groups.setValues(config.getConfigurationSection("Groups"));
		users.setValues(config.getConfigurationSection("Users"));

		Map<String,Map<String,Object>> perworldsettings = config.getConfigurationSection("per-"+worldOrServer+"-settings");
		Map<String,Object> groupMap = new HashMap<>();
		Map<String,Object> userMap = new HashMap<>();
		Map<String,Object> headerFooterMap = new HashMap<>();
		if (perworldsettings != null) {
			Map<String,Map<String,Object>> worldMap = new HashMap<>(perworldsettings);
			for (Entry<String, Map<String, Object>> entry : worldMap.entrySet()) {
				String world = entry.getKey();
				Map<String, Object> map = new HashMap<>(entry.getValue());
				Map<String,Object> headerFooter = new HashMap<>();
				for (Entry<String, Object> entry2 : map.entrySet()) {
					String value = entry2.getKey();
					if (value.equalsIgnoreCase("Groups"))
						groupMap.put(world,entry2.getValue());
					else if (value.equalsIgnoreCase("Users"))
						userMap.put(world,entry2.getValue());
					else if (value.equalsIgnoreCase("header") || value.equalsIgnoreCase("footer"))
						headerFooter.put(value,entry2.getValue());
				}
				headerFooterMap.put(world,headerFooter);
				if (entry.getValue().isEmpty())
					perworldsettings.remove(world);
			}
		}
		groups.set("per-"+worldOrServer, groupMap);
		groups.set("_DEFAULT_",groups.getConfigurationSection("_OTHER_"));
		groups.set("_OTHER_",null);
		users.set("per-"+worldOrServer, userMap);
		for (Object world : groups.getConfigurationSection("per-"+worldOrServer).keySet()) {
			String gPath = "per-"+worldOrServer+"."+world;
			if (!groups.hasConfigOption(gPath+"._OTHER_")) continue;
			groups.set(gPath+"._DEFAULT_",groups.getObject(gPath+"._OTHER_"));
			groups.set(gPath+"._OTHER_",null);
		}
		finalCfg.set("header-footer.per-"+worldOrServer, headerFooterMap);
	}

}