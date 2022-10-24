package me.neznamy.tab.api;

import java.util.UUID;

import io.netty.channel.Channel;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.Skin;
import me.neznamy.tab.api.protocol.TabPacket;

/**
 * An interface representing a player
 */
public interface TabPlayer {

    /**
     * Refreshes all visuals on the player
     */
    void forceRefresh();

    /**
     * Returns player's name
     *
     * @return  Player's name
     */
    String getName();

    /**
     * Returns player's UUID
     *
     * @return  Player's UUID
     */
    UUID getUniqueId();

    /**
     * Returns player's uuid used in TabList. This may only be different from real uuid if
     * TAB is installed on velocity with some specific velocity setup
     *
     * @return  player's uuid in TabList
     */
    UUID getTablistUUID();

    /**
     * Returns player's protocol version
     *
     * @return  player's protocol version
     */
    ProtocolVersion getVersion();

    /**
     * Returns platform-specific entity
     *
     * @return  an instance of bukkit/bungee player depending on platform
     */
    Object getPlayer();

    /**
     * Returns player's current world name (on BungeeCord this requires bridge installed)
     *
     * @return  name of world where player is currently in, "N/A" on BungeeCord if bridge is not installed
     */
    String getWorld();
    
    /**
     * Returns player's current server name on BungeeCord (on bukkit this returns "N/A")
     *
     * @return  name of server where player is currently in, "N/A" on bukkit
     */
    String getServer();

    /**
     * Performs platform-specific API call to check for permission and returns the result
     *
     * @param   permission
     *          the permission to check for
     * @return  true if player has permission, false if not
     */
    boolean hasPermission(String permission);

    /**
     * Sends the player a custom universal packet
     *
     * @param   packet
     *          packet to send
     */
    void sendCustomPacket(TabPacket packet);

    /**
     * Sends the player a custom universal packet and adds that packet into counter that
     * is displayed in /tab cpu
     *
     * @param   packet
     *          packet to send
     * @param   feature
     *          feature to increment sent packet counter of
     */
    void sendCustomPacket(TabPacket packet, TabFeature feature);
    
    /**
     * Sends the player a custom universal packet and adds that packet into counter that
     * is displayed in /tab cpu
     *
     * @param   packet
     *          packet to send
     * @param   feature
     *          feature to increment sent packet counter of
     */
    void sendCustomPacket(TabPacket packet, String feature);

    /**
     * Sends the player a platform-specific packet
     *
     * @param   packet
     *          an instance of packet depending on platform
     */
    void sendPacket(Object packet);

    /**
     * Returns player's property by name
     *
     * @param   name
     *          name of property
     * @return  the property or {@code null} if not found
     */
    Property getProperty(String name);

    /**
     * Sends a message to the player
     *
     * @param   message
     *          message to be sent
     * @param   translateColors
     *          whether colors should be translated or not
     */
    void sendMessage(String message, boolean translateColors);

    /**
     * Sends specified component as a chat message
     *
     * @param   message
     *          message to send
     */
    void sendMessage(IChatBaseComponent message);

    /**
     * Returns player's netty channel. On servers running version 1.7 or older returns {@code null}.
     *
     * @return  player's channel
     */
    Channel getChannel();

    /**
     * Returns player's ping calculated by server
     *
     * @return  player's ping
     */
    int getPing();

    /**
     * Returns player's skin data
     *
     * @return  player's skin
     */
    Skin getSkin();

    /**
     * Returns true once the player is successfully loaded (onJoin method ran through all methods)
     *
     * @return  {@code true} if player is fully loaded, {@code false} otherwise
     */
    boolean isLoaded();

    /**
     * Sets property with specified name to new value. If property did not exist before, it is
     * created and {@code true} is returned. If it existed, it is overridden and {@code true} is returned.
     * {@code false} is returned otherwise.
     *
     * @param   feature
     *          feature using this property to get placeholders registered
     * @param   identifier
     *          property name
     * @param   rawValue
     *          new raw value
     * @return  {@code true} if value changed / did not exist, {@code false} if value did not change
     */
    boolean setProperty(TabFeature feature, String identifier, String rawValue);

