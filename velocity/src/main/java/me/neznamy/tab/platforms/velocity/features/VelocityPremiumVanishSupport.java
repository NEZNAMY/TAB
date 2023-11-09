package me.neznamy.tab.platforms.velocity.features;

import com.velocitypowered.api.proxy.Player;
import de.myzelyam.api.vanish.VelocityVanishAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.premiumvanish.PremiumVanishSupport;

/**
 * PremiumVanishSupport implementation for Velocity
 */
public class VelocityPremiumVanishSupport extends PremiumVanishSupport {
    @Override
    public boolean canSee(TabPlayer viewer, TabPlayer viewed)
    {
        Player viewerPlayer = (Player)viewer.getPlayer();
        Player viewedPlayer = (Player)viewed.getPlayer();

        return VelocityVanishAPI.canSee(viewerPlayer, viewedPlayer);
    }
}