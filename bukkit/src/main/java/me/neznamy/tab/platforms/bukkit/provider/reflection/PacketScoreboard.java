package me.neznamy.tab.platforms.bukkit.provider.reflection;

import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.platforms.bukkit.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.util.ReflectionUtils;
import me.neznamy.tab.shared.util.function.BiFunctionWithException;
import me.neznamy.tab.shared.util.function.FunctionWithException;
import me.neznamy.tab.shared.util.function.QuintFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Scoreboard implementation which uses packets
 * to send scoreboards to use the full potential on all versions
 * and server software without any artificial limits.
 */
public class PacketScoreboard extends SafeScoreboard<BukkitTabPlayer> {

    private static final boolean is1_20_3Plus = ReflectionUtils.classExists("net.minecraft.network.protocol.game.ClientboundResetScorePacket");

    private static Class<?> Component;
    private static Object emptyScoreboard;
    private static Class<?> NumberFormat;
    private static Constructor<?> newFixedFormat;

    // Objective packet
    private static Class<?> ObjectivePacketClass;
    private static Constructor<?> newObjectivePacket;
    private static Field Objective_OBJECTIVE_NAME;
    private static Field Objective_METHOD;
    private static Constructor<?> newScoreboardObjective;
    private static Enum<?>[] healthDisplays;

    private static TeamPacketData teamPacketData;
    private static PacketSender packetSender;

    // DisplayObjective packet
    private static Class<?> DisplayObjectiveClass;
    private static Constructor<?> newDisplayObjective;
    private static Field DisplayObjective_OBJECTIVE_NAME;
    private static Object[] displaySlots;
    private static FunctionWithException<Object, Integer> packetToSlot;

    // Score packet
    private static QuintFunction<String, String, Integer, Object, Object, Object> setScore;
    private static BiFunctionWithException<String, String, Object> removeScore;

    public static void load() throws ReflectiveOperationException {
        Class<?> scoreboard = BukkitReflection.getClass("world.scores.Scoreboard");
        Class<?> scoreboardObjective = BukkitReflection.getClass("world.scores.Objective", "world.scores.ScoreboardObjective");
        ObjectivePacketClass = BukkitReflection.getClass(
                "network.protocol.game.ClientboundSetObjectivePacket", // Mojang mapped
                "network.protocol.game.PacketPlayOutScoreboardObjective" // Bukkit
        );
        emptyScoreboard = scoreboard.getConstructor().newInstance();
        Objective_OBJECTIVE_NAME = ReflectionUtils.getFields(ObjectivePacketClass, String.class).get(0);
        List<Field> list = ReflectionUtils.getFields(ObjectivePacketClass, int.class);
        Objective_METHOD = list.get(list.size()-1);
        newObjectivePacket = ObjectivePacketClass.getConstructor(scoreboardObjective, int.class);
        newScoreboardObjective = ReflectionUtils.getOnlyConstructor(scoreboardObjective);
        Component = BukkitReflection.getClass("network.chat.Component", "network.chat.IChatBaseComponent");
        Class<?> EnumScoreboardHealthDisplay = BukkitReflection.getClass(
                "world.scores.criteria.ObjectiveCriteria$RenderType",
                "world.scores.criteria.IScoreboardCriteria$EnumScoreboardHealthDisplay"
        );
        healthDisplays = (Enum<?>[]) EnumScoreboardHealthDisplay.getMethod("values").invoke(null);
        if (is1_20_3Plus) {
            NumberFormat = BukkitReflection.getClass("network.chat.numbers.NumberFormat");
            newFixedFormat = BukkitReflection.getClass("network.chat.numbers.FixedFormat").getConstructor(Component);
        }
        loadDisplayPacketData();
        loadScorePacketData();
        teamPacketData = new TeamPacketData();
        packetSender = new PacketSender();
    }

