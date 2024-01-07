package me.neznamy.tab.platforms.bukkit.scoreboard.packet;

import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.util.BiFunctionWithException;
import me.neznamy.tab.shared.util.QuintFunction;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * Class containing data for scoreboard score set/reset packets.
 */
public class ScorePacketData {

    private final QuintFunction<String, String, Integer, Object, Object, Object> setScore;
    private final BiFunctionWithException<String, String, Object> removeScore;

    /**
     * Constructs new instance and loads all fields. Throws exception if something went wrong.
     *
     * @throws  ReflectiveOperationException
     *          If something went wrong
     */
    public ScorePacketData() throws ReflectiveOperationException {
        Class<?> SetScorePacket = BukkitReflection.getClass(
                "network.protocol.game.ClientboundSetScorePacket", // Mojang mapped
                "network.protocol.game.PacketPlayOutScoreboardScore", // Bukkit 1.17+
                "PacketPlayOutScoreboardScore", // 1.7 - 1.16.5
                "Packet207SetScoreboardScore" // 1.5 - 1.6.4
        );
        if (BukkitReflection.is1_20_3Plus()) {
            Constructor<?> newSetScore = SetScorePacket.getConstructor(String.class, String.class, int.class, PacketScoreboard.Component, PacketScoreboard.NumberFormat);
            Constructor<?> newResetScore = BukkitReflection.getClass("network.protocol.game.ClientboundResetScorePacket").getConstructor(String.class, String.class);
            setScore = (objective, holder, score, displayName, numberFormat) -> newSetScore.newInstance(holder, objective, score, displayName, numberFormat);
            removeScore = (objective, holder) -> newResetScore.newInstance(holder, objective);
        } else if (BukkitReflection.getMinorVersion() >= 13) {
            Class<?> actionClass = BukkitReflection.getClass("server.ServerScoreboard$Method", "server.ScoreboardServer$Action", "ScoreboardServer$Action");
            Constructor<?> newSetScore = SetScorePacket.getConstructor(actionClass, String.class, String.class, int.class);
            Enum<?>[] scoreboardActions = (Enum<?>[]) actionClass.getMethod("values").invoke(null);
            setScore = (objective, holder, score, displayName, numberFormat) -> newSetScore.newInstance(scoreboardActions[0], objective, holder, score);
            removeScore = (objective, holder) -> newSetScore.newInstance(scoreboardActions[1], objective, holder, 0);
        } else {
            Class<?> ScoreboardScore = BukkitReflection.getClass("ScoreboardScore");
            Constructor<?> newResetScore = SetScorePacket.getConstructor(String.class);
            Field SetScorePacket_SCORE = ReflectionUtils.getFields(SetScorePacket, int.class).get(0);
            Constructor<?> newScoreboardScore = ScoreboardScore.getConstructor(PacketScoreboard.Scoreboard, PacketScoreboard.ScoreboardObjective, String.class);
            Constructor<?> newSetScore;
            if (BukkitReflection.getMinorVersion() >= 8) {
                newSetScore = SetScorePacket.getConstructor(ScoreboardScore);
            } else {
                newSetScore = SetScorePacket.getConstructor(ScoreboardScore, int.class);
            }
            setScore = (objective, holder, score, displayName, numberFormat) -> {
                Object scoreboardScore = newScoreboardScore.newInstance(
                        PacketScoreboard.emptyScoreboard,
                        PacketScoreboard.newScoreboardObjective.newInstance(PacketScoreboard.emptyScoreboard, objective, PacketScoreboard.IScoreboardCriteria_dummy),
                        holder
                );
                Object packet;
                if (BukkitReflection.getMinorVersion() >= 8) {
                    packet = newSetScore.newInstance(scoreboardScore);
                } else {
                    packet = newSetScore.newInstance(scoreboardScore, Scoreboard.ScoreAction.CHANGE);
                }
                SetScorePacket_SCORE.set(packet, score);
                return packet;
            };
            removeScore = (objective, holder) -> newResetScore.newInstance(holder);
        }
    }

    /**
     * Creates set score packet.
     *
     * @param   objective
     *          Objective name
     * @param   scoreHolder
     *          Score holder
     * @param   score
     *          Score value
     * @param   displayName
     *          Score holder display name
     * @param   numberFormat
     *          Score value number format
     * @return  Set score packet with given parameters
     */
    @SneakyThrows
    public Object setScore(@NotNull String objective, @NotNull String scoreHolder, int score,
                           @Nullable Object displayName, @Nullable Object numberFormat) {
        return setScore.apply(objective, scoreHolder, score, displayName, numberFormat);
    }

    /**
     * Creates reset score packet.
     *
     * @param   objective
     *          Objective name
     * @param   scoreHolder
     *          Score holder
     * @return  Reset score packet with given parameters.
     */
    @SneakyThrows
    public Object removeScore(@NotNull String objective, @NotNull String scoreHolder) {
        return removeScore.apply(objective, scoreHolder);
    }
}