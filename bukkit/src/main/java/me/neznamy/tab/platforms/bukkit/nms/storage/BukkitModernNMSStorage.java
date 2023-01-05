package me.neznamy.tab.platforms.bukkit.nms.storage;

/**
 * NMS loader for minecraft 1.17+ using Mojang packaging and bukkit names.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class BukkitModernNMSStorage extends NMSStorage {

    /**
     * Creates new instance, initializes required NMS classes and fields
     *
     * @throws  ReflectiveOperationException
     *          If any class, field or method fails to load
     */
    public BukkitModernNMSStorage() throws ReflectiveOperationException {
    }

    @Override
    public void loadNamedFieldsAndMethods() throws ReflectiveOperationException {
        PING = getField(EntityPlayer, "e");
        ChatSerializer_DESERIALIZE = ChatSerializer.getMethod("a", String.class);
        DataWatcher_REGISTER = getMethod(DataWatcher, new String[]{"register", "a"}, DataWatcherObject, Object.class); // {Bukkit, Bukkit 1.18+}
        ScoreboardScore_setScore = getMethod(ScoreboardScore, new String[]{"setScore", "b"}, int.class); // {Bukkit, Bukkit 1.18+}
        ScoreboardTeam_setAllowFriendlyFire = getMethod(ScoreboardTeam, new String[]{"setAllowFriendlyFire", "a"}, boolean.class); // {Bukkit, Bukkit 1.18+}
        ScoreboardTeam_setCanSeeFriendlyInvisibles = getMethod(ScoreboardTeam, new String[]{"setCanSeeFriendlyInvisibles", "b"}, boolean.class); // {Bukkit, Bukkit 1.18+}
        ScoreboardTeam_setPrefix = getMethod(ScoreboardTeam, new String[]{"setPrefix", "b"}, IChatBaseComponent); // {Bukkit, Bukkit 1.18+}
        ScoreboardTeam_setSuffix = getMethod(ScoreboardTeam, new String[]{"setSuffix", "c"}, IChatBaseComponent); // {Bukkit, Bukkit 1.18+}
        ScoreboardTeam_setNameTagVisibility = getMethod(ScoreboardTeam, new String[]{"setNameTagVisibility", "a"}, EnumNameTagVisibility); // {Bukkit, Bukkit 1.18+}
        if (minorVersion >= 19) {
            EntityTypes_ARMOR_STAND = EntityTypes.getDeclaredField("d").get(null);
            PacketPlayOutSpawnEntityLiving_ENTITYTYPE = getField(PacketPlayOutSpawnEntityLiving, "e");
            DataWatcher_b = DataWatcher.getMethod("b");
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

        PacketPlayOutPlayerListHeaderFooter = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutPlayerListHeaderFooter");
        if (minorVersion >= 19) {
            PacketPlayOutChat = Class.forName("net.minecraft.network.protocol.game.ClientboundSystemChatPacket");
        } else {
            PacketPlayOutChat = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutChat");
            ChatMessageType = (Class<Enum>) Class.forName("net.minecraft.network.chat.ChatMessageType");
        }

        // DataWatcher
        DataWatcher = Class.forName("net.minecraft.network.syncher.DataWatcher");
        DataWatcherItem = Class.forName("net.minecraft.network.syncher.DataWatcher$Item");
        DataWatcherObject = Class.forName("net.minecraft.network.syncher.DataWatcherObject");
        DataWatcherRegistry = Class.forName("net.minecraft.network.syncher.DataWatcherRegistry");
        DataWatcherSerializer = Class.forName("net.minecraft.network.syncher.DataWatcherSerializer");
        if (is1_19_3Plus()) {
            DataWatcher$DataValue = Class.forName("net.minecraft.network.syncher.DataWatcher$b");
        }

        // Entities
        if (minorVersion >= 19) {
            PacketPlayOutSpawnEntityLiving = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity");
        } else {
            PacketPlayOutSpawnEntityLiving = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutSpawnEntityLiving");
        }
        PacketPlayOutEntityTeleport = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport");
        PacketPlayInUseEntity = Class.forName("net.minecraft.network.protocol.game.PacketPlayInUseEntity");
        PacketPlayInUseEntity$d = Class.forName("net.minecraft.network.protocol.game.PacketPlayInUseEntity$d");
        PacketPlayOutEntity = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntity");
        PacketPlayOutEntityDestroy = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy");
        PacketPlayOutEntityLook = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntity$PacketPlayOutEntityLook");
        PacketPlayOutEntityMetadata = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata");
        PacketPlayOutNamedEntitySpawn = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn");
        EnumEntityUseAction = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.PacketPlayInUseEntity$EnumEntityUseAction");
        if (minorVersion >= 19) {
            EntityTypes = Class.forName("net.minecraft.world.entity.EntityTypes");
        }

        // Player Info
        if (minorVersion >= 19) {
            ProfilePublicKey = Class.forName("net.minecraft.world.entity.player.ProfilePublicKey");
            ProfilePublicKey$a = Class.forName("net.minecraft.world.entity.player.ProfilePublicKey$a");
        }
        if (is1_19_3Plus()) {
            //1.19.3+
            ClientboundPlayerInfoRemovePacket = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket");
            PacketPlayOutPlayerInfo = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");
            EnumPlayerInfoAction = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$a");
            PlayerInfoData = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$b");
            RemoteChatSession = Class.forName("net.minecraft.network.chat.RemoteChatSession");
            RemoteChatSession$Data = Class.forName("net.minecraft.network.chat.RemoteChatSession$a");
        } else {
            PacketPlayOutPlayerInfo = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo");
            EnumPlayerInfoAction = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
            PlayerInfoData = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo$PlayerInfoData");
        }
        EnumGamemode = (Class<Enum>) Class.forName("net.minecraft.world.level.EnumGamemode");

        // Scoreboard
        PacketPlayOutScoreboardDisplayObjective = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardDisplayObjective");
        PacketPlayOutScoreboardObjective = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardObjective");
        Scoreboard = Class.forName("net.minecraft.world.scores.Scoreboard");
        PacketPlayOutScoreboardScore = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore");
        ScoreboardObjective = Class.forName("net.minecraft.world.scores.ScoreboardObjective");
        ScoreboardScore = Class.forName("net.minecraft.world.scores.ScoreboardScore");
        IScoreboardCriteria = Class.forName("net.minecraft.world.scores.criteria.IScoreboardCriteria");
        EnumScoreboardHealthDisplay = (Class<Enum>) Class.forName("net.minecraft.world.scores.criteria.IScoreboardCriteria$EnumScoreboardHealthDisplay");
        EnumScoreboardAction = (Class<Enum>) Class.forName("net.minecraft.server.ScoreboardServer$Action");
        PacketPlayOutScoreboardTeam = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam");
        ScoreboardTeam = Class.forName("net.minecraft.world.scores.ScoreboardTeam");
        EnumNameTagVisibility = (Class<Enum>) Class.forName("net.minecraft.world.scores.ScoreboardTeamBase$EnumNameTagVisibility");
        EnumTeamPush = (Class<Enum>) Class.forName("net.minecraft.world.scores.ScoreboardTeamBase$EnumTeamPush");
        PacketPlayOutScoreboardTeam_PlayerAction = (Class<Enum>) Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam$a");
    }
}
