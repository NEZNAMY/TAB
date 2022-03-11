package me.neznamy.tab.platforms.velocity.storage;

import net.kyori.adventure.text.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public final class VelocityPacketStorage {

    //instance of this class
    private static VelocityPacketStorage instance;

    //PlayerListItem
    public Class<?> PlayerListItem;
    public Constructor<?> newPlayerListItem;
    public Method PlayerListItem_getAction;
    public Method PlayerListItem_getItems;
    //PlayerListItem$Item
    public Class<?> Item;
    public Constructor<?> newItem;
    public Method Item_getUuid;
    public Method Item_getName;
    public Method Item_setName;
    public Method Item_getProperties;
    public Method Item_setProperties;
    public Method Item_getGameMode;
    public Method Item_setGameMode;
    public Method Item_getLatency;
    public Method Item_setLatency;
    public Method Item_getDisplayName;
    public Method Item_setDisplayName;

    //ScoreboardDisplay
    public Class<?> ScoreboardDisplay;
    public Constructor<?> newScoreboardDisplay;
    public Method ScoreboardDisplay_getPosition;
    public Method ScoreboardDisplay_getName;

    //ScoreboardObjective
    public Class<?> ScoreboardObjective;
    public Constructor<?> newScoreboardObjective;
    public Method ScoreboardObjective_getAction;
    public Method ScoreboardObjective_getName;
    //ScoreboardObjective$HealthDisplay
    public Class<?> HealthDisplay;
    public Method HealthDisplay_valueOf;

    //ScoreboardSetScore
    public Class<?> ScoreboardSetScore;
    public Constructor<?> newScoreboardSetScore;

    //ScoreboardTeam
    public Class<?> ScoreboardTeam;
    public Constructor<?> newScoreboardTeam;
    public Method ScoreboardTeam_getName;
    public Method ScoreboardTeam_getPlayers;
    public Method ScoreboardTeam_setPlayers;

    /**
     * Creates new instance, initializes required NMS classes and fields
     * @throws    ReflectiveOperationException
     *             If any class, field or method fails to load
     */
    public VelocityPacketStorage() throws ReflectiveOperationException {
        initializePlayerInfoPacket();
        initializeScoreboardPackets();
    }

    /**
     * Sets new instance
     * @param instance - new instance
     */
    public static void setInstance(VelocityPacketStorage instance) {
        VelocityPacketStorage.instance = instance;
    }

    /**
     * Returns instance
     * @return instance
     */
    public static VelocityPacketStorage getInstance() {
        return instance;
    }

    private void initializePlayerInfoPacket() throws ReflectiveOperationException {
        PlayerListItem = Class.forName("com.velocitypowered.proxy.protocol.packet.PlayerListItem");
        newPlayerListItem = PlayerListItem.getConstructor(int.class, List.class);
        PlayerListItem_getAction = PlayerListItem.getMethod("getAction");
        PlayerListItem_getItems = PlayerListItem.getMethod("getItems");

        Item = Class.forName("com.velocitypowered.proxy.protocol.packet.PlayerListItem$Item");
        newItem = Item.getConstructor(UUID.class);
        Item_getUuid = Item.getMethod("getUuid");
        Item_getName = Item.getMethod("getName");
        Item_setName = Item.getMethod("setName", String.class);
        Item_getProperties = Item.getMethod("getProperties");
        Item_setProperties = Item.getMethod("setProperties", List.class);
        Item_getGameMode = Item.getMethod("getGameMode");
        Item_setGameMode = Item.getMethod("setGameMode", int.class);
        Item_getLatency = Item.getMethod("getLatency");
        Item_setLatency = Item.getMethod("setLatency", int.class);
        Item_getDisplayName = Item.getMethod("getDisplayName");
        Item_setDisplayName = Item.getMethod("setDisplayName", Component.class);
    }

    private void initializeScoreboardPackets() throws ReflectiveOperationException {
        ScoreboardDisplay = Class.forName("com.velocitypowered.proxy.protocol.packet.ScoreboardDisplay");
        newScoreboardDisplay = ScoreboardDisplay.getConstructor(byte.class, String.class);
        ScoreboardDisplay_getPosition = ScoreboardDisplay.getMethod("getPosition");
        ScoreboardDisplay_getName = ScoreboardDisplay.getMethod("getName");

        HealthDisplay = Class.forName("com.velocitypowered.proxy.protocol.packet.ScoreboardObjective$HealthDisplay");
        HealthDisplay_valueOf = HealthDisplay.getMethod("valueOf", String.class);
        ScoreboardObjective = Class.forName("com.velocitypowered.proxy.protocol.packet.ScoreboardObjective");
        newScoreboardObjective = ScoreboardObjective.getConstructor(String.class, String.class, HealthDisplay, byte.class);
        ScoreboardObjective_getAction = ScoreboardObjective.getMethod("getAction");
        ScoreboardObjective_getName = ScoreboardObjective.getMethod("getName");

        ScoreboardSetScore = Class.forName("com.velocitypowered.proxy.protocol.packet.ScoreboardSetScore");
        newScoreboardSetScore = ScoreboardSetScore.getConstructor(String.class, byte.class, String.class, int.class);

        ScoreboardTeam = Class.forName("com.velocitypowered.proxy.protocol.packet.ScoreboardTeam");
        newScoreboardTeam = ScoreboardTeam.getConstructor(String.class, byte.class, String.class, String.class, String.class, String.class, String.class, int.class, byte.class, List.class);
        ScoreboardTeam_getName = ScoreboardTeam.getMethod("getName");
        ScoreboardTeam_getPlayers = ScoreboardTeam.getMethod("getPlayers");
        ScoreboardTeam_setPlayers = ScoreboardTeam.getMethod("setPlayers", List.class);
    }
}
