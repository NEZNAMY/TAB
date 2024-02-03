package me.neznamy.tab.shared.platform;

import lombok.Getter;
import lombok.Setter;
import me.neznamy.tab.shared.chat.SimpleComponent;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.hook.FloodgateHook;
import me.neznamy.tab.shared.*;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.event.impl.PlayerLoadEventImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Abstract class storing common variables and functions for player,
 * which are not specific to any feature.
 */
public abstract class TabPlayer implements me.neznamy.tab.api.TabPlayer {

    /** Platform reference */
    protected final Platform platform;

    /** Platform-specific player object instance */
    @Setter protected Object player;

    /** Player's real name */
    @Getter private final String name;

    /** Player's name as seen in GameProfile */
    @Getter @Setter private String nickname;

    /** Player's unique ID */
    @Getter private final UUID uniqueId;

    /** Player's tablist UUID */
    @Getter private final UUID tablistId;

    /**
     * World the player is currently in, {@code "N/A"} if TAB is
     * installed on proxy and bukkit bridge is not installed
     */
    @Getter @Setter private String world;

    /** Server the player is currently in, {@code "N/A"} if TAB is installed on Bukkit */
    @Getter @Setter private String server;

    /** Player's permission group defined in permission plugin or with permission nodes */
    private String permissionGroup = TabConstants.NO_GROUP;

    /** Player's permission group override using API */
    private String temporaryGroup;

    /** Player's game type, {@code true} for Bedrock, {@code false} for Java */
    @Getter private final boolean bedrockPlayer;

    /** Player's property map where key is unique identifier and value is property object */
    private final Map<String, Property> properties = new HashMap<>();

    /** Player's game version */
    @Getter protected final ProtocolVersion version;

    /**
     * Player's load status, {@code true} when player is fully loaded,
     * {@code false} if not yet
     */
    @Getter private boolean loaded;

