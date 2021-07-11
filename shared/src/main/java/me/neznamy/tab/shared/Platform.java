package me.neznamy.tab.shared;

import java.io.File;

import me.neznamy.tab.shared.features.AlignedSuffix;
import me.neznamy.tab.shared.features.GhostPlayerFix;
import me.neznamy.tab.shared.features.GroupRefresher;
import me.neznamy.tab.shared.features.HeaderFooter;
import me.neznamy.tab.shared.features.Playerlist;
import me.neznamy.tab.shared.features.PluginInfo;
import me.neznamy.tab.shared.features.UpdateChecker;
import me.neznamy.tab.shared.features.layout.Layout;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.placeholders.Placeholder;

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
	 * Loads features
	 */
	public void loadFeatures();
	
	/**
	 * Sends a message into console
	 * @param message - the message
	 * @param translateColors - if color codes should be translated
	 */
	public void sendConsoleMessage(String message, boolean translateColors);
	
	/**
	 * Creates an instance of me.neznamy.tab.shared.placeholders.Placeholder to handle this unknown placeholder (typically a PAPI placeholder)
	 * @param identifier - placeholder's identifier
	 * @return new placeholder
	 */
	public Placeholder registerUnknownPlaceholder(String identifier);
	
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
	 * Returns name of config file in the jar file on specific platform
	 * @return name of config file of the platform
	 */
	public String getConfigName();

	/**
	 * Loads universal features present on all platforms with the same configuration
	 */
	public default void loadUniversalFeatures() {
		TAB tab = TAB.getInstance();
		if (tab.getConfiguration().getConfig().getBoolean("enable-header-footer", true)) tab.getFeatureManager().registerFeature("headerfooter", new HeaderFooter());
		if (tab.getConfiguration().isRemoveGhostPlayers()) tab.getFeatureManager().registerFeature("ghostplayerfix", new GhostPlayerFix());
		if (TAB.getInstance().getServerVersion().getMinorVersion() >= 8 && tab.getConfiguration().getConfig().getBoolean("change-tablist-prefix-suffix", true)) {
			Playerlist playerlist = new Playerlist();
			tab.getFeatureManager().registerFeature("playerlist", playerlist);
			if (tab.getConfiguration().getPremiumConfig() != null && tab.getConfiguration().getPremiumConfig().getBoolean("align-tabsuffix-on-the-right", false)) tab.getFeatureManager().registerFeature("alignedsuffix", new AlignedSuffix(playerlist));
		}
		tab.getFeatureManager().registerFeature("group", new GroupRefresher());
		tab.getFeatureManager().registerFeature("info", new PluginInfo());
		new UpdateChecker(tab);
		if (tab.getConfiguration().isLayout()) tab.getFeatureManager().registerFeature("layout", new Layout());
	}
}