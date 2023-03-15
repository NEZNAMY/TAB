package me.neznamy.tab.platforms.bungeecord;

import lombok.NonNull;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.Skin;
import me.neznamy.tab.api.util.ComponentCache;
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
import net.md_5.bungee.protocol.packet.BossBar;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;
import net.md_5.bungee.protocol.packet.ScoreboardScore;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

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
    public void sendBossBar(@NonNull UUID id, @NonNull String title, float progress, @NonNull BarColor color, @NonNull BarStyle style) {
        if (getVersion().getMinorVersion() < 9) return;
        BossBar bossbar = new BossBar(id, 0);
        bossbar.setHealth(progress);
        bossbar.setTitle(IChatBaseComponent.optimizedComponent(title).toString(getVersion()));
        bossbar.setColor(color.ordinal());
        bossbar.setDivision(style.ordinal());
        getPlayer().unsafe().sendPacket(bossbar);
    }

    @Override
    public void updateBossBar(@NonNull UUID id, @NonNull String title) {
        if (getVersion().getMinorVersion() < 9) return;
        BossBar bossbar = new BossBar(id, 3);
        bossbar.setTitle(IChatBaseComponent.optimizedComponent(title).toString(getVersion()));
        getPlayer().unsafe().sendPacket(bossbar);
    }

    @Override
    public void updateBossBar(@NonNull UUID id, float progress) {
        if (getVersion().getMinorVersion() < 9) return;
        BossBar bossbar = new BossBar(id, 2);
        bossbar.setHealth(progress);
        getPlayer().unsafe().sendPacket(bossbar);
    }

    @Override
    public void updateBossBar(@NonNull UUID id, @NonNull BarStyle style) {
        if (getVersion().getMinorVersion() < 9) return;
        BossBar bossbar = new BossBar(id, 4);
        bossbar.setDivision(style.ordinal());
        getPlayer().unsafe().sendPacket(bossbar);
    }

    @Override
    public void updateBossBar(@NonNull UUID id, @NonNull BarColor color) {
        if (getVersion().getMinorVersion() < 9) return;
        BossBar bossbar = new BossBar(id, 4);
        bossbar.setDivision(color.ordinal());
        getPlayer().unsafe().sendPacket(bossbar);
    }

    @Override
    public void removeBossBar(@NonNull UUID id) {
        if (getVersion().getMinorVersion() < 9) return;
        getPlayer().unsafe().sendPacket(new BossBar(id, 1));
    }

    @Override
    public void setObjectiveDisplaySlot(int slot, @NonNull String objective) {
        getPlayer().unsafe().sendPacket(new ScoreboardDisplay((byte)slot, objective));
    }

    @Override
    public void setScoreboardScore0(@NonNull String objective, @NonNull String player, int score) {
        getPlayer().unsafe().sendPacket(new ScoreboardScore(player, (byte) 0, objective, score));

    }

    @Override
    public void removeScoreboardScore0(@NonNull String objective, @NonNull String player) {
        getPlayer().unsafe().sendPacket(new ScoreboardScore(player, (byte) 1, objective, 0));
    }

    @Override
    public Object getProfilePublicKey() {
        return ((InitialHandler)getPlayer().getPendingConnection()).getLoginRequest().getPublicKey();
    }

    @Override
    public UUID getChatSessionId() {
        return ((InitialHandler)getPlayer().getPendingConnection()).getLoginRequest().getUuid();
    }

    @Override
    public void sendPluginMessage(byte[] message) {
        if (getPlayer().getServer() == null) return;
        getPlayer().getServer().sendData(TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME, message);
    }
}