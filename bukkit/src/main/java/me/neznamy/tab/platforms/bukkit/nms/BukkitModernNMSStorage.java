package me.neznamy.tab.platforms.bukkit.nms;

/**
 * NMS loader for minecraft 1.17+ using Mojang packaging and bukkit names.
 */
public class BukkitModernNMSStorage extends ModernNMSStorage {

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
        ChatSerializer_DESERIALIZE = getMethod(ChatSerializer, "a", String.class);
        DataWatcher_REGISTER = getMethod(DataWatcher, new String[]{"register", "a"}, DataWatcherObject, Object.class); // {Bukkit, Bukkit 1.18+}
        ScoreboardScore_setScore = getMethod(ScoreboardScore, new String[]{"setScore", "b"}, int.class); // {Bukkit, Bukkit 1.18+}
        ScoreboardTeam_setAllowFriendlyFire = getMethod(ScoreboardTeam, new String[]{"setAllowFriendlyFire", "a"}, boolean.class); // {Bukkit, Bukkit 1.18+}
        ScoreboardTeam_setCanSeeFriendlyInvisibles = getMethod(ScoreboardTeam, new String[]{"setCanSeeFriendlyInvisibles", "b"}, boolean.class); // {Bukkit, Bukkit 1.18+}
        ScoreboardTeam_setPrefix = getMethod(ScoreboardTeam, new String[]{"setPrefix", "b"}, IChatBaseComponent); // {Bukkit, Bukkit 1.18+}
        ScoreboardTeam_setSuffix = getMethod(ScoreboardTeam, new String[]{"setSuffix", "c"}, IChatBaseComponent); // {Bukkit, Bukkit 1.18+}
        ScoreboardTeam_setNameTagVisibility = getMethod(ScoreboardTeam, new String[]{"setNameTagVisibility", "a"}, EnumNameTagVisibility); // {Bukkit, Bukkit 1.18+}
        if (minorVersion >= 19) {
            Registry_a = Registry.getMethod("a", int.class);
            IRegistry_X = IRegistry.getDeclaredField("X").get(null);
            PacketPlayOutSpawnEntityLiving_ENTITYTYPE = getField(PacketPlayOutSpawnEntityLiving, "e");
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
        EnumChatFormat = Class.forName("net.minecraft.EnumChatFormat");
        EntityPlayer = Class.forName("net.minecraft.server.level.EntityPlayer");
        Entity = Class.forName("net.minecraft.world.entity.Entity");
        EntityLiving = Class.forName("net.minecraft.world.entity.EntityLiving");
        PlayerConnection = Class.forName("net.minecraft.server.network.PlayerConnection");
        PacketPlayOutPlayerListHeaderFooter = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutPlayerListHeaderFooter");
        PacketPlayOutChat = getModernClass("net.minecraft.network.protocol.game.ClientboundSystemChatPacket",
                "net.minecraft.network.protocol.game.PacketPlayOutChat");
        if (minorVersion < 19) {
            ChatMessageType = Class.forName("net.minecraft.network.chat.ChatMessageType");
        }

        // DataWatcher
        DataWatcher = Class.forName("net.minecraft.network.syncher.DataWatcher");
        DataWatcherItem = Class.forName("net.minecraft.network.syncher.DataWatcher$Item");
        DataWatcherObject = Class.forName("net.minecraft.network.syncher.DataWatcherObject");
        DataWatcherRegistry = Class.forName("net.minecraft.network.syncher.DataWatcherRegistry");
        DataWatcherSerializer = Class.forName("net.minecraft.network.syncher.DataWatcherSerializer");

        // Entities
        PacketPlayOutSpawnEntityLiving = getModernClass("net.minecraft.network.protocol.game.PacketPlayOutSpawnEntityLiving",
                "net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity");
        PacketPlayOutEntityTeleport = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport");
        PacketPlayInUseEntity = Class.forName("net.minecraft.network.protocol.game.PacketPlayInUseEntity");
        PacketPlayInUseEntity$d = Class.forName("net.minecraft.network.protocol.game.PacketPlayInUseEntity$d");
        PacketPlayOutEntity = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntity");
        PacketPlayOutEntityDestroy = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy");
        PacketPlayOutEntityLook = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntity$PacketPlayOutEntityLook");
        PacketPlayOutEntityMetadata = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata");
        PacketPlayOutNamedEntitySpawn = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn");
        EnumEntityUseAction = Class.forName("net.minecraft.network.protocol.game.PacketPlayInUseEntity$EnumEntityUseAction");
        if (minorVersion >= 19) {
            Registry = Class.forName("net.minecraft.core.Registry");
            IRegistry = Class.forName("net.minecraft.core.IRegistry");
            ProfilePublicKey = Class.forName("net.minecraft.world.entity.player.ProfilePublicKey");
            ProfilePublicKey$a = Class.forName("net.minecraft.world.entity.player.ProfilePublicKey$a");
        }

        // Player Info
        PacketPlayOutPlayerInfo = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo");
        EnumPlayerInfoAction = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
        PlayerInfoData = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo$PlayerInfoData");
        EnumGamemode = Class.forName("net.minecraft.world.level.EnumGamemode");

        // Scoreboard
        PacketPlayOutScoreboardDisplayObjective = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardDisplayObjective");
        PacketPlayOutScoreboardObjective = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardObjective");
        Scoreboard = Class.forName("net.minecraft.world.scores.Scoreboard");
        PacketPlayOutScoreboardScore = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore");
        ScoreboardObjective = Class.forName("net.minecraft.world.scores.ScoreboardObjective");
        ScoreboardScore = Class.forName("net.minecraft.world.scores.ScoreboardScore");
        IScoreboardCriteria = Class.forName("net.minecraft.world.scores.criteria.IScoreboardCriteria");
        EnumScoreboardHealthDisplay = Class.forName("net.minecraft.world.scores.criteria.IScoreboardCriteria$EnumScoreboardHealthDisplay");
        EnumScoreboardAction = Class.forName("net.minecraft.server.ScoreboardServer$Action");
        PacketPlayOutScoreboardTeam = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam");
        ScoreboardTeam = Class.forName("net.minecraft.world.scores.ScoreboardTeam");
        EnumNameTagVisibility = Class.forName("net.minecraft.world.scores.ScoreboardTeamBase$EnumNameTagVisibility");
        EnumTeamPush = Class.forName("net.minecraft.world.scores.ScoreboardTeamBase$EnumTeamPush");
        PacketPlayOutScoreboardTeam_PlayerAction = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam$a");
    }
}
