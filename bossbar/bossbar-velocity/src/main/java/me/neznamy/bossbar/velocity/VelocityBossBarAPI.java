package me.neznamy.bossbar.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.NonNull;
import me.neznamy.bossbar.shared.BossBarAPI;
import me.neznamy.bossbar.shared.SafeBossBarManager;
import me.neznamy.bossbar.shared.impl.AdventureBossBarManager;
import org.jetbrains.annotations.NotNull;

/**
 * BossBarAPI implementation for Velocity.
 */
public class VelocityBossBarAPI extends BossBarAPI<Player> {

    /**
     * Constructs new instance and registers listener for handling server switching.
     *
     * @param   plugin
     *          Plugin instance
     * @param   server
     *          Proxy server instance
     */
    public VelocityBossBarAPI(@NonNull Object plugin, @NonNull ProxyServer server) {
        server.getEventManager().register(plugin, this);
    }

    @Override
    @NotNull
    public SafeBossBarManager<?> createBossBarManager(@NonNull Player player) {
        return new AdventureBossBarManager(player);
    }

    /**
     * Freezes Boss bar for 1.20.2+ players due to bug with adventure that causes disconnect
     * on 1.20.5+ with "Network Protocol Error"
     *
     * @param   e
     *          Event fired before player switches server for proper freezing
     */
    @Subscribe
    public void preConnect(@NotNull ServerPreConnectEvent e) {
        if (!e.getResult().isAllowed()) return;
        if (e.getPlayer().getProtocolVersion().noLessThan(ProtocolVersion.MINECRAFT_1_20_2)) {
            ((SafeBossBarManager<?>)getBossBarManager(e.getPlayer())).freeze();
        }
    }

    /**
     * Unfreezes Boss bar for 1.20.2+ players and resends it.
     *
     * @param   e
     *          Post connect event
     */
    @Subscribe
    public void onConnect(@NotNull ServerPostConnectEvent e) {
        if (e.getPlayer().getProtocolVersion().noLessThan(ProtocolVersion.MINECRAFT_1_20_2)) {
            ((SafeBossBarManager<?>)getBossBarManager(e.getPlayer())).unfreezeAndResend();
        }
    }
}
