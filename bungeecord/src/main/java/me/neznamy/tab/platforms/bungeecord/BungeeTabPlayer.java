package me.neznamy.tab.platforms.bungeecord;

import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.protocol.DefinedPacket;
import org.jetbrains.annotations.NotNull;

/**
 * TabPlayer implementation for BungeeCord
 */
public class BungeeTabPlayer extends ProxyTabPlayer {

    /**
     * Constructs new instance for given player
     *
     * @param   platform
     *          Server platform
     * @param   p
     *          BungeeCord player
     */
    public BungeeTabPlayer(@NotNull BungeePlatform platform, @NotNull ProxiedPlayer p) {
        super(platform, p, p.getUniqueId(), p.getName(), p.getServer() != null ? p.getServer().getInfo().getName() : "-", p.getPendingConnection().getVersion());
    }

    @Override
    public boolean hasPermission0(@NotNull String permission) {
        return getPlayer().hasPermission(permission);
    }

    @Override
    public int getPing() {
        return getPlayer().getPing();
    }

    @Override
    public void sendMessage(@NotNull TabComponent message) {
        getPlayer().sendMessage(getPlatform().transformComponent(message, getVersion()));
    }

    @Override
    @NotNull
    public ProxiedPlayer getPlayer() {
        return (ProxiedPlayer) player;
    }

    @Override
    public BungeePlatform getPlatform() {
        return (BungeePlatform) platform;
    }

    @Override
    public void sendPluginMessage(byte[] message) {
        Server server = getPlayer().getServer();
        if (server != null) server.sendData(TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME, message);
    }

    /**
     * Sends packet to the player. If BungeeCord supports 1.20.2+, new packet queue method is used
     * to avoid error when sending packet in configuration phase.
     *
     * @param   packet
     *          Packet to send
     */
    public void sendPacket(@NotNull DefinedPacket packet) {
        ((UserConnection)getPlayer()).sendPacketQueued(packet);
    }
}