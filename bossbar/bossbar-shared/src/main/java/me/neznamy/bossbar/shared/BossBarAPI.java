package me.neznamy.bossbar.shared;

import com.google.common.collect.MapMaker;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Entry point of the boss bar API.
 *
 * @param   <P>
 *          Platform's player type
 */
public abstract class BossBarAPI<P> {

    /** Map of saved online players and their bossbar managers */
    protected final Map<P, SafeBossBarManager<?>> players = new MapMaker().weakKeys().makeMap();

    /** Instance of this class */
    @Setter
    @Getter
    private static BossBarAPI<?> instance;

    /**
     * Creates a new BossBar object for the given player, adds it to the tracking map and returns it.
     *
     * @param   player
     *          Player to create BossBar for
     * @return  New BossBar for the player
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public BossBarManager getBossBarManager(@NonNull Object player) {
        return players.computeIfAbsent((P) player, this::createBossBarManager);
    }

    /**
     * Creates a new BossBar object for the given player.
     *
     * @param   player
     *          Player to create BossBar for
     * @return  New BossBar for the player
     */
    @NotNull
    public abstract SafeBossBarManager<?> createBossBarManager(@NonNull P player);
}
