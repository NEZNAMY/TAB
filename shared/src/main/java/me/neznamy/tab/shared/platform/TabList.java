package me.neznamy.tab.shared.platform;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public interface TabList {

    String TEXTURES_PROPERTY = "textures";

    default void removeEntries(@NonNull Collection<UUID> entries) {
        entries.forEach(this::removeEntry);
    }

    default void addEntries(@NonNull Collection<Entry> entries) {
        entries.forEach(this::addEntry);
    }

    void removeEntry(@NonNull UUID entry);

    void updateDisplayName(@NonNull UUID entry, @Nullable IChatBaseComponent displayName);

    void updateLatency(@NonNull UUID entry, int latency);

    void updateGameMode(@NonNull UUID entry, int gameMode);

    void addEntry(@NonNull Entry entry);

    /**
     * Sets header and footer to specified values
     *
     * @param   header
     *          Header to use
     * @param   footer
     *          Footer to use
     */
    void setPlayerListHeaderFooter(@NonNull IChatBaseComponent header, @NonNull IChatBaseComponent footer);

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
        @NonNull private UUID uniqueId;

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

            @NonNull private UUID uniqueId;
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
        @NonNull private final String value;

        /** Skin signature */
        @Nullable private final String signature;
    }
}
