package me.neznamy.tab.api;

import java.util.Set;
import java.util.UUID;

import io.netty.channel.Channel;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;

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
	 * Makes player's nametag invisible until server restart/reload or /plugman reload tab
	 * @see showNametag 
	 * @see hasHiddenNametag
	 * @since 2.8.3
	 */
	public void hideNametag();
	
	public void hideNametag(UUID viewer);
	
	
	/**
	 * Makes player's nametag visible again
	 * @see hideNametag
	 * @see hasHiddenNametag
	 * @since 2.8.3
	 */
	public void showNametag();
	
	public void showNametag(UUID viewer);
	
	
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
	
	public UUID getTablistUUID();
	
	/**
	 * Returns player's protocol version
	 * @return player's protocol version
	 * @since 2.8.5
	 */
	public ProtocolVersion getVersion();
	
	/**
	 * Returns platform-specific entity
	 * @return an instance of bukkit/bungee/velocity player depending on platform
	 * @since 2.8.5
	 */
	public Object getPlayer();
	
	/**
	 * Returns player's current world name (server on bungeecord)
	 * @return namre of world (server on bungeecord) where player is currently in
	 * @since 2.8.5
	 */
	public String getWorldName();
	
	public void setWorldName(String name);
	
	
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

	public void sendCustomPacket(UniversalPacketPlayOut packet, TabFeature feature);
	
	/**
	 * Sends the player a platform-specific packet
	 * @param packet - an instance of packet depending on platform, or an instance of me.neznamy.tab.platforms.bukkit.nms.PacketPlayOut
	 * @since 2.8.5
	 */
	public void sendPacket(Object packet);
	
	public void sendPacket(Object packet, TabFeature feature);
	
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
	
	public void sendMessage(IChatBaseComponent message);
	
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
	
	/**
	 * Returns whether player has tab.staff permission or not
	 * @return whether player is staff or not
	 * @since 2.8.7
	 */
	public boolean isStaff();
	
	public Channel getChannel();
	
	public long getPing();
	
	public Object getSkin();
	
	public ArmorStandManager getArmorStandManager();
	
	public void setArmorStandManager(ArmorStandManager armorStandManager);
	
	public void unregisterTeam();
	
	public void unregisterTeam(TabPlayer viewer);
	
	public void registerTeam();
	
	public void registerTeam(TabPlayer viewer);
	
	public void updateTeam();
	
	public void updateTeamData();
	
	public boolean isLoaded();
	
	public void markAsLoaded();
	
	public boolean hasBossbarVisible();
	
	public void setBossbarVisible(boolean visible);
	
	public Set<BossBar> getActiveBossBars();
	
	public void setProperty(String identifier, String rawValue);
	
	public void loadPropertyFromConfig(String property);
	
	public void loadPropertyFromConfig(String property, String ifNotSet);
	
	public void setTeamName(String name);
	
	public String getTeamName();
	
	public void setTeamNameNote(String note);
	
	public String getTeamNameNote();
	
	public void setCollisionRule(Boolean collision);
	
	public Boolean getCollisionRule();
	
	public boolean isDisguised();
	
	
	
	
	public void setOnBoat(boolean onBoat);
	
	public boolean isOnBoat();
	
	public void setScoreboardVisible(boolean visible, boolean sendToggleMessage);
	
	public void toggleScoreboard(boolean sendToggleMessage);
	
	public boolean isScoreboardVisible();
	
	public void setActiveScoreboard(Scoreboard board);
	
	public Scoreboard getActiveScoreboard();
	
	public void setGroup(String group, boolean refreshIfChanged);
	
	public boolean hasInvisibilityPotion();
	
	public boolean hasForcedScoreboard();
	
	public boolean isVanished();
	
	public void showBossBar(BossBar bossbar);
	
	public void removeBossBar(BossBar bossbar);

	public void setAttribute(String attribute, String value);
	
	public void updateCollision();
}