    private static void loadDisplayPacketData() throws ReflectiveOperationException {
        Class<?> ScoreboardObjective = BukkitReflection.getClass("world.scores.Objective", "world.scores.ScoreboardObjective");
        DisplayObjectiveClass = BukkitReflection.getClass(
                "network.protocol.game.ClientboundSetDisplayObjectivePacket", // Mojang mapped
                "network.protocol.game.PacketPlayOutScoreboardDisplayObjective" // Bukkit
        );
        DisplayObjective_OBJECTIVE_NAME = ReflectionUtils.getOnlyField(DisplayObjectiveClass, String.class);
        if (BukkitReflection.is1_20_2Plus()) {
            Class<?> DisplaySlot = BukkitReflection.getClass("world.scores.DisplaySlot");
            displaySlots = (Object[]) DisplaySlot.getDeclaredMethod("values").invoke(null);
            Field DisplayObjective_POSITION = ReflectionUtils.getOnlyField(DisplayObjectiveClass, DisplaySlot);
            newDisplayObjective = DisplayObjectiveClass.getConstructor(DisplaySlot, ScoreboardObjective);
            packetToSlot = packet -> ((Enum<?>)DisplayObjective_POSITION.get(packet)).ordinal();
        } else {
            displaySlots = new Object[]{0, 1, 2};
            Field DisplayObjective_POSITION = ReflectionUtils.getOnlyField(DisplayObjectiveClass, int.class);
            newDisplayObjective = DisplayObjectiveClass.getConstructor(int.class, ScoreboardObjective);
            packetToSlot = DisplayObjective_POSITION::getInt;
        }
    }

    private static void loadScorePacketData() throws ReflectiveOperationException {
        Class<?> SetScorePacket = BukkitReflection.getClass(
                "network.protocol.game.ClientboundSetScorePacket", // Mojang mapped
                "network.protocol.game.PacketPlayOutScoreboardScore" // Bukkit
        );
        if (is1_20_3Plus) {
            QuintFunction<String, String, Integer, Object, Object, Object> setScore0;
            try {
                // 1.20.5+
                Constructor<?> newSetScore = SetScorePacket.getConstructor(String.class, String.class, int.class, Optional.class, Optional.class);
                setScore0 = (objective, holder, score, displayName, numberFormat) ->
                        newSetScore.newInstance(holder, objective, score, Optional.ofNullable(displayName), Optional.ofNullable(numberFormat));
            } catch (ReflectiveOperationException e) {
                // 1.20.3 - 1.20.4
                Constructor<?> newSetScore = SetScorePacket.getConstructor(String.class, String.class, int.class, PacketScoreboard.Component, PacketScoreboard.NumberFormat);
                setScore0 = (objective, holder, score, displayName, numberFormat) ->
                        newSetScore.newInstance(holder, objective, score, displayName, numberFormat);
            }
            setScore = setScore0;
            Constructor<?> newResetScore = BukkitReflection.getClass("network.protocol.game.ClientboundResetScorePacket").getConstructor(String.class, String.class);
            removeScore = (objective, holder) -> newResetScore.newInstance(holder, objective);
        } else {
            Class<?> actionClass = BukkitReflection.getClass("server.ServerScoreboard$Method", "server.ScoreboardServer$Action", "ScoreboardServer$Action");
            Constructor<?> newSetScore = SetScorePacket.getConstructor(actionClass, String.class, String.class, int.class);
            Enum<?>[] scoreboardActions = (Enum<?>[]) actionClass.getMethod("values").invoke(null);
            setScore = (objective, holder, score, displayName, numberFormat) -> newSetScore.newInstance(scoreboardActions[0], objective, holder, score);
            removeScore = (objective, holder) -> newSetScore.newInstance(scoreboardActions[1], objective, holder, 0);
        }
    }

    @SneakyThrows
    public static void onPacketSend(@NonNull Object packet, @NonNull SafeScoreboard<BukkitTabPlayer> scoreboard) {
        if (scoreboard.isAntiOverrideScoreboard()) {
            if (DisplayObjectiveClass.isInstance(packet)) {
                TAB.getInstance().getFeatureManager().onDisplayObjective(scoreboard.getPlayer(), packetToSlot.apply(packet),
                        (String) DisplayObjective_OBJECTIVE_NAME.get(packet));
            }
            if (ObjectivePacketClass.isInstance(packet))  {
                TAB.getInstance().getFeatureManager().onObjective(scoreboard.getPlayer(),
                        Objective_METHOD.getInt(packet), (String) Objective_OBJECTIVE_NAME.get(packet));
            }
        }
        if (scoreboard.isAntiOverrideTeams()) teamPacketData.onPacketSend(scoreboard.getPlayer(), packet);
    }

