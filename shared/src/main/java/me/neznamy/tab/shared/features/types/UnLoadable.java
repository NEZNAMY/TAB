package me.neznamy.tab.shared.features.types;

/**
 * Interface for features looking to unload players on plugin disable
 */
public interface UnLoadable {

    /**
     * Called on plugin unload.
     */
    void unload();
}
