package me.neznamy.tab.shared.platform;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.api.placeholder.RelationalPlaceholder;
import me.neznamy.tab.shared.chat.SimpleComponent;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.features.*;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import me.neznamy.tab.shared.features.globalplayerlist.GlobalPlayerList;
import me.neznamy.tab.shared.features.layout.LayoutManagerImpl;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.hook.FloodgateHook;
import me.neznamy.tab.shared.*;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.event.impl.PlayerLoadEventImpl;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
     * installed on proxy and bridge is not installed
     */
    public String world;

    /** Server the player is currently in, {@code "N/A"} if TAB is installed on backend */
    public String server;

    /** Player's permission group defined in permission plugin or with permission nodes */
    @Getter
    private String permissionGroup = TabConstants.NO_GROUP;

    /** Player's permission group override using API */
    private String temporaryGroup;

    /** Player's game type, {@code true} for Bedrock, {@code false} for Java */
    @Getter private final boolean bedrockPlayer;

    /** Player's game version */
    @Getter protected final ProtocolVersion version;

    /**
     * Player's load status, {@code true} when player is fully loaded,
     * {@code false} if not yet
     */
    @Getter private boolean loaded;

    /** Flag tracking whether the player is online or not */
    @Getter private boolean online = true;

    /** Data for sorting */
    public final Sorting.PlayerData sortingData = new Sorting.PlayerData();

    /** Data for sidebar scoreboard feature */
    public final ScoreboardManagerImpl.PlayerData scoreboardData = new ScoreboardManagerImpl.PlayerData();

    /** Data for scoreboard team */
    public final NameTag.PlayerData teamData = new NameTag.PlayerData();

    /** Data for Layout */
    public final LayoutManagerImpl.PlayerData layoutData = new LayoutManagerImpl.PlayerData();

    /** Data for BossBar */
    public final BossBarManagerImpl.PlayerData bossbarData = new BossBarManagerImpl.PlayerData();

    /** Data for Header/Footer */
    public final HeaderFooter.PlayerData headerFooterData = new HeaderFooter.PlayerData();

    /** Data for Playerlist Objective */
    public final YellowNumber.PlayerData playerlistObjectiveData = new YellowNumber.PlayerData();

    /** Data for Belowname Objective */
    public final BelowName.PlayerData belowNameData = new BelowName.PlayerData();

    /** Data for tablist formatting */
    public final PlayerList.PlayerData tablistData = new PlayerList.PlayerData();

    /** Data for global playerlist */
    public final GlobalPlayerList.PlayerData globalPlayerListData = new GlobalPlayerList.PlayerData();

    /** Data for plugin's PlaceholderAPI expansion */
    public final Map<String, String> expansionValues = new HashMap<>();

    /** LuckPerms user for fast access */
    @Nullable public User luckPermsUser;

    /** Last known values for each player placeholder after applying replacements and nested placeholders */
    public final Map<PlayerPlaceholder, String> lastPlaceholderValues = new ConcurrentHashMap<>();

    /** Last known values for each relational placeholder after applying replacements and nested placeholders */
    public final Map<RelationalPlaceholder, Map<TabPlayer, String>> lastRelationalValues = new ConcurrentHashMap<>();

    /** Player's scoreboard */
    @Getter
    @NotNull
    private final Scoreboard scoreboard;

    /** Player's bossbar view */
    @Getter
    @NotNull
    private final BossBar bossBar;

    /** Player's tablist view */
    @Getter
    @NotNull
    private final TabList tabList;

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
        scoreboard = platform.createScoreboard(this);
        bossBar = platform.createBossBar(this);
        tabList = platform.createTabList(this);
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
        ((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder(TabConstants.Placeholder.GROUP)).updateValue(this, getGroup());
        TAB.getInstance().getFeatureManager().onGroupChange(this);
    }

    @Override
    public void setTemporaryGroup(@Nullable String group) {
        if (Objects.equals(group, temporaryGroup)) return;
        temporaryGroup = group;
        ((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder(TabConstants.Placeholder.GROUP)).updateValue(this, getGroup());
        TAB.getInstance().getFeatureManager().onGroupChange(this);
    }

    @Override
    public boolean hasTemporaryGroup() {
        return temporaryGroup != null;
    }

    @Override
    public void setExpectedProfileName(@NonNull String profileName) {
        nickname = profileName;
        NickCompatibility nick = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.NICK_COMPATIBILITY);
        nick.processNameChange(this);
    }

    @Override
    @NotNull
    public String getExpectedProfileName() {
        return nickname;
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

    @Override
    public @NotNull String getGroup() {
        return temporaryGroup != null ? temporaryGroup : permissionGroup;
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
    public Property loadPropertyFromConfig(@Nullable RefreshableFeature feature, @NotNull String property, @NotNull String ifNotSet) {
        String[] value = TAB.getInstance().getConfiguration().getUsers().getProperty(name, property, server, world);
        if (value.length == 0) {
            value = TAB.getInstance().getConfiguration().getUsers().getProperty(uniqueId.toString(), property, server, world);
        }
        if (value.length == 0) {
            value = TAB.getInstance().getConfiguration().getGroups().getProperty(getGroup(), property, server, world);
        }
        if (value.length > 0) {
            return new Property(property, feature, this, value[0], value[1]);
        }
        return new Property(property, feature, this, ifNotSet, "None");
    }

    /**
     * Loads property from config using standard property loading algorithm. If the property is
     * not set in config, {@code ifNotSet} value is used.
     *
     * @param   property
     *          property to update
     * @param   ifNotSet
     *          value to use if property is not defined in config
     * @return  {@code true} if value did not exist or changed, {@code false} otherwise
     */
    public boolean updatePropertyFromConfig(@NotNull Property property, @NotNull String ifNotSet) {
        String[] value = TAB.getInstance().getConfiguration().getUsers().getProperty(name, property.getName(), server, world);
        if (value.length == 0) {
            value = TAB.getInstance().getConfiguration().getUsers().getProperty(uniqueId.toString(), property.getName(), server, world);
        }
        if (value.length == 0) {
            value = TAB.getInstance().getConfiguration().getGroups().getProperty(getGroup(), property.getName(), server, world);
        }
        if (value.length > 0) {
            return property.changeRawValue(value[0], value[1]);
        }
        return property.changeRawValue(ifNotSet, "None");
    }

    /**
     * Makes sure the player is loaded. If not, throws {@link IllegalStateException}.
     *
     * @throws  IllegalStateException
     *          If player is not loaded yet
     */
    public void ensureLoaded() {
        if (!loaded) throw new IllegalStateException("This player is not loaded yet. Try again later");
    }

    /**
     * Marks player as offline (online flag to {@code false}).
     */
    public void markOffline() {
        online = false;
    }

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
     * Returns platform representing this server type
     *
     * @return  Server platform
     */
    public abstract Platform getPlatform();
}
