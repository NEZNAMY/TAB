package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.proxy.Player;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * TabPlayer implementation for Velocity.
 */
public class VelocityTabPlayer extends ProxyTabPlayer {

    /**
     * Constructs new instance for given player
     *
     * @param   platform
     *          Server platform
     * @param   p
     *          velocity player
     */
    public VelocityTabPlayer(@NotNull VelocityPlatform platform, @NotNull Player p) {
        super(platform, p, p.getUniqueId(), p.getUsername(), p.getCurrentServer().map(s ->
                s.getServerInfo().getName()).orElse("null"), p.getProtocolVersion().getProtocol());
    }
    
    @Override
    public boolean hasPermission0(@NotNull String permission) {
        return getPlayer().hasPermission(permission);
    }
    
    @Override
    public int getPing() {
        return (int) getPlayer().getPing();
    }

    @Override
    public void sendMessage(@NotNull TabComponent message) {
        getPlayer().sendMessage(message.toAdventure());
    }

    @Override
    @NotNull
    public Player getPlayer() {
        return (Player) player;
    }

    @Override
    public VelocityPlatform getPlatform() {
        return (VelocityPlatform) platform;
    }

    @Override
    public void sendPluginMessage(byte[] message) {
        try {
            getPlayer().getCurrentServer().ifPresent(currentServer -> currentServer.sendPluginMessage(getPlatform().getMCI(), message));
        } catch (IllegalStateException VelocityBeingVelocityException) {
            // java.lang.IllegalStateException: Not connected to server!
        }
    }
}