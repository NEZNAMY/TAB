package me.neznamy.tab.shared.features.globalplayerlist;

import lombok.Getter;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.features.playerlist.PlayerList;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.OnlinePlayers;
import me.neznamy.tab.shared.util.PerformanceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Feature handler for global PlayerList feature.
 */
public class GlobalPlayerList extends RefreshableFeature implements JoinListener, QuitListener, VanishListener, GameModeListener,
        Loadable, UnLoadable, ServerSwitchListener, TabListClearListener, CustomThreaded, ProxyFeature {

    @Getter private final ThreadExecutor customThread = new ThreadExecutor("TAB Global PlayerList Thread");
    @Getter private OnlinePlayers onlinePlayers;
    @Nullable private final ProxySupport proxy = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PROXY_SUPPORT);
    @NotNull private final GlobalPlayerListConfiguration configuration;
    @Nullable private final PlayerList playerlist = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PLAYER_LIST);

    /**
     * Constructs new instance and registers new placeholders.
     *
     * @param   configuration
     *          Feature configuration
     */
    public GlobalPlayerList(@NotNull GlobalPlayerListConfiguration configuration) {
        this.configuration = configuration;
        TAB.getInstance().getDataManager().applyConfiguration(configuration);
        for (Map.Entry<String, List<String>> entry : configuration.getSharedServers().entrySet()) {
            TAB.getInstance().getPlaceholderManager().registerInternalServerPlaceholder(TabConstants.Placeholder.globalPlayerListGroup(entry.getKey()), 1000, () -> {
                if (onlinePlayers == null) return "0"; // Not loaded yet
                int count = 0;
                for (TabPlayer player : onlinePlayers.getPlayers()) {
                    if (entry.getValue().contains(player.server.getName()) && !player.isVanished()) count++;
                }
                if (proxy != null) {
                    for (ProxyPlayer player : proxy.getProxyPlayers().values()) {
                        if (entry.getValue().contains(player.server.getName()) && !player.isVanished()) count++;
                    }
                }
                return PerformanceUtil.toString(count);
            });
        }
    }

    @Override
    public void load() {
        onlinePlayers =  new OnlinePlayers(TAB.getInstance().getOnlinePlayers());
        if (configuration.isUpdateLatency()) addUsedPlaceholder(TabConstants.Placeholder.PING);
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            for (TabPlayer displayed : onlinePlayers.getPlayers()) {
                if (viewer.server == displayed.server) continue;
                if (shouldSee(viewer, displayed)) {
                    viewer.getTabList().addEntry(getAddInfoData(displayed, viewer));
                }
            }
        }
    }

    /**
     * Returns {@code true} if viewer should see the target player, {@code false} if not.
     *
     * @param   viewer
     *          Player viewing the tablist
     * @param   displayed
     *          Player who is being displayed
     * @return  {@code true} if viewer should see the target, {@code false} if not
     */
    public boolean shouldSee(@NotNull TabPlayer viewer, @NotNull TabPlayer displayed) {
        return viewer.server.canSee(displayed.server) && viewer.canSee(displayed);
    }

    @Override
    public void unload() {
        for (TabPlayer displayed : onlinePlayers.getPlayers()) {
            for (TabPlayer viewer : onlinePlayers.getPlayers()) {
                if (displayed.server != viewer.server) viewer.getTabList().removeEntry(displayed.getTablistId());
            }
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        onlinePlayers.addPlayer(connectedPlayer);
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            if (connectedPlayer.server == all.server) continue;
            if (shouldSee(all, connectedPlayer)) {
                all.getTabList().addEntry(getAddInfoData(connectedPlayer, all));
            }
            if (shouldSee(connectedPlayer, all)) {
                connectedPlayer.getTabList().addEntry(getAddInfoData(all, connectedPlayer));
            }
        }
        if (proxy != null) {
            for (ProxyPlayer proxied : proxy.getProxyPlayers().values()) {
                if (proxied.server != connectedPlayer.server && shouldSee(connectedPlayer, proxied)) {
                    connectedPlayer.getTabList().addEntry(proxied.asEntry());
                }
            }
        }
    }

    @Override
    public void onQuit(@NotNull TabPlayer disconnectedPlayer) {
        onlinePlayers.removePlayer(disconnectedPlayer);
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            all.getTabList().removeEntry(disconnectedPlayer.getTablistId());
        }
    }

    @Override
    public void onServerChange(@NotNull TabPlayer changed, @NotNull Server from, @NotNull Server to) {
        // TODO fix players potentially not appearing on rapid server switching (if anyone reports it)
        // Player who switched server is removed from tablist of other players in ~70-110ms (depending on online count), re-add with a delay
        customThread.executeLater(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            if (!changed.isOnline()) return; // Player disconnected in the meantime
            for (TabPlayer all : onlinePlayers.getPlayers()) {
                // Remove for everyone and add back if visible, easy solution to display-others-as-spectators option
                // Also do not remove/add players from the same server, let backend handle it
                if (all.server != changed.server) {
                    all.getTabList().removeEntry(changed.getTablistId());
                    if (shouldSee(all, changed)) {
                        all.getTabList().addEntry(getAddInfoData(changed, all));
                    }
                }
            }
        }, getFeatureName(), TabConstants.CpuUsageCategory.SERVER_SWITCH), 200);
    }

    @Override
    public void onTabListClear(@NotNull TabPlayer player) {
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            // Ignore players on the same server, since the server already sends add packet
            if (all.server != player.server && shouldSee(player, all)) {
                player.getTabList().addEntry(getAddInfoData(all, player));
            }
        }
        if (proxy != null) {
            for (ProxyPlayer proxied : proxy.getProxyPlayers().values()) {
                if (proxied.server != player.server && shouldSee(player, proxied)) {
                    player.getTabList().addEntry(proxied.asEntry());
                }
            }
        }
    }

    /**
     * Creates new entry of given target player for viewer.
     *
     * @param   p
     *          Displayed player
     * @param   viewer
     *          Player viewing the tablist
     * @return  Entry of target for viewer
     */
    @NotNull
    public TabList.Entry getAddInfoData(@NotNull TabPlayer p, @NotNull TabPlayer viewer) {
        TabComponent format = null;
        if (playerlist != null && !p.tablistData.disabled.get()) {
            format = playerlist.getTabFormat(p, viewer);
        }
        return new TabList.Entry(
                p.getTablistId(),
                p.getNickname(),
                p.getTabList().getSkin(),
                true,
                configuration.isUpdateLatency() ? p.getPing() : 0,
                configuration.isOthersAsSpectators() || (configuration.isVanishedAsSpectators() && p.isVanished()) ? 3 : p.getGamemode(),
                viewer.getVersion().getMinorVersion() >= 8 ? format : null,
                0,
                true
        );
    }

    @Override
    public void onGameModeChange(@NotNull TabPlayer player) {
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            if (player.server != viewer.server) {
                viewer.getTabList().updateGameMode(player, configuration.isOthersAsSpectators() ? 3 : player.getGamemode());
            }
        }
    }

    @Override
    public void onVanishStatusChange(@NotNull TabPlayer p) {
        if (p.isVanished()) {
            for (TabPlayer all : onlinePlayers.getPlayers()) {
                if (all == p) continue;
                if (!shouldSee(all, p)) {
                    all.getTabList().removeEntry(p.getTablistId());
                }
            }
        } else {
            for (TabPlayer viewer : onlinePlayers.getPlayers()) {
                if (viewer == p) continue;
                if (shouldSee(viewer, p)) {
                    viewer.getTabList().addEntry(getAddInfoData(p, viewer));
                }
            }
        }
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Updating latency";
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        //player ping changed, must manually update latency for players on other servers
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            if (refreshed.server != viewer.server && viewer.server.canSee(refreshed.server)) {
                viewer.getTabList().updateLatency(refreshed, refreshed.getPing());
            }
        }
    }

    private boolean shouldSee(@NotNull TabPlayer viewer, @NotNull ProxyPlayer target) {
        // Do not show duplicate player that will be removed in a sec
        if (TAB.getInstance().isPlayerConnected(target.getTablistId())) return false;
        return viewer.server.canSee(target.server) && (!target.isVanished() || viewer.hasPermission(TabConstants.Permission.SEE_VANISHED));
    }

    // ------------------
    // ProxySupport
    // ------------------

    @Override
    public void onJoin(@NotNull ProxyPlayer player) {
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            if (shouldSee(viewer, player) && viewer.server != player.server) {
                viewer.getTabList().addEntry(player.asEntry());
            }
        }
    }

    @Override
    public void onServerSwitch(@NotNull ProxyPlayer player) {
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            if (viewer.server == player.server) continue;
            if (shouldSee(viewer, player)) {
                viewer.getTabList().addEntry(player.asEntry());
            } else {
                viewer.getTabList().removeEntry(player.getTablistId());
            }
        }
    }

    @Override
    public void onQuit(@NotNull ProxyPlayer player) {
        TabPlayer connected = TAB.getInstance().getPlayer(player.getUniqueId());
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            // Make sure to not remove player if they are connected already and added into tablist by the server
            if (player.server != viewer.server && (connected == null || !shouldSee(viewer, connected))) {
                viewer.getTabList().removeEntry(player.getTablistId());
            }
        }
    }

    @Override
    public void onVanishStatusChange(@NotNull ProxyPlayer player) {
        if (player.isVanished()) {
            for (TabPlayer all : onlinePlayers.getPlayers()) {
                if (!shouldSee(all, player)) {
                    all.getTabList().removeEntry(player.getTablistId());
                }
            }
        } else {
            for (TabPlayer viewer : onlinePlayers.getPlayers()) {
                if (shouldSee(viewer, player)) {
                    viewer.getTabList().addEntry(player.asEntry());
                }
            }
        }
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return "Global PlayerList";
    }
}