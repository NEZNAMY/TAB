package me.neznamy.tab.platforms.sponge8;

import lombok.SneakyThrows;
import me.neznamy.tab.shared.util.function.BiFunctionWithException;
import me.neznamy.tab.shared.util.function.FunctionWithException;
import me.neznamy.tab.shared.util.ReflectionUtils;
import me.neznamy.tab.shared.util.function.ToIntFunction;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.objective.Objective;

import java.util.Optional;
import java.util.UUID;

/**
 * Class managing code that varies based on server version.
 */
@SuppressWarnings("unchecked")
public class SpongeMultiVersion {

    /** Function for getting score from an objective */
    private static final BiFunctionWithException<Objective, String, Score> findOrCreateScore;

    /** Function for getting player's ping*/
    public static final ToIntFunction<ServerPlayer> getPing;

    /** Function for getting UUID from disconnect event */
    public static final FunctionWithException<ServerSideConnectionEvent.Disconnect, UUID> getUniqueId;

    static {
        if (ReflectionUtils.methodExists(Objective.class, "findOrCreateScore", String.class)) {
            // Sponge 11+ (1.20.4+)
            findOrCreateScore = (objective, holder) -> (Score) Objective.class.getMethod("findOrCreateScore", String.class).invoke(objective, holder);
        } else {
            // Sponge 8 - 10 (and early 11) (1.16.5 - 1.20.2)
            findOrCreateScore = (objective, holder) -> objective.findOrCreateScore(Component.text(holder));
        }

        if (!ReflectionUtils.classExists("org.spongepowered.api.network.ServerPlayerConnection")) {
            // Sponge 11+ (1.20.6+)
            getPing = player -> {
                Object connection = player.getClass().getMethod("connection").invoke(player);
                Optional<Object> state = (Optional<Object>) connection.getClass().getMethod("state").invoke(connection);
                return (int) state.get().getClass().getMethod("latency").invoke(state.get());
            };
        } else {
            // Sponge 8 - 10 (and early 11) (1.16.5 - 1.20.4)
            getPing = player -> player.connection().latency();
        }

        if (!ReflectionUtils.methodExists(ServerSideConnectionEvent.Disconnect.class, "player")) {
            // Sponge 11+ (1.20.6+)
            getUniqueId = event -> ((Optional<GameProfile>) ReflectionUtils.setAccessible(event.getClass().getMethod("profile")).invoke(event)).get().uuid();
        } else {
            // Sponge 8 - 10 (and early 11) (1.16.5 - 1.20.4)
            getUniqueId = event -> event.player().uniqueId();
        }
    }

    /**
     * Returns score of holder in given objective. If it does not exist, it is created.
     *
     * @param   objective
     *          Objective to get score from
     * @param   holder
     *          Score holder
     * @return  Score of given holder in objective
     */
    @SneakyThrows
    @NotNull
    public static Score findOrCreateScore(@NotNull Objective objective, @NotNull String holder) {
        return findOrCreateScore.apply(objective, holder);
    }
}
