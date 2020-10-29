package me.neznamy.tab.shared;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.premium.AlignedSuffix;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.premium.scoreboard.ScoreboardManager;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.config.ConfigurationFile;
import me.neznamy.tab.shared.features.BelowName;
import me.neznamy.tab.shared.features.GhostPlayerFix;
import me.neznamy.tab.shared.features.GroupRefresher;
import me.neznamy.tab.shared.features.HeaderFooter;
import me.neznamy.tab.shared.features.Playerlist;
import me.neznamy.tab.shared.features.SpectatorFix;
import me.neznamy.tab.shared.features.TabObjective;
import me.neznamy.tab.shared.features.UpdateChecker;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;

/**
 * An interface with methods that are called in universal code, but require platform-specific API calls
 */
public interface PlatformMethods {

	/**
	 * Detects permission plugin and returns it's representing object
	 * @return the interface representing the permission hook
	 */
	public PermissionPlugin detectPermissionPlugin();
	
	/**
	 * Loads features from config
	 * @param inject - whether tab's pipleline handler needs to be injected or not
	 * @throws Exception - if something fails
	 */
	public void loadFeatures(boolean inject) throws Exception;
	
	/**
	 * Sends a message into console
	 * @param message - the message
	 * @param translateColors - if color codes should be translated
	 */
	public void sendConsoleMessage(String message, boolean translateColors);
		
	/**
	 * Loads config.yml and it's platform-specific variables
	 * @throws Exception - If something fails (such as yaml syntax error)
	 */
	public void loadConfig() throws Exception;
	
	/**
	 * Creates an instance of me.neznamy.tab.shared.placeholders.Placeholder to handle this unknown placeholder (typically a PAPI placeholder)
	 * @param identifier - placeholder's identifier
	 */
	public void registerUnknownPlaceholder(String identifier);
	
	/**
	 * Converts configuration file into the latest version by removing old options, adding new ones or renaming
	 * @param config - the configuration file to be converted
	 */
	public void convertConfig(ConfigurationFile config);
	
	/**
	 * Returns server's version
	 * @return server's version
	 */
	public String getServerVersion();
	
	/**
	 * Suggests switch to internal placeholders instead of PAPI's for better performance
	 */
	public void suggestPlaceholders();
	
	/**
	 * Returns the word used to separate config options. It's value is "world" for bukkit and "server" for proxies
	 * @return "world" on bukkit, "server" on proxies
	 */
	public String getSeparatorType();
	
	/**
	 * Returns plugin's data folder
	 * @return plugin's data folder
	 */
	public File getDataFolder();
	
	/**
	 * Calls platform-specific event
	 * This method is called when plugin is fully enabled
	 */
	public void callLoadEvent();
	
	/**
	 * Removes an old config option that is not present anymore
	 * @param config - configuration file
	 * @param oldKey - name of removed config option
	 */
	public default void removeOld(ConfigurationFile config, String oldKey) {
		if (config.hasConfigOption(oldKey)) {
			config.set(oldKey, null);
			Shared.print('2', "Removed old " + config.getName() + " option " + oldKey);
		}
	}
	
	/**
	 * Renames variable in given configuration file
	 * @param config - configuration file to rename variable in
	 * @param oldName - old config option's name
	 * @param newName - new config option's name
	 */
	public default void rename(ConfigurationFile config, String oldName, String newName) {
		if (config.hasConfigOption(oldName)) {
			Object value = config.getObject(oldName);
			config.set(oldName, null);
			config.set(newName, value);
			Shared.print('2', "Renamed config option " + oldName + " to " + newName);
		}
	}

	/**
	 * Suggests placeholder switch if the "from" placeholder is used
	 * @param from - PAPI placeholder to be replaced
	 * @param to - the internal placeholder to be replaced by
	 */
	public default void suggestPlaceholderSwitch(String from, String to) {
		if (Placeholders.allUsedPlaceholderIdentifiers.contains(from)) {
			Shared.print('9', "Hint: Found used PlaceholderAPI placeholder \"&d" + from + "&9\". Consider replacing it with plugin's internal \"&d" + to + "&9\" for better performance.");
		}
	}
	
