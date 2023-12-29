package me.neznamy.tab.shared.platform;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

/**
 * Interface for managing tablist entries.
 */
public interface TabList {

    /** Name of the textures property in game profile */
    String TEXTURES_PROPERTY = "textures";

    /**
     * Removes entries from the TabList.
     *
     * @param   entries
     *          Entries to remove
     */
    default void removeEntries(@NotNull Collection<UUID> entries) {
        entries.forEach(this::removeEntry);
    }

    /**
     * Adds specified entries into the TabList.
     *
     * @param   entries
     *          Entries to add
     */
    default void addEntries(@NotNull Collection<Entry> entries) {
        entries.forEach(this::addEntry);
    }

    /**
     * Removes entry from the TabList.
     *
     * @param   entry
     *          Entry to remove
     */
    void removeEntry(@NotNull UUID entry);

    /**
     * Updates display name of an entry. Using {@code null} makes it undefined and
     * scoreboard team prefix/suffix will be visible instead.
     *
     * @param   entry
     *          Entry to update
     * @param   displayName
     *          New display name
     */
    void updateDisplayName(@NotNull UUID entry, @Nullable IChatBaseComponent displayName);

    /**
     * Updates latency of specified entry.
     *
     * @param   entry
     *          Entry to update
     * @param   latency
     *          New latency
     */
    void updateLatency(@NotNull UUID entry, int latency);

    /**
     * Updates game mode of specified entry.
     *
     * @param   entry
     *          Entry to update
     * @param   gameMode
     *          New game mode
     */
    void updateGameMode(@NotNull UUID entry, int gameMode);

    /**
     * Adds specified entry into the TabList.
     *
     * @param   entry
     *          Entry to add
     */
    void addEntry(@NotNull Entry entry);

    /**
     * Sets header and footer to specified values.
     *
     * @param   header
     *          Header to use
     * @param   footer
     *          Footer to use
     */
    void setPlayerListHeaderFooter(@NotNull IChatBaseComponent header, @NotNull IChatBaseComponent footer);

    /**
     * Checks if all entries have display names as configured and if not,
     * they are forced. Only works on platforms with a full TabList API.
     * Not needed for platforms which support pipeline injection.
     */
    default void checkDisplayNames() {
        // Empty by default, overridden by Sponge7, Sponge8 and Velocity
    }

    /**
     * Processes packet for anti-override, ping spoof and nick compatibility.
     *
     * @param   packet
     *          Packet to process
     */
    default void onPacketSend(@NotNull Object packet) {
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
    default void displayNameWrong(String player, TabPlayer viewer) {
        TAB.getInstance().debug("TabList entry of player " + player + " has a different display name " +
                "for viewer " + viewer.getName() + " than expected, fixing.");
    }

    /**
     * TabList action.
     */
    enum Action {

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
    class Entry {

        /** Player UUID */
        @NotNull private UUID uniqueId;

        /** Real name of affected player */
        @NotNull private String name = "";

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
        @Nullable private IChatBaseComponent displayName;

        /**
         * Constructs new instance with given parameter.
         *
         * @param   uniqueId
         *          Entry ID
         */
        public Entry(@NotNull UUID uniqueId) {
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
        public static Entry displayName(@NotNull UUID id, @Nullable IChatBaseComponent displayName) {
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
        public static Entry latency(@NotNull UUID id, int latency) {
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
        public static Entry gameMode(@NotNull UUID id, int gameMode) {
            return new Entry(id, "", null, 0, gameMode, null);
        }
    }

    /**
     * Class representing a minecraft skin as a value - signature pair.
     */
    @Data @AllArgsConstructor
    class Skin {

        /** Skin value */
        @NotNull private final String value;

        /** Skin signature */
        @Nullable private final String signature;
    }
}
