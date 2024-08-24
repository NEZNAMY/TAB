package me.neznamy.tab.platforms.bukkit.hook;

import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.hook.SayanVanishHook;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.sayandev.sayanvanish.bukkit.api.BukkitUser;
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI;

/**
 * SayanVanish hook for Bukkit.
 */
public class BukkitSayanVanishHook extends SayanVanishHook {

    @Override
    public boolean canSee(@NotNull TabPlayer viewer, @NotNull TabPlayer target) {
        BukkitUser viewerUser = SayanVanishBukkitAPI.getInstance().getUser(viewer.getUniqueId());
        BukkitUser targetUser = SayanVanishBukkitAPI.getOrCreateUser(((BukkitTabPlayer)target).getPlayer());
        return SayanVanishBukkitAPI.getInstance().canSee(viewerUser, targetUser);
    }

    @Override
    public boolean isVanished(@NotNull TabPlayer player) {
        return SayanVanishBukkitAPI.getInstance().isVanished(player.getUniqueId());
    }
}
