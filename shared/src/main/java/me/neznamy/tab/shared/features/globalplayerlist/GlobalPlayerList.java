package me.neznamy.tab.shared.features.globalplayerlist;

import java.util.*;
import java.util.Map.Entry;

import lombok.Getter;
import me.neznamy.tab.api.feature.*;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.tablist.TabListEntry;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlayerList;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;

/**
 * Feature handler for global PlayerList feature
 */
public class GlobalPlayerList extends TabFeature implements JoinListener, QuitListener, VanishListener, GameModeListener,
    Loadable, UnLoadable, ServerSwitchListener {

    // config options
    private final List<String> spyServers = TAB.getInstance().getConfiguration().getConfig().getStringList("global-playerlist.spy-servers", Collections.singletonList("spyserver1"));
    private final Map<String, List<String>> sharedServers = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("global-playerlist.server-groups");
    private final boolean displayAsSpectators = TAB.getInstance().getConfiguration().getConfig().getBoolean("global-playerlist.display-others-as-spectators", false);
    private final boolean vanishedAsSpectators = TAB.getInstance().getConfiguration().getConfig().getBoolean("global-playerlist.display-vanished-players-as-spectators", true);
    private final boolean isolateUnlistedServers = TAB.getInstance().getConfiguration().getConfig().getBoolean("global-playerlist.isolate-unlisted-servers", false);
    private final boolean fillProfileKey = TAB.getInstance().getConfiguration().getConfig().getBoolean("global-playerlist.fill-profile-key", false);
    private final boolean updateLatency = TAB.getInstance().getConfiguration().getConfig().getBoolean("global-playerlist.update-latency", false);

    private final PlayerList playerlist = (PlayerList) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PLAYER_LIST);
    @Getter private final String featureName = "Global PlayerList";

    public GlobalPlayerList() {
        for (Entry<String, List<String>> entry : sharedServers.entrySet()) {
            TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(TabConstants.Placeholder.globalPlayerListGroup(entry.getKey()), 1000,
                    () -> Arrays.stream(TAB.getInstance().getOnlinePlayers()).filter(p -> entry.getValue().contains(p.getServer()) && !p.isVanished()).count());
        }
        TAB.getInstance().getPlaceholderManager().addUsedPlaceholders(Collections.singletonList(TabConstants.Placeholder.VANISHED));
    }

    @Override
    public void load() {
        if (updateLatency) TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.GLOBAL_PLAYER_LIST_LATENCY, new LatencyRefresher());
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            List<TabListEntry> entries = new ArrayList<>();
            for (TabPlayer displayed : TAB.getInstance().getOnlinePlayers()) {
                if (viewer.getServer().equals(displayed.getServer())) continue;
                if (shouldSee(viewer, displayed)) entries.add(getAddInfoData(displayed, viewer));
            }
            if (!entries.isEmpty()) viewer.getTabList().addEntries(entries);
        }
    }

    public boolean shouldSee(TabPlayer viewer, TabPlayer displayed) {
        if (displayed == viewer) return true;
        if (displayed.isVanished() && !viewer.hasPermission(TabConstants.Permission.SEE_VANISHED)) return false;
        if (isSpyServer(viewer.getServer())) return true;
        return getServerGroup(viewer.getServer()).equals(getServerGroup(displayed.getServer()));
    }

    public String getServerGroup(String serverName) {
        for (Entry<String, List<String>> group : sharedServers.entrySet()) {
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
    public void onJoin(TabPlayer connectedPlayer) {
        List<TabListEntry> entries = new ArrayList<>();
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (connectedPlayer.getServer().equals(all.getServer())) continue;
            if (shouldSee(all, connectedPlayer)) {
                all.getTabList().addEntry(getAddInfoData(connectedPlayer, all));
            }
            if (shouldSee(connectedPlayer, all)) entries.add(getAddInfoData(all, connectedPlayer));
        }
        if (!entries.isEmpty()) connectedPlayer.getTabList().addEntries(entries);
    }

    @Override
    public void onQuit(TabPlayer disconnectedPlayer) {
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all == disconnectedPlayer) continue;
            all.getTabList().removeEntry(disconnectedPlayer.getTablistId());
        }
    }

    @Override
    public void onServerChange(TabPlayer changed, String from, String to) {
        // Event is fired after all entries are removed from switched player's tablist, ready to re-add immediately
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            // Ignore players on the same server, since the server already sends add packet
            if (!all.getServer().equals(changed.getServer()) && shouldSee(changed, all)) {
                changed.getTabList().addEntry(getAddInfoData(all, changed));
            }
        }

        // Player who switched server is removed from tablist of other players in ~70-110ms (depending on online count), re-add with a delay
        TAB.getInstance().getCPUManager().runTaskLater(200, this, TabConstants.CpuUsageCategory.SERVER_SWITCH, () -> {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (all == changed) continue;
                if (shouldSee(all, changed)) {
                    if (!all.getServer().equals(changed.getServer()))
                        all.getTabList().addEntry(getAddInfoData(changed, all));
                } else {
                    all.getTabList().removeEntry(changed.getTablistId());
                }
            }
        });
    }

    public TabListEntry getAddInfoData(TabPlayer p, TabPlayer viewer) {
        IChatBaseComponent format = null;
        if (playerlist != null) {
            format = playerlist.getTabFormat(p, viewer);
        }
        return new TabListEntry(
                p.getTablistId(),
                p.getName(),
                p.getSkin(),
                true,
                p.getPing(),
                vanishedAsSpectators && p.isVanished() ? 3 : p.getGamemode(),
                viewer.getVersion().getMinorVersion() >= 8 ? format : null,
                fillProfileKey ? ((ProxyTabPlayer)p).getChatSession() : null
        );
    }

    @Override
    public int onGameModeChange(TabPlayer packetReceiver, UUID id, int gameMode) {
        TabPlayer packetPlayer = TAB.getInstance().getPlayerByTabListUUID(id);
        if (packetPlayer != null && packetPlayer == packetReceiver) {
            // Player changed gamemode, update on all servers
            for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                if (!packetPlayer.getServer().equals(viewer.getServer())) {
                    viewer.getTabList().updateGameMode(id, displayAsSpectators ? 3 : gameMode);
                }
            }
        }
        return gameMode;
    }

    @Override
    public void onVanishStatusChange(TabPlayer p) {
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

    public boolean isSpyServer(String server) {
        return spyServers.stream().anyMatch(server::equalsIgnoreCase);
    }
}