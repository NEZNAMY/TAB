package me.neznamy.tab.platforms.bukkit.nms.storage;

import java.util.Arrays;

/**
 * NMS loader for Minecraft 1.5.2 - 1.16.5 using Bukkit mapping.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class BukkitLegacyNMSStorage extends NMSStorage {

    /**
     * Creates new instance, initializes required NMS classes and fields
     *
     * @throws  ReflectiveOperationException
     *          If any class, field or method fails to load
     */
    public BukkitLegacyNMSStorage() throws ReflectiveOperationException {
    }

    /**
     * Returns class with given potential names in same order
     *
     * @param   names
     *          possible class names
     * @return  class for specified names
     * @throws  ClassNotFoundException
     *          if class does not exist
     */
    private Class<?> getLegacyClass(String... names) throws ClassNotFoundException {
        for (String name : names) {
            try {
                return getLegacyClass(name);
            } catch (ClassNotFoundException e) {
                //not the first class name in array
            }
        }
        throw new ClassNotFoundException("No class found with possible names " + Arrays.toString(names));
    }

    /**
     * Returns class from given name
     *
     * @param   name
     *          class name
     * @return  class from given name
     * @throws  ClassNotFoundException
     *          if class was not found
     */
    public Class<?> getLegacyClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + serverPackage + "." + name);
    }

    @Override
    public void loadNamedFieldsAndMethods() throws ReflectiveOperationException {
        PING = getField(EntityPlayer, "ping");
        ScoreboardScore_setScore = getMethod(ScoreboardScore, new String[] {"setScore", "c"}, int.class); // 1.5.1+, 1.5
        ScoreboardTeam_setAllowFriendlyFire = getMethod(ScoreboardTeam, new String[] {"setAllowFriendlyFire", "a"}, boolean.class); // 1.5.1+, 1.5
        ScoreboardTeam_setCanSeeFriendlyInvisibles = getMethod(ScoreboardTeam, new String[] {"setCanSeeFriendlyInvisibles", "b"}, boolean.class); // 1.5.1+, 1.5
        if (minorVersion >= 7) {
            ChatSerializer_DESERIALIZE = ChatSerializer.getMethod("a", String.class);
        }
        if (minorVersion >= 8) {
            ScoreboardTeam_setNameTagVisibility = getMethod(ScoreboardTeam, new String[] {"setNameTagVisibility", "a"}, EnumNameTagVisibility); // {1.8.1+, 1.8}
        }
        if (minorVersion >= 9) {
            DataWatcher_REGISTER = DataWatcher.getMethod("register", DataWatcherObject, Object.class);
        } else {
            DataWatcher_REGISTER = DataWatcher.getMethod("a", int.class, Object.class);
        }
        if (minorVersion >= 13) {
            ScoreboardTeam_setPrefix = ScoreboardTeam.getMethod("setPrefix", IChatBaseComponent);
            ScoreboardTeam_setSuffix = ScoreboardTeam.getMethod("setSuffix", IChatBaseComponent);
        } else {
            ScoreboardTeam_setPrefix = getMethod(ScoreboardTeam, new String[] {"setPrefix", "b"}, String.class); // 1.5.1+, 1.5
            ScoreboardTeam_setSuffix = getMethod(ScoreboardTeam, new String[] {"setSuffix", "c"}, String.class); // 1.5.1+, 1.5
        }
    }

    @Override
    public void loadClasses() throws ClassNotFoundException {
        EntityHuman = getLegacyClass("EntityHuman");
        World = getLegacyClass("World");
        Packet = getLegacyClass("Packet");
        EnumChatFormat = (Class<Enum>) getLegacyClass("EnumChatFormat");
        EntityPlayer = getLegacyClass("EntityPlayer");
        Entity = getLegacyClass("Entity");
        EntityLiving = getLegacyClass("EntityLiving");
        PlayerConnection = getLegacyClass("PlayerConnection");
        PacketPlayOutChat = getLegacyClass("PacketPlayOutChat", "Packet3Chat");
        NetworkManager = getLegacyClass("NetworkManager");
        if (minorVersion >= 7) {
            IChatBaseComponent = getLegacyClass("IChatBaseComponent");
            ChatSerializer = getLegacyClass("IChatBaseComponent$ChatSerializer", "ChatSerializer");
        }
        if (minorVersion >= 8) {
            PacketPlayOutPlayerListHeaderFooter = getLegacyClass("PacketPlayOutPlayerListHeaderFooter");
            EntityArmorStand = getLegacyClass("EntityArmorStand");
        }
        if (minorVersion >= 12) {
            ChatMessageType = (Class<Enum>) getLegacyClass("ChatMessageType");
        }

        // DataWatcher
        DataWatcher = getLegacyClass("DataWatcher");
        DataWatcherItem = getLegacyClass("DataWatcher$Item", "DataWatcher$WatchableObject", "WatchableObject");
        if (minorVersion >= 9) {
            DataWatcherObject = getLegacyClass("DataWatcherObject");
            DataWatcherRegistry = getLegacyClass("DataWatcherRegistry");
            DataWatcherSerializer = getLegacyClass("DataWatcherSerializer");
        }

        // Entities
        PacketPlayOutSpawnEntityLiving = getLegacyClass("PacketPlayOutSpawnEntityLiving", "Packet24MobSpawn");
        PacketPlayOutEntityTeleport = getLegacyClass("PacketPlayOutEntityTeleport", "Packet34EntityTeleport");
        PacketPlayInUseEntity = getLegacyClass("PacketPlayInUseEntity", "Packet7UseEntity");
        PacketPlayOutEntity = getLegacyClass("PacketPlayOutEntity", "Packet30Entity");
        PacketPlayOutEntityDestroy = getLegacyClass("PacketPlayOutEntityDestroy", "Packet29DestroyEntity");
        PacketPlayOutEntityLook = getLegacyClass("PacketPlayOutEntity$PacketPlayOutEntityLook", "PacketPlayOutEntityLook", "Packet32EntityLook");
        PacketPlayOutEntityMetadata = getLegacyClass("PacketPlayOutEntityMetadata", "Packet40EntityMetadata");
        PacketPlayOutNamedEntitySpawn = getLegacyClass("PacketPlayOutNamedEntitySpawn", "Packet20NamedEntitySpawn");
        if (minorVersion >= 7) {
            EnumEntityUseAction = (Class<Enum>) getLegacyClass("PacketPlayInUseEntity$EnumEntityUseAction", "EnumEntityUseAction");
        }

        // Player Info
        if (minorVersion >= 8) {
            PacketPlayOutPlayerInfo = getLegacyClass("PacketPlayOutPlayerInfo");
            EnumPlayerInfoAction = (Class<Enum>) getLegacyClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction", "EnumPlayerInfoAction");
            PlayerInfoData = getLegacyClass("PacketPlayOutPlayerInfo$PlayerInfoData", "PlayerInfoData");
            EnumGamemode = (Class<Enum>) getLegacyClass("EnumGamemode", "WorldSettings$EnumGamemode");
        }

        // Scoreboard
        PacketPlayOutScoreboardDisplayObjective = getLegacyClass("PacketPlayOutScoreboardDisplayObjective", "Packet208SetScoreboardDisplayObjective");
        PacketPlayOutScoreboardObjective = getLegacyClass("PacketPlayOutScoreboardObjective", "Packet206SetScoreboardObjective");
        PacketPlayOutScoreboardTeam = getLegacyClass("PacketPlayOutScoreboardTeam", "Packet209SetScoreboardTeam");
        PacketPlayOutScoreboardScore = getLegacyClass("PacketPlayOutScoreboardScore", "Packet207SetScoreboardScore");
        Scoreboard = getLegacyClass("Scoreboard");
        ScoreboardObjective = getLegacyClass("ScoreboardObjective");
        ScoreboardScore = getLegacyClass("ScoreboardScore");
        IScoreboardCriteria = getLegacyClass("IScoreboardCriteria", "IObjective"); // 1.5.1+, 1.5
        ScoreboardTeam = getLegacyClass("ScoreboardTeam");
        if (minorVersion >= 8) {
            EnumScoreboardHealthDisplay = (Class<Enum>) getLegacyClass("IScoreboardCriteria$EnumScoreboardHealthDisplay", "EnumScoreboardHealthDisplay");
            EnumScoreboardAction = (Class<Enum>) getLegacyClass("ScoreboardServer$Action", "PacketPlayOutScoreboardScore$EnumScoreboardAction", "EnumScoreboardAction");
            EnumNameTagVisibility = (Class<Enum>) getLegacyClass("ScoreboardTeamBase$EnumNameTagVisibility", "EnumNameTagVisibility");
        }
        if (minorVersion >= 9) {
            EnumTeamPush = (Class<Enum>) getLegacyClass("ScoreboardTeamBase$EnumTeamPush");
        }
    }
}
