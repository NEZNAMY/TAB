package me.neznamy.tab.shared.platform;

import lombok.*;
import me.neznamy.tab.shared.chat.component.TabComponent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Interface for managing tablist entries.
 */
public interface TabList {

    /** Name of the textures property in game profile */
    String TEXTURES_PROPERTY = "textures";

    /**
     * Removes entry from the TabList.
     *
     * @param   entry
     *          Entry to remove
     */
    void removeEntry(@NonNull UUID entry);

    /**
     * Updates display name of an entry. Using {@code null} makes it undefined and
     * scoreboard team prefix/suffix will be visible instead.
     *
     * @param   entry
     *          Entry to update
     * @param   displayName
     *          New display name
     */
    void updateDisplayName(@NonNull UUID entry, @Nullable TabComponent displayName);

    /**
     * Updates display name of specified player. Using {@code null} makes it undefined and
     * scoreboard team prefix/suffix will be visible instead. If the viewer cannot see the player,
     * the action will not be sent to the client.
     *
     * @param   player
     *          Player to safely update display name of
     * @param   displayName
     *          New display name
     */
    void updateDisplayName(@NonNull TabPlayer player, @Nullable TabComponent displayName);

    /**
     * Updates latency of specified entry.
     *
     * @param   entry
     *          Entry to update
     * @param   latency
     *          New latency
     */
    void updateLatency(@NonNull UUID entry, int latency);

    /**
     * Updates latency of specified player. If the viewer cannot see the player,
     * the action will not be sent to the client.
     *
     * @param   player
     *          Player to safely update latency of
     * @param   latency
     *          New latency
     */
    void updateLatency(@NonNull TabPlayer player, int latency);

    /**
     * Updates game mode of specified entry.
     *
     * @param   entry
     *          Entry to update
     * @param   gameMode
     *          New game mode
     */
    void updateGameMode(@NonNull UUID entry, int gameMode);

    /**
     * Updates game mode of specified player. If the viewer cannot see the player,
     * the action will not be sent to the client.
     *
     * @param   player
     *          Player to safely update gamemode of
     * @param   gameMode
     *          New game mode
     */
    void updateGameMode(@NonNull TabPlayer player, int gameMode);

    /**
     * Updates listed flag of specified entry (1.19.3+).
     *
     * @param   entry
     *          Entry to update
     * @param   listed
     *          New listed flag
     */
    void updateListed(@NonNull UUID entry, boolean listed);

    /**
     * Updates list order of specified entry (1.21.2+).
     *
     * @param   entry
     *          Entry to update
     * @param   listOrder
     *          New list order
     */
    void updateListOrder(@NonNull UUID entry, int listOrder);

    /**
     * Updates show hat flag of specified entry (1.21.4+).
     *
     * @param   entry
     *          Entry to update
     * @param   showHat
     *          New show hat flag value
     */
    void updateHat(@NonNull UUID entry, boolean showHat);

    /**
     * Adds specified entry into the TabList.
     *
     * @param   entry
     *          Entry to add
     */
    void addEntry(@NonNull Entry entry);

    /**
     * Returns {@code true} if tablist contains specified entry, {@code false} if not.
     *
     * @param   entry
     *          UUID of entry to check
     * @return  {@code true} if tablist contains specified entry, {@code false} if not
     */
    boolean containsEntry(@NonNull UUID entry);

    /**
     * Sets header and footer to specified values.
     *
     * @param   header
     *          Header to use
     * @param   footer
     *          Footer to use
     */
    void setPlayerListHeaderFooter(@Nullable TabComponent header, @Nullable TabComponent footer);

    /**
     * Returns player's skin data
     *
     * @return  player's skin
     */
    @Nullable
    Skin getSkin();

    /**
     * A subclass representing player list entry
     */
    @Getter
    @Setter
    @AllArgsConstructor
    class Entry {

        /** Player UUID */
        @NonNull private UUID uniqueId;

        /** Real name of affected player */
        @NonNull private String name;

        /** Player's skin, null for empty skin */
        @Nullable private Skin skin;

        /** Listed flag */
        private boolean listed;

        /** Latency */
        private int latency;

        /** GameMode */
        private int gameMode;

        /**
         * Display name displayed in TabList. Using {@code null} results in no display name
         * and scoreboard team prefix/suffix being visible in TabList instead.
         */
        @Nullable private TabComponent displayName;

        /** Player list weight */
        private int listOrder;

        /** Show hat flag */
        private boolean showHat;
    }

    /**
     * Class representing a minecraft skin as a value - signature pair.
     */
    @Data
    @AllArgsConstructor
    class Skin {

        /** Skin value */
        @NonNull
        private final String value;

        /** Skin signature */
        @Nullable
        private final String signature;
    }
}
