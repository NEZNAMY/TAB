package me.neznamy.tab.platforms.bukkit.hook;

import de.myzelyam.api.vanish.VanishAPI;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.hook.PremiumVanishHook;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * PremiumVanish hook for Bukkit.
 */
public class BukkitPremiumVanishHook extends PremiumVanishHook {

    @Override
    public boolean canSee(@NotNull TabPlayer viewer, @NotNull TabPlayer target) {
        return VanishAPI.canSee(((BukkitTabPlayer)viewer).getPlayer(), ((BukkitTabPlayer)target).getPlayer());
    }

    @Override
    public boolean isVanished(@NotNull TabPlayer player) {
        return VanishAPI.isInvisible(((BukkitTabPlayer)player).getPlayer());
    }
}
