package me.neznamy.tab.shared;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.shared.permission.PermissionPlugin;

/**
 * An interface with methods that are called in universal code,
 * but require platform-specific API calls
 */
public abstract class Platform {

    /** Platform's packet builder implementation */
    private final PacketBuilder packetBuilder;

    /**
     * Constructs new instance with given parameter
     *
     * @param   packetBuilder
     *          Platform's packet builder
     */
    protected Platform(PacketBuilder packetBuilder) {
        this.packetBuilder = packetBuilder;
    }

    /**
     * Calls platform-specific load event.
     * This method is called when plugin is fully enabled.
     */
    public void callLoadEvent(){
        //do nothing by default, old event system will be removed
    }

    /**
     * Calls platform-specific player load event.
     * This method is called when player is fully loaded.
     */
    public void callLoadEvent(TabPlayer player){
        //do nothing by default, old event system will be removed
    }

    /**
     * Returns platform-specific packet builder implementation
     *
     * @return  platform-specific packet builder
     */
    public PacketBuilder getPacketBuilder(){
        return packetBuilder;
    }

    /**
     * Detects permission plugin and returns it's representing object
     *
     * @return  the interface representing the permission hook
     */
    public abstract PermissionPlugin detectPermissionPlugin();

    /**
     * Loads platform-specific features
     */
    public abstract void loadFeatures();

    /**
     * Creates an instance of {@link me.neznamy.tab.api.placeholder.Placeholder}
     * to handle this unknown placeholder (typically a PAPI placeholder)
     *
     * @param   identifier
     *          placeholder's identifier
     */
    public abstract void registerUnknownPlaceholder(String identifier);

    /**
     * Returns {@code true} if this platform is a proxy, {@code false} if a game server
     *
     * @return  {@code true} if this platform is a proxy, {@code false} if a game server
     */
    public abstract boolean isProxy();

    /**
     * Performs platform-specific plugin manager call and returns the result.
     * If plugin is not installed, returns {@code null}.
     *
     * @param   plugin
     *          Plugin to check version of
     * @return  Version string if plugin is installed, {@code null} if not
     */
    public abstract String getPluginVersion(String plugin);

    /**
     * Returns name of default config file for this platform
     * as it appears in the final jar in root directory.
     *
     * @return  name of default config file for this platform
     */
    public abstract String getConfigName();
}