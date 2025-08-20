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

    private static Object emptyScoreboard;
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
        Class<?> component = BukkitReflection.getClass("network.chat.Component", "network.chat.IChatBaseComponent");
        Class<?> EnumScoreboardHealthDisplay = BukkitReflection.getClass(
                "world.scores.criteria.ObjectiveCriteria$RenderType",
                "world.scores.criteria.IScoreboardCriteria$EnumScoreboardHealthDisplay"
        );
        healthDisplays = (Enum<?>[]) EnumScoreboardHealthDisplay.getMethod("values").invoke(null);
        newFixedFormat = BukkitReflection.getClass("network.chat.numbers.FixedFormat").getConstructor(component);
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
        Class<?> DisplaySlot = BukkitReflection.getClass("world.scores.DisplaySlot");
        displaySlots = (Object[]) DisplaySlot.getDeclaredMethod("values").invoke(null);
        Field DisplayObjective_POSITION = ReflectionUtils.getOnlyField(DisplayObjectiveClass, DisplaySlot);
        newDisplayObjective = DisplayObjectiveClass.getConstructor(DisplaySlot, ScoreboardObjective);
        packetToSlot = packet -> ((Enum<?>)DisplayObjective_POSITION.get(packet)).ordinal();
    }

    private static void loadScorePacketData() throws ReflectiveOperationException {
        Class<?> SetScorePacket = BukkitReflection.getClass(
                "network.protocol.game.ClientboundSetScorePacket", // Mojang mapped
                "network.protocol.game.PacketPlayOutScoreboardScore" // Bukkit
        );
        Constructor<?> newSetScore = SetScorePacket.getConstructor(String.class, String.class, int.class, Optional.class, Optional.class);
        setScore = (objective, holder, score, displayName, numberFormat) ->
                newSetScore.newInstance(holder, objective, score, Optional.ofNullable(displayName), Optional.ofNullable(numberFormat));
        Constructor<?> newResetScore = BukkitReflection.getClass("network.protocol.game.ClientboundResetScorePacket").getConstructor(String.class, String.class);
        removeScore = (objective, holder) -> newResetScore.newInstance(holder, objective);
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
    @SneakyThrows
    public void onPacketSend(@NonNull Object packet) {
        if (DisplayObjectiveClass.isInstance(packet)) {
            TAB.getInstance().getFeatureManager().onDisplayObjective(player, packetToSlot.apply(packet),
                    (String) DisplayObjective_OBJECTIVE_NAME.get(packet));
        }
        if (ObjectivePacketClass.isInstance(packet))  {
            TAB.getInstance().getFeatureManager().onObjective(player,
                    Objective_METHOD.getInt(packet), (String) Objective_OBJECTIVE_NAME.get(packet));
        }
        teamPacketData.onPacketSend(player, packet);
    }

    @SneakyThrows
    private Object newObjectivePacket(int action, @NonNull Objective objective) {
        // TODO save objectives and reuse them for better performance
        return newObjectivePacket.newInstance(newObjective(objective), action);
    }

    @SneakyThrows
    private Object newObjective(@NonNull Objective objective) {
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
