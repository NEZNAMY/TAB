package me.neznamy.tab.platforms.bukkit.nms;

import me.neznamy.tab.platforms.bukkit.Main;

import java.util.Arrays;

/**
 * NMS loader for Minecraft 1.16.5 and lower using old NMS class structure.
 */
public class LegacyNMSStorage extends NMSStorage {

    /**
     * Creates new instance, initializes required NMS classes and fields
     *
     * @throws  ReflectiveOperationException
     *          If any class, field or method fails to load
     */
    public LegacyNMSStorage() throws ReflectiveOperationException {
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
    private Class<?> getLegacyClass(String name) throws ClassNotFoundException {
        try {
            return Class.forName("net.minecraft.server." + serverPackage + "." + name);
        } catch (ClassNotFoundException | NullPointerException e) {
            try {
                //modded server?
                Class<?> clazz = Main.class.getClassLoader().loadClass("net.minecraft.server." + serverPackage + "." + name);
                if (clazz != null) return clazz;
            } catch (NullPointerException ignored) {}
            throw new ClassNotFoundException(name);
        }
    }

    @Override
    public void loadNamedFieldsAndMethods() throws ReflectiveOperationException {
        PING = getField(EntityPlayer, "ping", "field_71138_i"); // {Bukkit, Thermos}
        ScoreboardScore_setScore = getMethod(ScoreboardScore, new String[]{"setScore", "func_96647_c"}, int.class); // {Bukkit, Thermos}
        ScoreboardTeam_setAllowFriendlyFire = getMethod(ScoreboardTeam,
                new String[]{"setAllowFriendlyFire", "func_96660_a"}, boolean.class); // {Bukkit, Thermos}
        ScoreboardTeam_setCanSeeFriendlyInvisibles = getMethod(ScoreboardTeam,
                new String[]{"setCanSeeFriendlyInvisibles", "func_98300_b"}, boolean.class); // {Bukkit, Thermos}
        if (minorVersion >= 7) {
            ChatSerializer_DESERIALIZE = getMethod(ChatSerializer, new String[]{"a", "func_150699_a"}, String.class); // {Bukkit, Thermos}
        }
        if (minorVersion >= 8) {
            ScoreboardTeam_setNameTagVisibility = getMethod(ScoreboardTeam, new String[] {"setNameTagVisibility", "a"}, EnumNameTagVisibility); // {1.8.1+, 1.8}
        }
        if (minorVersion >= 9) {
            DataWatcher_REGISTER = getMethod(DataWatcher, "register", DataWatcherObject, Object.class);
        } else {
            DataWatcher_REGISTER = getMethod(DataWatcher, new String[]{"a", "func_75682_a"}, int.class, Object.class); // {Bukkit, Thermos}
        }
        if (minorVersion >= 13) {
            ScoreboardTeam_setPrefix = getMethod(ScoreboardTeam, "setPrefix", IChatBaseComponent);
            ScoreboardTeam_setSuffix = getMethod(ScoreboardTeam, "setSuffix", IChatBaseComponent);
        } else {
            ScoreboardTeam_setPrefix = getMethod(ScoreboardTeam, new String[]{"setPrefix", "func_96666_b"}, String.class); // {Bukkit, Thermos}
            ScoreboardTeam_setSuffix = getMethod(ScoreboardTeam, new String[]{"setSuffix", "func_96662_c"}, String.class); // {Bukkit, Thermos}
        }
    }

    @Override
    public void loadClasses() throws ClassNotFoundException {
        EntityHuman = getLegacyClass("EntityHuman");
        World = getLegacyClass("World");
        Packet = getLegacyClass("Packet");
        EnumChatFormat = getLegacyClass("EnumChatFormat");
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
            ChatMessageType = getLegacyClass("ChatMessageType");
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
            EnumEntityUseAction = getLegacyClass("PacketPlayInUseEntity$EnumEntityUseAction", "EnumEntityUseAction");
        }

        // Player Info
        if (minorVersion >= 8) {
            PacketPlayOutPlayerInfo = getLegacyClass("PacketPlayOutPlayerInfo");
            EnumPlayerInfoAction = getLegacyClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction", "EnumPlayerInfoAction");
            PlayerInfoData = getLegacyClass("PacketPlayOutPlayerInfo$PlayerInfoData", "PlayerInfoData");
            EnumGamemode = getLegacyClass("EnumGamemode", "WorldSettings$EnumGamemode");
        }

        // Scoreboard
        PacketPlayOutScoreboardDisplayObjective = getLegacyClass("PacketPlayOutScoreboardDisplayObjective", "Packet208SetScoreboardDisplayObjective");
        PacketPlayOutScoreboardObjective = getLegacyClass("PacketPlayOutScoreboardObjective", "Packet206SetScoreboardObjective");
        PacketPlayOutScoreboardTeam = getLegacyClass("PacketPlayOutScoreboardTeam", "Packet209SetScoreboardTeam");
        PacketPlayOutScoreboardScore = getLegacyClass("PacketPlayOutScoreboardScore", "Packet207SetScoreboardScore");
        Scoreboard = getLegacyClass("Scoreboard");
        ScoreboardObjective = getLegacyClass("ScoreboardObjective");
        ScoreboardScore = getLegacyClass("ScoreboardScore");
        IScoreboardCriteria = getLegacyClass("IScoreboardCriteria");
        ScoreboardTeam = getLegacyClass("ScoreboardTeam");
        if (minorVersion >= 8) {
            EnumScoreboardHealthDisplay = getLegacyClass("IScoreboardCriteria$EnumScoreboardHealthDisplay", "EnumScoreboardHealthDisplay");
            EnumScoreboardAction = getLegacyClass("ScoreboardServer$Action", "PacketPlayOutScoreboardScore$EnumScoreboardAction", "EnumScoreboardAction");
            EnumNameTagVisibility = getLegacyClass("ScoreboardTeamBase$EnumNameTagVisibility", "EnumNameTagVisibility");
        }
        if (minorVersion >= 9) {
            EnumTeamPush = getLegacyClass("ScoreboardTeamBase$EnumTeamPush");
        }
    }
}
