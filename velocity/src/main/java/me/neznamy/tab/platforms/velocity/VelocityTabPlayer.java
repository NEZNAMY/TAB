package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.GameProfile;
import lombok.Getter;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.impl.AdventureBossBar;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.impl.BridgeScoreboard;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * TabPlayer implementation for Velocity
 */
@Getter
public class VelocityTabPlayer extends ProxyTabPlayer {

    /** Player's scoreboard */
    @NotNull
    private final BridgeScoreboard scoreboard = new BridgeScoreboard(this);

    /** Player's tab list */
    @NotNull
    private final VelocityTabList tabList = new VelocityTabList(this);

    /** Player's boss bar view */
    @NotNull
    private final BossBar bossBar = new AdventureBossBar(this);

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
        getPlayer().sendMessage(message.toAdventure(getVersion()));
    }

    @Override
    @Nullable
    public TabList.Skin getSkin() {
        List<GameProfile.Property> properties = getPlayer().getGameProfile().getProperties();
        if (properties.isEmpty()) return null; //Offline mode
        return new TabList.Skin(properties.get(0).getValue(), properties.get(0).getSignature());
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