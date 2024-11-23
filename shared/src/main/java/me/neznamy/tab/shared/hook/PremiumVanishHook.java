package me.neznamy.tab.shared.hook;

import me.neznamy.tab.api.integration.VanishIntegration;

/**
 * Class for hooking into PremiumVanish to get vanish status of players.
 */
public abstract class PremiumVanishHook extends VanishIntegration {
    public PremiumVanishHook() {
        super("PremiumVanish");
    }
}
