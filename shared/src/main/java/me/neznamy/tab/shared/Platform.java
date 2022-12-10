package me.neznamy.tab.shared;

import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import org.slf4j.Logger;

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
     * Returns platform-specific packet builder implementation
     *
     * @return  platform-specific packet builder
     */
    public PacketBuilder getPacketBuilder(){
        return packetBuilder;
    }

    public void sendConsoleMessage(String message, boolean translateColors) {
        Object logger = TAB.getInstance().getLogger();
        if (logger instanceof java.util.logging.Logger) {
            ((java.util.logging.Logger) logger).info(translateColors ? EnumChatFormat.color(message) : message);
        } else if (logger instanceof Logger) {
            ((Logger) logger).info(translateColors ? EnumChatFormat.color(message) : message);
        }
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
     * Performs platform-specific plugin manager call and returns the result.
     * If plugin is not installed, returns {@code null}.
     *
     * @param   plugin
     *          Plugin to check version of
     * @return  Version string if plugin is installed, {@code null} if not
     */
    public abstract String getPluginVersion(String plugin);
}