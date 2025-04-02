package me.neznamy.tab.platforms.bukkit.scoreboard.packet;

import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.util.function.BiFunctionWithException;
import me.neznamy.tab.shared.util.function.QuintFunction;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.Optional;

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
                "network.protocol.game.PacketPlayOutScoreboardScore" // Bukkit
        );
        if (BukkitReflection.is1_20_3Plus()) {
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
    public Object setScore(@NonNull String objective, @NonNull String scoreHolder, int score,
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
    public Object removeScore(@NonNull String objective, @NonNull String scoreHolder) {
        return removeScore.apply(objective, scoreHolder);
    }
}