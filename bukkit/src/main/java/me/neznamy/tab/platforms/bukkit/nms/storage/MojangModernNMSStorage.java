package me.neznamy.tab.platforms.bukkit.nms.storage;

/**
 * NMS loader for minecraft 1.17+ using Mojang packaging and names.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class MojangModernNMSStorage extends NMSStorage {

    /**
     * Creates new instance, initializes required NMS classes and fields
     *
     * @throws  ReflectiveOperationException
     *          If any class, field or method fails to load
     */
    public MojangModernNMSStorage() throws ReflectiveOperationException {
    }

    @Override
    public void loadNamedFieldsAndMethods() throws ReflectiveOperationException {
        PING = getField(EntityPlayer, "latency");
        ChatSerializer_DESERIALIZE = ChatSerializer.getMethod("fromJson", String.class);
        DataWatcher_REGISTER = DataWatcher.getMethod("define", DataWatcherObject, Object.class);
        ScoreboardScore_setScore = ScoreboardScore.getMethod("setScore", int.class);
        ScoreboardTeam_setAllowFriendlyFire = ScoreboardTeam.getMethod("setAllowFriendlyFire", boolean.class);
        ScoreboardTeam_setCanSeeFriendlyInvisibles = ScoreboardTeam.getMethod("setSeeFriendlyInvisibles", boolean.class);
        ScoreboardTeam_setPrefix = ScoreboardTeam.getMethod("setPlayerPrefix", IChatBaseComponent);
        ScoreboardTeam_setSuffix = ScoreboardTeam.getMethod("setPlayerSuffix", IChatBaseComponent);
        ScoreboardTeam_setNameTagVisibility = ScoreboardTeam.getMethod("setNameTagVisibility", EnumNameTagVisibility);
        if (minorVersion >= 19) {
            EntityTypes_ARMOR_STAND = EntityTypes.getDeclaredField("ARMOR_STAND").get(null);
            PacketPlayOutSpawnEntityLiving_ENTITYTYPE = getField(PacketPlayOutSpawnEntityLiving, "type");
            DataWatcher_b = DataWatcher.getMethod("packDirty");
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
        PacketPlayOutPlayerListHeaderFooter = Class.forName("net.minecraft.network.protocol.game.ClientboundTabListPacket");
        if (minorVersion >= 19) {
            PacketPlayOutChat = Class.forName("net.minecraft.network.protocol.game.ClientboundSystemChatPacket");
        } else {
            PacketPlayOutChat = Class.forName("net.minecraft.network.protocol.game.ClientboundChatPacket");
            ChatMessageType = (Class<Enum>) Class.forName("net.minecraft.network.chat.ChatType");
        }

        // DataWatcher
        DataWatcher = Class.forName("net.minecraft.network.syncher.SynchedEntityData");
        DataWatcherItem = Class.forName("net.minecraft.network.syncher.SynchedEntityData$DataItem");
        DataWatcherObject = Class.forName("net.minecraft.network.syncher.EntityDataAccessor");
        DataWatcherRegistry = Class.forName("net.minecraft.network.syncher.EntityDataSerializers");
        DataWatcherSerializer = Class.forName("net.minecraft.network.syncher.EntityDataSerializer");
        if (is1_19_3Plus()) {
            DataWatcher$DataValue = Class.forName("net.minecraft.network.syncher.SynchedEntityData$DataValue");
        }

        // Entities
        if (minorVersion >= 19) {
            PacketPlayOutSpawnEntityLiving = Class.forName("net.minecraft.network.protocol.game.ClientboundAddEntityPacket");
        } else {
            PacketPlayOutSpawnEntityLiving = Class.forName("net.minecraft.network.protocol.game.ClientboundAddMobPacket");
        }
        PacketPlayOutEntityTeleport = Class.forName("net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket");
        PacketPlayInUseEntity = Class.forName("net.minecraft.network.protocol.game.ServerboundInteractPacket");
        PacketPlayInUseEntity$d = Class.forName("net.minecraft.network.protocol.game.ServerboundInteractPacket$InteractionAction");
        PacketPlayOutEntity = Class.forName("net.minecraft.network.protocol.game.ClientboundMoveEntityPacket");
        PacketPlayOutEntityDestroy = Class.forName("net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket");
        PacketPlayOutEntityLook = Class.forName("net.minecraft.network.protocol.game.ClientboundMoveEntityPacket$Rot");
        PacketPlayOutEntityMetadata = Class.forName("net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket");
        PacketPlayOutNamedEntitySpawn = Class.forName("net.minecraft.network.protocol.game.ClientboundAddPlayerPacket");
        EnumEntityUseAction = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.ServerboundInteractPacket$Action");
        if (minorVersion >= 19) {
            EntityTypes = Class.forName("net.minecraft.world.entity.EntityType");
        }

        // Player Info
        if (minorVersion >= 19) {
            ProfilePublicKey = Class.forName("net.minecraft.world.entity.player.ProfilePublicKey");
            ProfilePublicKey$a = Class.forName("net.minecraft.world.entity.player.ProfilePublicKey$Data");
        }
        if (is1_19_3Plus()) {
            ClientboundPlayerInfoRemovePacket = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket");
            PacketPlayOutPlayerInfo = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");
            EnumPlayerInfoAction = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action");
            PlayerInfoData = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Entry");
            RemoteChatSession = Class.forName("net.minecraft.network.chat.RemoteChatSession");
            RemoteChatSession$Data = Class.forName("net.minecraft.network.chat.RemoteChatSession$Data");
        } else {
            PacketPlayOutPlayerInfo = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket");
            EnumPlayerInfoAction = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket$Action");
            PlayerInfoData = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket$PlayerUpdate");
        }
        EnumGamemode = (Class<Enum>) Class.forName("net.minecraft.world.level.GameType");

        // Scoreboard
        PacketPlayOutScoreboardDisplayObjective = Class.forName("net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket");
        PacketPlayOutScoreboardObjective = Class.forName("net.minecraft.network.protocol.game.ClientboundSetObjectivePacket");
        Scoreboard = Class.forName("net.minecraft.world.scores.Scoreboard");
        PacketPlayOutScoreboardScore = Class.forName("net.minecraft.network.protocol.game.ClientboundSetScorePacket");
        ScoreboardObjective = Class.forName("net.minecraft.world.scores.Objective");
        ScoreboardScore = Class.forName("net.minecraft.world.scores.Score");
        IScoreboardCriteria = Class.forName("net.minecraft.world.scores.criteria.ObjectiveCriteria");
        EnumScoreboardHealthDisplay = (Class<Enum>) Class.forName("net.minecraft.world.scores.criteria.ObjectiveCriteria$RenderType");
        EnumScoreboardAction = (Class<Enum>) Class.forName("net.minecraft.server.ServerScoreboard$Method");
        PacketPlayOutScoreboardTeam = Class.forName("net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket");
        ScoreboardTeam = Class.forName("net.minecraft.world.scores.PlayerTeam");
        EnumNameTagVisibility = (Class<Enum>) Class.forName("net.minecraft.world.scores.Team$Visibility");
        EnumTeamPush = (Class<Enum>) Class.forName("net.minecraft.world.scores.Team$CollisionRule");
        PacketPlayOutScoreboardTeam_PlayerAction = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket$Action");
    }
}
