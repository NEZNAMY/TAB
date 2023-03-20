package me.neznamy.tab.api;

import me.neznamy.tab.api.feature.TabFeature;

import java.util.UUID;

/**
 * Feature manager allows registration and work with features.
 */
public interface FeatureManager {

    /**
     * Registers a feature, which will start receiving events
     * 
     * @param   name
     *          name of feature
     * @param   feature
     *          the handler
     */
    void registerFeature(String name, TabFeature feature);
    
    /**
     * Unregisters feature making it no longer receive events. This does not run unload method nor cancel
     * tasks created by the feature
     *
     * @param   name
     *          feature name defined in registerFeature
     */
    void unregisterFeature(String name);
    
    /**
     * Returns whether a feature with said name is registered or not
     * 
     * @param   name
     *          name of feature defined in registerFeature method
     * @return  true if feature exists, false if not
     */
    boolean isFeatureEnabled(String name);

    /**
     * Returns feature handler by its name
     * 
     * @param   name
     *          name of feature defined in registerFeature method
     * @return  the feature or null if feature does not exist
     */
    TabFeature getFeature(String name);

    /**
     * Calls onQuit(TabPlayer) to all enabled features
     *
     * @param   disconnectedPlayer
     *          player who disconnected
     */
    void onQuit(TabPlayer disconnectedPlayer);

    /**
     * Calls onJoin(TabPlayer) to all enabled features
     *
     * @param   connectedPlayer
     *          player who connected
     */
    void onJoin(TabPlayer connectedPlayer);

    /**
     * Calls onServerChange(TabPlayer, String, String) to all enabled features
     *
     * @param   playerUUID
     *          player who switched server
     * @param   to
     *          name of the new server
     */
    void onServerChange(UUID playerUUID, String to);

    /**
     * Calls onWorldChange(TabPlayer, String, String) to all enabled features
     *
     * @param   playerUUID
     *          player who switched world
     * @param   to
     *          name of the new world
     */
    void onWorldChange(UUID playerUUID, String to);

    /**
     * Calls onCommand(TabPlayer, String) to all enabled features
     *
     * @param   sender
     *          command sender
     * @param   command
     *          command line including /
     * @return  {@code true} if some feature marked the command for cancel, {@code false} if not
     */
    boolean onCommand(TabPlayer sender, String command);

    /**
     * Returns array of all currently enabled features
     *
     * @return  array of all currently enabled features
     */
    TabFeature[] getValues();
}