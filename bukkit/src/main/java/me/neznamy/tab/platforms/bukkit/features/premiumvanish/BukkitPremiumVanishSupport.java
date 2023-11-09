package me.neznamy.tab.platforms.bukkit.features.premiumvanish;

import de.myzelyam.api.vanish.VanishAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.premiumvanish.PremiumVanishSupport;
import org.bukkit.entity.Player;

/**
 * PremiumVanishSupport implementation for Bukkit
 */
public class BukkitPremiumVanishSupport extends PremiumVanishSupport {
    @Override
    public boolean canSee(TabPlayer viewer, TabPlayer viewed)
    {
        Player viewerPlayer = (Player)viewer.getPlayer();
        Player viewedPlayer = (Player)viewed.getPlayer();

        return VanishAPI.canSee(viewerPlayer, viewedPlayer);
    }
}