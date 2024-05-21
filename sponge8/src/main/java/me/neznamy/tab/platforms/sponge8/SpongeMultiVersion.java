package me.neznamy.tab.platforms.sponge8;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.scoreboard.objective.Objective;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * Class managing code that varies based on server version.
 */
@SuppressWarnings("unchecked")
public class SpongeMultiVersion {

    /** Function for getting score from an objective */
    public static final BiFunction<Objective, String, Score> findOrCreateScore;

    /** Function for getting player's ping*/
    public static final ToIntFunction<ServerPlayer> getPing;

    /** Function for getting UUID from disconnect event */
    public static final Function<ServerSideConnectionEvent.Disconnect, UUID> getUniqueId;

    static {
        if (ReflectionUtils.methodExists(Objective.class, "findOrCreateScore", String.class)) {
            // Sponge 11+ (1.20.4+)
            findOrCreateScore = (objective, holder) -> {
                try {
                    return (Score) Objective.class.getMethod("findOrCreateScore", String.class).invoke(objective, holder);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        } else {
            // Sponge 8 - 10 (and early 11) (1.16.5 - 1.20.2)
            findOrCreateScore = (objective, holder) -> objective.findOrCreateScore(TabComponent.optimized(holder).toAdventure(ProtocolVersion.LATEST_KNOWN_VERSION));
        }

        if (!ReflectionUtils.classExists("org.spongepowered.api.network.ServerPlayerConnection")) {
            // Sponge 11+ (1.20.6+)
            getPing = player -> {
                try {
                    Object connection = player.getClass().getMethod("connection").invoke(player);
                    Optional<Object> state = (Optional<Object>) connection.getClass().getMethod("state").invoke(connection);
                    return (int) state.get().getClass().getMethod("latency").invoke(state.get());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        } else {
            // Sponge 8 - 10 (and early 11) (1.16.5 - 1.20.4)
            getPing = player -> player.connection().latency();
        }

        if (!ReflectionUtils.methodExists(ServerSideConnectionEvent.Disconnect.class, "player")) {
            // Sponge 11+ (1.20.6+)
            getUniqueId = event -> {
                try {
                    Optional<GameProfile> profile = (Optional<GameProfile>) ReflectionUtils.setAccessible(event.getClass().getMethod("profile")).invoke(event);
                    return profile.get().uuid();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        } else {
            // Sponge 8 - 10 (and early 11) (1.16.5 - 1.20.4)
            getUniqueId = event -> event.player().uniqueId();
        }
    }
}
