package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.features.types.event.QuitEventListener;
import me.neznamy.tab.shared.features.types.event.WorldChangeListener;
import me.neznamy.tab.shared.features.types.packet.PlayerInfoPacketListener;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;

/**
 * Feature handler for global playerlist feature
 */
public class GlobalPlayerlist implements Loadable, JoinEventListener, QuitEventListener, WorldChangeListener, PlayerInfoPacketListener{

	private TAB tab;
	
	private List<String> spyServers;
	private Map<String, List<String>> sharedServers;
	private boolean displayAsSpectators;
	private boolean vanishedAsSpectators;
	private boolean isolateUnlistedServers;
	
	private List<TabPlayer> vanishedPlayers = new ArrayList<>();

	public GlobalPlayerlist(TAB tab) {
		this.tab = tab;
	}
	
	@Override
	public void load() {
		spyServers = tab.getConfiguration().getConfig().getStringList("global-playerlist.spy-servers", Arrays.asList("spaserver1"));
		sharedServers = tab.getConfiguration().getConfig().getConfigurationSection("global-playerlist.server-groups");
		displayAsSpectators = tab.getConfiguration().getConfig().getBoolean("global-playerlist.display-others-as-spectators", false);
		setVanishedAsSpectators(tab.getConfiguration().getConfig().getBoolean("global-playerlist.display-vanished-players-as-spectators", true));
		isolateUnlistedServers = tab.getConfiguration().getConfig().getBoolean("global-playerlist.isolate-unlisted-servers", false);
		for (TabPlayer displayed : tab.getPlayers()) {
			for (TabPlayer viewer : tab.getPlayers()) {
				if (viewer.getWorldName().equals(displayed.getWorldName())) continue;
				if (shouldSee(viewer, displayed)) viewer.sendCustomPacket(getAddPacket(displayed, viewer), getFeatureType());
			}
		}
		tab.debug(String.format("Loaded GlobalPlayerlist feature with parameters spyServers=%s, sharedServers=%s, displayAsSpectators=%s, vanishedAsSpectators=%s, isolateUnlistedServers=%s",
				spyServers, sharedServers, displayAsSpectators, isVanishedAsSpectators(), isolateUnlistedServers));
		startTask();
	}
	
	private void startTask() {
		tab.getCPUManager().startRepeatingMeasuredTask(500, "refreshing vanished players", getFeatureType(), UsageType.REPEATING_TASK, () -> {
			
			for (TabPlayer p : tab.getPlayers()) {
				if (vanishedPlayers.contains(p) && !p.isVanished()) {
					vanishedPlayers.remove(p);
					for (TabPlayer viewer : tab.getPlayers()) {
						if (viewer == p) continue;
						if (shouldSee(viewer, p)) {
							viewer.sendCustomPacket(getAddPacket(p, viewer), getFeatureType());
						}
					}
				}
				if (!vanishedPlayers.contains(p) && p.isVanished()) {
					vanishedPlayers.add(p);
					for (TabPlayer all : tab.getPlayers()) {
						if (all == p) continue;
						if (!shouldSee(all, p)) {
							all.sendCustomPacket(getRemovePacket(p), getFeatureType());
						}
					}
				}
			}
		});
	}
	
	public boolean shouldSee(TabPlayer viewer, TabPlayer displayed) {
		if (displayed == viewer) return true;
		if (displayed.isVanished() && !viewer.hasPermission("tab.seevanished")) return false;
		if (spyServers.contains(viewer.getWorldName())) return true;
		return getServerGroup(viewer.getWorldName()).equals(getServerGroup(displayed.getWorldName()));
	}
	
	private String getServerGroup(String serverName) {
		for (Entry<String, List<String>> group : sharedServers.entrySet()) {
			if (group.getValue().contains(serverName)) return group.getKey();
		}
		return isolateUnlistedServers ? "isolated:" + serverName : "DEFAULT";
	}
	
