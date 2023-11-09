package me.neznamy.tab.shared.features.premiumvanish;

import me.neznamy.tab.shared.features.types.TabFeature;
import org.jetbrains.annotations.NotNull;

/**
 * Feature using PremiumVanishAPI to check
 * if a player can see each other
 */
public abstract class PremiumVanishSupport extends TabFeature
{
    /**
     * @return Boolean if viewer can see viewed
     */
    public boolean canSee(Object viewer, Object viewed)
    {
        return false;
    }

    @Override
    @NotNull
    public String getFeatureName() {
        return "PremiumVanishSupport";
    }
}