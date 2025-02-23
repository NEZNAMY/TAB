package me.neznamy.tab.platforms.bukkit.scoreboard.packet;

import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.Scoreboard.TeamAction;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard.Team;
import me.neznamy.tab.shared.util.ReflectionUtils;
import me.neznamy.tab.shared.util.function.BiConsumerWithException;
import me.neznamy.tab.shared.util.function.BiFunctionWithException;
import me.neznamy.tab.shared.util.function.ConsumerWithException;
import me.neznamy.tab.shared.util.function.FunctionWithException;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Class storing all team related fields and methods.
 */
@SuppressWarnings("unchecked")
public class TeamPacketData {

    // Classes
    private final Class<?> ScoreboardClass = BukkitReflection.getClass("world.scores.Scoreboard", "Scoreboard");
    private final Class<?> ScoreboardTeamClass = BukkitReflection.getClass("world.scores.PlayerTeam", "world.scores.ScoreboardTeam", "ScoreboardTeam");
    private final Class<?> TeamPacketClass = BukkitReflection.getClass(
            "network.protocol.game.ClientboundSetPlayerTeamPacket", // Mojang mapped
            "network.protocol.game.PacketPlayOutScoreboardTeam", // Bukkit 1.17+
            "PacketPlayOutScoreboardTeam", // Bukkit 1.7 - 1.16.5
            "Packet209SetScoreboardTeam" // 1.5 - 1.6.4
    );

    // Constructors
    private final Constructor<?> newScoreboardTeam = ScoreboardTeamClass.getConstructor(ScoreboardClass, String.class);

    // Fields
    public final Field ScoreboardTeam_players = ReflectionUtils.getFields(ScoreboardTeamClass, Set.class).get(0);
    private final Field ScoreboardTeam_allowFriendlyFire = ReflectionUtils.getFields(ScoreboardTeamClass, boolean.class).get(0);
    private final Field ScoreboardTeam_seeFriendlyInvisibles = ReflectionUtils.getFields(ScoreboardTeamClass, boolean.class).get(1);
    private final Field TeamPacket_NAME = ReflectionUtils.getFields(TeamPacketClass, String.class).get(0);
    private final Field TeamPacket_ACTION;
    private final Field TeamPacket_PLAYERS = ReflectionUtils.getOnlyField(TeamPacketClass, Collection.class);

    // Objects
    private final Object emptyScoreboard = ScoreboardClass.getConstructor().newInstance();

    // Team / packet creation
    public final FunctionWithException<String, Object> createTeam = name -> newScoreboardTeam.newInstance(emptyScoreboard, name);
    public final BiFunctionWithException<Team, ProtocolVersion, Object> newRegisterTeamPacket;
    public final FunctionWithException<Team, Object> newUnregisterTeamPacket;
    public final BiFunctionWithException<Team, ProtocolVersion, Object> newUpdateTeamPacket;

    // Team property setters
    private final BiConsumerWithException<Team, ProtocolVersion> updatePlatformTeamPrefix = loadPrefix();
    private final BiConsumerWithException<Team, ProtocolVersion> updatePlatformTeamSuffix = loadSuffix();
    private final ConsumerWithException<Team> updatePlatformTeamColor = loadColor();
    private final ConsumerWithException<Team> updatePlatformTeamVisibility = loadVisibility();
    private final ConsumerWithException<Team> updatePlatformTeamCollision = loadCollision();

    /**
     * Constructs new instance and loads all required NMS classes, fields, and methods.
     * If anything fails, exception is thrown.
     *
     * @throws  ReflectiveOperationException
     *          If anything fails
     */
    public TeamPacketData() throws ReflectiveOperationException {
        int minorVersion = BukkitReflection.getMinorVersion();
        List<Field> intFields = ReflectionUtils.getInstanceFields(TeamPacketClass, int.class);
        if (minorVersion >= 8 && minorVersion <= 12) {
            TeamPacket_ACTION = intFields.get(1);
        } else {
            TeamPacket_ACTION = intFields.get(0);
        }

        // Packet constructors
        if (BukkitReflection.getMinorVersion() >= 17) {
            Method TeamPacketConstructor_of = ReflectionUtils.getOnlyMethod(TeamPacketClass, TeamPacketClass, ScoreboardTeamClass);
            Method TeamPacketConstructor_ofBoolean = ReflectionUtils.getOnlyMethod(TeamPacketClass, TeamPacketClass, ScoreboardTeamClass, boolean.class);
            newRegisterTeamPacket = (team, version) -> TeamPacketConstructor_ofBoolean.invoke(null, team.getPlatformTeam(), true);
            newUnregisterTeamPacket = team -> TeamPacketConstructor_of.invoke(null, team.getPlatformTeam());
            newUpdateTeamPacket = (team, version) -> TeamPacketConstructor_ofBoolean.invoke(null, team.getPlatformTeam(), false);
        } else {
            Constructor<?> newTeamPacket = TeamPacketClass.getConstructor(ScoreboardTeamClass, int.class);
            newRegisterTeamPacket = (team, version) -> newTeamPacket.newInstance(team.getPlatformTeam(), TeamAction.CREATE);
            newUnregisterTeamPacket = team -> newTeamPacket.newInstance(team.getPlatformTeam(), TeamAction.REMOVE);
            newUpdateTeamPacket = (team, version) -> newTeamPacket.newInstance(team.getPlatformTeam(), TeamAction.UPDATE);
        }
    }

