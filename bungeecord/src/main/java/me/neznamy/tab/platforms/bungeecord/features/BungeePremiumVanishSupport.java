package me.neznamy.tab.platforms.bungeecord.features;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.premiumvanish.PremiumVanishSupport;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * PremiumVanishSupport implementation for Velocity
 */
public class BungeePremiumVanishSupport extends PremiumVanishSupport {
    @Override
    public boolean canSee(Object viewerObj, Object viewedObj)
    {
        if(viewerObj instanceof ProxiedPlayer && viewedObj instanceof ProxiedPlayer)
        {
            ProxiedPlayer viewer = (ProxiedPlayer)viewerObj;
            ProxiedPlayer viewed = (ProxiedPlayer)viewedObj;

            return BungeeVanishAPI.canSee(viewer, viewed);
        }

        TAB.getInstance().getErrorManager().printError("ERROR: Invalid instance!");
        return true;
    }
}