package me.neznamy.tab.platforms.bukkit.nms.storage.nms;

import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherHelper;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherItem;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherObject;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.*;

/**
 * NMS loader for minecraft 1.17+ using Mojang packaging and names.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class MojangModernNMSStorage extends NMSStorage {

    public MojangModernNMSStorage() throws ReflectiveOperationException {}

    @Override
    public void loadNamedFieldsAndMethods() throws ReflectiveOperationException {
        (PING = EntityPlayer.getDeclaredField("latency")).setAccessible(true);
        ChatSerializer_DESERIALIZE = ChatSerializer.getMethod("fromJson", String.class);
        DataWatcher.REGISTER = DataWatcher.CLASS.getMethod("define", DataWatcherObject.CLASS, Object.class);
        PacketPlayOutScoreboardScoreStorage.ScoreboardScore_setScore = PacketPlayOutScoreboardScoreStorage.ScoreboardScore.getMethod("setScore", int.class);
        PacketPlayOutScoreboardTeamStorage.ScoreboardTeam_setAllowFriendlyFire = PacketPlayOutScoreboardTeamStorage.ScoreboardTeam.getMethod("setAllowFriendlyFire", boolean.class);
        PacketPlayOutScoreboardTeamStorage.ScoreboardTeam_setCanSeeFriendlyInvisibles = PacketPlayOutScoreboardTeamStorage.ScoreboardTeam.getMethod("setSeeFriendlyInvisibles", boolean.class);
        PacketPlayOutScoreboardTeamStorage.ScoreboardTeam_setPrefix = PacketPlayOutScoreboardTeamStorage.ScoreboardTeam.getMethod("setPlayerPrefix", IChatBaseComponent);
        PacketPlayOutScoreboardTeamStorage.ScoreboardTeam_setSuffix = PacketPlayOutScoreboardTeamStorage.ScoreboardTeam.getMethod("setPlayerSuffix", IChatBaseComponent);
        PacketPlayOutScoreboardTeamStorage.ScoreboardTeam_setNameTagVisibility = PacketPlayOutScoreboardTeamStorage.ScoreboardTeam.getMethod("setNameTagVisibility", PacketPlayOutScoreboardTeamStorage.EnumNameTagVisibility);
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
        PacketPlayOutPlayerListHeaderFooterStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.ClientboundTabListPacket");

        // DataWatcher
        DataWatcher.CLASS = Class.forName("net.minecraft.network.syncher.SynchedEntityData");
        DataWatcherItem.CLASS = Class.forName("net.minecraft.network.syncher.SynchedEntityData$DataItem");
        DataWatcherObject.CLASS = Class.forName("net.minecraft.network.syncher.EntityDataAccessor");
        DataWatcherHelper.DataWatcherRegistry = Class.forName("net.minecraft.network.syncher.EntityDataSerializers");
        DataWatcherHelper.DataWatcherSerializer = Class.forName("net.minecraft.network.syncher.EntityDataSerializer");
        if (is1_19_3Plus()) {
            DataWatcher.DataValue = Class.forName("net.minecraft.network.syncher.SynchedEntityData$DataValue");
        }

        // Entities
        if (minorVersion >= 19) {
            PacketPlayOutSpawnEntityLivingStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.ClientboundAddEntityPacket");
        } else {
            PacketPlayOutSpawnEntityLivingStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.ClientboundAddMobPacket");
        }
        PacketPlayOutEntityTeleportStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket");
        PacketPlayInUseEntity = Class.forName("net.minecraft.network.protocol.game.ServerboundInteractPacket");
        PacketPlayInUseEntity$d = Class.forName("net.minecraft.network.protocol.game.ServerboundInteractPacket$InteractionAction");
        PacketPlayOutEntity = Class.forName("net.minecraft.network.protocol.game.ClientboundMoveEntityPacket");
        PacketPlayOutEntityDestroyStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket");
        PacketPlayOutEntityLook = Class.forName("net.minecraft.network.protocol.game.ClientboundMoveEntityPacket$Rot");
        PacketPlayOutEntityMetadataStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket");
        PacketPlayOutNamedEntitySpawn = Class.forName("net.minecraft.network.protocol.game.ClientboundAddPlayerPacket");
        EnumEntityUseAction = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.ServerboundInteractPacket$Action");
        if (minorVersion >= 19) {
            PacketPlayOutSpawnEntityLivingStorage.EntityTypes = Class.forName("net.minecraft.world.entity.EntityType");
        }

        // Player Info
        if (minorVersion >= 19) {
            PacketPlayOutPlayerInfoStorage.ProfilePublicKey$a = Class.forName("net.minecraft.world.entity.player.ProfilePublicKey$Data");
        }
        if (is1_19_3Plus()) {
            PacketPlayOutPlayerInfoStorage.ClientboundPlayerInfoRemovePacket = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket");
            PacketPlayOutPlayerInfoStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");
            PacketPlayOutPlayerInfoStorage.EnumPlayerInfoActionClass = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action");
            PacketPlayOutPlayerInfoStorage.PlayerInfoDataStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Entry");
            PacketPlayOutPlayerInfoStorage.RemoteChatSession$Data = Class.forName("net.minecraft.network.chat.RemoteChatSession$Data");
        } else {
            PacketPlayOutPlayerInfoStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket");
            PacketPlayOutPlayerInfoStorage.EnumPlayerInfoActionClass = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket$Action");
            PacketPlayOutPlayerInfoStorage.PlayerInfoDataStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket$PlayerUpdate");
        }
        PacketPlayOutPlayerInfoStorage.EnumGamemodeClass = (Class<Enum>) Class.forName("net.minecraft.world.level.GameType");

        // Scoreboard
        PacketPlayOutScoreboardDisplayObjectiveStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket");
        PacketPlayOutScoreboardObjectiveStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.ClientboundSetObjectivePacket");
        Scoreboard = Class.forName("net.minecraft.world.scores.Scoreboard");
        PacketPlayOutScoreboardScoreStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.ClientboundSetScorePacket");
        PacketPlayOutScoreboardObjectiveStorage.ScoreboardObjective = Class.forName("net.minecraft.world.scores.Objective");
        PacketPlayOutScoreboardScoreStorage.ScoreboardScore = Class.forName("net.minecraft.world.scores.Score");
        IScoreboardCriteria = Class.forName("net.minecraft.world.scores.criteria.ObjectiveCriteria");
        PacketPlayOutScoreboardObjectiveStorage.EnumScoreboardHealthDisplay = (Class<Enum>) Class.forName("net.minecraft.world.scores.criteria.ObjectiveCriteria$RenderType");
        PacketPlayOutScoreboardScoreStorage.EnumScoreboardAction = (Class<Enum>) Class.forName("net.minecraft.server.ServerScoreboard$Method");
        PacketPlayOutScoreboardTeamStorage.CLASS = Class.forName("net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket");
        PacketPlayOutScoreboardTeamStorage.ScoreboardTeam = Class.forName("net.minecraft.world.scores.PlayerTeam");
        PacketPlayOutScoreboardTeamStorage.EnumNameTagVisibility = (Class<Enum>) Class.forName("net.minecraft.world.scores.Team$Visibility");
        PacketPlayOutScoreboardTeamStorage.EnumTeamPush = (Class<Enum>) Class.forName("net.minecraft.world.scores.Team$CollisionRule");
        PacketPlayOutScoreboardTeamStorage.PlayerAction = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket$Action");
    }
}