    /**
     * Constructs new instance with given player.
     *
     * @param   player
     *          Player this scoreboard will belong to
     */
    public PacketScoreboard(@NonNull BukkitTabPlayer player) {
        super(player);
    }

    @Override
    public void registerObjective(@NonNull Objective objective) {
        packetSender.sendPacket(player, newObjectivePacket(ObjectiveAction.REGISTER, objective));
    }

    @Override
    @SneakyThrows
    public void setDisplaySlot(@NonNull Objective objective) {
        packetSender.sendPacket(player, newDisplayObjective.newInstance(displaySlots[objective.getDisplaySlot().ordinal()], newObjective(objective)));
    }

    @Override
    public void unregisterObjective(@NonNull Objective objective) {
        packetSender.sendPacket(player, newObjectivePacket(ObjectiveAction.UNREGISTER, objective));
    }

    @Override
    public void updateObjective(@NonNull Objective objective) {
        packetSender.sendPacket(player, newObjectivePacket(ObjectiveAction.UPDATE, objective));
    }

    @Override
    @SneakyThrows
    public void setScore(@NonNull Score score) {
        packetSender.sendPacket(player, setScore.apply(
                score.getObjective().getName(),
                score.getHolder(),
                score.getValue(),
                score.getDisplayName() == null ? null : score.getDisplayName().convert(),
                score.getNumberFormat() == null ? null : toFixedFormat(score.getNumberFormat())
        ));
    }

    @Override
    @SneakyThrows
    public void removeScore(@NonNull Score score) {
        packetSender.sendPacket(player, removeScore.apply(score.getObjective().getName(), score.getHolder()));
    }

    @Override
    @NotNull
    @SneakyThrows
    public Object createTeam(@NonNull String name) {
        return teamPacketData.createTeam.apply(name);
    }

    @Override
    @SneakyThrows
    public void registerTeam(@NonNull Team team) {
        teamPacketData.ScoreboardTeam_players.set(team.getPlatformTeam(), new HashSet<>(team.getPlayers()));
        teamPacketData.updateTeamData(team);
        packetSender.sendPacket(player, teamPacketData.newRegisterTeamPacket.apply(team));
    }

    @Override
    @SneakyThrows
    public void unregisterTeam(@NonNull Team team) {
        packetSender.sendPacket(player, teamPacketData.newUnregisterTeamPacket.apply(team));
    }

    @Override
    @SneakyThrows
    public void updateTeam(@NonNull Team team) {
        teamPacketData.updateTeamData(team);
        packetSender.sendPacket(player, teamPacketData.newUpdateTeamPacket.apply(team));
    }

    @Override
    public void onPacketSend(@NonNull Object packet) {
        onPacketSend(packet, this);
    }

    @SneakyThrows
    private Object newObjectivePacket(int action, @NonNull Objective objective) {
        // TODO save objectives and reuse them for better performance
        return newObjectivePacket.newInstance(newObjective(objective), action);
    }

    @SneakyThrows
    private Object newObjective(@NonNull Objective objective) {
        if (is1_20_3Plus) {
            return newScoreboardObjective.newInstance(
                    emptyScoreboard,
                    objective.getName(),
                    null, // Criteria
                    objective.getTitle().convert(),
                    healthDisplays[objective.getHealthDisplay().ordinal()],
                    false, // Auto update
                    objective.getNumberFormat() == null ? null : toFixedFormat(objective.getNumberFormat())
            );
        }
        return newScoreboardObjective.newInstance(
                emptyScoreboard,
                objective.getName(),
                null, // Criteria
                objective.getTitle().convert(),
                healthDisplays[objective.getHealthDisplay().ordinal()]
        );
    }

    @Nullable
    @SneakyThrows
    private Object toFixedFormat(@NonNull TabComponent component) {
        if (newFixedFormat == null) return null;
        return component.toFixedFormat(this::convertFixedFormat);
    }

    @NotNull
    @SneakyThrows
    private Object convertFixedFormat(@NotNull Object nmsComponent) {
        return newFixedFormat.newInstance(nmsComponent);
    }
}
