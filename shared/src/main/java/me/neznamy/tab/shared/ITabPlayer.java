package me.neznamy.tab.shared;

import java.nio.charset.StandardCharsets;
import java.util.*;

import io.netty.channel.Channel;
import me.neznamy.tab.api.*;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.api.protocol.*;
import me.neznamy.tab.api.protocol.PacketPlayOutChat.ChatMessageType;
import me.neznamy.tab.api.util.Preconditions;
import me.neznamy.tab.shared.event.impl.PlayerLoadEventImpl;
import me.neznamy.tab.shared.features.sorting.Sorting;
import org.geysermc.floodgate.api.FloodgateApi;

/**
 * Abstract class storing common variables and functions for player,
 * which are not specific to any feature.
 */
public abstract class ITabPlayer implements TabPlayer {

    /** Platform-specific player object instance */
    protected final Object player;

    /** Player's real name */
    private final String name;

    /** Player's unique ID */
    private final UUID uniqueId;

    /** Player's tablist UUID */
    private final UUID tabListId;

    /**
     * World the player is currently in, {@code "N/A"} if TAB is
     * installed on proxy and bukkit bridge is not installed
     */
    private String world;

    /** Server the player is currently in, {@code "N/A"} if TAB is installed on Bukkit */
    private String server;

    /** Player's permission group defined in permission plugin or with permission nodes */
    private String permissionGroup;

    /** Player's permission group override using API */
    private String temporaryGroup;

    /** Player's game type, {@code true} for Bedrock, {@code false} for Java */
    private final boolean bedrockPlayer;

    /** Player's property map where key is unique identifier and value is property object */
    private final Map<String, Property> properties = new HashMap<>();

    /** Player's game version */
    protected final ProtocolVersion version;

    /** Player's network channel */
    protected Channel channel;

    /**
     * Player's load status, {@code true} when player is fully loaded,
     * {@code false} if not yet
     */
    private boolean onJoinFinished;

    /** Scoreboard teams player has registered */
    private final List<String> registeredTeams = new ArrayList<>();

    /** Scoreboard objectives player has registered */
    private final List<String> registeredObjectives = new ArrayList<>();

