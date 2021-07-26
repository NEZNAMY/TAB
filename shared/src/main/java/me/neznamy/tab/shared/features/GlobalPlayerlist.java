package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.UsageType;

/**
 * Feature handler for global playerlist feature
 */
public class GlobalPlayerlist extends TabFeature {

	private List<String> spyServers;
	private Map<String, List<String>> sharedServers;
	private boolean displayAsSpectators;
	private boolean vanishedAsSpectators;
	private boolean isolateUnlistedServers;

	private List<TabPlayer> vanishedPlayers = new ArrayList<>();

	public GlobalPlayerlist() {
		super("Global playerlist");
		spyServers = TAB.getInstance().getConfiguration().getConfig().getStringList("global-playerlist.spy-servers", Arrays.asList("spaserver1"));
		sharedServers = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("global-playerlist.server-groups");
		displayAsSpectators = TAB.getInstance().getConfiguration().getConfig().getBoolean("global-playerlist.display-others-as-spectators", false);
		vanishedAsSpectators = TAB.getInstance().getConfiguration().getConfig().getBoolean("global-playerlist.display-vanished-players-as-spectators", true);
		isolateUnlistedServers = TAB.getInstance().getConfiguration().getConfig().getBoolean("global-playerlist.isolate-unlisted-servers", false);
		TAB.getInstance().debug(String.format("Loaded GlobalPlayerlist feature with parameters spyServers=%s, sharedServers=%s, displayAsSpectators=%s, vanishedAsSpectators=%s, isolateUnlistedServers=%s",
				spyServers, sharedServers, displayAsSpectators, vanishedAsSpectators, isolateUnlistedServers));
	}

	@Override
	public void load() {
		for (TabPlayer displayed : TAB.getInstance().getOnlinePlayers()) {
			for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
				if (viewer.getServer().equals(displayed.getServer())) continue;
				if (shouldSee(viewer, displayed)) viewer.sendCustomPacket(getAddPacket(displayed, viewer), this);
			}
		}
		startTask();
	}

	private void startTask() {
		TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(500, "refreshing vanished players", this, UsageType.REPEATING_TASK, () -> {

			for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
				if (vanishedPlayers.contains(p) && !p.isVanished()) {
					vanishedPlayers.remove(p);
					for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
						if (viewer == p) continue;
						if (shouldSee(viewer, p)) {
							viewer.sendCustomPacket(getAddPacket(p, viewer), this);
						}
					}
				}
				if (!vanishedPlayers.contains(p) && p.isVanished()) {
					vanishedPlayers.add(p);
					for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
						if (all == p) continue;
						if (!shouldSee(all, p)) {
							all.sendCustomPacket(getRemovePacket(p), this);
						}
					}
				}
			}
		});
	}

	public boolean shouldSee(TabPlayer viewer, TabPlayer displayed) {
		if (displayed == viewer) return true;
		if (displayed.isVanished() && !viewer.hasPermission("TAB.getInstance().seevanished")) return false;
		if (spyServers.contains(viewer.getServer())) return true;
		return getServerGroup(viewer.getServer()).equals(getServerGroup(displayed.getServer()));
	}

	private String getServerGroup(String serverName) {
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
		TAB.getInstance().getCPUManager().runTaskLater(50, "removing players", this, UsageType.PLAYER_QUIT_EVENT, () -> {

			PacketPlayOutPlayerInfo remove = getRemovePacket(disconnectedPlayer);
			for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
				if (all == disconnectedPlayer) continue;
				all.sendCustomPacket(remove, this);
			}
		});
	}

	@Override
	public void onServerChange(TabPlayer p, String from, String to) {
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
	}

	public PacketPlayOutPlayerInfo getRemovePacket(TabPlayer p) {
		PlayerInfoData data = new PlayerInfoData(p.getTablistUUID());
		data.setName(p.getName());
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, data);
	}

	public PacketPlayOutPlayerInfo getAddPacket(TabPlayer p, TabPlayer viewer) {
		IChatBaseComponent format = null;
		Playerlist playerlist = (Playerlist) TAB.getInstance().getFeatureManager().getFeature("playerlist");
		if (playerlist != null) {
			format = playerlist.getTabFormat(p, viewer);
		}
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, new PlayerInfoData(p.getName(), p.getTablistUUID(), p.getSkin(), 
				(int)p.getPing(), isVanishedAsSpectators() && p.isVanished() ? EnumGamemode.SPECTATOR : EnumGamemode.CREATIVE, format));
	}

	@Override
	public void onPlayerInfo(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
		if (info.getAction() == EnumPlayerInfoAction.REMOVE_PLAYER) {
			for (PlayerInfoData playerInfoData : info.getEntries()) {
				//not preventing NPC removals
				if (TAB.getInstance().getPlayerByTablistUUID(playerInfoData.getUniqueId()) != null && (playerInfoData.getName() == null || playerInfoData.getName().length() == 0)) {
					//remove packet not coming from tab
					//changing to random non-existing player, the easiest way to cancel the removal
					playerInfoData.setUniqueId(UUID.randomUUID());
				}
			}
		}
		if (!displayAsSpectators) return;
		if (info.getAction() == EnumPlayerInfoAction.ADD_PLAYER || info.getAction() == EnumPlayerInfoAction.UPDATE_GAME_MODE) {
			for (PlayerInfoData playerInfoData : info.getEntries()) {
				TabPlayer packetPlayer = TAB.getInstance().getPlayerByTablistUUID(playerInfoData.getUniqueId());
				if (packetPlayer != null && !receiver.getServer().equals(packetPlayer.getServer())) {
					playerInfoData.setGameMode(EnumGamemode.SPECTATOR);
				}
			}
		}
	}

	public boolean isVanishedAsSpectators() {
		return vanishedAsSpectators;
	}

	public void setVanishedAsSpectators(boolean vanishedAsSpectators) {
		this.vanishedAsSpectators = vanishedAsSpectators;
	}
}