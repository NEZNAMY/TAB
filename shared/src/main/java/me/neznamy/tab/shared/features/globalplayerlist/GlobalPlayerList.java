package me.neznamy.tab.shared.features.globalplayerlist;

import java.util.*;

import lombok.Getter;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.PlayerList;
import me.neznamy.tab.shared.features.types.*;
import org.jetbrains.annotations.NotNull;

/**
 * Feature handler for global PlayerList feature
 */
public class GlobalPlayerList extends TabFeature implements JoinListener, QuitListener, VanishListener, GameModeListener,
        Loadable, UnLoadable, ServerSwitchListener {

    // config options
    private final List<String> spyServers = TAB.getInstance().getConfiguration().getConfig().getStringList("global-playerlist.spy-servers", Collections.singletonList("spyserver1"));
    private final Map<String, List<String>> sharedServers = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("global-playerlist.server-groups");
    private final boolean othersAsSpectators = TAB.getInstance().getConfiguration().getConfig().getBoolean("global-playerlist.display-others-as-spectators", false);
    private final boolean vanishedAsSpectators = TAB.getInstance().getConfiguration().getConfig().getBoolean("global-playerlist.display-vanished-players-as-spectators", true);
    private final boolean isolateUnlistedServers = TAB.getInstance().getConfiguration().getConfig().getBoolean("global-playerlist.isolate-unlisted-servers", false);

    private final PlayerList playerlist = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PLAYER_LIST);
    @Getter private final String featureName = "Global PlayerList";

    public GlobalPlayerList() {
        for (Map.Entry<String, List<String>> entry : sharedServers.entrySet()) {
            TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(TabConstants.Placeholder.globalPlayerListGroup(entry.getKey()), 1000,
                    () -> Arrays.stream(TAB.getInstance().getOnlinePlayers()).filter(p -> entry.getValue().contains(p.getServer()) && !p.isVanished()).count());
        }
    }

    @Override
    public void load() {
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.GLOBAL_PLAYER_LIST_LATENCY, new LatencyRefresher());
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            List<TabList.Entry> entries = new ArrayList<>();
            for (TabPlayer displayed : TAB.getInstance().getOnlinePlayers()) {
                if (viewer.getServer().equals(displayed.getServer())) continue;
                if (shouldSee(viewer, displayed)) entries.add(getAddInfoData(displayed, viewer));
            }
            if (!entries.isEmpty()) viewer.getTabList().addEntries(entries);
        }
    }

    public boolean shouldSee(@NotNull TabPlayer viewer, @NotNull TabPlayer displayed) {
        if (displayed == viewer) return true;
        if (displayed.isVanished() && !viewer.hasPermission(TabConstants.Permission.SEE_VANISHED)) return false;
        if (isSpyServer(viewer.getServer())) return true;
        return getServerGroup(viewer.getServer()).equals(getServerGroup(displayed.getServer()));
    }

    public @NotNull String getServerGroup(@NotNull String serverName) {
        for (Map.Entry<String, List<String>> group : sharedServers.entrySet()) {
            if (group.getValue().stream().anyMatch(serverName::equalsIgnoreCase)) return group.getKey();
        }
        return isolateUnlistedServers ? "isolated:" + serverName : "DEFAULT";
    }

    @Override
    public void unload() {
        for (TabPlayer displayed : TAB.getInstance().getOnlinePlayers()) {
            for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                if (!displayed.getServer().equals(viewer.getServer())) viewer.getTabList().removeEntry(displayed.getTablistId());
            }
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (connectedPlayer.getServer().equals(all.getServer())) continue;
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
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all == disconnectedPlayer) continue;
            all.getTabList().removeEntry(disconnectedPlayer.getTablistId());
        }
    }

    @Override
    public void onServerChange(@NotNull TabPlayer changed, @NotNull String from, @NotNull String to) {
        // Event is fired after all entries are removed from switched player's tablist, ready to re-add immediately
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            // Ignore players on the same server, since the server already sends add packet
            if (!all.getServer().equals(changed.getServer()) && shouldSee(changed, all)) {
                changed.getTabList().addEntry(getAddInfoData(all, changed));
            }
        }

        // Player who switched server is removed from tablist of other players in ~70-110ms (depending on online count), re-add with a delay
        TAB.getInstance().getCPUManager().runTaskLater(200, featureName, TabConstants.CpuUsageCategory.SERVER_SWITCH, () -> {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                // Remove for everyone and add back if visible, easy solution to display-others-as-spectators option
                all.getTabList().removeEntry(changed.getTablistId());
                if (shouldSee(all, changed)) {
                    all.getTabList().addEntry(getAddInfoData(changed, all));
                }
            }
        });
    }

    public @NotNull TabList.Entry getAddInfoData(@NotNull TabPlayer p, @NotNull TabPlayer viewer) {
        IChatBaseComponent format = null;
        if (playerlist != null && !playerlist.getDisableChecker().isDisabledPlayer(p)) {
            format = playerlist.getTabFormat(p, viewer);
        }
        int gameMode = (othersAsSpectators && !p.getServer().equals(viewer.getServer())) ||
                (vanishedAsSpectators && p.isVanished()) ? 3 : p.getGamemode();
        return new TabList.Entry(
                p.getTablistId(),
                p.getNickname(),
                p.getSkin(),
                p.getPing(),
                gameMode,
                viewer.getVersion().getMinorVersion() >= 8 ? format : null
        );
    }

    @Override
    public void onGameModeChange(@NotNull TabPlayer player) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (!player.getServer().equals(viewer.getServer())) {
                viewer.getTabList().updateGameMode(player.getTablistId(), othersAsSpectators ? 3 : player.getGamemode());
            }
        }
    }

    @Override
    public void onVanishStatusChange(@NotNull TabPlayer p) {
        if (p.isVanished()) {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (all == p) continue;
                if (!shouldSee(all, p)) {
                    all.getTabList().removeEntry(p.getTablistId());
                }
            }
        } else {
            for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                if (viewer == p) continue;
                if (shouldSee(viewer, p)) {
                    viewer.getTabList().addEntry(getAddInfoData(p, viewer));
                }
            }
        }
    }

    public boolean isSpyServer(@NotNull String server) {
        return spyServers.stream().anyMatch(server::equalsIgnoreCase);
    }
}