package me.neznamy.tab.shared;

import java.io.File;

import me.neznamy.tab.shared.config.ConfigurationFile;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.placeholders.Placeholders;

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
	 * Builds packet into it's platform-specific type and returns it
	 * @param packet - custom packet to be built
	 * @param protocolVersion - client's protocol version
	 * @return The built packet
	 * @throws Exception - If packet creation fails
	 */
	public Object buildPacket(UniversalPacketPlayOut packet, ProtocolVersion protocolVersion) throws Exception;
	
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
}