    @SneakyThrows
    @NotNull
    private BiConsumerWithException<Team, ProtocolVersion> loadPrefix() {
        if (BukkitReflection.getMinorVersion() >= 13) {
            Class<?> Component = BukkitReflection.getClass("network.chat.Component", "network.chat.IChatBaseComponent", "IChatBaseComponent");
            Field prefix = ReflectionUtils.getFields(ScoreboardTeamClass, Component).get(1);
            return (team, version) -> prefix.set(team.getPlatformTeam(), team.getPrefix().convert());
        } else {
            Field prefix = ReflectionUtils.getFields(ScoreboardTeamClass, String.class).get(2);
            return (team, version) -> {
                String legacy = team.getPrefix().toLegacyText();
                if (version.getMinorVersion() < 13 || TAB.getInstance().getConfiguration().getConfig().isPacketEventsCompensation()) {
                    legacy = SafeScoreboard.cutTo(legacy, Limitations.TEAM_PREFIX_SUFFIX_PRE_1_13);
                }
                prefix.set(team.getPlatformTeam(), legacy);
            };
        }
    }

    @SneakyThrows
    @NotNull
    private BiConsumerWithException<Team, ProtocolVersion> loadSuffix() {
        if (BukkitReflection.getMinorVersion() >= 13) {
            Class<?> Component = BukkitReflection.getClass("network.chat.Component", "network.chat.IChatBaseComponent", "IChatBaseComponent");
            Field suffix = ReflectionUtils.getFields(ScoreboardTeamClass, Component).get(2);
            return (team, version) -> suffix.set(team.getPlatformTeam(), team.getSuffix().convert());
        } else {
            Field suffix = ReflectionUtils.getFields(ScoreboardTeamClass, String.class).get(3);
            return (team, version) -> {
                String legacy = team.getSuffix().toLegacyText();
                if (version.getMinorVersion() < 13 || TAB.getInstance().getConfiguration().getConfig().isPacketEventsCompensation()) {
                    legacy = SafeScoreboard.cutTo(legacy, Limitations.TEAM_PREFIX_SUFFIX_PRE_1_13);
                }
                suffix.set(team.getPlatformTeam(), legacy);
            };
        }
    }

    @SneakyThrows
    @NotNull
    private ConsumerWithException<Team> loadColor() {
        if (BukkitReflection.getMinorVersion() >= 13) {
            Class<?> EnumChatFormatClass = BukkitReflection.getClass("ChatFormatting", "EnumChatFormat");
            Enum<?>[] chatFormats = (Enum<?>[]) EnumChatFormatClass.getMethod("values").invoke(null);
            Method setColor = ReflectionUtils.getOnlyMethod(ScoreboardTeamClass, void.class, EnumChatFormatClass);
            return team -> setColor.invoke(team.getPlatformTeam(), chatFormats[team.getColor().getLegacyColor().ordinal()]);
        } else {
            return team -> {};
        }
    }

    @SneakyThrows
    @NotNull
    private ConsumerWithException<Team> loadVisibility() {
        if (BukkitReflection.getMinorVersion() >= 8) {
            Class<?> enumNameTagVisibility = BukkitReflection.getClass(
                    "world.scores.Team$Visibility", // Mojang mapped
                    "world.scores.ScoreboardTeamBase$EnumNameTagVisibility", // Bukkit 1.17+
                    "ScoreboardTeamBase$EnumNameTagVisibility", // Bukkit 1.8.1 - 1.16.5
                    "EnumNameTagVisibility" // Bukkit 1.8.0
            );
            Enum<?>[] nameVisibilities = (Enum<?>[]) enumNameTagVisibility.getMethod("values").invoke(null);
            Field ScoreboardTeam_nameTagVisibility = ReflectionUtils.getFields(ScoreboardTeamClass, enumNameTagVisibility).get(0);
            return team -> ScoreboardTeam_nameTagVisibility.set(team.getPlatformTeam(), nameVisibilities[team.getVisibility().ordinal()]);
        } else {
            return team -> {};
        }
    }

    @SneakyThrows
    @NotNull
    private ConsumerWithException<Team> loadCollision() {
        if (BukkitReflection.getMinorVersion() >= 9) {
            Class<?> enumTeamPush = BukkitReflection.getClass("world.scores.Team$CollisionRule",
                    "world.scores.ScoreboardTeamBase$EnumTeamPush", "ScoreboardTeamBase$EnumTeamPush");
            Enum<?>[] collisionRules = (Enum<?>[]) enumTeamPush.getMethod("values").invoke(null);
            Method setCollisionRule = ReflectionUtils.getOnlyMethod(ScoreboardTeamClass, void.class, enumTeamPush);
            return team -> setCollisionRule.invoke(team.getPlatformTeam(), collisionRules[team.getCollision().ordinal()]);
        } else {
            return team -> {};
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
    public void updateTeamData(@NonNull Team team, @NotNull ProtocolVersion clientVersion) {
        Object nmsTeam = team.getPlatformTeam();
        ScoreboardTeam_allowFriendlyFire.set(nmsTeam, (team.getOptions() & 0x1) > 0);
        ScoreboardTeam_seeFriendlyInvisibles.set(nmsTeam, (team.getOptions() & 0x2) > 0);
        updatePlatformTeamPrefix.accept(team, clientVersion);
        updatePlatformTeamSuffix.accept(team, clientVersion);
        updatePlatformTeamColor.accept(team);
        updatePlatformTeamVisibility.accept(team);
        updatePlatformTeamCollision.accept(team);
    }

    /**
     * Checks if the packet is a team packet and removes all real players from the team
     * if sent by other plugins and anti-override is fully active on a player.
     *
     * @param   player
     *          Player who received the packet
     * @param   packet
     *          The received packet
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
