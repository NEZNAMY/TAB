package me.neznamy.tab.api.feature;

/**
 * Interface for features looking to unload players on plugin disable
 */
public interface UnLoadable {

    /**
     * Called on plugin unload
     */
    void unload();
}
