package me.neznamy.tab.shared;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.shared.permission.PermissionPlugin;

/**
 * An interface with methods that are called in universal code,
 * but require platform-specific API calls
 */
public interface Platform {

	/**
	 * Detects permission plugin and returns it's representing object
	 *
	 * @return	the interface representing the permission hook
	 */
	PermissionPlugin detectPermissionPlugin();
	
	/**
	 * Loads platform-specific features
	 */
	void loadFeatures();
	
	/**
	 * Sends a message into console
	 *
	 * @param	message
	 * 			message to send
	 * @param	translateColors
	 * 			if color codes should be translated or not
	 */
	void sendConsoleMessage(String message, boolean translateColors);
	
	/**
	 * Creates an instance of {@link me.neznamy.tab.api.placeholder.Placeholder}
	 * to handle this unknown placeholder (typically a PAPI placeholder)
	 *
	 * @param	identifier
	 * 			placeholder's identifier
	 */
	void registerUnknownPlaceholder(String identifier);
	
	/**
	 * Calls platform-specific load event.
	 * This method is called when plugin is fully enabled.
	 */
	void callLoadEvent();
	
	/**
	 * Calls platform-specific player load event.
	 * This method is called when player is fully loaded.
	 */
	void callLoadEvent(TabPlayer player);
	
	/**
	 * Returns platform-specific packet builder implementation
	 *
	 * @return	platform-specific packet builder
	 */
	PacketBuilder getPacketBuilder();

	/**
	 * Returns {@code true} if this platform is a proxy, {@code false} if a game server
	 *
	 * @return	{@code true} if this platform is a proxy, {@code false} if a game server
	 */
	boolean isProxy();

	/**
	 * Performs platform-specific plugin manager call and returns the result.
	 * If plugin is not installed, returns {@code null}.
	 *
	 * @param	plugin
	 * 			Plugin to check version of
	 * @return	Version string if plugin is installed, {@code null} if not
	 */
	String getPluginVersion(String plugin);

	/**
	 * Returns name of default config file for this platform
	 * as it appears in the final jar in root directory.
	 *
	 * @return	name of default config file for this platform
	 */
	String getConfigName();
}