    /**
     * Constructs new instance with given parameters
     *
     * @param   player
     *          platform-specific player object
     * @param   uniqueId
     *          Player's unique ID
     * @param   name
     *          Player's name
     * @param   server
     *          Player's server
     * @param   world
     *          Player's world
     * @param   protocolVersion
     *          Player's game version
     * @param   useRealId
     *          Whether tablist uses real uuid or offline
     */
    protected TabPlayer(@NotNull Platform platform, @NotNull Object player, @NotNull UUID uniqueId, @NotNull String name,
                        @NotNull String server, @NotNull String world, int protocolVersion, boolean useRealId) {
        this.platform = platform;
        this.player = player;
        this.uniqueId = uniqueId;
        this.name = name;
        this.server = server;
        this.world = world;
        nickname = name;
        version = ProtocolVersion.fromNetworkId(protocolVersion);
        bedrockPlayer = FloodgateHook.getInstance().isFloodgatePlayer(uniqueId, name);
        permissionGroup = TAB.getInstance().getGroupManager().detectPermissionGroup(this);
        tablistId = useRealId ? uniqueId : UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Sets player's property with provided key to provided value. If it existed,
     * the raw value is changed. If it did not exist, it is created.
     *
     * @param   feature
     *          Feature creating the property
     * @param   identifier
     *          Property's unique identifier
     * @param   rawValue
     *          Raw value with raw placeholders
     * @param   source
     *          Source of raw value
     * @return  {@code true} if property did not exist or existed with different raw value,
     *          {@code false} if property existed with the same raw value already.
     */
    private boolean setProperty(@Nullable Refreshable feature, @NotNull String identifier, @NotNull String rawValue,
                                @Nullable String source, boolean exposeInExpansion) {
        Property p = getProperty(identifier);
        if (p == null) {
            properties.put(identifier, new Property(exposeInExpansion ? identifier : null, feature, this, rawValue, source));
            return true;
        } else {
            if (!p.getOriginalRawValue().equals(rawValue)) {
                p.changeRawValue(rawValue, source);
                return true;
            }
            return false;
        }
    }

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
    public boolean setProperty(@Nullable Refreshable feature, @NotNull String identifier, @NotNull String rawValue) {
        return setProperty(feature, identifier, rawValue, null, false);
    }

    /**
     * Marks the player as loaded and calls PlayerLoadEvent
     *
     * @param   join
     *          {@code true} if this is a player join, {@code false} if reload
     */
    public void markAsLoaded(boolean join) {
        loaded = true;
        if (TAB.getInstance().getEventBus() != null) TAB.getInstance().getEventBus().fire(new PlayerLoadEventImpl(this, join));
    }

    /**
     * Changes player's group to provided value and all features are refreshed.
     *
     * @param   permissionGroup
     *          New permission group
     */
    public void setGroup(@NotNull String permissionGroup) {
        if (this.permissionGroup.equals(permissionGroup)) return;
        this.permissionGroup = permissionGroup;
        forceRefresh();
    }

    @Override
    public void setTemporaryGroup(@Nullable String group) {
        if (Objects.equals(group, temporaryGroup)) return;
        temporaryGroup = group;
        forceRefresh();
    }

    @Override
    public boolean hasTemporaryGroup() {
        return temporaryGroup != null;
    }

    /**
     * Sends a message to the player
     *
     * @param   message
     *          message to be sent
     * @param   translateColors
     *          whether colors should be translated or not
     */
    public void sendMessage(@NotNull String message, boolean translateColors) {
        if (message.isEmpty()) return;
        if (translateColors) {
            sendMessage(TabComponent.fromColoredText(message));
        } else {
            sendMessage(new SimpleComponent(message));
        }
    }

    public void forceRefresh() {
        if (!loaded) return;
        TAB.getInstance().getFeatureManager().refresh(this, true);
    }

    /**
     * Returns property with given name.
     *
     * @param   name
     *          Name of the property
     * @return  Property with given name
     */
    public Property getProperty(@NotNull String name) {
        return properties.get(name);
    }

    @Override
    public @NotNull String getGroup() {
        return temporaryGroup != null ? temporaryGroup : permissionGroup;
    }

    /**
     * Loads property from config using standard property loading algorithm
     *
     * @param   feature
     *          Feature using this property
     * @param   property
     *          property name to load
     * @return  {@code true} if value did not exist or changed, {@code false} otherwise
     */
    public boolean loadPropertyFromConfig(@Nullable Refreshable feature, @NotNull String property) {
        return loadPropertyFromConfig(feature, property, "");
    }

    /**
     * Loads property from config using standard property loading algorithm. If the property is
     * not set in config, {@code ifNotSet} value is used.
     *
     * @param   feature
     *          Feature using this property
     * @param   property
     *          property name to load
     * @param   ifNotSet
     *          value to use if property is not defined in config
     * @return  {@code true} if value did not exist or changed, {@code false} otherwise
     */
    public boolean loadPropertyFromConfig(@Nullable Refreshable feature, @NotNull String property, @NotNull String ifNotSet) {
        String[] value = TAB.getInstance().getConfiguration().getUsers().getProperty(name, property, server, world);
        if (value.length == 0) {
            value = TAB.getInstance().getConfiguration().getUsers().getProperty(uniqueId.toString(), property, server, world);
        }
        if (value.length == 0) {
            value = TAB.getInstance().getConfiguration().getGroups().getProperty(getGroup(), property, server, world);
        }
        if (value.length > 0) {
            return setProperty(feature, property, value[0], value[1], true);
        }
        return setProperty(feature, property, ifNotSet, "None", true);
    }

    /**
     * Returns scoreboard interface for calling scoreboard-related methods
     *
     * @return  scoreboard interface for calling scoreboard-related methods
     */
    public abstract @NotNull Scoreboard<? extends TabPlayer> getScoreboard();

    /**
     * Returns handler for calling bossbar-related methods
     *
     * @return  handler for calling bossbar-related methods
     */
    public abstract @NotNull BossBar getBossBar();

    /**
     * Returns {@code true} if player is disguised using LibsDisguises, {@code false} if not
     *
     * @return  {@code true} if player is disguised, {@code false} if not
     */
    public abstract boolean isDisguised();

    /**
     * Returns {@code true} if player has invisibility potion, {@code false} if not.
     * For bukkit, API is used, for BungeeCord, bridge is used.
     *
     * @return  {@code true} if player has invisibility potion, {@code false} if not
     */
    public abstract boolean hasInvisibilityPotion();

    /**
     * Checks if player is vanished using any supported vanish plugin and returns the result.
     *
     * @return  {@code true} if player is vanished, {@code false} if not
     */
    public abstract boolean isVanished();

    /**
     * Returns GameMode of the player (0 for survival, 1 creative, 2 adventure, 3 spectator)
     *
     * @return  GameMode of the player
     */
    public abstract int getGamemode();

    /**
     * Returns player's ping calculated by server
     *
     * @return  player's ping
     */
    public abstract int getPing();

    /**
     * Returns player's skin data
     *
     * @return  player's skin
     */
    public abstract @Nullable TabList.Skin getSkin();

    /**
     * Returns TabList interface for calling tablist-related methods
     *
     * @return  TabList interface for calling tablist-related methods
     */
    public abstract @NotNull TabList getTabList();

    /**
     * Sends specified component as a chat message
     *
     * @param   message
     *          message to send
     */
    public abstract void sendMessage(@NotNull TabComponent message);

    /**
     * Performs platform-specific API call to check for permission and returns the result
     *
     * @param   permission
     *          the permission to check for
     * @return  true if player has permission, false if not
     */
    public abstract boolean hasPermission(@NotNull String permission);

    /**
     * Calls platform-specific method and returns the result
     *
     * @return  {@code true} if player is online, {@code false} if not
     */
    public abstract boolean isOnline();

    /**
     * Returns platform representing this server type
     *
     * @return  Server platform
     */
    public abstract Platform getPlatform();
}
