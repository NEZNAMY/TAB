package me.neznamy.tab.shared.placeholders.expansion;

/**
 * Dummy implementation when expansion is disabled or not supported by platform
 */
public class EmptyTabExpansion implements TabExpansion {

    @Override
    public void unregisterExpansion() {/* Do nothing */}
}
