package me.neznamy.tab.platforms.bukkit.scoreboard.packet;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.nms.PacketSender;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;

/**
 * Scoreboard implementation which uses packets
 * to send scoreboards to use the full potential on all versions
 * and server software without any artificial limits.
 */
public class PacketScoreboard extends SafeScoreboard<BukkitTabPlayer> {

    static Class<?> Component;
    static Class<?> Scoreboard;
    static Class<?> ScoreboardObjective;
    static Object emptyScoreboard;
    static Class<?> NumberFormat;
    private static Constructor<?> newFixedFormat;

    // Objective packet
    private static Class<?> ObjectivePacketClass;
    private static Constructor<?> newObjectivePacket;
    private static Field Objective_OBJECTIVE_NAME;
    private static Field Objective_METHOD;
    private static Field Objective_RENDER_TYPE;
    static Constructor<?> newScoreboardObjective;
    private static Method ScoreboardObjective_setDisplayName;
    private static Enum<?>[] healthDisplays;
    static Object IScoreboardCriteria_dummy;

    private static ScorePacketData scorePacketData;
    @Getter private static TeamPacketData teamPacketData;
    @Getter private static DisplayPacketData displayPacketData;
    private static PacketSender packetSender;

    public static void load() throws ReflectiveOperationException {
        int minorVersion = BukkitReflection.getMinorVersion();
        Scoreboard = BukkitReflection.getClass("world.scores.Scoreboard", "Scoreboard");
        ScoreboardObjective = BukkitReflection.getClass("world.scores.Objective", "world.scores.ScoreboardObjective", "ScoreboardObjective");
        Class<?> IScoreboardCriteria = BukkitReflection.getClass(
                "world.scores.criteria.ObjectiveCriteria", // Mojang mapped
                "world.scores.criteria.IScoreboardCriteria", // Bukkit 1.17.+
                "IScoreboardCriteria", // 1.5.1 - 1.16.5
                "IObjective" // 1.5.0
        );
        ObjectivePacketClass = BukkitReflection.getClass(
                "network.protocol.game.ClientboundSetObjectivePacket", // Mojang mapped
                "network.protocol.game.PacketPlayOutScoreboardObjective", // Bukkit 1.17+
                "PacketPlayOutScoreboardObjective", // 1.7 - 1.16.5
                "Packet206SetScoreboardObjective" // 1.5 - 1.6.4
        );
        emptyScoreboard = Scoreboard.getConstructor().newInstance();
        Objective_OBJECTIVE_NAME = ReflectionUtils.getFields(ObjectivePacketClass, String.class).get(0);
        List<Field> list = ReflectionUtils.getFields(ObjectivePacketClass, int.class);
        Objective_METHOD = list.get(list.size()-1);
        newObjectivePacket = ObjectivePacketClass.getConstructor(ScoreboardObjective, int.class);
        IScoreboardCriteria_dummy = ReflectionUtils.getFields(IScoreboardCriteria, IScoreboardCriteria).get(0).get(null);
        newScoreboardObjective = ReflectionUtils.getOnlyConstructor(ScoreboardObjective);
        if (minorVersion >= 7) {
            Component = BukkitReflection.getClass("network.chat.Component", "network.chat.IChatBaseComponent", "IChatBaseComponent");
        }
        if (minorVersion >= 8) {
            Class<?> EnumScoreboardHealthDisplay = BukkitReflection.getClass(
                    "world.scores.criteria.ObjectiveCriteria$RenderType",
                    "world.scores.criteria.IScoreboardCriteria$EnumScoreboardHealthDisplay",
                    "IScoreboardCriteria$EnumScoreboardHealthDisplay",
                    "EnumScoreboardHealthDisplay");
            healthDisplays = (Enum<?>[]) EnumScoreboardHealthDisplay.getMethod("values").invoke(null);
            if (minorVersion < 13) {
                Objective_RENDER_TYPE = ReflectionUtils.getOnlyField(ObjectivePacketClass, EnumScoreboardHealthDisplay);
            }
        }
        if (minorVersion < 13) {
            ScoreboardObjective_setDisplayName = ReflectionUtils.getOnlyMethod(ScoreboardObjective, void.class, String.class);
        }
        if (BukkitReflection.is1_20_3Plus()) {
            NumberFormat = BukkitReflection.getClass("network.chat.numbers.NumberFormat");
            newFixedFormat = BukkitReflection.getClass("network.chat.numbers.FixedFormat").getConstructor(Component);
        }
        scorePacketData = new ScorePacketData();
        teamPacketData = new TeamPacketData();
        displayPacketData = new DisplayPacketData();
        packetSender = new PacketSender();
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
    public void setDisplaySlot(@NonNull Objective objective) {
        packetSender.sendPacket(player, displayPacketData.setDisplaySlot(objective.getDisplaySlot().ordinal(), newObjective(objective)));
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
    public void setScore(@NonNull Score score) {
        packetSender.sendPacket(player, scorePacketData.setScore(score.getObjective().getName(), score.getHolder(), score.getValue(),
                score.getDisplayName() == null ? null : score.getDisplayName().convert(),
                score.getNumberFormat() == null ? null : toFixedFormat(score.getNumberFormat())));
    }

    @Override
    public void removeScore(@NonNull Score score) {
        packetSender.sendPacket(player, scorePacketData.removeScore(score.getObjective().getName(), score.getHolder()));
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
        teamPacketData.updateTeamData(team, player.getVersion());
        packetSender.sendPacket(player, teamPacketData.newRegisterTeamPacket.apply(team, player.getVersion()));
    }

    @Override
    @SneakyThrows
    public void unregisterTeam(@NonNull Team team) {
        packetSender.sendPacket(player, teamPacketData.newUnregisterTeamPacket.apply(team));
    }

    @Override
    @SneakyThrows
    public void updateTeam(@NonNull Team team) {
        teamPacketData.updateTeamData(team, player.getVersion());
        packetSender.sendPacket(player, teamPacketData.newUpdateTeamPacket.apply(team, player.getVersion()));
    }

    @Override
    @SneakyThrows
    public void onPacketSend(@NonNull Object packet) {
        if (isAntiOverrideScoreboard()) {
            displayPacketData.onPacketSend(player, packet);
            if (ObjectivePacketClass.isInstance(packet))  {
                TAB.getInstance().getFeatureManager().onObjective(player,
                        Objective_METHOD.getInt(packet), (String) Objective_OBJECTIVE_NAME.get(packet));
            }
        }
        if (isAntiOverrideTeams()) teamPacketData.onPacketSend(player, packet);
    }

    @SneakyThrows
    private Object newObjectivePacket(int action, @NonNull Objective objective) {
        // TODO save objectives and reuse them for better performance
        Object packet = newObjectivePacket.newInstance(newObjective(objective), action);
        if (BukkitReflection.getMinorVersion() >= 8 && BukkitReflection.getMinorVersion() < 13) {
            Objective_RENDER_TYPE.set(packet, healthDisplays[objective.getHealthDisplay().ordinal()]);
        }
        return packet;
    }

    @SneakyThrows
    private Object newObjective(@NonNull Objective objective) {
        if (BukkitReflection.is1_20_3Plus()) {
            // 1.20.3+
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
        if (BukkitReflection.getMinorVersion() >= 13) {
            // 1.13 - 1.20.2
            return newScoreboardObjective.newInstance(
                    emptyScoreboard,
                    objective.getName(),
                    null, // Criteria
                    objective.getTitle().convert(),
                    healthDisplays[objective.getHealthDisplay().ordinal()]
            );
        }
        // 1.5 - 1.12.2
        Object nmsObjective = newScoreboardObjective.newInstance(emptyScoreboard, objective.getName(), IScoreboardCriteria_dummy);
        String title = objective.getTitle().toLegacyText();
        if (player.getVersion().getMinorVersion() < 13 || TAB.getInstance().getConfiguration().getConfig().isPacketEventsCompensation()) {
            title = cutTo(title, Limitations.SCOREBOARD_TITLE_PRE_1_13);
        }
        ScoreboardObjective_setDisplayName.invoke(nmsObjective, title);
        return nmsObjective;
    }

    @Nullable
    @SneakyThrows
    private static Object toFixedFormat(@NonNull TabComponent component) {
        if (newFixedFormat == null) return null;
        return component.toFixedFormat(PacketScoreboard::convertFixedFormat);
    }

    @NotNull
    @SneakyThrows
    private static Object convertFixedFormat(@NotNull Object nmsComponent) {
        return newFixedFormat.newInstance(nmsComponent);
    }
}
