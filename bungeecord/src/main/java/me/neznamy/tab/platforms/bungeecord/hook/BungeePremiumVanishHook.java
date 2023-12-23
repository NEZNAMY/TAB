package me.neznamy.tab.platforms.bungeecord.hook;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.hook.PremiumVanishHook;
import me.neznamy.tab.shared.platform.Platform;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * PremiumVanish hook for BungeeCord.
 */
public class BungeePremiumVanishHook extends PremiumVanishHook {

    private final boolean canSeeEnabled;

    /**
     * Constructs new instance. If using an old version of PV, warn
     * is printed into the platform's logger.
     *
     * @param   platform
     *          Platform to print potential warn to
     */
    public BungeePremiumVanishHook(@NotNull Platform platform) {
        if (ReflectionUtils.methodExists(BungeeVanishAPI.class, "canSee", ProxiedPlayer.class, ProxiedPlayer.class)) {
            canSeeEnabled = true;
        } else {
            canSeeEnabled = false;
            platform.logWarn(new IChatBaseComponent("Detected an outdated version of " +
                    "PremiumVanish with limited API. Vanish compatibility " +
                    "may not work as expected. Update PremiumVanish to version 2.7.11+ for optimal experience."));
        }
    }

    @Override
    public boolean canSee(@NotNull TabPlayer viewer, @NotNull TabPlayer target) {
        //noinspection ConstantValue
        return canSeeEnabled && BungeeVanishAPI.canSee(((BungeeTabPlayer)viewer).getPlayer(), ((BungeeTabPlayer)target).getPlayer());
    }

    @Override
    public boolean isVanished(@NotNull TabPlayer player) {
        try {
            return BungeeVanishAPI.isInvisible(((BungeeTabPlayer)player).getPlayer());
        } catch (IllegalStateException ignored) {
            // PV Bug: PremiumVanish must be enabled to use its API
        }
        return false;
    }
}
