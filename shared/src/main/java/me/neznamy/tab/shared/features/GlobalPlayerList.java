package me.neznamy.tab.shared.features;

import java.util.*;
import java.util.stream.Collectors;

import lombok.Getter;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.util.OnlinePlayers;
import org.jetbrains.annotations.NotNull;

/**
 * Feature handler for global PlayerList feature.
 */
public class GlobalPlayerList extends RefreshableFeature implements JoinListener, QuitListener, VanishListener, GameModeListener,
        Loadable, UnLoadable, ServerSwitchListener, TabListClearListener, CustomThreaded {

    @Getter
    private final ThreadExecutor customThread = new ThreadExecutor("TAB Global PlayerList Thread");

    @Getter
    private OnlinePlayers onlinePlayers;

    // config options
    private final List<String> spyServers = config().getStringList("global-playerlist.spy-servers",
            Collections.singletonList("spyserver1")).stream().map(String::toLowerCase).collect(Collectors.toList());
    private final Map<String, List<String>> sharedServers = config().getConfigurationSection("global-playerlist.server-groups");
    private final boolean othersAsSpectators = config().getBoolean("global-playerlist.display-others-as-spectators", false);
    private final boolean vanishedAsSpectators = config().getBoolean("global-playerlist.display-vanished-players-as-spectators", true);
    private final boolean isolateUnlistedServers = config().getBoolean("global-playerlist.isolate-unlisted-servers", false);
    private final Map<String, String> serverToGroup = new HashMap<>();
    private final PlayerList playerlist = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PLAYER_LIST);

    /**
     * Constructs new instance and registers new placeholders.
     */
    public GlobalPlayerList() {
        super("Global PlayerList", "Updating latency");
        for (Map.Entry<String, List<String>> entry : sharedServers.entrySet()) {
            TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(TabConstants.Placeholder.globalPlayerListGroup(entry.getKey()), 1000, () -> {
                int count = 0;
                for (TabPlayer player : onlinePlayers.getPlayers()) {
                    if (entry.getValue().contains(player.server) && !player.isVanished()) count++;
                }
                return Integer.toString(count);
            });
        }
    }

    @Override
    public void load() {
        onlinePlayers =  new OnlinePlayers(TAB.getInstance().getOnlinePlayers());
        addUsedPlaceholder(TabConstants.Placeholder.PING);
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            for (TabPlayer displayed : onlinePlayers.getPlayers()) {
                if (viewer.server.equals(displayed.server)) continue;
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
        if (displayed == viewer) return true;
        if (!TAB.getInstance().getPlatform().canSee(viewer, displayed)) return false;
        if (isSpyServer(viewer.server)) return true;
        return getServerGroup(viewer.server).equals(getServerGroup(displayed.server));
    }

    /**
     * Returns server group of specified server. If not part of any group,
     * returns either default or unique name if isolate unlisted servers is enabled.
     *
     * @param   playerServer
     *          Server to get group of
     * @return  Name of server group for this server
     */
    @NotNull
    public synchronized String getServerGroup(@NotNull String playerServer) {
        return serverToGroup.computeIfAbsent(playerServer, server -> {
            for (Map.Entry<String, List<String>> group : sharedServers.entrySet()) {
                for (String serverDefinition : group.getValue()) {
                    if (serverDefinition.endsWith("*")) {
                        if (server.toLowerCase().startsWith(serverDefinition.substring(0, serverDefinition.length()-1).toLowerCase()))
                            return group.getKey();
                    } else if (serverDefinition.startsWith("*")) {
                        if (server.toLowerCase().endsWith(serverDefinition.substring(1).toLowerCase()))
                            return group.getKey();
                    }  else {
                        if (server.equalsIgnoreCase(serverDefinition))
                            return group.getKey();
                    }
                }
            }
            return isolateUnlistedServers ? "isolated:" + server : "DEFAULT";
        });
    }

    @Override
    public void unload() {
        for (TabPlayer displayed : onlinePlayers.getPlayers()) {
            for (TabPlayer viewer : onlinePlayers.getPlayers()) {
                if (!displayed.server.equals(viewer.server)) viewer.getTabList().removeEntry(displayed.getTablistId());
            }
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        onlinePlayers.addPlayer(connectedPlayer);
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            if (connectedPlayer.server.equals(all.server)) continue;
            if (shouldSee(all, connectedPlayer)) {
                all.getTabList().addEntry(getAddInfoData(connectedPlayer, all));
            }
            if (shouldSee(connectedPlayer, all)) {
                connectedPlayer.getTabList().addEntry(getAddInfoData(all, connectedPlayer));
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
    public void onServerChange(@NotNull TabPlayer changed, @NotNull String from, @NotNull String to) {
        // TODO fix players potentially not appearing on rapid server switching (if anyone reports it)
        // Player who switched server is removed from tablist of other players in ~70-110ms (depending on online count), re-add with a delay
        customThread.executeLater(() -> {
            for (TabPlayer all : onlinePlayers.getPlayers()) {
                // Remove for everyone and add back if visible, easy solution to display-others-as-spectators option
                // Also do not remove/add players from the same server, let backend handle it
                if (!all.server.equals(changed.server)) {
                    all.getTabList().removeEntry(changed.getTablistId());
                    if (shouldSee(all, changed)) {
                        all.getTabList().addEntry(getAddInfoData(changed, all));
                    }
                }
            }
        }, getFeatureName(), TabConstants.CpuUsageCategory.SERVER_SWITCH, 200);
    }

    @Override
    public void onTabListClear(@NotNull TabPlayer player) {
        for (TabPlayer all : onlinePlayers.getPlayers()) {
            // Ignore players on the same server, since the server already sends add packet
            if (!all.server.equals(player.server) && shouldSee(player, all)) {
                player.getTabList().addEntry(getAddInfoData(all, player));
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
        int gameMode = (othersAsSpectators && !p.server.equals(viewer.server)) ||
                (vanishedAsSpectators && p.isVanished()) ? 3 : p.getGamemode();
        return new TabList.Entry(
                p.getTablistId(),
                p.getNickname(),
                p.getSkin(),
                true,
                p.getPing(),
                gameMode,
                viewer.getVersion().getMinorVersion() >= 8 ? format : null
        );
    }

    @Override
    public void onGameModeChange(@NotNull TabPlayer player) {
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            if (!player.server.equals(viewer.server)) {
                viewer.getTabList().updateGameMode(player.getTablistId(), othersAsSpectators ? 3 : player.getGamemode());
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

    /**
     * Returns {@code true} if specified server is spy-server,
     * {@code false} if not.
     *
     * @param   server
     *          Server name to check
     * @return  {@code true} if is spy-server, {@code false} if not
     */
    public boolean isSpyServer(@NotNull String server) {
        return spyServers.contains(server.toLowerCase());
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        //player ping changed, must manually update latency for players on other servers
        for (TabPlayer viewer : onlinePlayers.getPlayers()) {
            if (!refreshed.server.equals(viewer.server) && viewer.getTabList().containsEntry(refreshed.getTablistId())) {
                viewer.getTabList().updateLatency(refreshed.getTablistId(), refreshed.getPing());
            }
        }
    }
}