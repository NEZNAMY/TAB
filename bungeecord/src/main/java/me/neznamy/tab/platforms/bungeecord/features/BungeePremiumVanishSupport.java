package me.neznamy.tab.platforms.bungeecord.features;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.premiumvanish.PremiumVanishSupport;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * PremiumVanishSupport implementation for Velocity
 */
public class BungeePremiumVanishSupport extends PremiumVanishSupport {
    @Override
    public boolean canSee(TabPlayer viewer, TabPlayer viewed)
    {
        ProxiedPlayer viewerPlayer = (ProxiedPlayer)viewer.getPlayer();
        ProxiedPlayer viewedPlayer = (ProxiedPlayer)viewed.getPlayer();

        return BungeeVanishAPI.canSee(viewerPlayer, viewedPlayer);
    }
}