package me.neznamy.tab.shared;

import java.io.File;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.shared.permission.PermissionPlugin;

/**
 * An interface with methods that are called in universal code, but require platform-specific API calls
 */
public interface Platform {

	/**
	 * Detects permission plugin and returns it's representing object
	 * @return the interface representing the permission hook
	 */
	PermissionPlugin detectPermissionPlugin();
	
	/**
	 * Loads features
	 */
	void loadFeatures();
	
	/**
	 * Sends a message into console
	 * @param message - the message
	 * @param translateColors - if color codes should be translated
	 */
	void sendConsoleMessage(String message, boolean translateColors);
	
	/**
	 * Creates an instance of {@link me.neznamy.tab.api.placeholder.Placeholder} to handle this unknown placeholder (typically a PAPI placeholder)
	 * @param identifier - placeholder's identifier
	 */
	void registerUnknownPlaceholder(String identifier);
	
	/**
	 * Returns server's version
	 * @return server's version
	 */
	String getServerVersion();

	/**
	 * Returns plugin's data folder
	 * @return plugin's data folder
	 */
	File getDataFolder();
	
	/**
	 * Calls platform-specific event
	 * This method is called when plugin is fully enabled
	 */
	void callLoadEvent();
	
	/**
	 * Calls platform-specific event
	 * This method is called when player is fully loaded
	 */
	void callLoadEvent(TabPlayer player);
	
	/**
	 * Returns max player count configured in server files
	 * @return max player count
	 */
	int getMaxPlayers();
	
	/**
	 * Returns platform-specific packet builder
	 * @return platform-specific packet builder
	 */
	PacketBuilder getPacketBuilder();
	
	/**
	 * Converts value-signature array into platform-specific skin object
	 * @param properties - value and signature
	 * @return platform-specific skin object
	 */
	Object getSkin(List<String> properties);
	
	boolean isProxy();
	
	boolean isPluginEnabled(String plugin);

	String getConfigName();
}