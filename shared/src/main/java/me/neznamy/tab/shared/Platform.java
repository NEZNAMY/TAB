package me.neznamy.tab.shared;

import java.io.File;
import java.util.Map;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.config.ConfigurationFile;
import me.neznamy.tab.shared.features.AlignedSuffix;
import me.neznamy.tab.shared.features.BelowName;
import me.neznamy.tab.shared.features.GhostPlayerFix;
import me.neznamy.tab.shared.features.GroupRefresher;
import me.neznamy.tab.shared.features.HeaderFooter;
import me.neznamy.tab.shared.features.PingSpoof;
import me.neznamy.tab.shared.features.Playerlist;
import me.neznamy.tab.shared.features.PluginInfo;
import me.neznamy.tab.shared.features.SpectatorFix;
import me.neznamy.tab.shared.features.TabObjective;
import me.neznamy.tab.shared.features.UpdateChecker;
import me.neznamy.tab.shared.features.bossbar.BossBar;
import me.neznamy.tab.shared.features.layout.Layout;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManager;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;

/**
 * An interface with methods that are called in universal code, but require platform-specific API calls
 */
public interface Platform {

	/**
	 * Detects permission plugin and returns it's representing object
	 * @return the interface representing the permission hook
	 */
	public PermissionPlugin detectPermissionPlugin();
	
	/**
	 * Loads features from config
	 * @throws Exception - if something fails
	 */
	public void loadFeatures() throws Exception;
	
	/**
	 * Sends a message into console
	 * @param message - the message
	 * @param translateColors - if color codes should be translated
	 */
	public void sendConsoleMessage(String message, boolean translateColors);
	
	/**
	 * Creates an instance of me.neznamy.tab.shared.placeholders.Placeholder to handle this unknown placeholder (typically a PAPI placeholder)
	 * @param identifier - placeholder's identifier
	 */
	public void registerUnknownPlaceholder(String identifier);
	
	/**
	 * Returns server's version
	 * @return server's version
	 */
	public String getServerVersion();
	
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
	 * Returns max player count configured in server files
	 * @return max player count
	 */
	public int getMaxPlayers();
	
	/**
	 * Removes an old config option that is not present anymore
	 * @param config - configuration file
	 * @param oldKey - name of removed config option
	 */
	public default void removeOld(ConfigurationFile config, String oldKey) {
		if (config.hasConfigOption(oldKey)) {
			config.set(oldKey, null);
			TAB.getInstance().print('2', "Removed old " + config.getName() + " option " + oldKey);
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
			TAB.getInstance().print('2', "Renamed config option " + oldName + " to " + newName);
		}
	}
	
	/**
	 * Replaces all placeholders in given string
	 * @param string - string to replace
	 * @param player - player to replaced placeholders for
	 * @return replaced string
	 */
	public default String replaceAllPlaceholders(String string, TabPlayer player) {
		if (string == null) return null;
		String replaced = string;
		for (Placeholder p : TAB.getInstance().getPlaceholderManager().getAllPlaceholders()) {
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
	 * Converts configuration options same on all platforms
	 * @param file - file to convert
	 */
	public default void convertUniversalOptions(ConfigurationFile file) {
		if (file.getName().equals("animations.yml")) {
			Map<String, Object> values = file.getValues();
			if (values.size() == 1 && values.containsKey("animations")) {
				file.setValues(file.getConfigurationSection("animations"));
				file.save();
				TAB.getInstance().print('2', "Converted animations.yml to new format.");
			}
		}
	}
	
	/**
	 * Loads universal features present on all platforms with the same configuration
	 */
	public default void loadUniversalFeatures() {
		TAB tab = TAB.getInstance();
		if (tab.getConfiguration().config.getBoolean("enable-header-footer", true)) tab.getFeatureManager().registerFeature("headerfooter", new HeaderFooter(tab));
		if (tab.getConfiguration().config.getBoolean("do-not-move-spectators", false)) tab.getFeatureManager().registerFeature("spectatorfix", new SpectatorFix());
		if (tab.getConfiguration().config.getBoolean("classic-vanilla-belowname.enabled", true)) tab.getFeatureManager().registerFeature("belowname", new BelowName(tab));
		if (tab.getConfiguration().premiumconfig != null && tab.getConfiguration().premiumconfig.getBoolean("scoreboard.enabled", false)) tab.getFeatureManager().registerFeature("scoreboard", new ScoreboardManager(tab));
		if (tab.getConfiguration().removeGhostPlayers) tab.getFeatureManager().registerFeature("ghostplayerfix", new GhostPlayerFix());
		if (tab.getConfiguration().config.getString("yellow-number-in-tablist", "%ping%").length() > 0) tab.getFeatureManager().registerFeature("tabobjective", new TabObjective(tab));
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8 && tab.getConfiguration().config.getBoolean("change-tablist-prefix-suffix", true)) {
			Playerlist playerlist = new Playerlist(tab);
			tab.getFeatureManager().registerFeature("playerlist", playerlist);
			if (tab.getConfiguration().premiumconfig != null && tab.getConfiguration().premiumconfig.getBoolean("align-tabsuffix-on-the-right", false)) tab.getFeatureManager().registerFeature("alignedsuffix", new AlignedSuffix(playerlist, tab));
		}
		tab.getFeatureManager().registerFeature("group", new GroupRefresher(tab));
		tab.getFeatureManager().registerFeature("info", new PluginInfo());
		new UpdateChecker(tab);
		if (tab.getConfiguration().layout) tab.getFeatureManager().registerFeature("layout", new Layout(tab));
		if (tab.getConfiguration().bossbar.getBoolean("bossbar-enabled", false)) tab.getFeatureManager().registerFeature("bossbar", new BossBar(tab));
		if (tab.getConfiguration().config.getBoolean("ping-spoof.enabled", false)) tab.getFeatureManager().registerFeature("pingspoof", new PingSpoof());
	}
}