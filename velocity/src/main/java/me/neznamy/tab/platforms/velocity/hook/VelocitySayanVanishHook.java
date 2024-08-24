package me.neznamy.tab.platforms.velocity.hook;

import lombok.extern.java.Log;
import me.neznamy.tab.platforms.velocity.VelocityTabPlayer;
import me.neznamy.tab.shared.hook.SayanVanishHook;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI;
import org.sayandev.sayanvanish.velocity.api.VelocityUser;

import java.util.logging.Logger;

/**
 * SayanVanish hook for Velocity.
 */
public class VelocitySayanVanishHook extends SayanVanishHook {

    @Override
    public boolean canSee(@NotNull TabPlayer viewer, @NotNull TabPlayer target) {
        VelocityUser viewerUser = SayanVanishVelocityAPI.getInstance().getUser(viewer.getUniqueId());
        VelocityUser targetUser = SayanVanishVelocityAPI.getOrCreateUser(((VelocityTabPlayer)target).getPlayer());
        return SayanVanishVelocityAPI.getInstance().canSee(viewerUser, targetUser);
    }

    @Override
    public boolean isVanished(@NotNull TabPlayer player) {
        return SayanVanishVelocityAPI.getInstance().isVanished(player.getUniqueId());
    }
}
