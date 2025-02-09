package me.neznamy.bossbar.bungee;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.bossbar.shared.BossBarAPI;
import me.neznamy.bossbar.shared.SafeBossBarManager;
import me.neznamy.bossbar.shared.impl.DummyBossBarManager;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.packet.Login;
import org.jetbrains.annotations.NotNull;

/**
 * BossBarAPI implementation for BungeeCord.
 */
public class BungeeBossBarAPI extends BossBarAPI<ProxiedPlayer> implements Listener {

    /**
     * Constructs new instance and registers listener for handling server switching.
     *
     * @param   plugin
     *          Plugin instance
     */
    public BungeeBossBarAPI(@NonNull Plugin plugin) {
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, this);
    }

    @Override
    @NotNull
    public SafeBossBarManager<?> createBossBarManager(@NonNull ProxiedPlayer player) {
        if (player.getPendingConnection().getVersion() >= 107) { // 1.9
            return new BungeeBossBarManager(player);
        } else {
            return new DummyBossBarManager(player);
        }
    }

    /**
     * Freezes Boss bar for 1.20.2+ players to avoid:<p>
     * * 1.20.5+ client disconnect with "Network Protocol Error" when updating a BossBar that was not resent yet
     *
     * @param   e
     *          Event fired before player switches server for proper freezing
     */
    @EventHandler(priority = Byte.MIN_VALUE)
    public void onSwitch(@NotNull ServerSwitchEvent e) {
        if (e.getPlayer().getPendingConnection().getVersion() < 764) return; // Below 1.20.2
        if (e.getFrom() == null) {
            // Join
            ((UserConnection) e.getPlayer()).getCh().getHandle().pipeline().addBefore("inbound-boss", "TAB-BossBar", new BossbarChannelDuplexHandler(e.getPlayer()));
        }
        ((SafeBossBarManager<?>)getBossBarManager(e.getPlayer())).freeze();
    }

    @RequiredArgsConstructor
    private class BossbarChannelDuplexHandler extends ChannelDuplexHandler {

        /** Player this channel belongs to */
        @NotNull
        private final ProxiedPlayer player;

        @Override
        public void write(@NotNull ChannelHandlerContext context, @NotNull Object packet, @NotNull ChannelPromise channelPromise) throws Exception {
            super.write(context, packet, channelPromise);
            if (packet instanceof Login) {
                ((SafeBossBarManager<?>)getBossBarManager(player)).unfreezeAndResend();
            }
        }
    }
}
