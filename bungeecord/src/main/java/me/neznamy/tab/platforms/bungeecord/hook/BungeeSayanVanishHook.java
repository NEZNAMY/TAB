package me.neznamy.tab.platforms.bungeecord.hook;

import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.hook.SayanVanishHook;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.sayandev.sayanvanish.bungeecord.api.BungeeUser;
import org.sayandev.sayanvanish.bungeecord.api.SayanVanishBungeeAPI;

/**
 * SayanVanish hook for BungeeCord.
 */
public class BungeeSayanVanishHook extends SayanVanishHook {

    @Override
    public boolean canSee(@NotNull TabPlayer viewer, @NotNull TabPlayer target) {
        BungeeUser viewerUser = SayanVanishBungeeAPI.getInstance().getUser(viewer.getUniqueId());
        BungeeUser targetUser = SayanVanishBungeeAPI.getOrCreateUser(((BungeeTabPlayer)target).getPlayer());
        return SayanVanishBungeeAPI.getInstance().canSee(viewerUser, targetUser);
    }

    @Override
    public boolean isVanished(@NotNull TabPlayer player) {
        return SayanVanishBungeeAPI.getInstance().isVanished(player.getUniqueId());
    }
}
