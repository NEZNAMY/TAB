package me.neznamy.tab.shared.features.types;

/**
 * Interface for features looking to load players on plugin enable
 */
public interface Loadable {

    /**
     * Called on plugin (re)load.
     */
    void load();
}
