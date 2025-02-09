package me.neznamy.bossbar.fabric;

import lombok.NonNull;
import me.neznamy.bossbar.shared.BossBarAPI;
import me.neznamy.bossbar.shared.SafeBossBarManager;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * BossBarAPI implementation for Fabric.
 */
public class FabricBossBarAPI extends BossBarAPI<ServerPlayer> {

    @Override
    @NotNull
    public SafeBossBarManager<?> createBossBarManager(@NonNull ServerPlayer player) {
        return new FabricBossBarManager(player);
    }
}
