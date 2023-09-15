package me.neznamy.tab.shared.platform;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public interface TabList {

    String TEXTURES_PROPERTY = "textures";

    default void removeEntries(@NotNull Collection<UUID> entries) {
        entries.forEach(this::removeEntry);
    }

    default void addEntries(@NotNull Collection<Entry> entries) {
        entries.forEach(this::addEntry);
    }

    void removeEntry(@NotNull UUID entry);

    void updateDisplayName(@NotNull UUID entry, @Nullable IChatBaseComponent displayName);

    void updateLatency(@NotNull UUID entry, int latency);

    void updateGameMode(@NotNull UUID entry, int gameMode);

    void addEntry(@NotNull Entry entry);

    /**
     * Sets header and footer to specified values
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
    default void checkDisplayNames(){}

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

    enum Action {
        ADD_PLAYER, REMOVE_PLAYER, UPDATE_DISPLAY_NAME, UPDATE_LATENCY, UPDATE_GAME_MODE
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
        @Nullable private String name;

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

        @RequiredArgsConstructor
        public static class Builder {

            @NotNull private UUID uniqueId;
            @Nullable private String name;
            @Nullable private Skin skin;
            private int latency;
            private int gameMode;
            @Nullable private IChatBaseComponent displayName;

            public @NotNull Builder name(String name) { this.name = name; return this; }
            public @NotNull Builder skin(Skin skin) { this.skin = skin; return this; }
            public @NotNull Builder latency(int latency) { this.latency = latency; return this; }
            public @NotNull Builder gameMode(int gameMode) { this.gameMode = gameMode; return this; }
            public @NotNull Builder displayName(IChatBaseComponent displayName) { this.displayName = displayName; return this; }

            public @NotNull Entry build() {
                return new Entry(uniqueId, name, skin, latency, gameMode, displayName);
            }
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
