package me.neznamy.tab.platforms.bukkit.scoreboard.packet;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard.Team;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.Scoreboard.TeamAction;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.BiConsumerWithException;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Class storing all team related fields and methods.
 */
@SuppressWarnings("unchecked")
public class TeamPacketData {

    /** Flag tracking if server uses modern team data using components */
    private final boolean MODERN_TEAM_DATA_VERSION = BukkitReflection.getMinorVersion() >= 13;

    /** Flag tracking if server uses static constructor-like methods */
    private final boolean STATIC_CONSTRUCTOR_VERSION = BukkitReflection.getMinorVersion() >= 17;

    private final Object emptyScoreboard;
    @Getter private final Class<?> TeamPacketClass;
    private Constructor<?> newTeamPacket;
    private final Constructor<?> newScoreboardTeam;
    private Method TeamPacketConstructor_of;
    private Method TeamPacketConstructor_ofBoolean;
    private final Field TeamPacket_NAME;
    private final Field TeamPacket_ACTION;
    private final Field TeamPacket_PLAYERS;
    private final Method ScoreboardTeam_setPrefix;
    private final Method ScoreboardTeam_setSuffix;
    private Method ScoreboardTeam_setColor;
    private final Method ScoreboardTeam_setAllowFriendlyFire;
    private final Method ScoreboardTeam_setCanSeeFriendlyInvisibles;
    private final Enum<?>[] chatFormats;

    private BiConsumerWithException<Object, Scoreboard.NameVisibility> setVisibility = (team, visibility) -> {};
    private BiConsumerWithException<Object, Scoreboard.CollisionRule> setCollision = (team, collision) -> {};

    /**
     * Constructs new instance and loads all required NMS classes, fields and methods.
     * If anything fails, exception is thrown.
     *
     * @throws  ReflectiveOperationException
     *          If anything fails
     */
    public TeamPacketData() throws ReflectiveOperationException {
        int minorVersion = BukkitReflection.getMinorVersion();
        Class<?> Scoreboard = BukkitReflection.getClass("world.scores.Scoreboard", "Scoreboard");
        Class<?> scoreboardTeam = BukkitReflection.getClass("world.scores.PlayerTeam", "world.scores.ScoreboardTeam", "ScoreboardTeam");
        Class<?> enumChatFormatClass = BukkitReflection.getClass("ChatFormatting", "EnumChatFormat");
        TeamPacketClass = BukkitReflection.getClass(
                "network.protocol.game.ClientboundSetPlayerTeamPacket", // Mojang mapped
                "network.protocol.game.PacketPlayOutScoreboardTeam", // Bukkit 1.17+
                "PacketPlayOutScoreboardTeam", // Bukkit 1.7 - 1.16.5
                "Packet209SetScoreboardTeam" // 1.5 - 1.6.4
        );
        emptyScoreboard = Scoreboard.getConstructor().newInstance();
        newScoreboardTeam = scoreboardTeam.getConstructor(Scoreboard, String.class);
        TeamPacket_NAME = ReflectionUtils.getFields(TeamPacketClass, String.class).get(0);
        List<Field> intFields = ReflectionUtils.getInstanceFields(TeamPacketClass, int.class);
        if (minorVersion >= 8 && minorVersion <= 12) {
            TeamPacket_ACTION = intFields.get(1);
        } else {
            TeamPacket_ACTION = intFields.get(0);
        }
        TeamPacket_PLAYERS = ReflectionUtils.getOnlyField(TeamPacketClass, Collection.class);
        chatFormats = (Enum<?>[]) enumChatFormatClass.getMethod("values").invoke(null);
        ScoreboardTeam_setAllowFriendlyFire = ReflectionUtils.getMethod(
                scoreboardTeam,
                new String[]{"func_96660_a", "setAllowFriendlyFire", "a", "m_83355_"}, // {Thermos, 1.5.1+, 1.5 & 1.18+, Mohist 1.18.2}
                boolean.class
        );
        ScoreboardTeam_setCanSeeFriendlyInvisibles = ReflectionUtils.getMethod(
                scoreboardTeam,
                new String[]{"func_98300_b", "setCanSeeFriendlyInvisibles", "b", "m_83362_", "setSeeFriendlyInvisibles"}, // {Thermos, 1.5.1+, 1.5 & 1.18+, Mohist 1.18.2, 1.20.2+}
                boolean.class
        );
        if (minorVersion >= 8) loadVisibility(scoreboardTeam);
        if (minorVersion >= 9) loadCollision(scoreboardTeam);
        if (MODERN_TEAM_DATA_VERSION) {
            Class<?> Component = BukkitReflection.getClass("network.chat.Component", "network.chat.IChatBaseComponent", "IChatBaseComponent");
            ScoreboardTeam_setColor = ReflectionUtils.getOnlyMethod(scoreboardTeam, void.class, enumChatFormatClass);
            ScoreboardTeam_setPrefix = ReflectionUtils.getMethod(
                    scoreboardTeam,
                    new String[]{"setPrefix", "b", "m_83360_", "setPlayerPrefix"}, // {1.17.1-, 1.18 - 1.20.1, Mohist 1.18.2, 1.20.2+}
                    Component
            );
            ScoreboardTeam_setSuffix = ReflectionUtils.getMethod(
                    scoreboardTeam,
                    new String[]{"setSuffix", "c", "m_83365_", "setPlayerSuffix"}, // {1.17.1-, 1.18 - 1.20.1, Mohist 1.18.2, 1.20.2+}
                    Component
            );
        } else {
            ScoreboardTeam_setPrefix = ReflectionUtils.getMethod(
                    scoreboardTeam,
                    new String[]{"func_96666_b", "setPrefix", "b"}, // {Thermos, 1.5.1+, 1.5}
                    String.class
            );
            ScoreboardTeam_setSuffix = ReflectionUtils.getMethod(
                    scoreboardTeam,
                    new String[]{"func_96662_c", "setSuffix", "c"}, // {Thermos, 1.5.1+, 1.5}
                    String.class
            );
        }
        if (STATIC_CONSTRUCTOR_VERSION) {
            TeamPacketConstructor_of = ReflectionUtils.getOnlyMethod(TeamPacketClass, TeamPacketClass, scoreboardTeam);
            TeamPacketConstructor_ofBoolean = ReflectionUtils.getOnlyMethod(TeamPacketClass, TeamPacketClass, scoreboardTeam, boolean.class);
        } else {
            newTeamPacket = TeamPacketClass.getConstructor(scoreboardTeam, int.class);
        }
    }

