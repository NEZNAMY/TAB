package me.neznamy.tab.platforms.bukkit.scoreboard.packet;

import lombok.Getter;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.BiConsumerWithException;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Class storing all team related fields and methods.
 */
@SuppressWarnings("unchecked")
public class TeamPacketData {

    /** First version with modern team data using components */
    private final int MODERN_TEAM_DATA_VERSION = 13;

    /** First version with static constructor-like methods */
    private final int STATIC_CONSTRUCTOR_VERSION = 17;

    private Class<?> Component;
    private final Object emptyScoreboard;
    @Getter private final Class<?> TeamPacketClass;
    private Constructor<?> newTeamPacket;
    private final Constructor<?> newScoreboardTeam;
    private Method TeamPacketConstructor_of;
    private Method TeamPacketConstructor_ofBoolean;
    private final Field TeamPacket_NAME;
    private final Field TeamPacket_ACTION;
    private final Field TeamPacket_PLAYERS;
    private final Method ScoreboardTeam_getPlayerNameSet;
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
        Class<?> enumChatFormatClass = BukkitReflection.getClass("ChatFormatting", "EnumChatFormat", "EnumChatFormat");
        TeamPacketClass = BukkitReflection.getClass(
                "network.protocol.game.ClientboundSetPlayerTeamPacket", // Mojang mapped
                "network.protocol.game.PacketPlayOutScoreboardTeam", // Bukkit 1.17+
                "PacketPlayOutScoreboardTeam", // Bukkit 1.7 - 1.16.5
                "Packet209SetScoreboardTeam" // 1.5 - 1.6.4
        );
        emptyScoreboard = Scoreboard.getConstructor().newInstance();
        newScoreboardTeam = scoreboardTeam.getConstructor(Scoreboard, String.class);
        TeamPacket_NAME = ReflectionUtils.getFields(TeamPacketClass, String.class).get(0);
        TeamPacket_ACTION = ReflectionUtils.getInstanceFields(TeamPacketClass, int.class).get(0);
        TeamPacket_PLAYERS = ReflectionUtils.getOnlyField(TeamPacketClass, Collection.class);
        ScoreboardTeam_getPlayerNameSet = ReflectionUtils.getOnlyMethod(scoreboardTeam, Collection.class);
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
        if (minorVersion >= 7) {
            Component = BukkitReflection.getClass("network.chat.Component", "network.chat.IChatBaseComponent", "IChatBaseComponent");
        }
        if (minorVersion >= 8) loadVisibility(scoreboardTeam);
        if (minorVersion >= 9) loadCollision(scoreboardTeam);
        if (minorVersion >= MODERN_TEAM_DATA_VERSION) {
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
        if (minorVersion >= STATIC_CONSTRUCTOR_VERSION) {
            TeamPacketConstructor_of = ReflectionUtils.getOnlyMethod(TeamPacketClass, TeamPacketClass, scoreboardTeam);
            TeamPacketConstructor_ofBoolean = ReflectionUtils.getOnlyMethod(TeamPacketClass, TeamPacketClass, scoreboardTeam, boolean.class);
        } else {
            newTeamPacket = TeamPacketClass.getConstructor(scoreboardTeam, int.class);
        }
    }

