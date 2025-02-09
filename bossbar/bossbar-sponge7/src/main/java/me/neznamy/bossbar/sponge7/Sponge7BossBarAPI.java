package me.neznamy.bossbar.sponge7;

import lombok.NonNull;
import me.neznamy.bossbar.shared.BossBarAPI;
import me.neznamy.bossbar.shared.SafeBossBarManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;

/**
 * BossBarAPI implementation for Sponge 7.
 */
public class Sponge7BossBarAPI extends BossBarAPI<Player> {

    @Override
    @NotNull
    public SafeBossBarManager<?> createBossBarManager(@NonNull Player player) {
        return new Sponge7BossBarManager(player);
    }
}
