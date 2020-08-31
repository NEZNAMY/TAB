package me.neznamy.tab.api;

import java.util.UUID;

import org.bukkit.entity.Player;

import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOut;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * An interface representing a player
 */
public interface TabPlayer {

	/**
	 * Changes the requested property of a player temporarily (until next restart, reload or /tab reload)
	 * @param type Type of property
	 * @param value The value to be used
	 * @since 2.8.3
	 */
	public void setValueTemporarily(EnumProperty type, String value);
	
	
	/**
	 * Changes the requested property of a player permanently (saved into config too)
	 * @param type Type of property
	 * @param value The value to be used
	 * @since 2.8.3
	 */
	public void setValuePermanently(EnumProperty type, String value);
	
	
	/**
	 * Returns temporary value of player's property or null if not set
	 * @param type Type of property
	 * @return Temporary value of player's property or null if not set
	 * @see hasTemporaryValue
	 * @see setValueTemporarily
	 * @since 2.8.3
	 */
	public String getTemporaryValue(EnumProperty type);
	
	
	/**
	 * Returns Whether player has temporary value or not
	 * @param type Type of property
	 * @return Whether player has temporary value or not
	 * @since 2.8.3
	 */
	public boolean hasTemporaryValue(EnumProperty type);
	
	
	/**
	 * Removes temporary value from player if set
	 * @param type Type of property
	 * @since 2.8.3
	 */
	public void removeTemporaryValue(EnumProperty type);
	
	
	/**
	 * Returns original value of property of player
	 * @param type Type of property
	 * @return Original value of property of player
	 * @since 2.8.3
	 */
	public String getOriginalValue(EnumProperty type);
	
	
	/**
	 * Sends requested header and footer to player
	 * @param header - Header
	 * @param footer - Footer
	 * @since 2.8.3
	 */
	public void sendHeaderFooter(String header, String footer);
	
	
	/**
	 * Makes player's nametag invisible until server restart/reload or /plugman reload tab
	 * @see showNametag 
	 * @see hasHiddenNametag
	 * @since 2.8.3
	 */
	public void hideNametag();
	
	
	/**
	 * Makes player's nametag visible again
	 * @see hideNametag
	 * @see hasHiddenNametag
	 * @since 2.8.3
	 */
	public void showNametag();
	
	
	/**
	 * Return whether player has hidden nametag or not
	 * @return Whether player has hidden nametag or not
	 * @since 2.8.3
	 * @see hideNametag
	 * @see showNametag
	 */
	public boolean hasHiddenNametag();
	
	
	/**
	 * Refreshes all visuals on the player
	 * @since 2.8.3
	 */
	public void forceRefresh();
	
	
	/**
	 * Displays a scoreboard created using TABAPI.createScoreboard method
	 * @see removeCustomScoreboard
	 * @since 2.8.3
	 */
	public void showScoreboard(Scoreboard scoreboard);
	
	
	/**
	 * Displays a scoreboard defined in premiumconfig.yml
	 * @see removeCustomScoreboard
	 * @since 2.8.3
	 */
	public void showScoreboard(String name);
	
	
	/**
	 * Removes forced scoreboard sent using one of the showScoreboard methods
	 * @see showScoreboard
	 * @since 2.8.3
	 */
	public void removeCustomScoreboard();
	
	
	/**
	 * Returns player's name
	 * @return Player's name
	 * @since 2.8.3
	 */
	public String getName();
	
	
	/**
	 * Returns player's UUID
	 * @return Player's UUID
	 * @since 2.8.3
	 */
	public UUID getUniqueId();
	
	/**
	 * Returns player's protocol version
	 * @return player's protocol version
	 * @since 2.8.5
	 */
	public ProtocolVersion getVersion();
	
	/**
	 * Returns the bukkit player
	 * @return an instance of org.bukkit.entity.Player
	 * @throws IllegalStateException if called from bungeecord/velocity
	 * @since 2.8.5
	 */
	public Player getBukkitEntity();
	
	/**
	 * Returns the bungee player
	 * @return an instance of net.md-5.bungee.api.connection.ProxiedPlayer
	 * @throws IllegalStateException if called from bukkit/velocity
	 * @since 2.8.5
	 */
	public ProxiedPlayer getBungeeEntity();
	
	/**
	 * Returns the velocity player
	 * @return an instance of com.velocitypowered.api.proxy.Player
	 * @throws IllegalStateException if called from bukkit/bungeecord
	 * @since 2.8.5
	 */
	public com.velocitypowered.api.proxy.Player getVelocityEntity();
	
	/**
	 * Sends the player a packet represented by a custom class
	 * @param packet - an instance of me.neznamy.tab.platforms.bukkit.packets.PacketPlayOut
	 * @since 2.8.5
	 */
	public void sendCustomBukkitPacket(PacketPlayOut packet);
	
	/**
	 * Returns player's current world name (server on bungeecord)
	 * @return namre of world (server on bungeecord) where player is currently in
	 * @since 2.8.5
	 */
	public String getWorldName();
	
	
	/**
	 * Performs platform-specific API call to check for permission and returns the result
	 * @param permission - the permission to check for
	 * @return - true if has, false if not
	 * @since 2.8.5
	 */
	public boolean hasPermission(String permission);
	
	/**
	 * Sends the player a custom universal packet
	 * @param packet - an instance of me.neznamy.tab.shared.packets.UniversalPacketPlayOut
	 * @since 2.8.5
	 */
	public void sendCustomPacket(UniversalPacketPlayOut packet);
	
	/**
	 * Returns player's property by name
	 * @param name - name of property
	 * @return the property or null if not found
	 * @since 2.8.5
	 */
	public Property getProperty(String name);
	
	/**
	 * Sends a message to the player
	 * @param message - message to be sent
	 * @param translateColors - whether colors should be translated or not
	 * @since 2.8.5
	 */
	public void sendMessage(String message, boolean translateColors);
	
	/**
	 * Returns player's primary permission group
	 * @return group depending on configuration
	 * @since 2.8.5
	 */
	public String getGroup();
	
	/**
	 * Toggles armor stands preview mode for the player
	 * @throws IllegalStateException - if unlimited nametag mode is not enabled
	 * @since 2.8.5
	 */
	public void toggleNametagPreview();
	
	/**
	 * Returns whether player is previewing nametag or not
	 * @return true if previewing, false if not
	 * @since 2.8.5
	 */
	public boolean isPreviewingNametag();
}
