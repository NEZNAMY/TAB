package me.neznamy.tab.platforms.bungeecord;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import io.netty.channel.Channel;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.protocol.Skin;
import me.neznamy.tab.api.util.Preconditions;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * TabPlayer implementation for BungeeCord
 */
public class BungeeTabPlayer extends ProxyTabPlayer {

    /** Inaccessible bungee internals */
    private static Method InitialHandler_getLoginProfile;
    private static Method ChannelWrapper_getHandle;
    private static Method LoginResult_Property_getValue;
    private static Method LoginResult_Property_getSignature;
    private static Method LoginResult_getProperties;
    private static Method UserConnection_getGamemode;
    private static Field wrapperField;
    private static Object directionData;
    private static Method getId;

    static {
        try {
            Class<?> initialHandler = Class.forName("net.md_5.bungee.connection.InitialHandler");
            InitialHandler_getLoginProfile = initialHandler.getMethod("getLoginProfile");
            Class<?> channelWrapper = Class.forName("net.md_5.bungee.netty.ChannelWrapper");
            ChannelWrapper_getHandle = channelWrapper.getMethod("getHandle");
            Class<?> loginResult = Class.forName("net.md_5.bungee.connection.LoginResult");
            Class<?> loginResult_Property = Class.forName("net.md_5.bungee.protocol.Property");
            LoginResult_Property_getValue = loginResult_Property.getMethod("getValue");
            LoginResult_Property_getSignature = loginResult_Property.getMethod("getSignature");
            LoginResult_getProperties = loginResult.getMethod("getProperties");
            Class<?> userConnection = Class.forName("net.md_5.bungee.UserConnection");
            UserConnection_getGamemode = userConnection.getMethod("getGamemode");
            Field f = Protocol.class.getDeclaredField("TO_CLIENT");
            f.setAccessible(true);
            directionData = f.get(Protocol.GAME);
            getId = directionData.getClass().getDeclaredMethod("getId", Class.class, int.class);
            getId.setAccessible(true);
            wrapperField = initialHandler.getDeclaredField("ch");
            wrapperField.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            TAB.getInstance().getErrorManager().criticalError("Failed to initialize bungee internal fields", e);
        }
    }

    /**
     * Constructs new instance for given player
     *
     * @param   p
     *          BungeeCord player
     */
    public BungeeTabPlayer(ProxiedPlayer p) {
        super(p, p.getUniqueId(), p.getName(), p.getServer() != null ? p.getServer().getInfo().getName() : "-", -1);
        try {
            channel = (Channel) ChannelWrapper_getHandle.invoke(wrapperField.get(getPlayer().getPendingConnection()));
        } catch (IllegalAccessException | InvocationTargetException e) {
            TAB.getInstance().getErrorManager().criticalError("Failed to get channel of " + getPlayer().getName(), e);
        }
    }

    @Override
    public boolean hasPermission0(String permission) {
        Preconditions.checkNotNull(permission, "permission");
        long time = System.nanoTime();
        boolean value = getPlayer().hasPermission(permission);
        TAB.getInstance().getCPUManager().addMethodTime("hasPermission", System.nanoTime()-time);
        return value;
    }

    @Override
    public int getPing() {
        return getPlayer().getPing();
    }

    @Override
    public void sendPacket(Object nmsPacket) {
        long time = System.nanoTime();
        if (nmsPacket != null && getPlayer().isConnected()) getPlayer().unsafe().sendPacket((DefinedPacket) nmsPacket);
        TAB.getInstance().getCPUManager().addMethodTime("sendPacket", System.nanoTime()-time);
    }

    @Override
    public Skin getSkin() {
        try {
            Object loginResult = InitialHandler_getLoginProfile.invoke(getPlayer().getPendingConnection());
            if (loginResult == null) return null;
            Object[] properties = (Object[]) LoginResult_getProperties.invoke(loginResult);
            if (properties == null || properties.length == 0) return null;
            return new Skin((String) LoginResult_Property_getValue.invoke(properties[0]),
                    (String) LoginResult_Property_getSignature.invoke(properties[0]));
        } catch (ReflectiveOperationException e) {
            TAB.getInstance().getErrorManager().printError("Failed to get skin of " + getName(), e);
            return null;
        }
    }

    @Override
    public ProxiedPlayer getPlayer() {
        return (ProxiedPlayer) player;
    }

    /**
     * Returns packet ID for this player of provided packet class
     *
     * @param   clazz
     *          packet class
     * @return  packet ID
     */
    public int getPacketId(Class<? extends DefinedPacket> clazz) {
        Preconditions.checkNotNull(clazz, "class");
        try {
            return (int) getId.invoke(directionData, clazz, getPlayer().getPendingConnection().getVersion());
        } catch (ReflectiveOperationException e) {
            TAB.getInstance().getErrorManager().printError("Failed to get packet id for packet " + clazz + " with client version " + getPlayer().getPendingConnection().getVersion(), e);
            return -1;
        }
    }

    @Override
    public ProtocolVersion getVersion() {
        return ProtocolVersion.fromNetworkId(getPlayer().getPendingConnection().getVersion());
    }

    @Override
    public boolean isVanished() {
        try {
            if (ProxyServer.getInstance().getPluginManager().getPlugin(TabConstants.Plugin.PREMIUM_VANISH) != null && BungeeVanishAPI.isInvisible(getPlayer())) return true;
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().printError("PremiumVanish v" + TAB.getInstance().getPlatform().getPluginVersion(TabConstants.Plugin.PREMIUM_VANISH) +
                    " generated an error when retrieving vanish status of " + getName(), e);
        }
        return super.isVanished();
    }

    @Override
    public boolean isOnline() {
        return getPlayer().isConnected();
    }

    @Override
    public int getGamemode() {
        try {
            return (int) UserConnection_getGamemode.invoke(player);
        } catch (ReflectiveOperationException e) {
            TAB.getInstance().getErrorManager().printError("Failed to get gamemode of " + getPlayer().getName(), e);
            return 0;
        }
    }

    @Override
    public void sendPluginMessage(byte[] message) {
        Preconditions.checkNotNull(message, "message");
        if (getPlayer().getServer() == null) return;
        getPlayer().getServer().sendData(TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME, message);
        TAB.getInstance().getCPUManager().packetSent("Plugin Message (" + new String(message) + ")");
    }
}