package me.neznamy.tab.shared;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.neznamy.tab.api.*;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.feature.Refreshable;
import me.neznamy.tab.shared.event.impl.PlayerLoadEventImpl;
import me.neznamy.tab.shared.features.sorting.Sorting;
import org.geysermc.floodgate.api.FloodgateApi;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Abstract class storing common variables and functions for player,
 * which are not specific to any feature.
 */
public abstract class ITabPlayer implements TabPlayer {

    /** Platform-specific player object instance */
    protected Object player;

    /** Player's real name */
    @Getter private final String name;

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
    private String permissionGroup;

    /** Player's permission group override using API */
    private String temporaryGroup;

    /** Player's game type, {@code true} for Bedrock, {@code false} for Java */
    @Getter private final boolean bedrockPlayer;

    /** Player's property map where key is unique identifier and value is property object */
    private final Map<String, Property> properties = new HashMap<>();

    /** Player's game version */
    @Getter protected final ProtocolVersion version;

    /** Player's network channel */
    @Getter @Setter protected Channel channel;

    /**
     * Player's load status, {@code true} when player is fully loaded,
     * {@code false} if not yet
     */
    @Getter private boolean loaded;

    /** Player's name as seen in GameProfile, can be altered by nick plugins */
    @Getter @Setter private String nickname;

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
    protected ITabPlayer(Object player, UUID uniqueId, String name, String server, String world, int protocolVersion, boolean useRealId) {
        this.player = player;
        this.uniqueId = uniqueId;
        this.name = name;
        this.nickname = name;
        this.server = server;
        this.world = world;
        this.version = ProtocolVersion.fromNetworkId(protocolVersion);
        this.bedrockPlayer = TAB.getInstance().isFloodgateInstalled() && FloodgateApi.getInstance() != null && FloodgateApi.getInstance().isFloodgatePlayer(uniqueId);
        this.permissionGroup = TAB.getInstance().getGroupManager().detectPermissionGroup(this);
        UUID offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        this.tablistId = useRealId ? getUniqueId() : offlineId;
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
    private boolean setProperty(Refreshable feature, String identifier, String rawValue, String source, boolean exposeInExpansion) {
        DynamicText p = (DynamicText) getProperty(identifier);
        if (p == null) {
            properties.put(identifier, new DynamicText(exposeInExpansion ? identifier : null, feature, this, rawValue, source));
            return true;
        } else {
            if (!p.getOriginalRawValue().equals(rawValue)) {
                p.changeRawValue(rawValue, source);
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean setProperty(Refreshable feature, String identifier, String rawValue) {
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
    public void setGroup(@NonNull String permissionGroup) {
        if (this.permissionGroup.equals(permissionGroup)) return;
        this.permissionGroup = permissionGroup;
        forceRefresh();
    }

    @Override
    public void setTemporaryGroup(String group) {
        if (Objects.equals(group, temporaryGroup)) return;
        temporaryGroup = group;
        forceRefresh();
    }

    @Override
    public boolean hasTemporaryGroup() {
        return temporaryGroup != null;
    }

    @Override
    public void resetTemporaryGroup() {
        setTemporaryGroup(null);
    }

    @Override
    public void sendMessage(String message, boolean translateColors) {
        if (message == null || message.length() == 0) return;
        IChatBaseComponent component;
        if (translateColors) {
            component = IChatBaseComponent.fromColoredText(message);
        } else {
            component = new IChatBaseComponent(message);
        }
        sendMessage(component);
    }

    @Override
    public void forceRefresh() {
        if (!loaded) return;
        TAB.getInstance().getFeatureManager().refresh(this, true);
    }

    @Override
    public Property getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public String getGroup() {
        return temporaryGroup != null ? temporaryGroup : permissionGroup;
    }

    @Override
    public boolean loadPropertyFromConfig(Refreshable feature, String property) {
        return loadPropertyFromConfig(feature, property, "");
    }

    @Override
    public boolean loadPropertyFromConfig(Refreshable feature, String property, String ifNotSet) {
        String[] value = TAB.getInstance().getConfiguration().getUsers().getProperty(getName(), property, server, world);
        if (value.length == 0) {
            value = TAB.getInstance().getConfiguration().getUsers().getProperty(getUniqueId().toString(), property, server, world);
        }
        if (value.length == 0) {
            value = TAB.getInstance().getConfiguration().getGroups().getProperty(getGroup(), property, server, world);
        }
        if (value.length > 0) {
            return setProperty(feature, property, value[0], value[1], true);
        }
        return setProperty(feature, property, ifNotSet, "None", true);
    }

    @Override
    public String getTeamName() {
        Sorting sorting = (Sorting) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SORTING);
        if (sorting == null) return null;
        return sorting.getShortTeamName(this);
    }
}
