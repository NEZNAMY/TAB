package me.neznamy.tab.shared.features.globalplayerlist;

import java.util.*;
import java.util.Map.Entry;

import lombok.Getter;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlayerList;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;

/**
 * Feature handler for global PlayerList feature
 */
public class GlobalPlayerList extends TabFeature {

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

    @Override
    public void load() {
        if (updateLatency) TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.GLOBAL_PLAYER_LIST_LATENCY, new LatencyRefresher());
        TAB.getInstance().getPlaceholderManager().addUsedPlaceholders(Collections.singletonList(TabConstants.Placeholder.VANISHED));
        for (Entry<String, List<String>> entry : sharedServers.entrySet()) {
            TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(TabConstants.Placeholder.globalPlayerListGroup(entry.getKey()), 1000,
                    () -> Arrays.stream(TAB.getInstance().getOnlinePlayers()).filter(p -> entry.getValue().contains(p.getServer()) && !p.isVanished()).count());
        }
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            List<PlayerInfoData> entries = new ArrayList<>();
            for (TabPlayer displayed : TAB.getInstance().getOnlinePlayers()) {
                if (viewer.getServer().equals(displayed.getServer())) continue;
                if (shouldSee(viewer, displayed)) entries.add(getAddInfoData(displayed, viewer));
            }
            if (!entries.isEmpty()) viewer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, entries), this);
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
            PacketPlayOutPlayerInfo displayedRemovePacket = getRemovePacket(displayed);
            for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                if (!displayed.getServer().equals(viewer.getServer())) viewer.sendCustomPacket(displayedRemovePacket, this);
            }
        }
    }

    @Override
    public void onJoin(TabPlayer connectedPlayer) {
        List<PlayerInfoData> entries = new ArrayList<>();
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (connectedPlayer.getServer().equals(all.getServer())) continue;
            if (shouldSee(all, connectedPlayer)) {
                all.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, getAddInfoData(connectedPlayer, all)), this);
            }
            if (shouldSee(connectedPlayer, all)) entries.add(getAddInfoData(all, connectedPlayer));
        }
        if (!entries.isEmpty()) connectedPlayer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, entries), this);
    }

    @Override
    public void onQuit(TabPlayer disconnectedPlayer) {
        PacketPlayOutPlayerInfo remove = getRemovePacket(disconnectedPlayer);
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all == disconnectedPlayer) continue;
            all.sendCustomPacket(remove, this);
        }
    }

    @Override
    public void onServerChange(TabPlayer changed, String from, String to) {
        // Event is fired after all entries are removed from switched player's tablist, ready to re-add immediately
        List<PlayerInfoData> entries = new ArrayList<>();
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all == changed) continue;
            if (shouldSee(changed, all)) {
                entries.add(getAddInfoData(all, changed));
            } else {
                changed.sendCustomPacket(getRemovePacket(all), this);
            }
        }
        if (!entries.isEmpty()) changed.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, entries), this);

        // Player who switched server is removed from tablist of other players in ~70-110ms (depending on online count), re-add with a delay
        TAB.getInstance().getCPUManager().runTaskLater(200, this, TabConstants.CpuUsageCategory.SERVER_SWITCH, () -> {
            PacketPlayOutPlayerInfo removeChanged = getRemovePacket(changed);
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (all == changed) continue;
                if (shouldSee(all, changed)) {
                    all.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, getAddInfoData(changed, all)), this);
                } else {
                    all.sendCustomPacket(removeChanged, this);
                }
            }
        });
    }

    public PacketPlayOutPlayerInfo getRemovePacket(TabPlayer p) {
        return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, new PlayerInfoData(p.getTablistId()));
    }

    public PlayerInfoData getAddInfoData(TabPlayer p, TabPlayer viewer) {
        IChatBaseComponent format = null;
        if (playerlist != null) {
            format = playerlist.getTabFormat(p, viewer);
        }
        return new PlayerInfoData(
                p.getName(),
                p.getTablistId(),
                p.getSkin(),
                true,
                p.getPing(),
                vanishedAsSpectators && p.isVanished() ? EnumGamemode.SPECTATOR : EnumGamemode.CREATIVE,
                viewer.getVersion().getMinorVersion() >= 8 ? format : null,
                fillProfileKey ? ((ProxyTabPlayer)p).getChatSessionId() : null,
                fillProfileKey ? ((ProxyTabPlayer)p).getProfilePublicKey() : null
        );
    }

    @Override
    public void onPlayerInfo(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
        if (!displayAsSpectators) return;
        if (info.getActions().contains(EnumPlayerInfoAction.UPDATE_GAME_MODE)) {
            for (PlayerInfoData playerInfoData : info.getEntries()) {
                TabPlayer packetPlayer = TAB.getInstance().getPlayerByTabListUUID(playerInfoData.getUniqueId());
                if (packetPlayer != null && !receiver.getServer().equals(packetPlayer.getServer())) {
                    playerInfoData.setGameMode(EnumGamemode.SPECTATOR);
                }
            }
        }
    }

    @Override
    public void onVanishStatusChange(TabPlayer p) {
        if (p.isVanished()) {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (all == p) continue;
                if (!shouldSee(all, p)) {
                    all.sendCustomPacket(getRemovePacket(p), TabConstants.PacketCategory.GLOBAL_PLAYERLIST_VANISH);
                }
            }
        } else {
            for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                if (viewer == p) continue;
                if (shouldSee(viewer, p)) {
                    viewer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, getAddInfoData(p, viewer)), TabConstants.PacketCategory.GLOBAL_PLAYERLIST_VANISH);
                }
            }
        }
    }

    public boolean isSpyServer(String server) {
        return spyServers.stream().anyMatch(server::equalsIgnoreCase);
    }
}