	@Override
	public void unload() {
		for (TabPlayer displayed : tab.getPlayers()) {
			PacketPlayOutPlayerInfo displayedRemovePacket = getRemovePacket(displayed);
			for (TabPlayer viewer : tab.getPlayers()) {
				if (!displayed.getWorldName().equals(viewer.getWorldName())) viewer.sendCustomPacket(displayedRemovePacket, getFeatureType());
			}
		}
	}
	
	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		for (TabPlayer all : tab.getPlayers()) {
			if (all == connectedPlayer) continue;
			if (shouldSee(all, connectedPlayer)) {
				all.sendCustomPacket(getAddPacket(connectedPlayer, all), getFeatureType());
			}
			if (shouldSee(connectedPlayer, all)) {
				connectedPlayer.sendCustomPacket(getAddPacket(all, connectedPlayer), getFeatureType());
			}
		}
	}
	
	@Override
	public void onQuit(TabPlayer disconnectedPlayer) {
		//delay due to waterfall bug calling server switch when players leave
		tab.getCPUManager().runTaskLater(50, "removing players", getFeatureType(), UsageType.PLAYER_QUIT_EVENT, () -> {
			
			PacketPlayOutPlayerInfo remove = getRemovePacket(disconnectedPlayer);
			for (TabPlayer all : tab.getPlayers()) {
				if (all == disconnectedPlayer) continue;
				all.sendCustomPacket(remove, getFeatureType());
			}
		});
	}
	
	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		PacketPlayOutPlayerInfo removeChanged = getRemovePacket(p);
		for (TabPlayer all : tab.getPlayers()) {
			if (all == p) continue;
			if (shouldSee(all, p)) {
				all.sendCustomPacket(getAddPacket(p, all), getFeatureType());
			} else {
				all.sendCustomPacket(removeChanged, getFeatureType());
			}
			if (shouldSee(p, all)) {
				p.sendCustomPacket(getAddPacket(all, p), getFeatureType());
			} else {
				p.sendCustomPacket(getRemovePacket(all), getFeatureType());
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
		Playerlist playerlist = (Playerlist) tab.getFeatureManager().getFeature("playerlist");
		if (playerlist != null) {
			format = playerlist.getTabFormat(p, viewer);
		}
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, new PlayerInfoData(p.getName(), p.getTablistUUID(), p.getSkin(), 
				(int)p.getPing(), isVanishedAsSpectators() && p.isVanished() ? EnumGamemode.SPECTATOR : EnumGamemode.CREATIVE, format));
	}
	
	@Override
	public void onPacketSend(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
		if (info.getAction() == EnumPlayerInfoAction.REMOVE_PLAYER) {
			for (PlayerInfoData playerInfoData : info.getEntries()) {
					//not preventing NPC removals
				if (tab.getPlayerByTablistUUID(playerInfoData.getUniqueId()) != null && (playerInfoData.getName() == null || playerInfoData.getName().length() == 0)) {
					//remove packet not coming from tab
					//changing to random non-existing player, the easiest way to cancel the removal
					playerInfoData.setUniqueId(UUID.randomUUID());
				}
			}
		}
		if (!displayAsSpectators) return;
		if (info.getAction() == EnumPlayerInfoAction.ADD_PLAYER || info.getAction() == EnumPlayerInfoAction.UPDATE_GAME_MODE) {
			for (PlayerInfoData playerInfoData : info.getEntries()) {
				TabPlayer packetPlayer = tab.getPlayerByTablistUUID(playerInfoData.getUniqueId());
				if (packetPlayer != null && !receiver.getWorldName().equals(packetPlayer.getWorldName())) {
					playerInfoData.setGameMode(EnumGamemode.SPECTATOR);
				}
			}
		}
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.GLOBAL_PLAYERLIST;
	}

	public boolean isVanishedAsSpectators() {
		return vanishedAsSpectators;
	}

	public void setVanishedAsSpectators(boolean vanishedAsSpectators) {
		this.vanishedAsSpectators = vanishedAsSpectators;
	}
}