package me.neznamy.tab.api.tablist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A subclass representing player list entry
 */
@Data
@AllArgsConstructor
public class TabListEntry {

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

    /** Player's chat signing key (1.19+), remote chat session (1.19.3+) */
    @Nullable private Object chatSession;

    @RequiredArgsConstructor
    public static class Builder {

        @NonNull private UUID uniqueId;
        @Nullable private String name;
        @Nullable private Skin skin;
        private boolean listed;
        private int latency;
        private int gameMode;
        @Nullable private IChatBaseComponent displayName;
        @Nullable private Object chatSession;

        public Builder name(String name) { this.name = name; return this; }
        public Builder skin(Skin skin) { this.skin = skin; return this; }
        public Builder listed(boolean listed) { this.listed = listed; return this; }
        public Builder latency(int latency) { this.latency = latency; return this; }
        public Builder gameMode(int gameMode) { this.gameMode = gameMode; return this; }
        public Builder displayName(IChatBaseComponent displayName) { this.displayName = displayName; return this; }
        public Builder chatSession(Object chatSession) { this.chatSession = chatSession; return this; }

        public TabListEntry build() {
            return new TabListEntry(uniqueId, name, skin, listed, latency, gameMode, displayName, chatSession);
        }
    }
}
