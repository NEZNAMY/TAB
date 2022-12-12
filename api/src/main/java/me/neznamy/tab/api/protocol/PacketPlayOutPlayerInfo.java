package me.neznamy.tab.api.protocol;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.util.Preconditions;

/**
 * A class representing platform specific packet class
 */
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
     * Constructs new instance with given parameters.
     *
     * @param   actions
     *          Packet actions
     * @param   entries
     *          Affected entries
     */
    public PacketPlayOutPlayerInfo(EnumSet<EnumPlayerInfoAction> actions, List<PlayerInfoData> entries) {
        Preconditions.checkNotNull(actions, "action");
        Preconditions.checkNotNull(entries, "entries");
        this.actions = actions;
        this.entries = entries;
    }

    @Override
    public String toString() {
        return String.format("PacketPlayOutPlayerInfo{actions=%s,entries=%s}", actions, entries);
    }

    /**
     * Returns {@link #actions}
     *
     * @return  packet action
     */
    public EnumSet<EnumPlayerInfoAction> getActions() {
        return actions;
    }

    /**
     * Returns {@link #entries}
     *
     * @return  affected entries
     */
    public List<PlayerInfoData> getEntries() {
        return entries;
    }

    /**
     * A subclass representing player list entry
     */
    public static class PlayerInfoData {

        /** Latency */
        private int latency;

        /** I have no idea what this is */
        private boolean listed;

        /** GameMode */
        private EnumGamemode gameMode = EnumGamemode.SURVIVAL; //ProtocolLib causes NPE even when action does not use GameMode

        /** 
         * Display name displayed in TabList. Using {@code null} results in no display name
         * and scoreboard team prefix/suffix being visible in TabList instead.
         */
        private IChatBaseComponent displayName;

        /** Real name of affected player */
        private String name;

        /** Player UUID */
        private UUID uniqueId;

        /** Player's skin, null for empty skin */
        private Skin skin;

        /** Chat session ID */
        private UUID chatSessionId;

        /** Player's chat signing key */
        private Object profilePublicKey;

        /**
         * Constructs new instance with given parameters. Suitable for 
         * {@link EnumPlayerInfoAction#ADD_PLAYER} action
         * 
         * @param   name
         *          Player's name
         * @param   uniqueId
         *          Player's uuid
         * @param   skin
         *          Player's platform-specific skin object
         * @param   latency
         *          Player's ping
         * @param   gameMode
         *          Player's GameMode
         * @param   displayName
         *          Player's display name
         * @param   profilePublicKey
         *          Player's chat signing key
         */
        public PlayerInfoData(String name, UUID uniqueId, Skin skin, boolean listed, int latency, EnumGamemode gameMode, IChatBaseComponent displayName, UUID chatSessionId, Object profilePublicKey) {
            Preconditions.checkNotNull(uniqueId, "uuid");
            this.name = name;
            this.uniqueId = uniqueId;
            this.skin = skin;
            this.listed = listed;
            this.latency = latency;
            this.gameMode = gameMode;
            this.displayName = displayName;
            this.chatSessionId = chatSessionId;
            this.profilePublicKey = profilePublicKey;
        }

        /**
         * Constructs new instance with given parameters. Suitable for 
         * {@link EnumPlayerInfoAction#UPDATE_GAME_MODE} action
         * 
         * @param   uniqueId
         *          Player's uuid
         * @param   gameMode
         *          Player's GameMode
         */
        public PlayerInfoData(UUID uniqueId, EnumGamemode gameMode) {
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
        public PlayerInfoData(UUID uniqueId, int latency) {
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
        public PlayerInfoData(UUID uniqueId, IChatBaseComponent displayName) {
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
        public PlayerInfoData(UUID uniqueId) {
            this.uniqueId = uniqueId;
        }

        @Override
        public String toString() {
            return String.format("PlayerInfoData{latency=%s,gameMode=%s,displayName=%s,name=%s,uniqueId=%s,skin=%s,profilePublicKey=%s}",
                    latency, gameMode, displayName, name, uniqueId, skin, profilePublicKey);
        }

        /**
         * Returns {@link #latency}
         *
         * @return  latency
         */
        public int getLatency() {
            return latency;
        }

        /**
         * Sets {@link #latency} to specified value
         *
         * @param   latency
         *          Latency to use
         */
        public void setLatency(int latency) {
            this.latency = latency;
        }

        /**
         * Returns {@link #listed}
         *
         * @return  listed
         */
        public boolean isListed() {
            return listed;
        }

        /**
         * Sets {@link #listed} to specified value
         *
         * @param   listed
         *          Listed flag
         */
        public void setListed(boolean listed) {
            this.listed = listed;
        }

        /**
         * Returns {@link #displayName}
         *
         * @return  displayName
         */
        public IChatBaseComponent getDisplayName() {
            return displayName;
        }

        /**
         * Sets {@link #displayName} to specified value
         *
         * @param   displayName
         *          Display name to use
         */
        public void setDisplayName(IChatBaseComponent displayName) {
            this.displayName = displayName;
        }

        /**
         * Returns {@link #uniqueId}
         *
         * @return  uniqueId
         */
        public UUID getUniqueId() {
            return uniqueId;
        }

        /**
         * Sets {@link #uniqueId} to specified value
         *
         * @param   uniqueId
         *          UUID to use
         */
        public void setUniqueId(UUID uniqueId) {
            this.uniqueId = uniqueId;
        }

        /**
         * Returns {@link #name}
         *
         * @return  name
         */
        public String getName() {
            return name;
        }

        /**
         * Sets {@link #name} to specified value
         *
         * @param   name
         *          name to use
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Returns {@link #gameMode}
         *
         * @return  gameMode
         */
        public EnumGamemode getGameMode() {
            return gameMode;
        }

        /**
         * Sets {@link #gameMode} to specified value
         *
         * @param   gameMode
         *          GameMode to use
         */
        public void setGameMode(EnumGamemode gameMode) {
            this.gameMode = gameMode;
        }

        /**
         * Returns {@link #skin}
         *
         * @return  skin
         */
        public Skin getSkin() {
            return skin;
        }

        /**
         * Sets {@link #skin} to specified value
         *
         * @param   skin
         *          Skin to use
         */
        public void setSkin(Skin skin) {
            this.skin = skin;
        }

        /**
         * Returns {@link #chatSessionId}
         *
         * @return  chatSessionId
         */
        public UUID getChatSessionId() {
            return chatSessionId;
        }

        /**
         * Sets {@link #chatSessionId} to specified value
         *
         * @param   chatSessionId
         *          chatSessionId to use
         */
        public void setChatSessionId(UUID chatSessionId) {
            this.chatSessionId = chatSessionId;
        }

        /**
         * Returns {@link #profilePublicKey}
         *
         * @return  profilePublicKey
         */
        public Object getProfilePublicKey() {
            return profilePublicKey;
        }

        /**
         * Sets {@link #profilePublicKey} to specified value
         *
         * @param   profilePublicKey
         *          profilePublicKey to use
         */
        public void setProfilePublicKey(Object profilePublicKey) {
            this.profilePublicKey = profilePublicKey;
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