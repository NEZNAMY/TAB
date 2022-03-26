package me.neznamy.tab.shared.permission;

import me.neznamy.tab.api.TabPlayer;

/**
 * An abstract class representing permission plugin hook
 */
public abstract class PermissionPlugin {

    /** Version of the permission plugin */
    private final String version;

    /**
     * Constructs new instance with given version parameter
     *
     * @param   version
     *          version of permission plugin
     */
    protected PermissionPlugin(String version) {
        this.version = version;
    }

    /**
     * Returns primary permission group of player
     *
     * @param   player
     *          player to get group of
     * @return  player's primary permission group
     */
    public abstract String getPrimaryGroup(TabPlayer player);
    
    /**
     * Returns version of the permission plugin
     *
     * @return  version of the permission plugin
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Returns name of the permission plugin
     *
     * @return  name of the permission plugin
     */
    public String getName() {
        return getClass().getSimpleName();
    }
}