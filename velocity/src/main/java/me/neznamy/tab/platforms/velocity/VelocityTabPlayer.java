package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.GameProfile;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
    @Nullable
    public TabList.Skin getSkin() {
        List<GameProfile.Property> properties = getPlayer().getGameProfile().getProperties();
        if (properties.isEmpty()) return null; //Offline mode
        for (GameProfile.Property property : properties) {
            if (property.getName().equals(TabList.TEXTURES_PROPERTY)) {
                return new TabList.Skin(property.getValue(), property.getSignature());
            }
        }
        return null;
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
            getPlayer().getCurrentServer().ifPresent(server -> server.sendPluginMessage(getPlatform().getMCI(), message));
        } catch (IllegalStateException VelocityBeingVelocityException) {
            // java.lang.IllegalStateException: Not connected to server!
        }
    }
}