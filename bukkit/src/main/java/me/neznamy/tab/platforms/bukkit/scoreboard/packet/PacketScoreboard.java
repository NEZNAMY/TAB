package me.neznamy.tab.platforms.bukkit.scoreboard.packet;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.nms.ComponentConverter;
import me.neznamy.tab.platforms.bukkit.nms.PacketSender;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scoreboard implementation which uses packets
 * to send scoreboards to use the full potential on all versions
 * and server software without any artificial limits.
 */
public class PacketScoreboard extends SafeScoreboard<BukkitTabPlayer> {

    @Getter
    private static boolean available;

    @Getter
    private static Exception exception;

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

    private final Map<String, Object> teams = new HashMap<>();

    static {
        try {
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
                ComponentConverter.ensureAvailable();
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
            available = true;
        } catch (Exception e) {
            exception = e;
        }
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
        packetSender.sendPacket(player.getPlayer(), newObjectivePacket(
                ObjectiveAction.REGISTER,
                objective.getName(),
                objective.getTitle(),
                objective.getHealthDisplay().ordinal(),
                objective.getNumberFormat() == null ? null : objective.getNumberFormat().convert(player.getVersion())
        ));
        packetSender.sendPacket(player.getPlayer(), displayPacketData.setDisplaySlot(objective.getDisplaySlot().ordinal(), newObjective(objective.getName(), "", 0, null)));
    }

    @Override
    public void unregisterObjective(@NonNull Objective objective) {
        packetSender.sendPacket(player.getPlayer(), newObjectivePacket(ObjectiveAction.UNREGISTER, objective.getName(), "", 0, null));
    }

    @Override
    public void updateObjective(@NonNull Objective objective) {
        packetSender.sendPacket(player.getPlayer(), newObjectivePacket(
                ObjectiveAction.UPDATE,
                objective.getName(),
                objective.getTitle(),
                objective.getHealthDisplay().ordinal(),
                objective.getNumberFormat() == null ? null : objective.getNumberFormat().convert(player.getVersion())
        ));
    }

    @Override
    public void setScore(@NonNull Score score) {
        packetSender.sendPacket(player.getPlayer(), scorePacketData.setScore(score.getObjective(), score.getHolder(), score.getValue(),
                score.getDisplayName() == null ? null : score.getDisplayName().convert(player.getVersion()),
                score.getNumberFormat() == null ? null : toFixedFormat(score.getNumberFormat().convert(player.getVersion()))));
    }

    @Override
    public void removeScore(@NonNull Score score) {
        packetSender.sendPacket(player.getPlayer(), scorePacketData.removeScore(score.getObjective(), score.getHolder()));
    }

    @Override
    public void registerTeam(@NonNull Team team) {
        Object nmsTeam = teamPacketData.createTeam(team.getName());
        teams.put(team.getName(), nmsTeam);
        packetSender.sendPacket(player.getPlayer(), teamPacketData.registerTeam(nmsTeam, team, toComponent(team.getPrefix()), toComponent(team.getSuffix())));
    }

    @Override
    public void unregisterTeam(@NonNull Team team) {
        packetSender.sendPacket(player.getPlayer(), teamPacketData.unregisterTeam(teams.remove(team.getName())));
    }

    @Override
    public void updateTeam(@NonNull Team team) {
        packetSender.sendPacket(player.getPlayer(), teamPacketData.updateTeam(teams.get(team.getName()), team, toComponent(team.getPrefix()), toComponent(team.getSuffix())));
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
    private Object newObjectivePacket(int action, @NonNull String objectiveName, @NonNull String title, int display,
                                      @Nullable Object numberFormat) {
        Object packet = newObjectivePacket.newInstance(newObjective(objectiveName, title, display, numberFormat), action);
        if (BukkitReflection.getMinorVersion() >= 8 && BukkitReflection.getMinorVersion() < 13) {
            Objective_RENDER_TYPE.set(packet, healthDisplays[display]);
        }
        return packet;
    }

    /**
     * Creates a new Scoreboard Objective with given parameters.
     *
     * @param   objectiveName
     *          Objective name
     * @param   title
     *          Objective title
     * @param   renderType
     *          Render type
     * @param   numberFormat
     *          Default number format (1.20.3+)
     * @return  Created objective
     */
    @SneakyThrows
    public Object newObjective(@NonNull String objectiveName, @NonNull String title, int renderType,
                               @Nullable Object numberFormat) {
        if (BukkitReflection.is1_20_3Plus()) {
            // 1.20.3+
            return newScoreboardObjective.newInstance(
                    emptyScoreboard,
                    objectiveName,
                    null, // Criteria
                    toComponent(title),
                    healthDisplays[renderType],
                    false, // Auto update
                    toFixedFormat(numberFormat)
            );
        }
        if (BukkitReflection.getMinorVersion() >= 13) {
            // 1.13 - 1.20.2
            return newScoreboardObjective.newInstance(
                    emptyScoreboard,
                    objectiveName,
                    null, // Criteria
                    toComponent(title),
                    healthDisplays[renderType]
            );
        }
        // 1.5 - 1.12.2
        Object objective = newScoreboardObjective.newInstance(emptyScoreboard, objectiveName, IScoreboardCriteria_dummy);
        ScoreboardObjective_setDisplayName.invoke(objective, title);
        return objective;
    }

    @NotNull
    private Object toComponent(@NonNull String text) {
        return TabComponent.optimized(text).convert(player.getVersion());
    }

    @Nullable
    @SneakyThrows
    static Object toFixedFormat(@Nullable Object component) {
        if (component == null || newFixedFormat == null) return null;
        return newFixedFormat.newInstance(component);
    }
}
