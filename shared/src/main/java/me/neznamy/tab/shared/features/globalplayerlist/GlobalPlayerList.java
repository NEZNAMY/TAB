package me.neznamy.tab.shared.features.globalplayerlist;

import java.util.*;
import java.util.Map.Entry;

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

/**
 * Feature handler for global PlayerList feature
 */
public class GlobalPlayerList extends TabFeature {

    private final List<String> spyServers = TAB.getInstance().getConfiguration().getConfig().getStringList("global-playerlist.spy-servers", Collections.singletonList("spyserver1"));
    private final Map<String, List<String>> sharedServers = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("global-playerlist.server-groups");
    private final boolean displayAsSpectators = TAB.getInstance().getConfiguration().getConfig().getBoolean("global-playerlist.display-others-as-spectators", false);
    private final boolean vanishedAsSpectators = TAB.getInstance().getConfiguration().getConfig().getBoolean("global-playerlist.display-vanished-players-as-spectators", true);
    private final boolean isolateUnlistedServers = TAB.getInstance().getConfiguration().getConfig().getBoolean("global-playerlist.isolate-unlisted-servers", false);
    private final boolean fillProfileKey = TAB.getInstance().getConfiguration().getConfig().getBoolean("global-playerlist.fill-profile-key", false);

    public GlobalPlayerList() {
        super("Global PlayerList", null);
        boolean updateLatency = TAB.getInstance().getConfiguration().getConfig().getBoolean("global-playerlist.update-latency", false);
        if (updateLatency) TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.GLOBAL_PLAYER_LIST_LATENCY, new LatencyRefresher());
        TAB.getInstance().getPlaceholderManager().addUsedPlaceholders(Collections.singletonList(TabConstants.Placeholder.VANISHED));
        TAB.getInstance().debug(String.format("Loaded GlobalPlayerList feature with parameters spyServers=%s, sharedServers=%s, displayAsSpectators=%s, vanishedAsSpectators=%s, isolateUnlistedServers=%s, updateLatency=%s",
                spyServers, sharedServers, displayAsSpectators, vanishedAsSpectators, isolateUnlistedServers, updateLatency));
    }

    @Override
    public void load() {
        for (TabPlayer displayed : TAB.getInstance().getOnlinePlayers()) {
            for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                if (viewer.getServer().equals(displayed.getServer())) continue;
                if (shouldSee(viewer, displayed)) viewer.sendCustomPacket(getAddPacket(displayed, viewer), this);
            }
        }
    }

    public boolean shouldSee(TabPlayer viewer, TabPlayer displayed) {
        if (displayed == viewer) return true;
        if (displayed.isVanished() && !viewer.hasPermission(TabConstants.Permission.SEE_VANISHED)) return false;
        if (spyServers.contains(viewer.getServer())) return true;
        return getServerGroup(viewer.getServer()).equals(getServerGroup(displayed.getServer()));
    }

    public String getServerGroup(String serverName) {
        for (Entry<String, List<String>> group : sharedServers.entrySet()) {
            if (group.getValue().contains(serverName)) return group.getKey();
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
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all == connectedPlayer) continue;
            if (shouldSee(all, connectedPlayer)) {
                all.sendCustomPacket(getAddPacket(connectedPlayer, all), this);
            }
            if (shouldSee(connectedPlayer, all)) {
                connectedPlayer.sendCustomPacket(getAddPacket(all, connectedPlayer), this);
            }
        }
    }

    @Override
    public void onQuit(TabPlayer disconnectedPlayer) {
        //delay due to waterfall bug calling server switch when players leave
        TAB.getInstance().getCPUManager().runTaskLater(50, this, TabConstants.CpuUsageCategory.PLAYER_QUIT, () -> {

            if (TAB.getInstance().getPlayer(disconnectedPlayer.getName()) != null) return;
            PacketPlayOutPlayerInfo remove = getRemovePacket(disconnectedPlayer);
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (all == disconnectedPlayer) continue;
                all.sendCustomPacket(remove, this);
            }
        });
    }

    @Override
    public void onServerChange(TabPlayer p, String from, String to) {
        Runnable r = () -> {
            PacketPlayOutPlayerInfo removeChanged = getRemovePacket(p);
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (all == p) continue;
                if (shouldSee(all, p)) {
                    all.sendCustomPacket(getAddPacket(p, all), this);
                } else {
                    all.sendCustomPacket(removeChanged, this);
                }
                if (shouldSee(p, all)) {
                    p.sendCustomPacket(getAddPacket(all, p), this);
                } else {
                    p.sendCustomPacket(getRemovePacket(all), this);
                }
            }
        };
        if (!TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.PIPELINE_INJECTION)) {
            TAB.getInstance().getCPUManager().runTaskLater(200, this, TabConstants.CpuUsageCategory.SERVER_SWITCH, r);
        } else {
            r.run();
        }
    }

    public PacketPlayOutPlayerInfo getRemovePacket(TabPlayer p) {
        PlayerInfoData data = new PlayerInfoData(p.getTablistUUID());
        data.setName(p.getName());
        return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, data);
    }

    public PacketPlayOutPlayerInfo getAddPacket(TabPlayer p, TabPlayer viewer) {
        IChatBaseComponent format = null;
        PlayerList playerlist = (PlayerList) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PLAYER_LIST);
        if (playerlist != null) {
            format = playerlist.getTabFormat(p, viewer);
        }
        return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER,
                new PlayerInfoData(
                        p.getName(),
                        p.getTablistUUID(),
                        p.getSkin(),
                        p.getPing(),
                        vanishedAsSpectators && p.isVanished() ? EnumGamemode.SPECTATOR : EnumGamemode.CREATIVE,
                        viewer.getVersion().getMinorVersion() >= 8 ? format : null,
                        fillProfileKey ? p.getProfilePublicKey() : null
                )
        );
    }

    @Override
    public void onPlayerInfo(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
        if (info.getAction() == EnumPlayerInfoAction.REMOVE_PLAYER) {
            for (PlayerInfoData playerInfoData : info.getEntries()) {
                TabPlayer packetPlayer = TAB.getInstance().getPlayerByTabListUUID(playerInfoData.getUniqueId());
                    //not preventing NPC removals
                if (packetPlayer != null && (playerInfoData.getName() == null || playerInfoData.getName().length() == 0) && !packetPlayer.isVanished()) {
                    //remove packet not coming from tab
                    //changing to random non-existing player, the easiest way to cancel the removal
                    playerInfoData.setUniqueId(UUID.randomUUID());
                }
            }
        }
        if (!displayAsSpectators) return;
        if (info.getAction() == EnumPlayerInfoAction.ADD_PLAYER || info.getAction() == EnumPlayerInfoAction.UPDATE_GAME_MODE) {
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
                    viewer.sendCustomPacket(getAddPacket(p, viewer), TabConstants.PacketCategory.GLOBAL_PLAYERLIST_VANISH);
                }
            }
        }
    }

    public List<String> getSpyServers() {
        return spyServers;
    }
}