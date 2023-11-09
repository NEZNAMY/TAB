package me.neznamy.tab.platforms.velocity.features;

import com.velocitypowered.api.proxy.Player;
import de.myzelyam.api.vanish.VelocityVanishAPI;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.premiumvanish.PremiumVanishSupport;

/**
 * PremiumVanishSupport implementation for Velocity
 */
public class VelocityPremiumVanishSupport extends PremiumVanishSupport
{
    @Override
    public boolean canSee(Object viewerObj, Object viewedObj)
    {
        if(viewerObj instanceof Player && viewedObj instanceof Player)
        {
            Player viewer = (Player)viewerObj;
            Player viewed = (Player)viewedObj;

            return VelocityVanishAPI.canSee(viewer, viewed);
        }

        TAB.getInstance().getErrorManager().printError("ERROR: Invalid instance!");
        return true;
    }
}