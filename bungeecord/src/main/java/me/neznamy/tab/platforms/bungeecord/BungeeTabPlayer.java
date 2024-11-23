package me.neznamy.tab.platforms.bungeecord;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        try {
            getPlayer().sendMessage((BaseComponent) message.convert(getVersion()));
        } catch (NullPointerException BungeeCordBug) {
            // TODO remove this at some point in the near future as it got fixed in build #1882 (when 1.21.4 is out and required bungee build gets bumped)
            // java.lang.NullPointerException: Cannot invoke "net.md_5.bungee.protocol.MinecraftEncoder.getProtocol()" because the return value of "io.netty.channel.ChannelPipeline.get(java.lang.Class)" is null
            //	at net.md_5.bungee.netty.ChannelWrapper.getEncodeProtocol(ChannelWrapper.java:51)
            //	at net.md_5.bungee.UserConnection.sendPacketQueued(UserConnection.java:198)
            //	at net.md_5.bungee.UserConnection.sendMessage(UserConnection.java:565)
            //	at net.md_5.bungee.UserConnection.sendMessage(UserConnection.java:520)
            //	at net.md_5.bungee.UserConnection.sendMessage(UserConnection.java:508)
            if (TAB.getInstance().getConfiguration().getConfig().isDebugMode()) {
                TAB.getInstance().getErrorManager().printError("Failed to send message to player " + getName() +
                        " (online = " + getPlayer().isConnected() + "): " + message.convert(getVersion()), BungeeCordBug);
            }
        }
    }

    @Override
    @Nullable
    public TabList.Skin getSkin() {
        LoginResult loginResult = ((InitialHandler)getPlayer().getPendingConnection()).getLoginProfile();
        if (loginResult == null) return null;
        Property[] properties = loginResult.getProperties();
        if (properties == null) return null; //Offline mode
        for (Property property : properties) {
            if (property.getName().equals(TabList.TEXTURES_PROPERTY)) {
                return new TabList.Skin(property.getValue(), property.getSignature());
            }
        }
        return null;
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
        if (!getPlayer().isConnected()) return;
        try {
            getPlayer().getServer().sendData(TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME, message);
        } catch (NullPointerException BungeeCordBug) {
            // TODO remove this at some point in the near future as it got fixed in build #1882 (when 1.21.4 is out and required bungee build gets bumped)
            // java.lang.NullPointerException: Cannot invoke "net.md_5.bungee.protocol.MinecraftEncoder.getProtocol()" because the return value of "io.netty.channel.ChannelPipeline.get(java.lang.Class)" is null
            //        at net.md_5.bungee.netty.ChannelWrapper.getEncodeProtocol(ChannelWrapper.java:51)
            //        at net.md_5.bungee.ServerConnection.sendPacketQueued(ServerConnection.java:48)
            //        at net.md_5.bungee.ServerConnection.sendData(ServerConnection.java:70)
            if (TAB.getInstance().getConfiguration().getConfig().isDebugMode()) {
                TAB.getInstance().getErrorManager().printError("Failed to deliver plugin message to player " + getName() +
                        " (online = " + getPlayer().isConnected() + ")", BungeeCordBug);
            }
        }
    }

    /**
     * Sends packet to the player. If BungeeCord supports 1.20.2+, new packet queue method is used
     * to avoid error when sending packet in configuration phase.
     *
     * @param   packet
     *          Packet to send
     */
    public void sendPacket(@NotNull DefinedPacket packet) {
        if (!getPlayer().isConnected()) return;
        try {
            ((UserConnection)getPlayer()).sendPacketQueued(packet);
        } catch (NullPointerException BungeeCordBug) {
            // TODO remove this at some point in the near future as it got fixed in build #1882 (when 1.21.4 is out and required bungee build gets bumped)
            // java.lang.NullPointerException: Cannot invoke "net.md_5.bungee.protocol.MinecraftEncoder.getProtocol()" because the return value of "io.netty.channel.ChannelPipeline.get(java.lang.Class)" is null
            //        at net.md_5.bungee.netty.ChannelWrapper.getEncodeProtocol(ChannelWrapper.java:51)
            //        at net.md_5.bungee.UserConnection.sendPacketQueued(UserConnection.java:194)
            if (TAB.getInstance().getConfiguration().getConfig().isDebugMode()) {
                TAB.getInstance().getErrorManager().printError("Failed to deliver packet to player " + getName() +
                        " (online = " + getPlayer().isConnected() + "): " + packet, BungeeCordBug);
            }
        }
    }
}