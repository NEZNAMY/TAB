package me.neznamy.tab.shared.platform;

import lombok.*;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Interface for managing tablist entries.
 *
 * @param   <Player>
 *          Platform's player class
 * @param   <Comp>
 *          Platform's component class
 */
@RequiredArgsConstructor
public abstract class TabList<Player extends TabPlayer, Comp> {

    /** Name of the textures property in game profile */
    public static final String TEXTURES_PROPERTY = "textures";

    /** Player this tablist belongs to */
    protected final Player player;

    /** Tablist display name anti-override flag */
    @Setter
    protected boolean antiOverride;

    /** Expected names based on configuration, saving to restore them if another plugin overrides them */
    private final Map<TabPlayer, Comp> expectedDisplayNames = Collections.synchronizedMap(new WeakHashMap<>());

    private final RedisSupport redisSupport = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);

    /** Expected names based on configuration, saving to restore them if another plugin overrides them */
    private final Map<RedisPlayer, Comp> expectedRedisDisplayNames = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Removes entries from the TabList.
     *
     * @param   entries
     *          Entries to remove
     */
    public void removeEntries(@NonNull Collection<UUID> entries) {
        entries.forEach(this::removeEntry);
    }

    /**
     * Adds specified entries into the TabList.
     *
     * @param   entries
     *          Entries to add
     */
    public void addEntries(@NonNull Collection<Entry> entries) {
        entries.forEach(this::addEntry);
    }

    /**
     * Removes entry from the TabList.
     *
     * @param   entry
     *          Entry to remove
     */
    public abstract void removeEntry(@NonNull UUID entry);

    /**
     * Updates display name of an entry. Using {@code null} makes it undefined and
     * scoreboard team prefix/suffix will be visible instead.
     *
     * @param   entry
     *          Entry to update
     * @param   displayName
     *          New display name
     */
    public void updateDisplayName(@NonNull UUID entry, @Nullable TabComponent displayName) {
        Comp component = displayName == null ? null : toComponent(displayName);
        setExpectedDisplayName(entry, component);
        updateDisplayName0(entry, component);
    }

    /**
     * Updates display name of an entry. Using {@code null} makes it undefined and
     * scoreboard team prefix/suffix will be visible instead.
     *
     * @param   entry
     *          Entry to update
     * @param   displayName
     *          New display name
     */
    public abstract void updateDisplayName0(@NonNull UUID entry, @Nullable Comp displayName);

    /**
     * Updates latency of specified entry.
     *
     * @param   entry
     *          Entry to update
     * @param   latency
     *          New latency
     */
    public abstract void updateLatency(@NonNull UUID entry, int latency);

    /**
     * Updates game mode of specified entry.
     *
     * @param   entry
     *          Entry to update
     * @param   gameMode
     *          New game mode
     */
    public abstract void updateGameMode(@NonNull UUID entry, int gameMode);

    /**
     * Adds specified entry into the TabList.
     *
     * @param   entry
     *          Entry to add
     */
    public void addEntry(@NonNull Entry entry) {
        Comp component = entry.displayName == null ? null : toComponent(entry.displayName);
        setExpectedDisplayName(entry.getUniqueId(), component);
        addEntry0(entry.uniqueId, entry.name, entry.skin, entry.latency, entry.gameMode, component);

        if (player.getVersion().getMinorVersion() == 8) {
            // Compensation for 1.8.0 client sided bug
            updateDisplayName0(entry.getUniqueId(), component);
        }
    }

    /**
     * Adds specified entry to tablist
     *
     * @param   id
     *          Entry UUID
     * @param   name
     *          Entry name
     * @param   skin
     *          Entry skin
     * @param   latency
     *          Entry latency
     * @param   gameMode
     *          Entry game mode
     * @param   displayName
     *          Entry display name
     */
    public abstract void addEntry0(@NonNull UUID id, @NonNull String name, @Nullable Skin skin, int latency, int gameMode, @Nullable Comp displayName);

    /**
     * Sets header and footer to specified values.
     *
     * @param   header
     *          Header to use
     * @param   footer
     *          Footer to use
     */
    public abstract void setPlayerListHeaderFooter(@NonNull TabComponent header, @NonNull TabComponent footer);

    /**
     * Returns {@code true} if tablist contains specified entry, {@code false} if not.
     *
     * @param   entry
     *          UUID of entry to check
     * @return  {@code true} if tablist contains specified entry, {@code false} if not
     */
    public abstract boolean containsEntry(@NonNull UUID entry);

    /**
     * Checks if all entries have display names as configured and if not,
     * they are forced. Only works on platforms with a full TabList API.
     * Not needed for platforms which support pipeline injection.
     */
    public void checkDisplayNames() {
        // Empty by default, overridden by Sponge7, Sponge8 and Velocity
    }

    /**
     * Processes packet for anti-override, ping spoof and nick compatibility.
     *
     * @param   packet
     *          Packet to process
     */
    public void onPacketSend(@NonNull Object packet) {
        // Empty by default, overridden by Bukkit, BungeeCord and Fabric
    }

    /**
     * Sends a debug message when display name is not as expected.
     *
     * @param   player
     *          Player with different display name than expected.
     * @param   viewer
     *          Viewer of the TabList with wrong entry.
     */
    protected void displayNameWrong(@NonNull String player, @NonNull TabPlayer viewer) {
        TAB.getInstance().debug("TabList entry of player " + player + " has a different display name " +
                "for viewer " + viewer.getName() + " than expected, fixing.");
    }

    private void setExpectedDisplayName(@NonNull UUID entry, @Nullable Comp displayName) {
        if (!antiOverride) return;
        TabPlayer player = TAB.getInstance().getPlayerByTabListUUID(entry);
        if (player != null) expectedDisplayNames.put(player, displayName);

        if (redisSupport != null) {
            RedisPlayer redisPlayer = redisSupport.getRedisPlayers().get(entry);
            if (redisPlayer != null) expectedRedisDisplayNames.put(redisPlayer, displayName);
        }
    }

    /**
     * Returns expected display name for specified UUID. If nothing is found,
     * {@code null} is returned.
     *
     * @param   id
     *          UUID of tablist entry
     * @return  Expected display name or {@code null}
     */
    @Nullable
    public Comp getExpectedDisplayName(@NotNull UUID id) {
        if (!antiOverride) return null;

        TabPlayer player = TAB.getInstance().getPlayerByTabListUUID(id);
        if (player != null && expectedDisplayNames.containsKey(player)) {
            return expectedDisplayNames.get(player);
        }

        if (redisSupport != null) {
            RedisPlayer redisPlayer = redisSupport.getRedisPlayers().get(id);
            if (redisPlayer != null && expectedRedisDisplayNames.containsKey(redisPlayer)) {
                return expectedRedisDisplayNames.get(redisPlayer);
            }
        }
        return null;
    }

    @Nullable
    protected Comp getExpectedDisplayName(@NonNull TabPlayer player) {
        return expectedDisplayNames.get(player);
    }

    /**
     * Converts TAB component into platform's component.
     *
     * @param   component
     *          Component to convert
     * @return  Converted component
     */
    public abstract Comp toComponent(@NonNull TabComponent component);

    /**
     * TabList action.
     */
    public enum Action {

        /** Adds player into the TabList */
        ADD_PLAYER,

        /** Removes player from the TabList */
        REMOVE_PLAYER,

        /** Updates display name */
        UPDATE_DISPLAY_NAME,

        /** Updates latency */
        UPDATE_LATENCY,

        /** Updates game mode*/
        UPDATE_GAME_MODE
    }

    /**
     * A subclass representing player list entry
     */
    @Data
    @AllArgsConstructor
    public static class Entry {

        /** Player UUID */
        @NonNull private UUID uniqueId;

        /** Real name of affected player */
        @NonNull private String name = "";

        /** Player's skin, null for empty skin */
        @Nullable private Skin skin;

        /** Latency */
        private int latency;

        /** GameMode */
        private int gameMode;

        /**
         * Display name displayed in TabList. Using {@code null} results in no display name
         * and scoreboard team prefix/suffix being visible in TabList instead.
         */
        @Nullable private TabComponent displayName;

        /**
         * Constructs new instance with given parameter.
         *
         * @param   uniqueId
         *          Entry ID
         */
        public Entry(@NonNull UUID uniqueId) {
            this.uniqueId = uniqueId;
        }

        /**
         * Creates new instance with given display name.
         *
         * @param   id
         *          Entry ID
         * @param   displayName
         *          Entry display name
         * @return  Entry with given parameters
         */
        public static Entry displayName(@NonNull UUID id, @Nullable TabComponent displayName) {
            return new Entry(id, "", null, 0, 0, displayName);
        }

        /**
         * Creates new instance with given latency.
         *
         * @param   id
         *          Entry ID
         * @param   latency
         *          Entry latency
         * @return  Entry with given parameters
         */
        public static Entry latency(@NonNull UUID id, int latency) {
            return new Entry(id, "", null, latency, 0, null);
        }

        /**
         * Creates new instance with given game mode.
         *
         * @param   id
         *          Entry ID
         * @param   gameMode
         *          Entry game mode
         * @return  Entry with given parameters
         */
        public static Entry gameMode(@NonNull UUID id, int gameMode) {
            return new Entry(id, "", null, 0, gameMode, null);
        }
    }

    /**
     * Class representing a minecraft skin as a value - signature pair.
     */
    @Data @AllArgsConstructor
    public static class Skin {

        /** Skin value */
        @NonNull private final String value;

        /** Skin signature */
        @Nullable private final String signature;
    }
}
