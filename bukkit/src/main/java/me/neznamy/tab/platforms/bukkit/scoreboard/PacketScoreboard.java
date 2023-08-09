package me.neznamy.tab.platforms.bukkit.scoreboard;

import lombok.Getter;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import static me.neznamy.tab.platforms.bukkit.nms.BukkitReflection.getLegacyClass;

/**
 * Scoreboard implementation for Bukkit, which uses packets
 * to send scoreboards to use the full potential on all versions
 * and server software without any artificial limits.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class PacketScoreboard extends Scoreboard<BukkitTabPlayer> {

    @Getter
    private static boolean available;

    private static Object emptyScoreboard;
    private static Field IScoreboardCriteria_self;

    // PacketPlayOutScoreboardDisplayObjective
    public static Class<?> DisplayObjectiveClass;
    private static Constructor<?> newDisplayObjective;
    public static Field DisplayObjective_POSITION;
    public static Field DisplayObjective_OBJECTIVE_NAME;

    // PacketPlayOutScoreboardScore
    private static Class<Enum> EnumScoreboardAction;
    private static Constructor<?> newScorePacket_1_13;
    private static Constructor<?> newScorePacket_String;
    private static Constructor<?> newScorePacket;
    private static Constructor<?> newScoreboardScore;
    private static Method ScoreboardScore_setScore;

    // PacketPlayOutScoreboardObjective
    public static Class<?> ObjectivePacketClass;
    private static Constructor<?> newObjectivePacket;
    public static Field Objective_OBJECTIVE_NAME;
    public static Field Objective_METHOD;
    private static Field Objective_RENDER_TYPE;
    private static Field Objective_DISPLAY_NAME;
    private static Class<Enum> EnumScoreboardHealthDisplay;
    private static Constructor<?> newScoreboardObjective;

    // PacketPlayOutScoreboardTeamStorage
    public static Class<?> TeamPacketClass;
    private static Constructor<?> newTeamPacket;
    private static Method TeamPacketConstructor_of;
    private static Method TeamPacketConstructor_ofBoolean;
    public static Field TeamPacket_NAME;
    public static Field TeamPacket_ACTION;
    public static Field TeamPacket_PLAYERS;
    private static Class<Enum> EnumNameTagVisibility;
    private static Class<Enum> EnumTeamPush;
    private static Class<Enum> EnumChatFormatClass;
    private static Constructor<?> newScoreboardTeam;
    private static Method ScoreboardTeam_getPlayerNameSet;
    private static Method ScoreboardTeam_setNameTagVisibility;
    private static Method ScoreboardTeam_setCollisionRule;
    private static Method ScoreboardTeam_setPrefix;
    private static Method ScoreboardTeam_setSuffix;
    private static Method ScoreboardTeam_setColor;
    private static Method ScoreboardTeam_setAllowFriendlyFire;
    private static Method ScoreboardTeam_setCanSeeFriendlyInvisibles;

    public static void load() throws ReflectiveOperationException {
        int minorVersion = BukkitReflection.getMinorVersion();
        Class<?> scoreboardTeam;
        Class<?> scoreboardObjective;
        Class<?> scoreboardScoreClass;
        Class<?> IScoreboardCriteria;
        Class<?> scoreboard;
        Class<?> scorePacketClass;
        Class<?> IChatBaseComponent = null;
        if (BukkitReflection.isMojangMapped()) {
            IChatBaseComponent = Class.forName("net.minecraft.network.chat.Component");
            EnumChatFormatClass = (Class<Enum>) Class.forName("net.minecraft.ChatFormatting");
            DisplayObjectiveClass = Class.forName("net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket");
            ObjectivePacketClass = Class.forName("net.minecraft.network.protocol.game.ClientboundSetObjectivePacket");
            scoreboard = Class.forName("net.minecraft.world.scores.Scoreboard");
            scorePacketClass = Class.forName("net.minecraft.network.protocol.game.ClientboundSetScorePacket");
            scoreboardObjective = Class.forName("net.minecraft.world.scores.Objective");
            scoreboardScoreClass = Class.forName("net.minecraft.world.scores.Score");
            IScoreboardCriteria = Class.forName("net.minecraft.world.scores.criteria.ObjectiveCriteria");
            EnumScoreboardHealthDisplay = (Class<Enum>) Class.forName("net.minecraft.world.scores.criteria.ObjectiveCriteria$RenderType");
            EnumScoreboardAction = (Class<Enum>) Class.forName("net.minecraft.server.ServerScoreboard$Method");
            TeamPacketClass = Class.forName("net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket");
            scoreboardTeam = Class.forName("net.minecraft.world.scores.PlayerTeam");
            EnumNameTagVisibility = (Class<Enum>) Class.forName("net.minecraft.world.scores.Team$Visibility");
            EnumTeamPush = (Class<Enum>) Class.forName("net.minecraft.world.scores.Team$CollisionRule");
        } else if (minorVersion >= 17) {
            IChatBaseComponent = Class.forName("net.minecraft.network.chat.IChatBaseComponent");
            EnumChatFormatClass = (Class<Enum>) Class.forName("net.minecraft.EnumChatFormat");
            DisplayObjectiveClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardDisplayObjective");
            ObjectivePacketClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardObjective");
            scoreboard = Class.forName("net.minecraft.world.scores.Scoreboard");
            scorePacketClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore");
            scoreboardObjective = Class.forName("net.minecraft.world.scores.ScoreboardObjective");
            scoreboardScoreClass = Class.forName("net.minecraft.world.scores.ScoreboardScore");
            IScoreboardCriteria = Class.forName("net.minecraft.world.scores.criteria.IScoreboardCriteria");
            EnumScoreboardHealthDisplay = (Class<Enum>) Class.forName("net.minecraft.world.scores.criteria.IScoreboardCriteria$EnumScoreboardHealthDisplay");
            EnumScoreboardAction = (Class<Enum>) Class.forName("net.minecraft.server.ScoreboardServer$Action");
            TeamPacketClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam");
            scoreboardTeam = Class.forName("net.minecraft.world.scores.ScoreboardTeam");
            EnumNameTagVisibility = (Class<Enum>) Class.forName("net.minecraft.world.scores.ScoreboardTeamBase$EnumNameTagVisibility");
            EnumTeamPush = (Class<Enum>) Class.forName("net.minecraft.world.scores.ScoreboardTeamBase$EnumTeamPush");
        } else {
            EnumChatFormatClass = (Class<Enum>) getLegacyClass("EnumChatFormat");
            DisplayObjectiveClass = getLegacyClass("PacketPlayOutScoreboardDisplayObjective", "Packet208SetScoreboardDisplayObjective");
            ObjectivePacketClass = getLegacyClass("PacketPlayOutScoreboardObjective", "Packet206SetScoreboardObjective");
            TeamPacketClass = getLegacyClass("PacketPlayOutScoreboardTeam", "Packet209SetScoreboardTeam");
            scorePacketClass = getLegacyClass("PacketPlayOutScoreboardScore", "Packet207SetScoreboardScore");
            scoreboard = getLegacyClass("Scoreboard");
            scoreboardObjective = getLegacyClass("ScoreboardObjective");
            scoreboardScoreClass = getLegacyClass("ScoreboardScore");
            IScoreboardCriteria = getLegacyClass("IScoreboardCriteria", "IObjective"); // 1.5.1+, 1.5
            scoreboardTeam = getLegacyClass("ScoreboardTeam");
            if (minorVersion >= 7) {
                IChatBaseComponent = getLegacyClass("IChatBaseComponent");
            }
            if (minorVersion >= 8) {
                EnumScoreboardHealthDisplay = (Class<Enum>) getLegacyClass("IScoreboardCriteria$EnumScoreboardHealthDisplay", "EnumScoreboardHealthDisplay");
                EnumScoreboardAction = (Class<Enum>) getLegacyClass("ScoreboardServer$Action", "PacketPlayOutScoreboardScore$EnumScoreboardAction", "EnumScoreboardAction");
                EnumNameTagVisibility = (Class<Enum>) getLegacyClass("ScoreboardTeamBase$EnumNameTagVisibility", "EnumNameTagVisibility");
            }
            if (minorVersion >= 9) {
                EnumTeamPush = (Class<Enum>) getLegacyClass("ScoreboardTeamBase$EnumTeamPush");
            }
        }
        
        emptyScoreboard = scoreboard.getConstructor().newInstance();
        IScoreboardCriteria_self = ReflectionUtils.getFields(IScoreboardCriteria, IScoreboardCriteria).get(0);
        newDisplayObjective = DisplayObjectiveClass.getConstructor(int.class, scoreboardObjective);
        DisplayObjective_POSITION = ReflectionUtils.getOnlyField(DisplayObjectiveClass, int.class);
        DisplayObjective_OBJECTIVE_NAME = ReflectionUtils.getOnlyField(DisplayObjectiveClass, String.class);
        newScoreboardObjective = ReflectionUtils.getOnlyConstructor(scoreboardObjective);
        Objective_OBJECTIVE_NAME = ReflectionUtils.getFields(ObjectivePacketClass, String.class).get(0);
        List<Field> list = ReflectionUtils.getFields(ObjectivePacketClass, int.class);
        Objective_METHOD = list.get(list.size()-1);
        newScoreboardScore = scoreboardScoreClass.getConstructor(scoreboard, scoreboardObjective, String.class);
        newScoreboardTeam = scoreboardTeam.getConstructor(scoreboard, String.class);
        TeamPacket_NAME = ReflectionUtils.getFields(TeamPacketClass, String.class).get(0);
        TeamPacket_ACTION = ReflectionUtils.getInstanceFields(TeamPacketClass, int.class).get(0);
        TeamPacket_PLAYERS = ReflectionUtils.getOnlyField(TeamPacketClass, Collection.class);
        ScoreboardTeam_getPlayerNameSet = ReflectionUtils.getOnlyMethod(scoreboardTeam, Collection.class);
        if (minorVersion >= 13) {
            newScorePacket_1_13 = scorePacketClass.getConstructor(EnumScoreboardAction, String.class, String.class, int.class);
            newObjectivePacket = ObjectivePacketClass.getConstructor(scoreboardObjective, int.class);
            Objective_DISPLAY_NAME = ReflectionUtils.getOnlyField(ObjectivePacketClass, IChatBaseComponent);
            ScoreboardTeam_setColor = ReflectionUtils.getOnlyMethod(scoreboardTeam, void.class, EnumChatFormatClass);
        } else {
            newScorePacket_String = scorePacketClass.getConstructor(String.class);
            newObjectivePacket = ObjectivePacketClass.getConstructor();
            Objective_DISPLAY_NAME = ReflectionUtils.getFields(ObjectivePacketClass, String.class).get(1);
            if (minorVersion >= 8) {
                newScorePacket = scorePacketClass.getConstructor(scoreboardScoreClass);
                Objective_RENDER_TYPE = ReflectionUtils.getOnlyField(ObjectivePacketClass, EnumScoreboardHealthDisplay);
            } else {
                newScorePacket = scorePacketClass.getConstructor(scoreboardScoreClass, int.class);
            }
        }
        if (minorVersion >= 9) {
            ScoreboardTeam_setCollisionRule = ReflectionUtils.getOnlyMethod(scoreboardTeam, void.class, EnumTeamPush);
        }
        if (minorVersion >= 17) {
            TeamPacketConstructor_of = ReflectionUtils.getOnlyMethod(TeamPacketClass, TeamPacketClass, scoreboardTeam);
            TeamPacketConstructor_ofBoolean = ReflectionUtils.getOnlyMethod(TeamPacketClass, TeamPacketClass, scoreboardTeam, boolean.class);
        } else {
            newTeamPacket = TeamPacketClass.getConstructor(scoreboardTeam, int.class);
        }
        if (BukkitReflection.isMojangMapped()) {
            ScoreboardScore_setScore = scoreboardScoreClass.getMethod("setScore", int.class);
            ScoreboardTeam_setAllowFriendlyFire = scoreboardTeam.getMethod("setAllowFriendlyFire", boolean.class);
            ScoreboardTeam_setCanSeeFriendlyInvisibles = scoreboardTeam.getMethod("setSeeFriendlyInvisibles", boolean.class);
            ScoreboardTeam_setPrefix = scoreboardTeam.getMethod("setPlayerPrefix", IChatBaseComponent);
            ScoreboardTeam_setSuffix = scoreboardTeam.getMethod("setPlayerSuffix", IChatBaseComponent);
            ScoreboardTeam_setNameTagVisibility = scoreboardTeam.getMethod("setNameTagVisibility", EnumNameTagVisibility);
        } else {
            ScoreboardScore_setScore = ReflectionUtils.getMethod(scoreboardScoreClass, new String[] {"func_96647_c", "setScore", "b", "c"}, int.class); // {Thermos, 1.5.1 - 1.17.1, 1.18+, 1.5}
            ScoreboardTeam_setAllowFriendlyFire = ReflectionUtils.getMethod(scoreboardTeam, new String[] {"func_96660_a", "setAllowFriendlyFire", "a"}, boolean.class); // {Thermos, 1.5.1+, 1.5 & 1.18+}
            ScoreboardTeam_setCanSeeFriendlyInvisibles = ReflectionUtils.getMethod(scoreboardTeam, new String[] {"func_98300_b", "setCanSeeFriendlyInvisibles", "b"}, boolean.class); // {Thermos, 1.5.1+, 1.5 & 1.18+}
            if (minorVersion >= 13) {
                ScoreboardTeam_setPrefix = ReflectionUtils.getMethod(scoreboardTeam, new String[]{"setPrefix", "b"}, IChatBaseComponent); // {1.17.1-, 1.18+}
                ScoreboardTeam_setSuffix = ReflectionUtils.getMethod(scoreboardTeam, new String[]{"setSuffix", "c"}, IChatBaseComponent); // {1.17.1-, 1.18+}
            } else {
                ScoreboardTeam_setPrefix = ReflectionUtils.getMethod(scoreboardTeam, new String[] {"func_96666_b", "setPrefix", "b"}, String.class); // {Thermos, 1.5.1+, 1.5}
                ScoreboardTeam_setSuffix = ReflectionUtils.getMethod(scoreboardTeam, new String[] {"func_96662_c", "setSuffix", "c"}, String.class); // {Thermos, 1.5.1+, 1.5}
            }
            if (minorVersion >= 8) {
                ScoreboardTeam_setNameTagVisibility = ReflectionUtils.getMethod(scoreboardTeam, new String[] {"setNameTagVisibility", "a"}, EnumNameTagVisibility); // {1.8.1+, 1.8 & 1.18+}
            }
        }
        available = true;
    }

    public PacketScoreboard(@NotNull BukkitTabPlayer player) {
        super(player);
    }

    @Override
    @SneakyThrows
    public void setDisplaySlot(@NotNull DisplaySlot slot, @NotNull String objective) {
        player.sendPacket(newDisplayObjective.newInstance(slot.ordinal(), newScoreboardObjective(objective)));
    }

    @Override
    @SneakyThrows
    public void registerObjective0(@NotNull String objectiveName, @NotNull String title, boolean hearts) {
        player.sendPacket(buildObjective(0, objectiveName, title, hearts));
    }

    @Override
    public void unregisterObjective0(@NotNull String objectiveName) {
        player.sendPacket(buildObjective(1, objectiveName, "", false));
    }

    @Override
    public void updateObjective0(@NotNull String objectiveName, @NotNull String title, boolean hearts) {
        player.sendPacket(buildObjective(2, objectiveName, title, hearts));
    }

    @SneakyThrows
    private Object buildObjective(int action, String objectiveName, String title, boolean hearts) {
        if (BukkitReflection.getMinorVersion() >= 13) {
            return newObjectivePacket.newInstance(
                    newScoreboardObjective.newInstance(null, objectiveName, null,
                            toComponent(IChatBaseComponent.optimizedComponent(title)),
                    asDisplayType(hearts)),
                    action
            );
        }
        Object nmsPacket = newObjectivePacket.newInstance();
        Objective_OBJECTIVE_NAME.set(nmsPacket, objectiveName);
        Objective_DISPLAY_NAME.set(nmsPacket, title);
        if (BukkitReflection.getMinorVersion() >= 8) {
            Objective_RENDER_TYPE.set(nmsPacket, asDisplayType(hearts));
        }
        Objective_METHOD.set(nmsPacket, action);
        return nmsPacket;
    }

    private Object asDisplayType(boolean hearts) {
        return Enum.valueOf(EnumScoreboardHealthDisplay, hearts ? "HEARTS" : "INTEGER");
    }

    @Override
    @SneakyThrows
    public void registerTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                              @NotNull NameVisibility visibility, @NotNull CollisionRule collision,
                              @NotNull Collection<String> players, int options) {
        Object team = createTeam(name, prefix, suffix, visibility, collision, options);
        ((Collection<String>)ScoreboardTeam_getPlayerNameSet.invoke(team)).addAll(players);
        if (BukkitReflection.getMinorVersion() >= 17) {
            player.sendPacket(TeamPacketConstructor_ofBoolean.invoke(null, team, true));
        } else {
            player.sendPacket(newTeamPacket.newInstance(team, 0));
        }
    }

    @Override
    @SneakyThrows
    public void unregisterTeam0(@NotNull String name) {
        Object team = newScoreboardTeam.newInstance(emptyScoreboard, name);
        if (BukkitReflection.getMinorVersion() >= 17) {
            player.sendPacket(TeamPacketConstructor_of.invoke(null, team));
        } else {
            player.sendPacket(newTeamPacket.newInstance(team, 1));
        }
    }

    @Override
    @SneakyThrows
    public void updateTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                            @NotNull NameVisibility visibility, @NotNull CollisionRule collision, int options) {
        Object team = createTeam(name, prefix, suffix, visibility, collision, options);
        if (BukkitReflection.getMinorVersion() >= 17) {
            player.sendPacket(TeamPacketConstructor_ofBoolean.invoke(null, team, false));
        } else {
            player.sendPacket(newTeamPacket.newInstance(team, 2));
        }
    }

    @SneakyThrows
    private Object createTeam(@NotNull String teamName, @NotNull String prefix, @NotNull String suffix,
                              @NotNull NameVisibility visibility, @NotNull CollisionRule collision, int options) {
        Object team = newScoreboardTeam.newInstance(emptyScoreboard, teamName);
        ScoreboardTeam_setAllowFriendlyFire.invoke(team, (options & 0x1) > 0);
        ScoreboardTeam_setCanSeeFriendlyInvisibles.invoke(team, (options & 0x2) > 0);
        if (BukkitReflection.getMinorVersion() >= 13) {
            ScoreboardTeam_setPrefix.invoke(team, toComponent(IChatBaseComponent.optimizedComponent(prefix)));
            ScoreboardTeam_setSuffix.invoke(team, toComponent(IChatBaseComponent.optimizedComponent(suffix)));
            ScoreboardTeam_setColor.invoke(team, Enum.valueOf(EnumChatFormatClass, EnumChatFormat.lastColorsOf(prefix).toString()));
        } else {
            ScoreboardTeam_setPrefix.invoke(team, prefix);
            ScoreboardTeam_setSuffix.invoke(team, suffix);
        }
        if (BukkitReflection.getMinorVersion() >= 8) ScoreboardTeam_setNameTagVisibility.invoke(team, Enum.valueOf(EnumNameTagVisibility, visibility.name()));
        if (BukkitReflection.getMinorVersion() >= 9) ScoreboardTeam_setCollisionRule.invoke(team, Enum.valueOf(EnumTeamPush, collision.name()));
        return team;
    }

    @Override
    @SneakyThrows
    public void setScore0(@NotNull String objective, @NotNull String playerName, int score) {
        if (BukkitReflection.getMinorVersion() >= 13) {
            player.sendPacket(newScorePacket_1_13.newInstance(Enum.valueOf(EnumScoreboardAction, "CHANGE"), objective, playerName, score));
        } else {
            Object scoreboardScore = newScoreboardScore.newInstance(emptyScoreboard, newScoreboardObjective(objective), playerName);
            ScoreboardScore_setScore.invoke(scoreboardScore, score);
            if (BukkitReflection.getMinorVersion() >= 8) {
                player.sendPacket(newScorePacket.newInstance(scoreboardScore));
            } else {
                player.sendPacket(newScorePacket.newInstance(scoreboardScore, 0));
            }
        }
    }

    @Override
    @SneakyThrows
    public void removeScore0(@NotNull String objective, @NotNull String playerName) {
        if (BukkitReflection.getMinorVersion() >= 13) {
            player.sendPacket(newScorePacket_1_13.newInstance(Enum.valueOf(EnumScoreboardAction, "REMOVE"), objective, playerName, 0));
        } else {
            player.sendPacket(newScorePacket_String.newInstance(playerName));
        }
    }

    /**
     * Creates a new Scoreboard Objective with given name.
     *
     * @param   objectiveName
     *          Objective name
     * @return  NMS Objective
     */
    @SneakyThrows
    public Object newScoreboardObjective(@NotNull String objectiveName) {
        if (BukkitReflection.getMinorVersion() >= 13) {
            return newScoreboardObjective.newInstance(
                    null,
                    objectiveName,
                    null,
                    toComponent(new IChatBaseComponent("")),
                    null
            );
        }
        return newScoreboardObjective.newInstance(
                null,
                objectiveName,
                IScoreboardCriteria_self.get(null)
        );
    }

    private Object toComponent(IChatBaseComponent component) {
        return NMSStorage.getInstance().toNMSComponent(component, player.getVersion());
    }
}