    @SneakyThrows
    private void loadVisibility(@NonNull Class<?> scoreboardTeam) {
        Class<?> enumNameTagVisibility = BukkitReflection.getClass(
                "world.scores.Team$Visibility", // Mojang mapped
                "world.scores.ScoreboardTeamBase$EnumNameTagVisibility", // Bukkit 1.17+
                "ScoreboardTeamBase$EnumNameTagVisibility", // Bukkit 1.8.1 - 1.16.5
                "EnumNameTagVisibility" // Bukkit 1.8.0
        );
        Enum<?>[] nameVisibilities = (Enum<?>[]) enumNameTagVisibility.getMethod("values").invoke(null);
        Method setNameTagVisibility = ReflectionUtils.getMethod(
                scoreboardTeam,
                new String[]{"setNameTagVisibility", "a", "m_83346_"}, // {1.8.1+, 1.8 & 1.18+, Mohist 1.18.2}
                enumNameTagVisibility
        );
        setVisibility = (team, visibility) -> setNameTagVisibility.invoke(team, nameVisibilities[visibility.ordinal()]);
    }

    @SneakyThrows
    private void loadCollision(@NonNull Class<?> scoreboardTeam) {
        Class<?> enumTeamPush = BukkitReflection.getClass("world.scores.Team$CollisionRule",
                "world.scores.ScoreboardTeamBase$EnumTeamPush", "ScoreboardTeamBase$EnumTeamPush");
        Enum<?>[] collisionRules = (Enum<?>[]) enumTeamPush.getMethod("values").invoke(null);
        Method setCollisionRule = ReflectionUtils.getOnlyMethod(scoreboardTeam, void.class, enumTeamPush);
        setCollision = (team, collision) -> setCollisionRule.invoke(team, collisionRules[collision.ordinal()]);
    }

    /**
     * Creates team register packet with specified parameters.
     *
     * @param   team
     *          Team to register
     * @param   clientVersion
     *          Version to create the values for
     * @return  Register team packet with specified parameters
     */
    @SneakyThrows
    public Object registerTeam(@NonNull Team team, @NotNull ProtocolVersion clientVersion) {
        updateTeamData(team, clientVersion);
        Object packet;
        if (STATIC_CONSTRUCTOR_VERSION) {
            packet = TeamPacketConstructor_ofBoolean.invoke(null, team.getPlatformTeam(), true);
        } else {
            packet = newTeamPacket.newInstance(team.getPlatformTeam(), TeamAction.CREATE);
        }
        TeamPacket_PLAYERS.set(packet, team.getPlayers());
        return packet;
    }