    /**
     * Loads property from config using standard property loading algorithm
     *
     * @param   property
     *          property name to load
     * @return  {@code true} if value did not exist or changed, {@code false} otherwise
     */
    boolean loadPropertyFromConfig(TabFeature feature, String property);

    /**
     * Loads property from config using standard property loading algorithm. If the property is
     * not set in config, {@code ifNotSet} value is used.
     *
     * @param   property
     *          property name to load
     * @param   ifNotSet
     *          value to use if property is not defined in config
     * @return  {@code true} if value did not exist or changed, {@code false} otherwise
     */
    boolean loadPropertyFromConfig(TabFeature feature, String property, String ifNotSet);

    /**
     * Returns name of player's scoreboard team or {@code null} if NameTag feature is disabled
     *
     * @return  name of player's team
     */
    String getTeamName();

    /**
     * Returns {@code true} if player is disguised using LibsDisguises, {@code false} if not
     *
     * @return  {@code true} if player is disguised, {@code false} if not
     */
    boolean isDisguised();

    /**
     * Returns {@code true} if player has invisibility potion, {@code false} if not.
     * For bukkit, API is used, for BungeeCord, bridge is used.
     *
     * @return  {@code true} if player has invisibility potion, {@code false} if not
     */
    boolean hasInvisibilityPotion();

    /**
     * Checks if player is vanished using any supported vanish plugin and returns the result.
     *
     * @return  {@code true} if player is vanished, {@code false} if not
     */
    boolean isVanished();

    /**
     * Calls platform-specific method and returns the result
     *
     * @return  {@code true} if player is online, {@code false} if not
     */
    boolean isOnline();
    
    /**
     * Returns GameMode of the player (0 for survival, 1 creative, 2 adventure, 3 spectator)
     *
     * @return  GameMode of the player
     */
    int getGamemode();

    /**
     * Returns {@code true} if this is a bedrock player, {@code false} if not
     *
     * @return  {@code true} if player is a bedrock player, {@code false} if not
     */
    boolean isBedrockPlayer();

    /**
     * Returns player's primary permission group. If group has been changed using
     * {@link #setTemporaryGroup(String)}, returns that value. Otherwise, returns group
     * detected by standard group assign logic
     *
     * @return  player's primary permission group
     */
    String getGroup();

    /**
     * Temporarily overrides player's group and applies all changes coming from new group.
     * This includes all properties and sorting, if used.
     *
     * @param   group
     *          New group to use
     * @see     #hasTemporaryGroup()
     * @see     #resetTemporaryGroup()
     */
    void setTemporaryGroup(String group);

    /**
     * Returns temporary group applied to the player using {@link #setTemporaryGroup(String)}.
     * If no group was set, returns {@code null}
     *
     * @return  Temporary group assigned to player or {@code null} if not set
     * @see     #setTemporaryGroup(String)
     * @see     #resetTemporaryGroup()
     */
    boolean hasTemporaryGroup();

    /**
     * Resets temporary group assigned using {@link #setTemporaryGroup(String)}.
     * If no temporary group is set, doesn't do anything.
     *
     * @see     #setTemporaryGroup(String)
     * @see     #hasTemporaryGroup()
     */
    void resetTemporaryGroup();

    /**
     * Returns player's game profile name as seen by other players. By default,
     * this returns player's name. If using a nickname / disguise plugin that changes
     * game profile name for other players and change was successfully detected, returns
     * that value.
     *
     * @return  player's game profile name as seen by other players
     */
    String getNickname();

    /**
     * Returns player's chat message signing key. Returned object is a direct
     * object from the server, which is different per platform.
     *
     * @return  Player's direct chat message signing key
     */
    Object getProfilePublicKey();
}
