package me.neznamy.tab.platforms.bukkit.nms.storage.nms;

import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherHelper;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherItem;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherObject;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.*;

/**
 * NMS loader for minecraft 1.17+ using Mojang packaging and bukkit names.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class BukkitModernNMSStorage extends NMSStorage {

    public BukkitModernNMSStorage() throws ReflectiveOperationException {}

    @Override
    public void loadNamedFieldsAndMethods() throws ReflectiveOperationException {
        (PING = EntityPlayer.getDeclaredField("e")).setAccessible(true);
        ChatSerializer_DESERIALIZE = ChatSerializer.getMethod("a", String.class);
        DataWatcher.REGISTER = getMethod(DataWatcher.CLASS, new String[]{"register", "a"}, DataWatcherObject.CLASS, Object.class); // {Bukkit, Bukkit 1.18+}
        PacketPlayOutScoreboardScoreStorage.ScoreboardScore_setScore = getMethod(PacketPlayOutScoreboardScoreStorage.ScoreboardScore, new String[]{"setScore", "b"}, int.class); // {Bukkit, Bukkit 1.18+}
        PacketPlayOutScoreboardTeamStorage.ScoreboardTeam_setAllowFriendlyFire = getMethod(PacketPlayOutScoreboardTeamStorage.ScoreboardTeam, new String[]{"setAllowFriendlyFire", "a"}, boolean.class); // {Bukkit, Bukkit 1.18+}
        PacketPlayOutScoreboardTeamStorage.ScoreboardTeam_setCanSeeFriendlyInvisibles = getMethod(PacketPlayOutScoreboardTeamStorage.ScoreboardTeam, new String[]{"setCanSeeFriendlyInvisibles", "b"}, boolean.class); // {Bukkit, Bukkit 1.18+}
        PacketPlayOutScoreboardTeamStorage.ScoreboardTeam_setPrefix = getMethod(PacketPlayOutScoreboardTeamStorage.ScoreboardTeam, new String[]{"setPrefix", "b"}, IChatBaseComponent); // {Bukkit, Bukkit 1.18+}
        PacketPlayOutScoreboardTeamStorage.ScoreboardTeam_setSuffix = getMethod(PacketPlayOutScoreboardTeamStorage.ScoreboardTeam, new String[]{"setSuffix", "c"}, IChatBaseComponent); // {Bukkit, Bukkit 1.18+}
        PacketPlayOutScoreboardTeamStorage.ScoreboardTeam_setNameTagVisibility = getMethod(PacketPlayOutScoreboardTeamStorage.ScoreboardTeam, new String[]{"setNameTagVisibility", "a"}, PacketPlayOutScoreboardTeamStorage.EnumNameTagVisibility); // {Bukkit, Bukkit 1.18+}
        DataWatcherHelper.DataWatcherSerializer_BYTE = DataWatcherHelper.DataWatcherRegistry.getDeclaredField("a").get(null);
        DataWatcherHelper.DataWatcherSerializer_FLOAT = DataWatcherHelper.DataWatcherRegistry.getDeclaredField("c").get(null);
        DataWatcherHelper.DataWatcherSerializer_STRING = DataWatcherHelper.DataWatcherRegistry.getDeclaredField("d").get(null);
        if (is1_19_3Plus()) {
            DataWatcherHelper.DataWatcherSerializer_OPTIONAL_COMPONENT = DataWatcherHelper.DataWatcherRegistry.getDeclaredField("g").get(null);
            if (is1_19_4Plus()) {
                DataWatcherHelper.DataWatcherSerializer_BOOLEAN = DataWatcherHelper.DataWatcherRegistry.getDeclaredField("k").get(null);
            } else {
                DataWatcherHelper.DataWatcherSerializer_BOOLEAN = DataWatcherHelper.DataWatcherRegistry.getDeclaredField("j").get(null);
            }
         } else {
            DataWatcherHelper.DataWatcherSerializer_OPTIONAL_COMPONENT = DataWatcherHelper.DataWatcherRegistry.getDeclaredField("f").get(null);
            DataWatcherHelper.DataWatcherSerializer_BOOLEAN = DataWatcherHelper.DataWatcherRegistry.getDeclaredField("i").get(null);
        }
        if (minorVersion >= 19) {
            PacketPlayOutSpawnEntityLivingStorage.EntityTypes_ARMOR_STAND = PacketPlayOutSpawnEntityLivingStorage.EntityTypes.getDeclaredField("d").get(null);
            (PacketPlayOutSpawnEntityLivingStorage.ENTITY_TYPE = PacketPlayOutSpawnEntityLivingStorage.CLASS.getDeclaredField("e")).setAccessible(true);
            DataWatcher.packDirty = DataWatcher.CLASS.getMethod("b");
        }
    }

    @Override
    public void loadClasses() throws ClassNotFoundException {
        ChatSerializer = Class.forName("net.minecraft.network.chat.IChatBaseComponent$ChatSerializer");
        World = Class.forName("net.minecraft.world.level.World");
        EntityArmorStand = Class.forName("net.minecraft.world.entity.decoration.EntityArmorStand");
        EntityHuman = Class.forName("net.minecraft.world.entity.player.EntityHuman");
        NetworkManager = Class.forName("net.minecraft.network.NetworkManager");
        IChatBaseComponent = Class.forName("net.minecraft.network.chat.IChatBaseComponent");
        Packet = Class.forName("net.minecraft.network.protocol.Packet");
        EnumChatFormat = (Class<Enum>) Class.forName("net.minecraft.EnumChatFormat");
        EntityPlayer = Class.forName("net.minecraft.server.level.EntityPlayer");
        Entity = Class.forName("net.minecraft.world.entity.Entity");
        EntityLiving = Class.forName("net.minecraft.world.entity.EntityLiving");
        PlayerConnection = Class.forName("net.minecraft.server.network.PlayerConnection");

        PacketPlayOutPlayerListHeaderFooterStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutPlayerListHeaderFooter");

        // DataWatcher
        DataWatcher.CLASS = Class.forName("net.minecraft.network.syncher.DataWatcher");
        DataWatcherItem.CLASS = Class.forName("net.minecraft.network.syncher.DataWatcher$Item");
        DataWatcherObject.CLASS = Class.forName("net.minecraft.network.syncher.DataWatcherObject");
        DataWatcherHelper.DataWatcherRegistry = Class.forName("net.minecraft.network.syncher.DataWatcherRegistry");
        DataWatcherHelper.DataWatcherSerializer = Class.forName("net.minecraft.network.syncher.DataWatcherSerializer");
        if (is1_19_3Plus()) {
            DataWatcher.DataValue = Class.forName("net.minecraft.network.syncher.DataWatcher$b");
        }

        // Entities
        if (minorVersion >= 19) {
            PacketPlayOutSpawnEntityLivingStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity");
        } else {
            PacketPlayOutSpawnEntityLivingStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutSpawnEntityLiving");
        }
        PacketPlayOutEntityTeleportStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport");
        PacketPlayInUseEntity = Class.forName("net.minecraft.network.protocol.game.PacketPlayInUseEntity");
        PacketPlayInUseEntity$d = Class.forName("net.minecraft.network.protocol.game.PacketPlayInUseEntity$d");
        PacketPlayOutEntity = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntity");
        PacketPlayOutEntityDestroyStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy");
        PacketPlayOutEntityLook = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntity$PacketPlayOutEntityLook");
        PacketPlayOutEntityMetadataStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata");
        PacketPlayOutNamedEntitySpawn = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn");
        EnumEntityUseAction = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.PacketPlayInUseEntity$EnumEntityUseAction");
        if (minorVersion >= 19) {
            PacketPlayOutSpawnEntityLivingStorage.EntityTypes = Class.forName("net.minecraft.world.entity.EntityTypes");
        }

        // Player Info
        if (minorVersion >= 19) {
            PacketPlayOutPlayerInfoStorage.ProfilePublicKey$a = Class.forName("net.minecraft.world.entity.player.ProfilePublicKey$a");
        }
        if (is1_19_3Plus()) {
            PacketPlayOutPlayerInfoStorage.ClientboundPlayerInfoRemovePacket = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket");
            PacketPlayOutPlayerInfoStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");
            PacketPlayOutPlayerInfoStorage.EnumPlayerInfoActionClass = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$a");
            PacketPlayOutPlayerInfoStorage.PlayerInfoDataStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$b");
            PacketPlayOutPlayerInfoStorage.RemoteChatSession$Data = Class.forName("net.minecraft.network.chat.RemoteChatSession$a");
        } else {
            PacketPlayOutPlayerInfoStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo");
            PacketPlayOutPlayerInfoStorage.EnumPlayerInfoActionClass = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
            PacketPlayOutPlayerInfoStorage.PlayerInfoDataStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo$PlayerInfoData");
        }
        PacketPlayOutPlayerInfoStorage.EnumGamemodeClass = (Class<Enum>) Class.forName("net.minecraft.world.level.EnumGamemode");

        // Scoreboard
        PacketPlayOutScoreboardDisplayObjectiveStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardDisplayObjective");
        PacketPlayOutScoreboardObjectiveStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardObjective");
        Scoreboard = Class.forName("net.minecraft.world.scores.Scoreboard");
        PacketPlayOutScoreboardScoreStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore");
        PacketPlayOutScoreboardObjectiveStorage.ScoreboardObjective = Class.forName("net.minecraft.world.scores.ScoreboardObjective");
        PacketPlayOutScoreboardScoreStorage.ScoreboardScore = Class.forName("net.minecraft.world.scores.ScoreboardScore");
        IScoreboardCriteria = Class.forName("net.minecraft.world.scores.criteria.IScoreboardCriteria");
        PacketPlayOutScoreboardObjectiveStorage.EnumScoreboardHealthDisplay = (Class<Enum>) Class.forName("net.minecraft.world.scores.criteria.IScoreboardCriteria$EnumScoreboardHealthDisplay");
        PacketPlayOutScoreboardScoreStorage.EnumScoreboardAction = (Class<Enum>) Class.forName("net.minecraft.server.ScoreboardServer$Action");
        PacketPlayOutScoreboardTeamStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam");
        PacketPlayOutScoreboardTeamStorage.ScoreboardTeam = Class.forName("net.minecraft.world.scores.ScoreboardTeam");
        PacketPlayOutScoreboardTeamStorage.EnumNameTagVisibility = (Class<Enum>) Class.forName("net.minecraft.world.scores.ScoreboardTeamBase$EnumNameTagVisibility");
        PacketPlayOutScoreboardTeamStorage.EnumTeamPush = (Class<Enum>) Class.forName("net.minecraft.world.scores.ScoreboardTeamBase$EnumTeamPush");
        PacketPlayOutScoreboardTeamStorage.PlayerAction = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam$a");
    }
}
