package me.neznamy.tab.platforms.bukkit.nms.storage.nms;

import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherHelper;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherItem;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherObject;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.*;

import java.util.Arrays;

/**
 * NMS loader for Minecraft 1.5.2 - 1.16.5 using Bukkit mapping.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class BukkitLegacyNMSStorage extends NMSStorage {

    public BukkitLegacyNMSStorage() throws ReflectiveOperationException {}

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
        (PING = EntityPlayer.getDeclaredField("ping")).setAccessible(true);
        PacketPlayOutScoreboardScoreStorage.ScoreboardScore_setScore = getMethod(PacketPlayOutScoreboardScoreStorage.ScoreboardScore, new String[] {"setScore", "c"}, int.class); // 1.5.1+, 1.5
        PacketPlayOutScoreboardTeamStorage.ScoreboardTeam_setAllowFriendlyFire = getMethod(PacketPlayOutScoreboardTeamStorage.ScoreboardTeam, new String[] {"setAllowFriendlyFire", "a"}, boolean.class); // 1.5.1+, 1.5
        PacketPlayOutScoreboardTeamStorage.ScoreboardTeam_setCanSeeFriendlyInvisibles = getMethod(PacketPlayOutScoreboardTeamStorage.ScoreboardTeam, new String[] {"setCanSeeFriendlyInvisibles", "b"}, boolean.class); // 1.5.1+, 1.5
        if (minorVersion >= 7) {
            ChatSerializer_DESERIALIZE = ChatSerializer.getMethod("a", String.class);
        }
        if (minorVersion >= 8) {
            PacketPlayOutScoreboardTeamStorage.ScoreboardTeam_setNameTagVisibility = getMethod(PacketPlayOutScoreboardTeamStorage.ScoreboardTeam, new String[] {"setNameTagVisibility", "a"}, PacketPlayOutScoreboardTeamStorage.EnumNameTagVisibility); // {1.8.1+, 1.8}
        }
        if (minorVersion >= 9) {
            DataWatcher.REGISTER = DataWatcher.CLASS.getMethod("register", DataWatcherObject.CLASS, Object.class);
            DataWatcherHelper.DataWatcherSerializer_BYTE = DataWatcherHelper.DataWatcherRegistry.getDeclaredField("a").get(null);
            DataWatcherHelper.DataWatcherSerializer_FLOAT = DataWatcherHelper.DataWatcherRegistry.getDeclaredField("c").get(null);
            DataWatcherHelper.DataWatcherSerializer_STRING = DataWatcherHelper.DataWatcherRegistry.getDeclaredField("d").get(null);
            if (getMinorVersion() >= 13) {
                DataWatcherHelper.DataWatcherSerializer_OPTIONAL_COMPONENT = DataWatcherHelper.DataWatcherRegistry.getDeclaredField("f").get(null);
                DataWatcherHelper.DataWatcherSerializer_BOOLEAN = DataWatcherHelper.DataWatcherRegistry.getDeclaredField("i").get(null);
            } else {
                DataWatcherHelper.DataWatcherSerializer_BOOLEAN = DataWatcherHelper.DataWatcherRegistry.getDeclaredField("h").get(null);
            }
        } else {
            DataWatcher.REGISTER = DataWatcher.CLASS.getMethod("a", int.class, Object.class);
        }
        if (minorVersion >= 13) {
            PacketPlayOutScoreboardTeamStorage.ScoreboardTeam_setPrefix = PacketPlayOutScoreboardTeamStorage.ScoreboardTeam.getMethod("setPrefix", IChatBaseComponent);
            PacketPlayOutScoreboardTeamStorage.ScoreboardTeam_setSuffix = PacketPlayOutScoreboardTeamStorage.ScoreboardTeam.getMethod("setSuffix", IChatBaseComponent);
        } else {
            PacketPlayOutScoreboardTeamStorage.ScoreboardTeam_setPrefix = getMethod(PacketPlayOutScoreboardTeamStorage.ScoreboardTeam, new String[] {"setPrefix", "b"}, String.class); // 1.5.1+, 1.5
            PacketPlayOutScoreboardTeamStorage.ScoreboardTeam_setSuffix = getMethod(PacketPlayOutScoreboardTeamStorage.ScoreboardTeam, new String[] {"setSuffix", "c"}, String.class); // 1.5.1+, 1.5
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
        NetworkManager = getLegacyClass("NetworkManager");
        if (minorVersion >= 7) {
            IChatBaseComponent = getLegacyClass("IChatBaseComponent");
            ChatSerializer = getLegacyClass("IChatBaseComponent$ChatSerializer", "ChatSerializer");
        }
        if (minorVersion >= 8) {
            PacketPlayOutPlayerListHeaderFooterStorage.CLASS = getLegacyClass("PacketPlayOutPlayerListHeaderFooter");
            EntityArmorStand = getLegacyClass("EntityArmorStand");
        }

        // DataWatcher
        DataWatcher.CLASS = getLegacyClass("DataWatcher");
        DataWatcherItem.CLASS = getLegacyClass("DataWatcher$Item", "DataWatcher$WatchableObject", "WatchableObject");
        if (minorVersion >= 9) {
            DataWatcherObject.CLASS = getLegacyClass("DataWatcherObject");
            DataWatcherHelper.DataWatcherRegistry = getLegacyClass("DataWatcherRegistry");
            DataWatcherHelper.DataWatcherSerializer = getLegacyClass("DataWatcherSerializer");
        }

        // Entities
        PacketPlayOutSpawnEntityLivingStorage.CLASS = getLegacyClass("PacketPlayOutSpawnEntityLiving", "Packet24MobSpawn");
        PacketPlayOutEntityTeleportStorage.CLASS = getLegacyClass("PacketPlayOutEntityTeleport", "Packet34EntityTeleport");
        PacketPlayInUseEntity = getLegacyClass("PacketPlayInUseEntity", "Packet7UseEntity");
        PacketPlayOutEntity = getLegacyClass("PacketPlayOutEntity", "Packet30Entity");
        PacketPlayOutEntityDestroyStorage.CLASS = getLegacyClass("PacketPlayOutEntityDestroy", "Packet29DestroyEntity");
        PacketPlayOutEntityLook = getLegacyClass("PacketPlayOutEntity$PacketPlayOutEntityLook", "PacketPlayOutEntityLook", "Packet32EntityLook");
        PacketPlayOutEntityMetadataStorage.CLASS = getLegacyClass("PacketPlayOutEntityMetadata", "Packet40EntityMetadata");
        PacketPlayOutNamedEntitySpawn = getLegacyClass("PacketPlayOutNamedEntitySpawn", "Packet20NamedEntitySpawn");
        if (minorVersion >= 7) {
            EnumEntityUseAction = (Class<Enum>) getLegacyClass("PacketPlayInUseEntity$EnumEntityUseAction", "EnumEntityUseAction");
        }

        // Player Info
        if (minorVersion >= 8) {
            PacketPlayOutPlayerInfoStorage.CLASS = getLegacyClass("PacketPlayOutPlayerInfo");
            PacketPlayOutPlayerInfoStorage.EnumPlayerInfoActionClass = (Class<Enum>) getLegacyClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction", "EnumPlayerInfoAction");
            PacketPlayOutPlayerInfoStorage.PlayerInfoDataStorage.CLASS = getLegacyClass("PacketPlayOutPlayerInfo$PlayerInfoData", "PlayerInfoData");
            PacketPlayOutPlayerInfoStorage.EnumGamemodeClass = (Class<Enum>) getLegacyClass("EnumGamemode", "WorldSettings$EnumGamemode");
        }

        // Scoreboard
        PacketPlayOutScoreboardDisplayObjectiveStorage.CLASS = getLegacyClass("PacketPlayOutScoreboardDisplayObjective", "Packet208SetScoreboardDisplayObjective");
        PacketPlayOutScoreboardObjectiveStorage.CLASS = getLegacyClass("PacketPlayOutScoreboardObjective", "Packet206SetScoreboardObjective");
        PacketPlayOutScoreboardTeamStorage.CLASS = getLegacyClass("PacketPlayOutScoreboardTeam", "Packet209SetScoreboardTeam");
        PacketPlayOutScoreboardScoreStorage.CLASS = getLegacyClass("PacketPlayOutScoreboardScore", "Packet207SetScoreboardScore");
        Scoreboard = getLegacyClass("Scoreboard");
        PacketPlayOutScoreboardObjectiveStorage.ScoreboardObjective = getLegacyClass("ScoreboardObjective");
        PacketPlayOutScoreboardScoreStorage.ScoreboardScore = getLegacyClass("ScoreboardScore");
        IScoreboardCriteria = getLegacyClass("IScoreboardCriteria", "IObjective"); // 1.5.1+, 1.5
        PacketPlayOutScoreboardTeamStorage.ScoreboardTeam = getLegacyClass("ScoreboardTeam");
        if (minorVersion >= 8) {
            PacketPlayOutScoreboardObjectiveStorage.EnumScoreboardHealthDisplay = (Class<Enum>) getLegacyClass("IScoreboardCriteria$EnumScoreboardHealthDisplay", "EnumScoreboardHealthDisplay");
            PacketPlayOutScoreboardScoreStorage.EnumScoreboardAction = (Class<Enum>) getLegacyClass("ScoreboardServer$Action", "PacketPlayOutScoreboardScore$EnumScoreboardAction", "EnumScoreboardAction");
            PacketPlayOutScoreboardTeamStorage.EnumNameTagVisibility = (Class<Enum>) getLegacyClass("ScoreboardTeamBase$EnumNameTagVisibility", "EnumNameTagVisibility");
        }
        if (minorVersion >= 9) {
            PacketPlayOutScoreboardTeamStorage.EnumTeamPush = (Class<Enum>) getLegacyClass("ScoreboardTeamBase$EnumTeamPush");
        }
    }
}
