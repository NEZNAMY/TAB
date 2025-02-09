package me.neznamy.bossbar.shared.impl;

import lombok.NonNull;
import me.neznamy.bossbar.shared.BossBarAPI;
import me.neznamy.bossbar.shared.SafeBossBarManager;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

/**
 * BossBarAPI implementation using Adventure API.
 */
public class AdventureBossBarAPI extends BossBarAPI<Audience> {

    @Override
    @NotNull
    public SafeBossBarManager<?> createBossBarManager(@NonNull Audience player) {
        return new AdventureBossBarManager(player);
    }
}
