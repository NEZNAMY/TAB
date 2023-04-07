package me.neznamy.tab.shared.platform.tablist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface TabList {

    String TEXTURES_PROPERTY = "textures";

    void removeEntry(@NonNull UUID entry);

    void removeEntries(@NonNull Collection<UUID> entries);

    void updateDisplayName(@NonNull UUID entry, @Nullable IChatBaseComponent displayName);

    void updateDisplayNames(@NonNull Map<UUID, IChatBaseComponent> entries);

    void updateLatency(@NonNull UUID entry, int latency);

    void updateLatencies(@NonNull Map<UUID, Integer> entries);

    void updateGameMode(@NonNull UUID entry, int gameMode);

    void updateGameModes(@NonNull Map<UUID, Integer> entries);

    void addEntry(@NonNull TabList.Entry entry);

    void addEntries(@NonNull Collection<Entry> entries);

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

        /** If player should appear in tablist or not */
        private boolean listed;

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
            private boolean listed;
            private int latency;
            private int gameMode;
            @Nullable private IChatBaseComponent displayName;

            public Builder name(String name) { this.name = name; return this; }
            public Builder skin(Skin skin) { this.skin = skin; return this; }
            public Builder listed(boolean listed) { this.listed = listed; return this; }
            public Builder latency(int latency) { this.latency = latency; return this; }
            public Builder gameMode(int gameMode) { this.gameMode = gameMode; return this; }
            public Builder displayName(IChatBaseComponent displayName) { this.displayName = displayName; return this; }

            public Entry build() {
                return new Entry(uniqueId, name, skin, listed, latency, gameMode, displayName);
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