    /**
     * Creates unregister team packet with given team.
     *
     * @param   team
     *          Team to unregister
     * @return  Packet for unregistering team
     */
    @SneakyThrows
    public Object unregisterTeam(@NonNull Team team) {
        if (STATIC_CONSTRUCTOR_VERSION) {
            return TeamPacketConstructor_of.invoke(null, team.getPlatformTeam());
        } else {
            return newTeamPacket.newInstance(team.getPlatformTeam(), TeamAction.REMOVE);
        }
    }

    /**
     * Creates team update packet with specified parameters.
     *
     * @param   team
     *          Team to update
     * @param   clientVersion
     *          Version to create the values for
     * @return  Update team packet with specified parameters
     */
    @SneakyThrows
    public Object updateTeam(@NonNull Team team, @NotNull ProtocolVersion clientVersion) {
        updateTeamData(team, clientVersion);
        if (STATIC_CONSTRUCTOR_VERSION) {
            return TeamPacketConstructor_ofBoolean.invoke(null, team.getPlatformTeam(), false);
        } else {
            return newTeamPacket.newInstance(team.getPlatformTeam(), TeamAction.UPDATE);
        }
    }

    /**
     * Updates team properties.
     *
     * @param   team
     *          Team to update
     * @param   clientVersion
     *          Version to create the values for
     */
    @SneakyThrows
    private void updateTeamData(@NonNull Team team, @NotNull ProtocolVersion clientVersion) {
        Object nmsTeam = team.getPlatformTeam();
        ScoreboardTeam_setAllowFriendlyFire.invoke(nmsTeam, (team.getOptions() & 0x1) > 0);
        ScoreboardTeam_setCanSeeFriendlyInvisibles.invoke(nmsTeam, (team.getOptions() & 0x2) > 0);
        if (MODERN_TEAM_DATA_VERSION) {
            ScoreboardTeam_setPrefix.invoke(nmsTeam, (Object) team.getPrefix().convert(clientVersion));
            ScoreboardTeam_setSuffix.invoke(nmsTeam, (Object) team.getSuffix().convert(clientVersion));
            ScoreboardTeam_setColor.invoke(nmsTeam, chatFormats[team.getColor().ordinal()]);
        } else {
            String prefix = team.getPrefix().toLegacyText();
            String suffix = team.getSuffix().toLegacyText();
            if (clientVersion.getMinorVersion() < 13) {
                prefix = SafeScoreboard.cutTo(prefix, Limitations.TEAM_PREFIX_SUFFIX_PRE_1_13);
                suffix = SafeScoreboard.cutTo(suffix, Limitations.TEAM_PREFIX_SUFFIX_PRE_1_13);
            }
            ScoreboardTeam_setPrefix.invoke(nmsTeam, prefix);
            ScoreboardTeam_setSuffix.invoke(nmsTeam, suffix);
        }
        setVisibility.accept(nmsTeam, team.getVisibility());
        setCollision.accept(nmsTeam, team.getCollision());
    }

    /**
     * Creates a team with given name.
     *
     * @param   name
     *          Team name
     * @return  Team with specified name
     */
    @SneakyThrows
    @NotNull
    public Object createTeam(@NonNull String name) {
        return newScoreboardTeam.newInstance(emptyScoreboard, name);
    }

    /**
     * Checks if packet is team packet and removes all real players from team
     * if sent by other plugins and anti-override is fully active on a player.
     *
     * @param   player
     *          Player who received the packet
     * @param   packet
     *          Received packet
     */
    @SneakyThrows
    public void onPacketSend(@NonNull TabPlayer player, @NonNull Object packet) {
        if (!TeamPacketClass.isInstance(packet)) return;
        int action = TeamPacket_ACTION.getInt(packet);
        if (action == TeamAction.UPDATE) return;
        Collection<String> players = (Collection<String>) TeamPacket_PLAYERS.get(packet);
        if (players == null) players = Collections.emptyList();
        TeamPacket_PLAYERS.set(packet, ((SafeScoreboard<?>)player.getScoreboard()).onTeamPacket(
                action, (String) TeamPacket_NAME.get(packet), players));
    }
}
