package me.neznamy.tab.platforms.velocity.hook;

import de.myzelyam.api.vanish.VelocityVanishAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.velocity.VelocityTabPlayer;
import me.neznamy.tab.shared.hook.PremiumVanishHook;
import org.jetbrains.annotations.NotNull;

/**
 * PremiumVanish hook for Velocity.
 */
public class VelocityPremiumVanishHook extends PremiumVanishHook {

    @Override
    public synchronized boolean canSee(@NotNull TabPlayer viewer, @NotNull TabPlayer target) {
        return VelocityVanishAPI.canSee(((VelocityTabPlayer)viewer).getPlayer(), ((VelocityTabPlayer)target).getPlayer());
    }

    @Override
    public boolean isVanished(@NotNull TabPlayer player) {
        return VelocityVanishAPI.isInvisible(((VelocityTabPlayer)player).getPlayer());
    }
}
