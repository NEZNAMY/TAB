package me.neznamy.tab.platforms.bukkit.nms.storage.nms;

import me.neznamy.tab.platforms.bukkit.BukkitTabList;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherHelper;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherItem;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherObject;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.*;
import me.neznamy.tab.platforms.bukkit.scoreboard.PacketScoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * NMS loader for Minecraft 1.5.2 - 1.16.5 using Bukkit mapping.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class BukkitLegacyNMSStorage extends NMSStorage {

    /**
     * Returns class with given potential names in same order
     *
     * @param   names
     *          possible class names
     * @return  class for specified names
     * @throws  ClassNotFoundException
     *          if class does not exist
     */
    private Class<?> getLegacyClass(@NotNull String... names) throws ClassNotFoundException {
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
    public Class<?> getLegacyClass(@NotNull String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + serverPackage + "." + name);
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
        DataWatcher.CLASS = getLegacyClass("DataWatcher");
        DataWatcherItem.CLASS = getLegacyClass("DataWatcher$Item", "DataWatcher$WatchableObject", "WatchableObject");
        PacketPlayOutSpawnEntityLivingStorage.CLASS = getLegacyClass("PacketPlayOutSpawnEntityLiving", "Packet24MobSpawn");
        PacketPlayOutEntityTeleportStorage.CLASS = getLegacyClass("PacketPlayOutEntityTeleport", "Packet34EntityTeleport");
        PacketPlayOutEntity = getLegacyClass("PacketPlayOutEntity", "Packet30Entity");
        PacketPlayOutEntityDestroyStorage.CLASS = getLegacyClass("PacketPlayOutEntityDestroy", "Packet29DestroyEntity");
        PacketPlayOutEntityLook = getLegacyClass("PacketPlayOutEntity$PacketPlayOutEntityLook", "PacketPlayOutEntityLook", "Packet32EntityLook");
        PacketPlayOutEntityMetadataStorage.CLASS = getLegacyClass("PacketPlayOutEntityMetadata", "Packet40EntityMetadata");
        PacketPlayOutNamedEntitySpawn = getLegacyClass("PacketPlayOutNamedEntitySpawn", "Packet20NamedEntitySpawn");
        PacketScoreboard.DisplayObjectiveClass = getLegacyClass("PacketPlayOutScoreboardDisplayObjective", "Packet208SetScoreboardDisplayObjective");
        PacketScoreboard.ObjectivePacketClass = getLegacyClass("PacketPlayOutScoreboardObjective", "Packet206SetScoreboardObjective");
        PacketScoreboard.TeamPacketClass = getLegacyClass("PacketPlayOutScoreboardTeam", "Packet209SetScoreboardTeam");
        PacketScoreboard.ScorePacketClass = getLegacyClass("PacketPlayOutScoreboardScore", "Packet207SetScoreboardScore");
        PacketScoreboard.Scoreboard = getLegacyClass("Scoreboard");
        PacketScoreboard.ScoreboardObjective = getLegacyClass("ScoreboardObjective");
        PacketScoreboard.ScoreboardScoreClass = getLegacyClass("ScoreboardScore");
        PacketScoreboard.IScoreboardCriteria = getLegacyClass("IScoreboardCriteria", "IObjective"); // 1.5.1+, 1.5
        PacketScoreboard.ScoreboardTeam = getLegacyClass("ScoreboardTeam");
        if (minorVersion >= 7) {
            IChatBaseComponent = getLegacyClass("IChatBaseComponent");
            ChatSerializer = getLegacyClass("IChatBaseComponent$ChatSerializer", "ChatSerializer");
        }
        if (minorVersion >= 8) {
            BukkitTabList.PacketPlayOutPlayerListHeaderFooterClass = getLegacyClass("PacketPlayOutPlayerListHeaderFooter");
            EntityArmorStand = getLegacyClass("EntityArmorStand");
            BukkitTabList.PacketPlayOutPlayerInfoClass = getLegacyClass("PacketPlayOutPlayerInfo");
            BukkitTabList.EnumPlayerInfoActionClass = (Class<Enum>) getLegacyClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction", "EnumPlayerInfoAction");
            BukkitTabList.PlayerInfoDataClass = getLegacyClass("PacketPlayOutPlayerInfo$PlayerInfoData", "PlayerInfoData");
            BukkitTabList.EnumGamemodeClass = (Class<Enum>) getLegacyClass("EnumGamemode", "WorldSettings$EnumGamemode");
            PacketScoreboard.EnumScoreboardHealthDisplay = (Class<Enum>) getLegacyClass("IScoreboardCriteria$EnumScoreboardHealthDisplay", "EnumScoreboardHealthDisplay");
            PacketScoreboard.EnumScoreboardAction = (Class<Enum>) getLegacyClass("ScoreboardServer$Action", "PacketPlayOutScoreboardScore$EnumScoreboardAction", "EnumScoreboardAction");
            PacketScoreboard.EnumNameTagVisibility = (Class<Enum>) getLegacyClass("ScoreboardTeamBase$EnumNameTagVisibility", "EnumNameTagVisibility");
        }
        if (minorVersion >= 9) {
            DataWatcherObject.CLASS = getLegacyClass("DataWatcherObject");
            DataWatcherHelper.DataWatcherRegistry = getLegacyClass("DataWatcherRegistry");
            DataWatcherHelper.DataWatcherSerializer = getLegacyClass("DataWatcherSerializer");
            PacketScoreboard.EnumTeamPush = (Class<Enum>) getLegacyClass("ScoreboardTeamBase$EnumTeamPush");
        }
    }
}
