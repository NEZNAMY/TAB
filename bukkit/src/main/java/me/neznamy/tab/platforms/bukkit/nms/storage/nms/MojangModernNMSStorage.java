package me.neznamy.tab.platforms.bukkit.nms.storage.nms;

import me.neznamy.tab.platforms.bukkit.BukkitTabList;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherHelper;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherItem;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherObject;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.*;
import me.neznamy.tab.platforms.bukkit.scoreboard.PacketScoreboard;

/**
 * NMS loader for minecraft 1.17+ using Mojang packaging and names.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class MojangModernNMSStorage extends NMSStorage {

    @Override
    public void loadNamedFieldsAndMethods() throws ReflectiveOperationException {
        ChatSerializer_DESERIALIZE = ChatSerializer.getMethod("fromJson", String.class);
        DataWatcher.REGISTER = DataWatcher.CLASS.getMethod("define", DataWatcherObject.CLASS, Object.class);
        PacketScoreboard.ScoreboardScore_setScore = PacketScoreboard.ScoreboardScoreClass.getMethod("setScore", int.class);
        PacketScoreboard.ScoreboardTeam_setAllowFriendlyFire = PacketScoreboard.ScoreboardTeam.getMethod("setAllowFriendlyFire", boolean.class);
        PacketScoreboard.ScoreboardTeam_setCanSeeFriendlyInvisibles = PacketScoreboard.ScoreboardTeam.getMethod("setSeeFriendlyInvisibles", boolean.class);
        PacketScoreboard.ScoreboardTeam_setPrefix = PacketScoreboard.ScoreboardTeam.getMethod("setPlayerPrefix", IChatBaseComponent);
        PacketScoreboard.ScoreboardTeam_setSuffix = PacketScoreboard.ScoreboardTeam.getMethod("setPlayerSuffix", IChatBaseComponent);
        PacketScoreboard.ScoreboardTeam_setNameTagVisibility = PacketScoreboard.ScoreboardTeam.getMethod("setNameTagVisibility", PacketScoreboard.EnumNameTagVisibility);
        DataWatcherHelper.DataWatcherSerializer_BYTE = DataWatcherHelper.DataWatcherRegistry.getDeclaredField("BYTE").get(null);
        DataWatcherHelper.DataWatcherSerializer_FLOAT = DataWatcherHelper.DataWatcherRegistry.getDeclaredField("FLOAT").get(null);
        DataWatcherHelper.DataWatcherSerializer_STRING = DataWatcherHelper.DataWatcherRegistry.getDeclaredField("STRING").get(null);
        DataWatcherHelper.DataWatcherSerializer_OPTIONAL_COMPONENT = DataWatcherHelper.DataWatcherRegistry.getDeclaredField("OPTIONAL_COMPONENT").get(null);
        DataWatcherHelper.DataWatcherSerializer_BOOLEAN = DataWatcherHelper.DataWatcherRegistry.getDeclaredField("BOOLEAN").get(null);
        if (minorVersion >= 19) {
            PacketPlayOutSpawnEntityLivingStorage.EntityTypes_ARMOR_STAND = PacketPlayOutSpawnEntityLivingStorage.EntityTypes.getDeclaredField("ARMOR_STAND").get(null);
            (PacketPlayOutSpawnEntityLivingStorage.ENTITY_TYPE = PacketPlayOutSpawnEntityLivingStorage.CLASS.getDeclaredField("type")).setAccessible(true);
            DataWatcher.packDirty = DataWatcher.CLASS.getMethod("packDirty");
        }
    }

    @Override
    public void loadClasses() throws ClassNotFoundException {
        IChatBaseComponent = Class.forName("net.minecraft.network.chat.Component");
        ChatSerializer = Class.forName("net.minecraft.network.chat.Component$Serializer");
        World = Class.forName("net.minecraft.world.level.Level");
        EntityArmorStand = Class.forName("net.minecraft.world.entity.decoration.ArmorStand");
        EntityHuman = Class.forName("net.minecraft.world.entity.player.Player");
        NetworkManager = Class.forName("net.minecraft.network.Connection");
        Packet = Class.forName("net.minecraft.network.protocol.Packet");
        EnumChatFormat = (Class<Enum>) Class.forName("net.minecraft.ChatFormatting");
        EntityPlayer = Class.forName("net.minecraft.server.level.ServerPlayer");
        Entity = Class.forName("net.minecraft.world.entity.Entity");
        EntityLiving = Class.forName("net.minecraft.world.entity.LivingEntity");
        PlayerConnection = Class.forName("net.minecraft.server.network.ServerGamePacketListenerImpl");
        BukkitTabList.PacketPlayOutPlayerListHeaderFooterClass = Class.forName("net.minecraft.network.protocol.game.ClientboundTabListPacket");
        DataWatcher.CLASS = Class.forName("net.minecraft.network.syncher.SynchedEntityData");
        DataWatcherItem.CLASS = Class.forName("net.minecraft.network.syncher.SynchedEntityData$DataItem");
        DataWatcherObject.CLASS = Class.forName("net.minecraft.network.syncher.EntityDataAccessor");
        DataWatcherHelper.DataWatcherRegistry = Class.forName("net.minecraft.network.syncher.EntityDataSerializers");
        DataWatcherHelper.DataWatcherSerializer = Class.forName("net.minecraft.network.syncher.EntityDataSerializer");
        PacketPlayOutEntityTeleportStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket");
        PacketPlayOutEntity = Class.forName("net.minecraft.network.protocol.game.ClientboundMoveEntityPacket");
        PacketPlayOutEntityDestroyStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket");
        PacketPlayOutEntityLook = Class.forName("net.minecraft.network.protocol.game.ClientboundMoveEntityPacket$Rot");
        PacketPlayOutEntityMetadataStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket");
        PacketPlayOutNamedEntitySpawn = Class.forName("net.minecraft.network.protocol.game.ClientboundAddPlayerPacket");
        BukkitTabList.EnumGamemodeClass = (Class<Enum>) Class.forName("net.minecraft.world.level.GameType");
        PacketScoreboard.DisplayObjectiveClass = Class.forName("net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket");
        PacketScoreboard.ObjectivePacketClass = Class.forName("net.minecraft.network.protocol.game.ClientboundSetObjectivePacket");
        PacketScoreboard.Scoreboard = Class.forName("net.minecraft.world.scores.Scoreboard");
        PacketScoreboard.ScorePacketClass = Class.forName("net.minecraft.network.protocol.game.ClientboundSetScorePacket");
        PacketScoreboard.ScoreboardObjective = Class.forName("net.minecraft.world.scores.Objective");
        PacketScoreboard.ScoreboardScoreClass = Class.forName("net.minecraft.world.scores.Score");
        PacketScoreboard.IScoreboardCriteria = Class.forName("net.minecraft.world.scores.criteria.ObjectiveCriteria");
        PacketScoreboard.EnumScoreboardHealthDisplay = (Class<Enum>) Class.forName("net.minecraft.world.scores.criteria.ObjectiveCriteria$RenderType");
        PacketScoreboard.EnumScoreboardAction = (Class<Enum>) Class.forName("net.minecraft.server.ServerScoreboard$Method");
        PacketScoreboard.TeamPacketClass = Class.forName("net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket");
        PacketScoreboard.ScoreboardTeam = Class.forName("net.minecraft.world.scores.PlayerTeam");
        PacketScoreboard.EnumNameTagVisibility = (Class<Enum>) Class.forName("net.minecraft.world.scores.Team$Visibility");
        PacketScoreboard.EnumTeamPush = (Class<Enum>) Class.forName("net.minecraft.world.scores.Team$CollisionRule");
        if (minorVersion >= 19) {
            PacketPlayOutSpawnEntityLivingStorage.EntityTypes = Class.forName("net.minecraft.world.entity.EntityType");
            PacketPlayOutSpawnEntityLivingStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.ClientboundAddEntityPacket");
        } else {
            PacketPlayOutSpawnEntityLivingStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.ClientboundAddMobPacket");
        }
        if (is1_19_3Plus()) {
            BukkitTabList.ClientboundPlayerInfoRemovePacket = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket");
            BukkitTabList.PacketPlayOutPlayerInfoClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");
            BukkitTabList.EnumPlayerInfoActionClass = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action");
            BukkitTabList.PlayerInfoDataClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Entry");
            BukkitTabList.RemoteChatSession$Data = Class.forName("net.minecraft.network.chat.RemoteChatSession$Data");
        } else {
            BukkitTabList.PacketPlayOutPlayerInfoClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket");
            BukkitTabList.EnumPlayerInfoActionClass = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket$Action");
            BukkitTabList.PlayerInfoDataClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket$PlayerUpdate");
        }
    }
}
