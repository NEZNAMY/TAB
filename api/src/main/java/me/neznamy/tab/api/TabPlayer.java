package me.neznamy.tab.api;

import java.util.UUID;

import io.netty.channel.Channel;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.TabPacket;

/**
 * An interface representing a player
 */
public interface TabPlayer {

	/**
	 * Refreshes all visuals on the player
	 */
	public void forceRefresh();

	/**
	 * Returns player's name
	 * @return Player's name
	 */
	public String getName();

	/**
	 * Returns player's UUID
	 * @return Player's UUID
	 */
	public UUID getUniqueId();

	/**
	 * Returns player's uuid used in tablist. This may only be different than real uuid if
	 * TAB is installed on velocity with some specific velocity setup
	 * @return player's uuid in tablist
	 */
	public UUID getTablistUUID();

	/**
	 * Returns player's protocol version
	 * @return player's protocol version
	 */
	public ProtocolVersion getVersion();

	/**
	 * Returns platform-specific entity
	 * @return an instance of bukkit/bungee player depending on platform
	 */
	public Object getPlayer();

	/**
	 * Returns player's current world name (on bungeecord this requires bridge installed)
	 * @return name of world where player is currently in, null on bungeecord if bridge is not installed
	 */
	public String getWorld();
	
	/**
	 * Returns player's current server name on bungeecord (on bukkit this returns null)
	 * @return name of server where player is currently in, null on bukkit
	 */
	public String getServer();

	/**
	 * Performs platform-specific API call to check for permission and returns the result
	 * @param permission - the permission to check for
	 * @return - true if has, false if not
	 */
	public boolean hasPermission(String permission);

	/**
	 * Sends the player a custom universal packet
	 * @param packet - packet to send
	 */
	public void sendCustomPacket(TabPacket packet);

	/**
	 * Sends the player a custom universal packet and adds that packet into counter that
	 * is displayed in /tab cpu
	 * @param packet - packet to send
	 * @param feature - feature to increment sent packet counter of 
	 */
	public void sendCustomPacket(TabPacket packet, TabFeature feature);
	
	/**
	 * Sends the player a custom universal packet and adds that packet into counter that
	 * is displayed in /tab cpu
	 * @param packet - packet to send
	 * @param feature - feature to increment sent packet counter of 
	 */
	public void sendCustomPacket(TabPacket packet, String feature);

	/**
	 * Sends the player a platform-specific packet
	 * @param packet - an instance of packet depending on platform
	 */
	public void sendPacket(Object packet);

	/**
	 * Sends the player a platform-specific packet and adds that packet into counter that
	 * is displayed in /tab cpu
	 * @param packet - an instance of packet depending on platform
	 * @param feature - feature to increment sent packet counter of 
	 */
	public void sendPacket(Object packet, TabFeature feature);

	/**
	 * Sends the player a platform-specific packet and adds that packet into counter that
	 * is displayed in /tab cpu
	 * @param packet - an instance of packet depending on platform
	 * @param feature - feature to increment sent packet counter of
	 */
	public void sendPacket(Object packet, String feature);

	/**
	 * Returns player's property by name
	 * @param name - name of property
	 * @return the property or null if not found
	 */
	public Property getProperty(String name);

	/**
	 * Sends a message to the player
	 * @param message - message to be sent
	 * @param translateColors - whether colors should be translated or not
	 */
	public void sendMessage(String message, boolean translateColors);

	/**
	 * Sends specified component as a chat message
	 * @param message - message to send
	 */
	public void sendMessage(IChatBaseComponent message);

	/**
	 * Returns player's primary permission group
	 * @return player's group depending on configuration
	 */
	public String getGroup();

	/**
	 * Toggles armor stands preview mode for the player
	 */
	public void toggleNametagPreview();

	/**
	 * Returns whether player is previewing nametag or not
	 * @return true if previewing, false if not
	 */
	public boolean isPreviewingNametag();

	/**
	 * Returns player's channel or null if server is 1.7 or older
	 * @return player's channel
	 */
	public Channel getChannel();

	/**
	 * Returns player's ping calculated by server
	 * @return player's ping
	 */
	public int getPing();

	/**
	 * Returns player's platform-specific skin data
	 * @return player's skin
	 */
	public Object getSkin();

	/**
	 * Returns true once the player is successfully loaded (onJoin method ran through all methods)
	 * @return true if player is fully loaded, false otherwise
	 */
	public boolean isLoaded();

	/**
	 * Sets property with specified name to new value. If property did no exist before, it is
	 * created and true is returned. If it existed, it is overridden and true is returned. False is returned othewise.
	 * @param feature - feature using this property to get placeholders registered
	 * @param identifier - property name
	 * @param rawValue - new raw value
	 * @return true if value changed / did not exist, false if value did not change
	 */
	public boolean setProperty(TabFeature feature, String identifier, String rawValue);

	/**
	 * Loads property from config using standard property loading algorithm
	 * @param property - property name to load
	 */
	public void loadPropertyFromConfig(TabFeature feature, String property);

	/**
	 * Loads property from config using standard property loading algorithm. If the property is
	 * not set in config, sets it to ifNotSet value
	 * @param property - property name to load
	 * @param ifNotSet - value to use if property is not defined in config
	 */
	public void loadPropertyFromConfig(TabFeature feature, String property, String ifNotSet);

	/**
	 * Returns name of player's scoreboard team or null if nametag feature is disabled
	 * @return name of player's team
	 */
	public String getTeamName();

	/**
	 * Returns user-friendly explanation of team name
	 * @return explanation behind team name
	 */
	public String getTeamNameNote();

	/**
	 * Returns true if player is disguised using iDisguide or LibsDisguises on bukkit, LibsDisguises 
	 * only on bungeecord via bukkit bridge
	 * @return true if player is disguised, false if not
	 */
	public boolean isDisguised();

	/**
	 * Returns true if player has invisiblity potion, false if not. For bukkit, API is used, for bungeecord
	 * bukkit bridge is used
	 * @return true if has invisiblity potion, false if not
	 */
	public boolean hasInvisibilityPotion();

	/**
	 * Currently only PV hook on bungeecord for global playerlist function. Looking to expand this
	 * in the future
	 * @return true if vanished with PV on bungeecord
	 */
	public boolean isVanished();

	/**
	 * Returns true if player is online according to server
	 * @return true if online, false if not
	 */
	public boolean isOnline();

	/**
	 * Returns player's armor stand manager if unlimited nametag mode is enabled, null if disabled
	 * @return player's armor stand manager
	 */
	public ArmorStandManager getArmorStandManager();

	/**
	 * Sets player's armor stands manager to new instance
	 * @param armorStandManager - new instance
	 */
	public void setArmorStandManager(ArmorStandManager armorStandManager);
	
	/**
	 * Returns gamemode of the player (0 for survival, 1 creative, 2 adventure, 3 spectator)
	 * @return gamemode of the player
	 */
	public int getGamemode();
	
	public boolean isBedrockPlayer();
}