	/**
	 * Replaces all placeholders in given string
	 * @param string - string to replace
	 * @param player - player to replaced placeholders for
	 * @return replaced string
	 */
	public default String replaceAllPlaceholders(String string, TabPlayer player) {
		String replaced = string;
		for (Placeholder p : Placeholders.getAllPlaceholders()) {
			if (replaced.contains(p.getIdentifier())) {
				if (p instanceof ServerPlaceholder) {
					((ServerPlaceholder)p).update();
				}
				if (p instanceof PlayerPlaceholder) {
					((PlayerPlaceholder)p).update(player);
				}
				replaced = p.set(replaced, player);
			}
		}
		return replaced;
	}
	
	/**
	 * Converts premiumconfig variables to latest version
	 * @param config - the file
	 */
	public default void convertPremiumConfig(ConfigurationFile config) {
		removeOld(config, "scoreboard.refresh-interval-ticks");
		if (!config.hasConfigOption("placeholder-output-replacements")) {
			Map<String, Map<String, String>> replacements = new HashMap<String, Map<String, String>>();
			Map<String, String> essVanished = new HashMap<String, String>();
			essVanished.put("Yes", "&7| Vanished");
			essVanished.put("No", "");
			replacements.put("%essentials_vanished%", essVanished);
			Map<String, String> tps = new HashMap<String, String>();
			tps.put("20", "&aPerfect");
			replacements.put("%tps%", tps);
			config.set("placeholder-output-replacements", replacements);
			Shared.print('2', "Added new missing \"placeholder-output-replacements\" premiumconfig.yml section.");
		}
		boolean scoreboardsConverted = false;
		for (Object scoreboard : config.getConfigurationSection("scoreboards").keySet()) {
			Boolean permReq = config.getBoolean("scoreboards." + scoreboard + ".permission-required");
			if (permReq != null) {
				if (permReq) {
					config.set("scoreboards." + scoreboard + ".display-condition", "permission:tab.scoreboard." + scoreboard);
				}
				config.set("scoreboards." + scoreboard + ".permission-required", null);
				scoreboardsConverted = true;
			}
			String childBoard = config.getString("scoreboards." + scoreboard + ".if-permission-missing");
			if (childBoard != null) {
				config.set("scoreboards." + scoreboard + ".if-permission-missing", null);
				config.set("scoreboards." + scoreboard + ".if-condition-not-met", childBoard);
				scoreboardsConverted = true;
			}
		}
		if (scoreboardsConverted) {
			Shared.print('2', "Converted old premiumconfig.yml scoreboard display condition system to new one.");
		}
		removeOld(config, "scoreboard.refresh-interval-milliseconds");
		rename(config, "allign-tabsuffix-on-the-right", "align-tabsuffix-on-the-right");
	}
	
	/**
	 * Loads universal features present on all platforms with the same configuration
	 */
	public default void loadUniversalFeatures() {
		if (Configs.config.getBoolean("enable-header-footer", true)) Shared.featureManager.registerFeature("headerfooter", new HeaderFooter());
		if (Configs.config.getBoolean("do-not-move-spectators", false)) Shared.featureManager.registerFeature("spectatorfix", new SpectatorFix());
		if (Configs.config.getBoolean("classic-vanilla-belowname.enabled", true)) Shared.featureManager.registerFeature("belowname", new BelowName());
		if (Premium.is() && Premium.premiumconfig.getBoolean("scoreboard.enabled", false)) Shared.featureManager.registerFeature("scoreboard", new ScoreboardManager());
		if (Configs.SECRET_remove_ghost_players) Shared.featureManager.registerFeature("ghostplayerfix", new GhostPlayerFix());
		if (Configs.config.getString("yellow-number-in-tablist", "%ping%").length() > 0) Shared.featureManager.registerFeature("tabobjective", new TabObjective());
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8 && Configs.config.getBoolean("change-tablist-prefix-suffix", true)) {
			Playerlist playerlist = new Playerlist();
			Shared.featureManager.registerFeature("playerlist", playerlist);
			if (Premium.alignTabsuffix) Shared.featureManager.registerFeature("alignedsuffix", new AlignedSuffix(playerlist));
		}
		new GroupRefresher();
		new UpdateChecker();
	}
}