    @SneakyThrows
    private void loadVisibility(@NotNull Class<?> scoreboardTeam) {
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
    private void loadCollision(@NotNull Class<?> scoreboardTeam) {
        Class<?> enumTeamPush = BukkitReflection.getClass("world.scores.Team$CollisionRule",
                "world.scores.ScoreboardTeamBase$EnumTeamPush", "ScoreboardTeamBase$EnumTeamPush");
        Enum<?>[] collisionRules = (Enum<?>[]) enumTeamPush.getMethod("values").invoke(null);
        Method setCollisionRule = ReflectionUtils.getOnlyMethod(scoreboardTeam, void.class, enumTeamPush);
        setCollision = (team, collision) -> setCollisionRule.invoke(team, collisionRules[collision.ordinal()]);
    }

    /**
     * Creates team register packet with specified parameters.
     *
     * @param   name
     *          Team name
     * @param   prefix
     *          Team prefix for 1.12-
     * @param   prefixComponent
     *          Team prefix component for 1.13+
     * @param   suffix
     *          Team suffix for 1.12-
     * @param   suffixComponent
     *          Team suffix component for 1.13+
     * @param   visibility
     *          Nametag visibility rule
     * @param   collision
     *          Player collision rule
     * @param   players
     *          Team members
     * @param   options
     *          Team flags
     * @param   color
     *          Team color for 1.13+
     * @return  Register team packet with specified parameters
     */
    @SneakyThrows
    public Object registerTeam(@NotNull String name, @NotNull String prefix, @Nullable Object prefixComponent,
                               @NotNull String suffix, @Nullable Object suffixComponent,
                               @NotNull Scoreboard.NameVisibility visibility, @NotNull Scoreboard.CollisionRule collision,
                               @NotNull Collection<String> players, int options, @NotNull EnumChatFormat color) {
        Object team = createTeam(name, prefix, prefixComponent, suffix, suffixComponent, visibility, collision, options, color);
        ((Collection<String>) ScoreboardTeam_getPlayerNameSet.invoke(team)).addAll(players);
        if (BukkitReflection.getMinorVersion() >= STATIC_CONSTRUCTOR_VERSION) {
            return TeamPacketConstructor_ofBoolean.invoke(null, team, true);
        } else {
            return newTeamPacket.newInstance(team, Scoreboard.TeamAction.CREATE);
        }
    }

    /**
     * Creates unregister team packet with given team name.
     *
     * @param   name
     *          Team name to unregister
     * @return  Packet for unregistering team
     */
    @SneakyThrows
    public Object unregisterTeam(@NotNull String name) {
        Object team = newScoreboardTeam.newInstance(emptyScoreboard, name);
        if (BukkitReflection.getMinorVersion() >= STATIC_CONSTRUCTOR_VERSION) {
            return TeamPacketConstructor_of.invoke(null, team);
        } else {
            return newTeamPacket.newInstance(team, Scoreboard.TeamAction.REMOVE);
        }
    }

    /**
     * Creates team update packet with specified parameters.
     *
     * @param   name
     *          Team name
     * @param   prefix
     *          Team prefix for 1.12-
     * @param   prefixComponent
     *          Team prefix component for 1.13+
     * @param   suffix
     *          Team suffix for 1.12-
     * @param   suffixComponent
     *          Team suffix component for 1.13+
     * @param   visibility
     *          Nametag visibility rule
     * @param   collision
     *          Player collision rule
     * @param   options
     *          Team flags
     * @param   color
     *          Team color for 1.13+
     * @return  Update team packet with specified parameters
     */
    @SneakyThrows
    public Object updateTeam(@NotNull String name, @NotNull String prefix, @Nullable Object prefixComponent,
                             @NotNull String suffix, @Nullable Object suffixComponent,
                             @NotNull Scoreboard.NameVisibility visibility, @NotNull Scoreboard.CollisionRule collision,
                             int options, @NotNull EnumChatFormat color) {
        Object team = createTeam(name, prefix, prefixComponent, suffix, suffixComponent, visibility, collision, options, color);
        if (BukkitReflection.getMinorVersion() >= STATIC_CONSTRUCTOR_VERSION) {
            return TeamPacketConstructor_ofBoolean.invoke(null, team, false);
        } else {
            return newTeamPacket.newInstance(team, Scoreboard.TeamAction.UPDATE);
        }
    }

    /**
     * Creates player team with specified parameters.
     *
     * @param   teamName
     *          Team name
     * @param   prefix
     *          Team prefix for 1.12-
     * @param   prefixComponent
     *          Team prefix component for 1.13+
     * @param   suffix
     *          Team suffix for 1.12-
     * @param   suffixComponent
     *          Team suffix component for 1.13+
     * @param   visibility
     *          Nametag visibility rule
     * @param   collision
     *          Player collision rule
     * @param   options
     *          Team flags
     * @param   color
     *          Team color for 1.13+
     * @return  Team with specified parameters
     */
    @SneakyThrows
    private Object createTeam(@NotNull String teamName, @NotNull String prefix, @Nullable Object prefixComponent,
                              @NotNull String suffix, @Nullable Object suffixComponent,
                              @NotNull Scoreboard.NameVisibility visibility, @NotNull Scoreboard.CollisionRule collision,
                              int options, @NotNull EnumChatFormat color) {
        Object team = newScoreboardTeam.newInstance(emptyScoreboard, teamName);
        ScoreboardTeam_setAllowFriendlyFire.invoke(team, (options & 0x1) > 0);
        ScoreboardTeam_setCanSeeFriendlyInvisibles.invoke(team, (options & 0x2) > 0);
        if (BukkitReflection.getMinorVersion() >= MODERN_TEAM_DATA_VERSION) {
            ScoreboardTeam_setPrefix.invoke(team, prefixComponent);
            ScoreboardTeam_setSuffix.invoke(team, suffixComponent);
            ScoreboardTeam_setColor.invoke(team, chatFormats[color.ordinal()]);
        } else {
            ScoreboardTeam_setPrefix.invoke(team, prefix);
            ScoreboardTeam_setSuffix.invoke(team, suffix);
        }
        setVisibility.accept(team, visibility);
        setCollision.accept(team, collision);
        return team;
    }

    /**
     * Removes all real players from team if sent by other plugins and
     * anti-override is fully active on a player.
     *
     * @param   team
     *          Team packet
     */
    @SneakyThrows
    public void onTeamPacket(@NotNull Object team) {
        if (TAB.getInstance().getNameTagManager() == null) return;
        int action = TeamPacket_ACTION.getInt(team);
        if (action == 1 || action == 2 || action == 4) return;
        Collection<String> players = (Collection<String>) TeamPacket_PLAYERS.get(team);
        String teamName = (String) TeamPacket_NAME.get(team);
        if (players == null) return;
        //creating a new list to prevent NoSuchFieldException in minecraft packet encoder when a player is removed
        Collection<String> newList = new ArrayList<>();
        for (String entry : players) {
            TabPlayer p = Scoreboard.getPlayer(entry);
            if (p == null) {
                newList.add(entry);
                continue;
            }
            Sorting sorting = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SORTING);
            String expectedTeam = sorting.getShortTeamName(p);
            if (expectedTeam == null) {
                newList.add(entry);
                continue;
            }
            if (!TAB.getInstance().getNameTagManager().getDisableChecker().isDisabledPlayer(p) &&
                    !TAB.getInstance().getNameTagManager().hasTeamHandlingPaused(p) && !teamName.equals(expectedTeam)) {
                Scoreboard.logTeamOverride(teamName, p.getName(), expectedTeam);
            } else {
                newList.add(entry);
            }
        }
        TeamPacket_PLAYERS.set(team, newList);
    }
}
