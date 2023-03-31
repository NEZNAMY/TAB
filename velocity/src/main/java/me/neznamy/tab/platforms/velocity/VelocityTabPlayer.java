package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.TabListEntry;
import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.BossBarHandler;
import me.neznamy.tab.api.Scoreboard;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.tablist.Skin;
import me.neznamy.tab.api.tablist.TabList;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;

/**
 * TabPlayer implementation for Velocity
 */
public class VelocityTabPlayer extends ProxyTabPlayer {

    @Getter private final Scoreboard scoreboard = new VelocityScoreboard(this);
    @Getter private final TabList tabList = new VelocityTabList(this);
    @Getter private final BossBarHandler bossBarHandler = new VelocityBossBarHandler(this);

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
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendMessage(IChatBaseComponent message) {
        getPlayer().sendMessage(VelocityTAB.getComponentCache().get(message, getVersion()));
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
        getPlayer().sendPlayerListHeaderAndFooter(VelocityTAB.getComponentCache().get(header, version), VelocityTAB.getComponentCache().get(footer, version));
    }

    @Override
    public Object getChatSession() {
        return null; // not supported by Velocity
    }

    @Override
    public void sendPluginMessage(byte[] message) {
        try {
            getPlayer().getCurrentServer().ifPresentOrElse(
                    server -> server.sendPluginMessage(VelocityTAB.getMinecraftChannelIdentifier(), message),
                    () -> error(message)
            );
        } catch (IllegalStateException VelocityBeingVelocityException) {
            // java.lang.IllegalStateException: Not connected to server!
            error(message);
        }
    }

    private void error(byte[] message) {
        TAB.getInstance().getErrorManager().printError("Skipped plugin message send to " + getName() + ", because player is not" +
                "connected to any server (message=" + new String(message) + ")");
    }
}