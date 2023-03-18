package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.TabListEntry;
import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.Scoreboard;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.Skin;
import me.neznamy.tab.api.tablist.TabList;
import me.neznamy.tab.api.util.ComponentCache;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TabPlayer implementation for Velocity
 */
public class VelocityTabPlayer extends ProxyTabPlayer {

    /** Component cache to save CPU when creating components */
    private static final ComponentCache<IChatBaseComponent, Component> componentCache = new ComponentCache<>(10000,
            (component, clientVersion) -> GsonComponentSerializer.gson().deserialize(component.toString(clientVersion)));

    /** BossBars currently displayed to this player */
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    @Getter private final Scoreboard scoreboard = new VelocityScoreboard(this);

    @Getter private final TabList tabList = new VelocityTabList(this);

    /**
     * Constructs new instance for given player
     *
     * @param   p
     *          velocity player
     */
    public VelocityTabPlayer(Player p) {
        super(p, p.getUniqueId(), p.getUsername(), p.getCurrentServer().get().getServerInfo().getName(), p.getProtocolVersion().getProtocol());
    }
    
    @Override
    public boolean hasPermission0(String permission) {
        return getPlayer().hasPermission(permission);
    }
    
    @Override
    public int getPing() {
        return (int) getPlayer().getPing();
    }
    
    @Override
    public void sendPacket(Object packet) {
        throw new IllegalStateException("No longer supported");
    }

    @Override
    public void sendMessage(IChatBaseComponent message) {
        getPlayer().sendMessage(componentCache.get(message, getVersion()));
    }

    @Override
    public Skin getSkin() {
        if (getPlayer().getGameProfile().getProperties().size() == 0) return null; //offline mode
        return new Skin(getPlayer().getGameProfile().getProperties().get(0).getValue(), getPlayer().getGameProfile().getProperties().get(0).getSignature());
    }
    
    @Override
    public Player getPlayer() {
        return (Player) player;
    }
    
    @Override
    public boolean isOnline() {
        return getPlayer().isActive();
    }

    @Override
    public int getGamemode() {
        for (TabListEntry entry : getPlayer().getTabList().getEntries()) {
            if (entry.getProfile().getId().equals(getTablistId())) return entry.getGameMode();
        }
        return 0;
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull IChatBaseComponent header, @NonNull IChatBaseComponent footer) {
        getPlayer().sendPlayerListHeaderAndFooter(componentCache.get(header, version), componentCache.get(footer, version));
    }

    @Override
    public void sendBossBar(@NonNull UUID id, @NonNull String title, float progress, @NonNull BarColor color, @NonNull BarStyle style) {
        if (bossBars.containsKey(id)) return;
        BossBar bar = BossBar.bossBar(componentCache.get(IChatBaseComponent.optimizedComponent(title), getVersion()),
                progress, Color.valueOf(color.toString()), Overlay.valueOf(style.toString()));
        bossBars.put(id, bar);
        getPlayer().showBossBar(bar);
    }

    @Override
    public void updateBossBar(@NonNull UUID id, @NonNull String title) {
        bossBars.get(id).name(componentCache.get(IChatBaseComponent.optimizedComponent(title), getVersion()));
    }

    @Override
    public void updateBossBar(@NonNull UUID id, float progress) {
        bossBars.get(id).progress(progress);
    }

    @Override
    public void updateBossBar(@NonNull UUID id, @NonNull BarStyle style) {
        bossBars.get(id).overlay(Overlay.valueOf(style.toString()));
    }

    @Override
    public void updateBossBar(@NonNull UUID id, @NonNull BarColor color) {
        bossBars.get(id).color(Color.valueOf(color.toString()));
    }

    @Override
    public void removeBossBar(@NonNull UUID id) {
        getPlayer().hideBossBar(bossBars.remove(id));
    }

    @Override
    public Object getChatSession() {
        return null; // not supported by Velocity
    }

    @Override
    public void sendPluginMessage(byte[] message) {
        try {
            getPlayer().getCurrentServer().ifPresent(server -> server.sendPluginMessage(VelocityTAB.getMinecraftChannelIdentifier(), message));
        } catch (IllegalStateException VelocityBeingVelocityException) {
            // java.lang.IllegalStateException: Not connected to server!
        }
    }
}