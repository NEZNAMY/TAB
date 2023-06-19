package me.neznamy.tab.platforms.bungeecord;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import lombok.Getter;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.platforms.bungeecord.tablist.BungeeTabList1193;
import me.neznamy.tab.platforms.bungeecord.tablist.BungeeTabList17;
import me.neznamy.tab.platforms.bungeecord.tablist.BungeeTabList18;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.bossbar.BossBar;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Property;
import net.md_5.bungee.protocol.Protocol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * TabPlayer implementation for BungeeCord
 */
public class BungeeTabPlayer extends ProxyTabPlayer {

    /** Inaccessible bungee internals */
    private static @Nullable Object directionData;
    private static @Nullable Method getId;

    /** Flag tracking plugin presence */
    private static final boolean premiumVanish = ProxyServer.getInstance().getPluginManager().getPlugin("PremiumVanish") != null;

    static {
        try {
            directionData = ReflectionUtils.setAccessible(Protocol.class.getDeclaredField("TO_CLIENT")).get(Protocol.GAME);
            getId = ReflectionUtils.setAccessible(directionData.getClass().getDeclaredMethod("getId", Class.class, int.class));
        } catch (ReflectiveOperationException e) {
            TAB.getInstance().getErrorManager().criticalError("Failed to initialize bungee internal fields", e);
        }
    }

    /** Player's scoreboard */
    @Getter private final @NotNull Scoreboard<BungeeTabPlayer> scoreboard = new BungeeScoreboard(this);

    /** Player's tab list based on version */
    private final @NotNull TabList tabList1_7 = new BungeeTabList17(this);
    private final @NotNull TabList tabList1_8 = new BungeeTabList18(this);
    private final @NotNull TabList tabList1_19_3 = new BungeeTabList1193(this);

    @Getter private final @NotNull BossBar bossBar = new BungeeBossBar(this);

    /**
     * Constructs new instance for given player
     *
     * @param   p
     *          BungeeCord player
     */
    public BungeeTabPlayer(@NotNull ProxiedPlayer p) {
        super(p, p.getUniqueId(), p.getName(), p.getServer() != null ? p.getServer().getInfo().getName() : "-", -1);
    }

    @Override
    public boolean hasPermission0(@NotNull String permission) {
        return getPlayer().hasPermission(permission);
    }

    @Override
    public int getPing() {
        return getPlayer().getPing();
    }

    public void sendPacket(@NotNull Object nmsPacket) {
        getPlayer().unsafe().sendPacket((DefinedPacket) nmsPacket);
    }

    @Override
    public void sendMessage(@NotNull IChatBaseComponent message) {
        getPlayer().sendMessage(ComponentSerializer.parse(message.toString(getVersion())));
    }

    @Override
    public @Nullable TabList.Skin getSkin() {
        LoginResult loginResult = ((InitialHandler)getPlayer().getPendingConnection()).getLoginProfile();
        if (loginResult == null) return null;
        Property[] properties = loginResult.getProperties();
        if (properties == null || properties.length == 0) return null;
        return new TabList.Skin(properties[0].getValue(), properties[0].getSignature());
    }

    @Override
    public @NotNull ProxiedPlayer getPlayer() {
        return (ProxiedPlayer) player;
    }

    /**
     * Returns packet ID for this player of provided packet class
     *
     * @param   clazz
     *          packet class
     * @return  packet ID
     */
    public int getPacketId(@NotNull Class<? extends DefinedPacket> clazz) {
        if (getId == null) return -1;
        try {
            return (int) getId.invoke(directionData, clazz, getPlayer().getPendingConnection().getVersion());
        } catch (ReflectiveOperationException e) {
            TAB.getInstance().getErrorManager().printError("Failed to get packet id for packet " + clazz +
                    " with client version " + getPlayer().getPendingConnection().getVersion(), e);
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
    public @NotNull ProtocolVersion getVersion() {
        return ProtocolVersion.fromNetworkId(getPlayer().getPendingConnection().getVersion());
    }

    @Override
    public boolean isVanished() {
        //noinspection ConstantConditions
        if (premiumVanish && BungeeVanishAPI.isInvisible(getPlayer())) return true;
        return super.isVanished();
    }

    @Override
    public boolean isOnline() {
        return getPlayer().isConnected();
    }

    @Override
    public @NotNull TabList getTabList() {
        return getVersion().getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId() ?
                tabList1_19_3 : getVersion().getMinorVersion() >= 8 ? tabList1_8 : tabList1_7;
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