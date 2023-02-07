package me.neznamy.tab.api.protocol;

import lombok.*;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.util.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * A class representing platform specific packet class
 */
@Data @AllArgsConstructor
public class PacketPlayOutPlayerInfo implements TabPacket {

    /** Packet actions */
    private final EnumSet<EnumPlayerInfoAction> actions;

    /** List of affected entries */
    private final List<PlayerInfoData> entries;

    /**
     * Constructs new instance with given parameters.
     * 
     * @param   action
     *          Packet action
     * @param   entries
     *          Affected entries
     */
    public PacketPlayOutPlayerInfo(EnumPlayerInfoAction action, PlayerInfoData... entries) {
        this(action, Arrays.asList(entries));
    }

    /**
     * Constructs new instance with given parameters.
     * 
     * @param   action
     *          Packet action
     * @param   entries
     *          Affected entries
     */
    public PacketPlayOutPlayerInfo(EnumPlayerInfoAction action, List<PlayerInfoData> entries) {
        Preconditions.checkNotNull(action, "action");
        Preconditions.checkNotNull(entries, "entries");
        if (action == EnumPlayerInfoAction.ADD_PLAYER) {
            actions = EnumSet.of(EnumPlayerInfoAction.ADD_PLAYER,
                    EnumPlayerInfoAction.INITIALIZE_CHAT,
                    EnumPlayerInfoAction.UPDATE_GAME_MODE,
                    EnumPlayerInfoAction.UPDATE_LISTED,
                    EnumPlayerInfoAction.UPDATE_LATENCY,
                    EnumPlayerInfoAction.UPDATE_DISPLAY_NAME);
        } else {
            this.actions = EnumSet.of(action);
        }
        this.entries = entries;
    }

    /**
     * Constructs new instance with given parameters.
     *
     * @param   actions
     *          Packet actions
     * @param   entries
     *          Affected entries
     */
    public PacketPlayOutPlayerInfo(EnumSet<EnumPlayerInfoAction> actions, PlayerInfoData... entries) {
        Preconditions.checkNotNull(actions, "action");
        Preconditions.checkNotNull(entries, "entries");
        this.actions = actions;
        this.entries = Arrays.asList(entries);
    }

    /**
     * A subclass representing player list entry
     */
    @Data @AllArgsConstructor
    public static class PlayerInfoData {

        /** Real name of affected player */
        private String name;

        /** Player UUID */
        @NonNull private UUID uniqueId;

        /** Player's skin, null for empty skin */
        private Skin skin;

        /** I have no idea what this is */
        private boolean listed;

        /** Latency */
        private int latency;

        /** GameMode */
        private EnumGamemode gameMode = EnumGamemode.SURVIVAL; //ProtocolLib causes NPE even when action does not use GameMode

        /** 
         * Display name displayed in TabList. Using {@code null} results in no display name
         * and scoreboard team prefix/suffix being visible in TabList instead.
         */
        private IChatBaseComponent displayName;

        /** Chat session ID */
        private UUID chatSessionId;

        /** Player's chat signing key */
        private Object profilePublicKey;

        /**
         * Constructs new instance with given parameters. Suitable for 
         * {@link EnumPlayerInfoAction#UPDATE_GAME_MODE} action
         * 
         * @param   uniqueId
         *          Player's uuid
         * @param   gameMode
         *          Player's GameMode
         */
        public PlayerInfoData(@NotNull UUID uniqueId, EnumGamemode gameMode) {
            Preconditions.checkNotNull(uniqueId, "uuid");
            this.uniqueId = uniqueId;
            this.gameMode = gameMode;
        }

        /**
         * Constructs new instance with given parameters. Suitable for 
         * {@link EnumPlayerInfoAction#UPDATE_LATENCY} action
         * 
         * @param   uniqueId
         *          Player's uuid
         * @param   latency
         *          Player's ping
         */
        public PlayerInfoData(@NotNull UUID uniqueId, int latency) {
            Preconditions.checkNotNull(uniqueId, "uuid");
            this.uniqueId = uniqueId;
            this.latency = latency;
        }

        /**
         * Constructs new instance with given parameters. Suitable for 
         * {@link EnumPlayerInfoAction#UPDATE_DISPLAY_NAME} action
         * 
         * @param   uniqueId
         *          Player's uuid
         * @param   displayName
         *          Player's display name
         */
        public PlayerInfoData(@NotNull UUID uniqueId, IChatBaseComponent displayName) {
            Preconditions.checkNotNull(uniqueId, "uuid");
            this.uniqueId = uniqueId;
            this.displayName = displayName;
        }

        /**
         * Constructs new instance with given parameter. Suitable for 
         * {@link EnumPlayerInfoAction#REMOVE_PLAYER} action
         * 
         * @param   uniqueId
         *          Player's uuid
         */
        public PlayerInfoData(@NotNull UUID uniqueId) {
            this.uniqueId = uniqueId;
        }
    }

    /**
     * En enum representing packet action
     */
    public enum EnumPlayerInfoAction {

        ADD_PLAYER,
        INITIALIZE_CHAT,
        UPDATE_GAME_MODE,
        UPDATE_LISTED,
        UPDATE_LATENCY,
        UPDATE_DISPLAY_NAME,
        REMOVE_PLAYER
    }

    /**
     * An enum representing GameMode
     */
    public enum EnumGamemode {

        NOT_SET,
        SURVIVAL,
        CREATIVE,
        ADVENTURE,
        SPECTATOR;

        /** Value array to iterate over to avoid array creations on each call */
        public static final EnumGamemode[] VALUES = values();
    }
}