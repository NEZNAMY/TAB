package me.neznamy.tab.platforms.bukkit.scoreboard;

import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.TAB;
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

/**
 * Scoreboard implementation for Bukkit, which uses packets
 * to send scoreboards. While md_5 keeps saying that you don't
 * need packets for scoreboards in every single spigot update,
 * this is not true. It may seem that way at first, but after
 * investigating, fatal problems are found:<p>
 * #1 - Limitations on legacy versions are forced in the API.
 *      While this may not seem like a problem, it enforces those
 *      limits even for 1.13+ players (if using ViaVersion).<p>
 * #2 - Modern versions no longer have any limits, but md_5
 *      decided to add some random limits for absolutely no reason
 *      at all. Scoreboard title received a random 128 characters
 *      limit including color codes. Together with the almighty bukkit
 *      RGB format using 14 characters for 1 color code, this makes
 *      gradients just impossible to use. Team prefix/suffix also
 *      received a 64 characters limit (excluding color codes at least),
 *      however that might not be enough for displaying a line of text
 *      in sidebar, which would require splitting the text into prefix
 *      and suffix, which is just begging for bugs to be introduced.<p>
 * #3 - Other plugins can decide to put players into their own
 *      scoreboard, automatically destroying all visuals made by the
 *      plugin. They might also put all players into the same scoreboard,
 *      making per-player view of teams, especially sidebar not working.<p>
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class PacketScoreboard extends Scoreboard<BukkitTabPlayer> {

    private static NMSStorage nms;

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
    private static Constructor<?> newScoreboardTeam;
    private static Method ScoreboardTeam_getPlayerNameSet;
    private static Method ScoreboardTeam_setNameTagVisibility;
    private static Method ScoreboardTeam_setCollisionRule;
    private static Method ScoreboardTeam_setPrefix;
    private static Method ScoreboardTeam_setSuffix;
    private static Method ScoreboardTeam_setColor;
    private static Method ScoreboardTeam_setAllowFriendlyFire;
    private static Method ScoreboardTeam_setCanSeeFriendlyInvisibles;

    public static void load(NMSStorage nms) throws ReflectiveOperationException {
        PacketScoreboard.nms = nms;
        Class<?> scoreboardTeam;
        Class<?> scoreboardObjective;
        Class<?> scoreboardScoreClass;
        Class<?> IScoreboardCriteria;
        Class<?> scoreboard;
        Class<?> scorePacketClass;
        if (nms.isMojangMapped()) {
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
        } else if (nms.getMinorVersion() >= 17) {
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
            DisplayObjectiveClass = nms.getLegacyClass("PacketPlayOutScoreboardDisplayObjective", "Packet208SetScoreboardDisplayObjective");
            ObjectivePacketClass = nms.getLegacyClass("PacketPlayOutScoreboardObjective", "Packet206SetScoreboardObjective");
            TeamPacketClass = nms.getLegacyClass("PacketPlayOutScoreboardTeam", "Packet209SetScoreboardTeam");
            scorePacketClass = nms.getLegacyClass("PacketPlayOutScoreboardScore", "Packet207SetScoreboardScore");
            scoreboard = nms.getLegacyClass("Scoreboard");
            scoreboardObjective = nms.getLegacyClass("ScoreboardObjective");
            scoreboardScoreClass = nms.getLegacyClass("ScoreboardScore");
            IScoreboardCriteria = nms.getLegacyClass("IScoreboardCriteria", "IObjective"); // 1.5.1+, 1.5
            scoreboardTeam = nms.getLegacyClass("ScoreboardTeam");
            if (nms.getMinorVersion() >= 8) {
                EnumScoreboardHealthDisplay = (Class<Enum>) nms.getLegacyClass("IScoreboardCriteria$EnumScoreboardHealthDisplay", "EnumScoreboardHealthDisplay");
                EnumScoreboardAction = (Class<Enum>) nms.getLegacyClass("ScoreboardServer$Action", "PacketPlayOutScoreboardScore$EnumScoreboardAction", "EnumScoreboardAction");
                EnumNameTagVisibility = (Class<Enum>) nms.getLegacyClass("ScoreboardTeamBase$EnumNameTagVisibility", "EnumNameTagVisibility");
            }
            if (nms.getMinorVersion() >= 9) {
                EnumTeamPush = (Class<Enum>) nms.getLegacyClass("ScoreboardTeamBase$EnumTeamPush");
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
        if (nms.getMinorVersion() >= 13) {
            newScorePacket_1_13 = scorePacketClass.getConstructor(EnumScoreboardAction, String.class, String.class, int.class);
            newObjectivePacket = ObjectivePacketClass.getConstructor(scoreboardObjective, int.class);
            Objective_DISPLAY_NAME = ReflectionUtils.getOnlyField(ObjectivePacketClass, nms.IChatBaseComponent);
            ScoreboardTeam_setColor = ReflectionUtils.getOnlyMethod(scoreboardTeam, void.class, nms.EnumChatFormat);
        } else {
            newScorePacket_String = scorePacketClass.getConstructor(String.class);
            newObjectivePacket = ObjectivePacketClass.getConstructor();
            Objective_DISPLAY_NAME = ReflectionUtils.getFields(ObjectivePacketClass, String.class).get(1);
            if (nms.getMinorVersion() >= 8) {
                newScorePacket = scorePacketClass.getConstructor(scoreboardScoreClass);
                Objective_RENDER_TYPE = ReflectionUtils.getOnlyField(ObjectivePacketClass, EnumScoreboardHealthDisplay);
            } else {
                newScorePacket = scorePacketClass.getConstructor(scoreboardScoreClass, int.class);
            }
        }
        if (nms.getMinorVersion() >= 9) {
            ScoreboardTeam_setCollisionRule = ReflectionUtils.getOnlyMethod(scoreboardTeam, void.class, EnumTeamPush);
        }
        if (nms.getMinorVersion() >= 17) {
            TeamPacketConstructor_of = ReflectionUtils.getOnlyMethod(TeamPacketClass, TeamPacketClass, scoreboardTeam);
            TeamPacketConstructor_ofBoolean = ReflectionUtils.getOnlyMethod(TeamPacketClass, TeamPacketClass, scoreboardTeam, boolean.class);
        } else {
            newTeamPacket = TeamPacketClass.getConstructor(scoreboardTeam, int.class);
        }
        if (nms.isMojangMapped()) {
            ScoreboardScore_setScore = scoreboardScoreClass.getMethod("setScore", int.class);
            ScoreboardTeam_setAllowFriendlyFire = scoreboardTeam.getMethod("setAllowFriendlyFire", boolean.class);
            ScoreboardTeam_setCanSeeFriendlyInvisibles = scoreboardTeam.getMethod("setSeeFriendlyInvisibles", boolean.class);
            ScoreboardTeam_setPrefix = scoreboardTeam.getMethod("setPlayerPrefix", nms.IChatBaseComponent);
            ScoreboardTeam_setSuffix = scoreboardTeam.getMethod("setPlayerSuffix", nms.IChatBaseComponent);
            ScoreboardTeam_setNameTagVisibility = scoreboardTeam.getMethod("setNameTagVisibility", EnumNameTagVisibility);
        } else {
            ScoreboardScore_setScore = ReflectionUtils.getMethod(scoreboardScoreClass, new String[] {"func_96647_c", "setScore", "b", "c"}, int.class); // {Thermos, 1.5.1 - 1.17.1, 1.18+, 1.5}
            ScoreboardTeam_setAllowFriendlyFire = ReflectionUtils.getMethod(scoreboardTeam, new String[] {"func_96660_a", "setAllowFriendlyFire", "a"}, boolean.class); // {Thermos, 1.5.1+, 1.5 & 1.18+}
            ScoreboardTeam_setCanSeeFriendlyInvisibles = ReflectionUtils.getMethod(scoreboardTeam, new String[] {"func_98300_b", "setCanSeeFriendlyInvisibles", "b"}, boolean.class); // {Thermos, 1.5.1+, 1.5 & 1.18+}
            if (nms.getMinorVersion() >= 13) {
                ScoreboardTeam_setPrefix = ReflectionUtils.getMethod(scoreboardTeam, new String[]{"setPrefix", "b"}, nms.IChatBaseComponent); // {1.17.1-, 1.18+}
                ScoreboardTeam_setSuffix = ReflectionUtils.getMethod(scoreboardTeam, new String[]{"setSuffix", "c"}, nms.IChatBaseComponent); // {1.17.1-, 1.18+}
            } else {
                ScoreboardTeam_setPrefix = ReflectionUtils.getMethod(scoreboardTeam, new String[] {"func_96666_b", "setPrefix", "b"}, String.class); // {Thermos, 1.5.1+, 1.5}
                ScoreboardTeam_setSuffix = ReflectionUtils.getMethod(scoreboardTeam, new String[] {"func_96662_c", "setSuffix", "c"}, String.class); // {Thermos, 1.5.1+, 1.5}
            }
            if (nms.getMinorVersion() >= 8) {
                ScoreboardTeam_setNameTagVisibility = ReflectionUtils.getMethod(scoreboardTeam, new String[] {"setNameTagVisibility", "a"}, EnumNameTagVisibility); // {1.8.1+, 1.8 & 1.18+}
            }
        }
    }

    public PacketScoreboard(BukkitTabPlayer player) {
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
        if (nms.getMinorVersion() >= 13) {
            return newObjectivePacket.newInstance(
                    newScoreboardObjective.newInstance(null, objectiveName, null,
                    nms.toNMSComponent(IChatBaseComponent.optimizedComponent(title), player.getVersion()),
                    asDisplayType(hearts)),
                    action
            );
        }
        Object nmsPacket = newObjectivePacket.newInstance();
        Objective_OBJECTIVE_NAME.set(nmsPacket, objectiveName);
        Objective_DISPLAY_NAME.set(nmsPacket, title);
        if (nms.getMinorVersion() >= 8) {
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
        if (nms.getMinorVersion() >= 17) {
            player.sendPacket(TeamPacketConstructor_ofBoolean.invoke(null, team, true));
        } else {
            player.sendPacket(newTeamPacket.newInstance(team, 0));
        }
    }

    @Override
    @SneakyThrows
    public void unregisterTeam0(@NotNull String name) {
        Object team = newScoreboardTeam.newInstance(emptyScoreboard, name);
        if (nms.getMinorVersion() >= 17) {
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
        if (nms.getMinorVersion() >= 17) {
            player.sendPacket(TeamPacketConstructor_ofBoolean.invoke(null, team, false));
        } else {
            player.sendPacket(newTeamPacket.newInstance(team, 2));
        }
    }

    @SneakyThrows
    private Object createTeam(String teamName, String prefix, String suffix, NameVisibility visibility, CollisionRule collision, int options) {
        Object team = newScoreboardTeam.newInstance(emptyScoreboard, teamName);
        ScoreboardTeam_setAllowFriendlyFire.invoke(team, (options & 0x1) > 0);
        ScoreboardTeam_setCanSeeFriendlyInvisibles.invoke(team, (options & 0x2) > 0);
        if (nms.getMinorVersion() >= 13) {
            ScoreboardTeam_setPrefix.invoke(team, nms.toNMSComponent(IChatBaseComponent.optimizedComponent(prefix), player.getVersion()));
            ScoreboardTeam_setSuffix.invoke(team, nms.toNMSComponent(IChatBaseComponent.optimizedComponent(suffix), player.getVersion()));
            ScoreboardTeam_setColor.invoke(team, Enum.valueOf(nms.EnumChatFormat, EnumChatFormat.lastColorsOf(prefix).toString()));
        } else {
            ScoreboardTeam_setPrefix.invoke(team, prefix);
            ScoreboardTeam_setSuffix.invoke(team, suffix);
        }
        if (nms.getMinorVersion() >= 8) ScoreboardTeam_setNameTagVisibility.invoke(team, Enum.valueOf(EnumNameTagVisibility, visibility.name()));
        if (nms.getMinorVersion() >= 9) ScoreboardTeam_setCollisionRule.invoke(team, Enum.valueOf(EnumTeamPush, collision.name()));
        return team;
    }

    @Override
    @SneakyThrows
    public void setScore0(@NotNull String objective, @NotNull String playerName, int score) {
        Object packet;
        if (nms.getMinorVersion() >= 13) {
            packet = newScorePacket_1_13.newInstance(Enum.valueOf(EnumScoreboardAction, "CHANGE"), objective, playerName, score);
        } else {
            Object scoreboardScore = newScoreboardScore.newInstance(emptyScoreboard, newScoreboardObjective(objective), playerName);
            ScoreboardScore_setScore.invoke(scoreboardScore, score);
            if (nms.getMinorVersion() >= 8) {
                packet = newScorePacket.newInstance(scoreboardScore);
            } else {
                packet = newScorePacket.newInstance(scoreboardScore, 0);
            }
        }
        player.sendPacket(packet);
    }

    @Override
    @SneakyThrows
    public void removeScore0(@NotNull String objective, @NotNull String playerName) {
        if (nms.getMinorVersion() >= 13) {
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
        if (nms.getMinorVersion() >= 13) {
            return newScoreboardObjective.newInstance(
                    null,
                    objectiveName,
                    null,
                    nms.toNMSComponent(new IChatBaseComponent(""), TAB.getInstance().getServerVersion()),
                    null
            );
        }
        return newScoreboardObjective.newInstance(
                null,
                objectiveName,
                IScoreboardCriteria_self.get(null)
        );
    }
}
