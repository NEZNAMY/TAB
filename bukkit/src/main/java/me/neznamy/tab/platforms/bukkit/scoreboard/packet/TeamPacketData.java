package me.neznamy.tab.platforms.bukkit.scoreboard.packet;

import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.platform.Scoreboard.TeamAction;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard.Team;
import me.neznamy.tab.shared.util.ReflectionUtils;
import me.neznamy.tab.shared.util.function.BiFunctionWithException;
import me.neznamy.tab.shared.util.function.FunctionWithException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Class storing all team related fields and methods.
 */
@SuppressWarnings("unchecked")
public class TeamPacketData {

    // Classes
    private final Class<?> Component = BukkitReflection.getClass("network.chat.Component", "network.chat.IChatBaseComponent", "IChatBaseComponent");
    private final Class<?> EnumChatFormatClass = BukkitReflection.getClass("ChatFormatting", "EnumChatFormat");
    private final Class<?> ScoreboardClass = BukkitReflection.getClass("world.scores.Scoreboard", "Scoreboard");
    private final Class<?> ScoreboardTeamClass = BukkitReflection.getClass("world.scores.PlayerTeam", "world.scores.ScoreboardTeam", "ScoreboardTeam");
    private final Class<?> TeamPacketClass = BukkitReflection.getClass(
            "network.protocol.game.ClientboundSetPlayerTeamPacket", // Mojang mapped
            "network.protocol.game.PacketPlayOutScoreboardTeam", // Bukkit 1.17+
            "PacketPlayOutScoreboardTeam" // Bukkit 1.7 - 1.16.5
    );
    private final Class<?> enumNameTagVisibility = BukkitReflection.getClass(
            "world.scores.Team$Visibility", // Mojang mapped
            "world.scores.ScoreboardTeamBase$EnumNameTagVisibility", // Bukkit 1.17+
            "ScoreboardTeamBase$EnumNameTagVisibility" // Bukkit 1.8.1 - 1.16.5
    );
    private final Class<?> enumTeamPush = BukkitReflection.getClass("world.scores.Team$CollisionRule",
            "world.scores.ScoreboardTeamBase$EnumTeamPush", "ScoreboardTeamBase$EnumTeamPush");

    // Constructors
    private final Constructor<?> newScoreboardTeam = ScoreboardTeamClass.getConstructor(ScoreboardClass, String.class);

    // Fields
    public final Field ScoreboardTeam_players = ReflectionUtils.getFields(ScoreboardTeamClass, Set.class).get(0);
    private final Field ScoreboardTeam_allowFriendlyFire = ReflectionUtils.getFields(ScoreboardTeamClass, boolean.class).get(0);
    private final Field ScoreboardTeam_seeFriendlyInvisibles = ReflectionUtils.getFields(ScoreboardTeamClass, boolean.class).get(1);
    private final Field ScoreboardTeam_prefix = ReflectionUtils.getFields(ScoreboardTeamClass, Component).get(1);
    private final Field ScoreboardTeam_suffix = ReflectionUtils.getFields(ScoreboardTeamClass, Component).get(2);
    private final Method ScoreboardTeam_setColor = ReflectionUtils.getOnlyMethod(ScoreboardTeamClass, void.class, EnumChatFormatClass);
    private final Field ScoreboardTeam_nameTagVisibility = ReflectionUtils.getFields(ScoreboardTeamClass, enumNameTagVisibility).get(0);
    private final Method ScoreboardTeam_setCollisionRule = ReflectionUtils.getOnlyMethod(ScoreboardTeamClass, void.class, enumTeamPush);
    private final Field TeamPacket_NAME = ReflectionUtils.getFields(TeamPacketClass, String.class).get(0);
    private final Field TeamPacket_ACTION = ReflectionUtils.getInstanceFields(TeamPacketClass, int.class).get(0);
    private final Field TeamPacket_PLAYERS = ReflectionUtils.getOnlyField(TeamPacketClass, Collection.class);

    // Objects
    private final Object emptyScoreboard = ScoreboardClass.getConstructor().newInstance();
    private final Enum<?>[] chatFormats = (Enum<?>[]) EnumChatFormatClass.getMethod("values").invoke(null);
    private final Enum<?>[] nameVisibilities = (Enum<?>[]) enumNameTagVisibility.getMethod("values").invoke(null);
    private final Enum<?>[] collisionRules = (Enum<?>[]) enumTeamPush.getMethod("values").invoke(null);

    // Team / packet creation
    public final FunctionWithException<String, Object> createTeam = name -> newScoreboardTeam.newInstance(emptyScoreboard, name);
    public final BiFunctionWithException<Team, ProtocolVersion, Object> newRegisterTeamPacket;
    public final FunctionWithException<Team, Object> newUnregisterTeamPacket;
    public final BiFunctionWithException<Team, ProtocolVersion, Object> newUpdateTeamPacket;

    /**
     * Constructs new instance and loads all required NMS classes, fields, and methods.
     * If anything fails, exception is thrown.
     *
     * @throws  ReflectiveOperationException
     *          If anything fails
     */
    public TeamPacketData() throws ReflectiveOperationException {
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

    /**
     * Updates team properties.
     *
     * @param   team
     *          Team to update
     */
    @SneakyThrows
    public void updateTeamData(@NonNull Team team) {
        Object nmsTeam = team.getPlatformTeam();
        ScoreboardTeam_allowFriendlyFire.set(nmsTeam, (team.getOptions() & 0x1) > 0);
        ScoreboardTeam_seeFriendlyInvisibles.set(nmsTeam, (team.getOptions() & 0x2) > 0);
        ScoreboardTeam_prefix.set(nmsTeam, team.getPrefix().convert());
        ScoreboardTeam_suffix.set(nmsTeam, team.getPrefix().convert());
        ScoreboardTeam_setColor.invoke(nmsTeam, chatFormats[team.getColor().getLegacyColor().ordinal()]);
        ScoreboardTeam_nameTagVisibility.set(nmsTeam, nameVisibilities[team.getVisibility().ordinal()]);
        ScoreboardTeam_setCollisionRule.invoke(nmsTeam, collisionRules[team.getCollision().ordinal()]);
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