    /** Player's name as seen in GameProfile, can be altered by nick plugins */
    private String nickname;

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
        this.tabListId = useRealId ? getUniqueId() : offlineId;
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
    private boolean setProperty(TabFeature feature, String identifier, String rawValue, String source, boolean exposeInExpansion) {
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
    public boolean setProperty(TabFeature feature, String identifier, String rawValue) {
        return setProperty(feature, identifier, rawValue, null, false);
    }

    /**
     * Marks the player as loaded and calls PlayerLoadEvent
     *
     * @param   join
     *          {@code true} if this is a player join, {@code false} if reload
     */
    public void markAsLoaded(boolean join) {
        onJoinFinished = true;
        if (TAB.getInstance().getEventBus() != null) TAB.getInstance().getEventBus().fire(new PlayerLoadEventImpl(this, join));
        TAB.getInstance().getPlatform().callLoadEvent(this);
    }

    /**
     * Changes player's group to provided value and all features are refreshed.
     *
     * @param   permissionGroup
     *          New permission group
     */
    public void setGroup(String permissionGroup) {
        Preconditions.checkNotNull(permissionGroup, "permissionGroup");
        if (this.permissionGroup.equals(permissionGroup)) return;
        this.permissionGroup = permissionGroup;
        ((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder(TabConstants.Placeholder.GROUP)).updateValue(this, permissionGroup);
        forceRefresh();
    }

    /**
     * Sets player's world to given value
     *
     * @param   name
     *          Name of the new world
     */
    public void setWorld(String name) {
        world = name;
    }

    /**
     * Sets player's server to given value
     *
     * @param   name
     *          Name of the new server
     */
    public void setServer(String name) {
        server = name;
    }

    /**
     * Clears maps of registered teams and objectives when Login packet is sent
     */
    public void clearRegisteredObjectives() {
        registeredTeams.clear();
        registeredObjectives.clear();
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
        sendCustomPacket(new PacketPlayOutChat(component, ChatMessageType.SYSTEM));
    }

    @Override
    public void sendMessage(IChatBaseComponent message) {
        sendCustomPacket(new PacketPlayOutChat(message, ChatMessageType.SYSTEM));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public UUID getTablistUUID() {
        return tabListId;
    }

    @Override
    public void forceRefresh() {
        if (!onJoinFinished) return;
        TAB.getInstance().getFeatureManager().refresh(this, true);
    }

    @Override
    public ProtocolVersion getVersion() {
        return version;
    }

    @Override
    public String getWorld() {
        return world;
    }
    
    @Override
    public String getServer() {
        return server;
    }

    @Override
    public void sendCustomPacket(TabPacket packet) {
        if (packet == null) return;
        //avoiding BungeeCord bug kicking all players
        if (packet instanceof PacketPlayOutScoreboardTeam) {
            String team = ((PacketPlayOutScoreboardTeam) packet).getName();
            int method = ((PacketPlayOutScoreboardTeam) packet).getAction();
            if (method == 0) {
                if (registeredTeams.contains(team)) {
                    TAB.getInstance().getErrorManager().printError("Tried to register duplicated team " + team + " to player " + getName());
                    return;
                }
                registeredTeams.add(team);
            } else if (method == 1) {
                registeredTeams.remove(team);
            }
        }
        //avoiding BungeeCord bug kicking all players
        if (packet instanceof PacketPlayOutScoreboardObjective) {
            String objective = ((PacketPlayOutScoreboardObjective) packet).getObjectiveName();
            int method = ((PacketPlayOutScoreboardObjective) packet).getAction();
            if (method == 0) {
                if (registeredObjectives.contains(objective)) {
                    TAB.getInstance().getErrorManager().printError("Tried to register duplicated objective " + objective + " to player " + getName());
                    return;
                }
                registeredObjectives.add(objective);
            } else if (method == 1) {
                registeredObjectives.remove(objective);
            }
        }
        //avoiding console spam from geyser
        if (packet instanceof PacketPlayOutScoreboardScore) {
            String objective = ((PacketPlayOutScoreboardScore) packet).getObjectiveName();
            String player = ((PacketPlayOutScoreboardScore) packet).getPlayer();
            if (!registeredObjectives.contains(objective)) {
                TAB.getInstance().getErrorManager().printError("Tried to update score (" + player + ") without the existence of its requested objective '" +
                        objective + "' to player " + getName());
                return;
            }
        }
        try {
            sendPacket(TAB.getInstance().getPlatform().getPacketBuilder().build(packet, getVersion()));
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().printError("An error occurred when creating " + packet.getClass().getSimpleName(), e);
        }
    }

    @Override
    public void sendCustomPacket(TabPacket packet, TabFeature feature) {
        sendCustomPacket(packet);
        if (feature != null) TAB.getInstance().getCPUManager().packetSent(feature.getFeatureName());
    }
    
    @Override
    public void sendCustomPacket(TabPacket packet, String feature) {
        sendCustomPacket(packet);
        if (feature != null) TAB.getInstance().getCPUManager().packetSent(feature);
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
    public Channel getChannel() {
        return channel;
    }

    @Override
    public boolean isLoaded() {
        return onJoinFinished;
    }

    @Override
    public boolean loadPropertyFromConfig(TabFeature feature, String property) {
        return loadPropertyFromConfig(feature, property, "");
    }

    @Override
    public boolean loadPropertyFromConfig(TabFeature feature, String property, String ifNotSet) {
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
    public boolean isBedrockPlayer() {
        return bedrockPlayer;
    }

    @Override
    public String getTeamName() {
        Sorting sorting = (Sorting) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SORTING);
        if (sorting == null) return null;
        return sorting.getShortTeamName(this);
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
