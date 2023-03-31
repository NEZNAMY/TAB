package me.neznamy.tab.platforms.bungeecord;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.BossBarHandler;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.Scoreboard;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.tablist.Skin;
import me.neznamy.tab.api.tablist.TabList;
import me.neznamy.tab.api.util.ComponentCache;
import me.neznamy.tab.platforms.bungeecord.bossbar.BungeeBossBarHandler;
import me.neznamy.tab.platforms.bungeecord.tablist.BungeeTabList1_19_3;
import me.neznamy.tab.platforms.bungeecord.tablist.BungeeTabList1_7;
import me.neznamy.tab.platforms.bungeecord.tablist.BungeeTabList1_8;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Property;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.LoginRequest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * TabPlayer implementation for BungeeCord
 */
public class BungeeTabPlayer extends ProxyTabPlayer {

    /** Component cache to save CPU when creating components */
    private static final ComponentCache<IChatBaseComponent, BaseComponent[]> componentCache = new ComponentCache<>(10000,
            (component, clientVersion) -> ComponentSerializer.parse(component.toString(clientVersion)));

    /** Inaccessible bungee internals */
    private static Object directionData;
    private static Method getId;

    static {
        try {
            Field f = Protocol.class.getDeclaredField("TO_CLIENT");
            f.setAccessible(true);
            directionData = f.get(Protocol.GAME);
            (getId = directionData.getClass().getDeclaredMethod("getId", Class.class, int.class)).setAccessible(true);
        } catch (ReflectiveOperationException e) {
            TAB.getInstance().getErrorManager().criticalError("Failed to initialize bungee internal fields", e);
        }
    }

    /** Player's scoreboard */
    @Getter private final Scoreboard scoreboard = new BungeeScoreboard(this);

    /** Player's tablist based on version */
    private final TabList tabList1_7 = new BungeeTabList1_7(this);
    private final TabList tabList1_8 = new BungeeTabList1_8(this);
    private final TabList tabList1_19_3 = new BungeeTabList1_19_3(this);

    @Getter private final BossBarHandler bossBarHandler = new BungeeBossBarHandler(this);

    /**
     * Constructs new instance for given player
     *
     * @param   p
     *          BungeeCord player
     */
    public BungeeTabPlayer(ProxiedPlayer p) {
        super(p, p.getUniqueId(), p.getName(), p.getServer() != null ? p.getServer().getInfo().getName() : "-", -1);
    }

    @Override
    public boolean hasPermission0(@NonNull String permission) {
        return getPlayer().hasPermission(permission);
    }

    @Override
    public int getPing() {
        return getPlayer().getPing();
    }

    @Override
    public void sendPacket(Object nmsPacket) {
        getPlayer().unsafe().sendPacket((DefinedPacket) nmsPacket);
    }

    @Override
    public void sendMessage(IChatBaseComponent message) {
        getPlayer().sendMessage(componentCache.get(message, getVersion()));
    }

    @Override
    public Skin getSkin() {
        LoginResult loginResult = ((InitialHandler)getPlayer().getPendingConnection()).getLoginProfile();
        if (loginResult == null) return null;
        Property[] properties = loginResult.getProperties();
        if (properties == null || properties.length == 0) return null;
        return new Skin(properties[0].getValue(), properties[0].getSignature());
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
    public int getPacketId(@NonNull Class<? extends DefinedPacket> clazz) {
        try {
            return (int) getId.invoke(directionData, clazz, getPlayer().getPendingConnection().getVersion());
        } catch (ReflectiveOperationException e) {
            TAB.getInstance().getErrorManager().printError("Failed to get packet id for packet " + clazz + " with client version " + getPlayer().getPendingConnection().getVersion(), e);
            return -1;
        }
    }

    /**
     * If ViaVersion is installed on BungeeCord, it changes protocol to match version
     * of server to which player is connected to. For that reason, we need to retrieve
     * the field more often than just on join.
     *
     * @return  Player's current protocol version
     */
    @Override
    public ProtocolVersion getVersion() {
        return ProtocolVersion.fromNetworkId(getPlayer().getPendingConnection().getVersion());
    }

    @Override
    public boolean isVanished() {
        try {
            if (ProxyServer.getInstance().getPluginManager().getPlugin(TabConstants.Plugin.PREMIUM_VANISH) != null &&
                    (boolean) Class.forName("de.myzelyam.api.vanish.BungeeVanishAPI").getMethod("isInvisible", ProxiedPlayer.class).invoke(null, getPlayer())) return true;
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
        return ((UserConnection)player).getGamemode();
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull IChatBaseComponent header, @NonNull IChatBaseComponent footer) {
        getPlayer().setTabHeader(componentCache.get(header, getVersion()), componentCache.get(footer, getVersion()));
    }

    @Override
    public TabList getTabList() {
        return getVersion().getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId() ?
                tabList1_19_3 : getVersion().getMinorVersion() >= 8 ? tabList1_8 : tabList1_7;
    }

    @Override
    public Object getChatSession() {
        LoginRequest login = ((InitialHandler)getPlayer().getPendingConnection()).getLoginRequest();
        return new Object[]{login.getUuid(), login.getPublicKey()}; // BungeeCord has no single object for this
    }

    @Override
    public void sendPluginMessage(byte[] message) {
        if (getPlayer().getServer() == null) {
            TAB.getInstance().getErrorManager().printError("Skipped plugin message send to " + getName() + ", because player is not" +
                    "connected to any server (message=" + new String(message) + ")");
            return;
        }
        getPlayer().getServer().sendData(TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME, message);
    }
}