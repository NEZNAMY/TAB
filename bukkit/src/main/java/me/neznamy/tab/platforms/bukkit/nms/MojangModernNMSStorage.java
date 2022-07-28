package me.neznamy.tab.platforms.bukkit.nms;

/**
 * NMS loader for minecraft 1.17+ using Mojang packaging and names.
 */
public class MojangModernNMSStorage extends ModernNMSStorage {

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
        ChatSerializer_DESERIALIZE = getMethod(ChatSerializer, "fromJson", String.class);
        DataWatcher_REGISTER = getMethod(DataWatcher, "define", DataWatcherObject, Object.class);
        ScoreboardScore_setScore = getMethod(ScoreboardScore, "setScore", int.class);
        ScoreboardTeam_setAllowFriendlyFire = getMethod(ScoreboardTeam, "setAllowFriendlyFire", boolean.class);
        ScoreboardTeam_setCanSeeFriendlyInvisibles = getMethod(ScoreboardTeam, "setSeeFriendlyInvisibles", boolean.class);
        ScoreboardTeam_setPrefix = getMethod(ScoreboardTeam, "setPlayerPrefix", IChatBaseComponent);
        ScoreboardTeam_setSuffix = getMethod(ScoreboardTeam, "setPlayerSuffix", IChatBaseComponent);
        ScoreboardTeam_setNameTagVisibility = getMethod(ScoreboardTeam, "setNameTagVisibility", EnumNameTagVisibility);
        if (minorVersion >= 19) {
            Registry_a = Registry.getMethod("byId", int.class);
            IRegistry_X = IRegistry.getDeclaredField("ENTITY_TYPE").get(null);
            PacketPlayOutSpawnEntityLiving_ENTITYTYPE = getField(PacketPlayOutSpawnEntityLiving, "type");
        }
    }

    @Override
    public void loadClasses() throws ClassNotFoundException {
        IChatBaseComponent = Class.forName("net.minecraft.network.chat.ComponentContents");
        ChatSerializer = Class.forName("net.minecraft.network.chat.Component$Serializer");
        World = Class.forName("net.minecraft.world.level.Level");
        EntityArmorStand = Class.forName("net.minecraft.world.entity.decoration.ArmorStand");
        EntityHuman = Class.forName("net.minecraft.world.entity.player.Player");
        NetworkManager = Class.forName("net.minecraft.network.Connection");
        Packet = Class.forName("net.minecraft.network.protocol.Packet");
        EnumChatFormat = Class.forName("net.minecraft.ChatFormatting");
        EntityPlayer = Class.forName("net.minecraft.server.level.ServerPlayer");
        Entity = Class.forName("net.minecraft.world.entity.Entity");
        EntityLiving = Class.forName("net.minecraft.world.entity.LivingEntity");
        PlayerConnection = Class.forName("net.minecraft.server.network.ServerGamePacketListenerImpl");
        PacketPlayOutPlayerListHeaderFooter = Class.forName("net.minecraft.network.protocol.game.ClientboundTabListPacket");
        PacketPlayOutChat = getModernClass("net.minecraft.network.protocol.game.ClientboundSystemChatPacket",
                "net.minecraft.network.protocol.game.ClientboundChatPacket");
        if (minorVersion < 19) {
            ChatMessageType = Class.forName("net.minecraft.network.chat.ChatType");
        }

        // DataWatcher
        DataWatcher = Class.forName("net.minecraft.network.syncher.SynchedEntityData");
        DataWatcherItem = Class.forName("net.minecraft.network.syncher.SynchedEntityData$DataItem");
        DataWatcherObject = Class.forName("net.minecraft.network.syncher.EntityDataAccessor");
        DataWatcherRegistry = Class.forName("net.minecraft.network.syncher.EntityDataSerializers");
        DataWatcherSerializer = Class.forName("net.minecraft.network.syncher.EntityDataSerializer");

        // Entities
        PacketPlayOutSpawnEntityLiving = getModernClass("net.minecraft.network.protocol.game.ClientboundAddMobPacket",
                "net.minecraft.network.protocol.game.ClientboundAddEntityPacket");
        PacketPlayOutEntityTeleport = Class.forName("net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket");
        PacketPlayInUseEntity = Class.forName("net.minecraft.network.protocol.game.ServerboundInteractPacket");
        PacketPlayInUseEntity$d = Class.forName("net.minecraft.network.protocol.game.ServerboundInteractPacket$InteractionAction");
        PacketPlayOutEntity = Class.forName("net.minecraft.network.protocol.game.ClientboundMoveEntityPacket");
        PacketPlayOutEntityDestroy = Class.forName("net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket");
        PacketPlayOutEntityLook = Class.forName("net.minecraft.network.protocol.game.ClientboundMoveEntityPacket$Rot");
        PacketPlayOutEntityMetadata = Class.forName("net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket");
        PacketPlayOutNamedEntitySpawn = Class.forName("net.minecraft.network.protocol.game.ClientboundAddPlayerPacket");
        EnumEntityUseAction = Class.forName("net.minecraft.network.protocol.game.ServerboundInteractPacket$Action");
        if (minorVersion >= 19) {
            Registry = Class.forName("net.minecraft.core.IdMap");
            IRegistry = Class.forName("net.minecraft.core.Registry");
        }

        // Player Info
        PacketPlayOutPlayerInfo = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket");
        EnumPlayerInfoAction = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket$Action");
        PlayerInfoData = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket$PlayerUpdate");
        EnumGamemode = Class.forName("net.minecraft.world.level.GameType");

        // Scoreboard
        PacketPlayOutScoreboardDisplayObjective = Class.forName("net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket");
        PacketPlayOutScoreboardObjective = Class.forName("net.minecraft.network.protocol.game.ClientboundSetObjectivePacket");
        Scoreboard = Class.forName("net.minecraft.world.scores.Scoreboard");
        PacketPlayOutScoreboardScore = Class.forName("net.minecraft.network.protocol.game.ClientboundSetScorePacket");
        ScoreboardObjective = Class.forName("net.minecraft.world.scores.Objective");
        ScoreboardScore = Class.forName("net.minecraft.world.scores.Score");
        IScoreboardCriteria = Class.forName("net.minecraft.world.scores.criteria.ObjectiveCriteria");
        EnumScoreboardHealthDisplay = Class.forName("net.minecraft.world.scores.criteria.ObjectiveCriteria$RenderType");
        EnumScoreboardAction = Class.forName("net.minecraft.server.ServerScoreboard$Method");
        PacketPlayOutScoreboardTeam = Class.forName("net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket");
        ScoreboardTeam = Class.forName("net.minecraft.world.scores.PlayerTeam");
        EnumNameTagVisibility = Class.forName("net.minecraft.world.scores.Team$Visibility");
        EnumTeamPush = Class.forName("net.minecraft.world.scores.Team$CollisionRule");
        PacketPlayOutScoreboardTeam_PlayerAction = Class.forName("net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket$Action");
    }
}
