package me.neznamy.tab.shared.features;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.interfaces.PlayerInfoPacketListener;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;

/**
 * Feature handler for global playerlist feature
 */
public class GlobalPlayerlist implements Loadable, JoinEventListener, QuitEventListener, WorldChangeListener, PlayerInfoPacketListener{

	private TAB tab;
	public final String PREMIUMVANISH_SEE_VANISHED_PERMISSION = "pv.see";
	private List<String> spyServers;
	private Map<String, List<String>> sharedServers;
	private boolean displayAsSpectators;

	public GlobalPlayerlist(TAB tab) {
		this.tab = tab;
	}
	
	@Override
	public void load() {
		spyServers = tab.getConfiguration().config.getStringList("global-playerlist.spy-servers", Arrays.asList("spaserver1"));
		sharedServers = tab.getConfiguration().config.getConfigurationSection("global-playerlist.server-groups");
		displayAsSpectators = tab.getConfiguration().config.getBoolean("global-playerlist.display-others-as-spectators", false);
		for (TabPlayer displayed : tab.getPlayers()) {
			PacketPlayOutPlayerInfo displayedAddPacket = getAddPacket(displayed);
			for (TabPlayer viewer : tab.getPlayers()) {
				if (viewer.getWorldName().equals(displayed.getWorldName())) continue;
				if (shouldSee(viewer, displayed)) viewer.sendCustomPacket(displayedAddPacket);
			}
		}
	}
	
	public boolean shouldSee(TabPlayer viewer, TabPlayer displayed) {
		if (displayed == viewer || spyServers.contains(viewer.getWorldName())) return true;
		if (displayed.isVanished() && !viewer.hasPermission(PREMIUMVANISH_SEE_VANISHED_PERMISSION)) return false;
		return getServerGroup(viewer.getWorldName()).equals(getServerGroup(displayed.getWorldName()));
	}
	
	private String getServerGroup(String serverName) {
		for (String group : sharedServers.keySet()) {
			if (sharedServers.get(group).contains(serverName)) return group;
		}
		return "null";
	}
	
	@Override
	public void unload() {
		for (TabPlayer displayed : tab.getPlayers()) {
			PacketPlayOutPlayerInfo displayedRemovePacket = getRemovePacket(displayed);
			for (TabPlayer viewer : tab.getPlayers()) {
				if (!displayed.getWorldName().equals(viewer.getWorldName())) viewer.sendCustomPacket(displayedRemovePacket);
			}
		}
	}
	
	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		PacketPlayOutPlayerInfo addConnected = getAddPacket(connectedPlayer);
		for (TabPlayer all : tab.getPlayers()) {
			if (all == connectedPlayer) continue;
			if (all.getWorldName().equals(connectedPlayer.getWorldName())) continue;
			if (shouldSee(all, connectedPlayer)) {
				all.sendCustomPacket(addConnected);
			}
			if (shouldSee(connectedPlayer, all)) {
				connectedPlayer.sendCustomPacket(getAddPacket(all));
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
				all.sendCustomPacket(remove);
			}
		});
	}
	
	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		PacketPlayOutPlayerInfo addChanged = getAddPacket(p);
		PacketPlayOutPlayerInfo removeChanged = getRemovePacket(p);
		for (TabPlayer all : tab.getPlayers()) {
			if (all == p) continue;
			if (shouldSee(all, p)) {
				all.sendCustomPacket(addChanged);
			} else {
				all.sendCustomPacket(removeChanged);
			}
			if (shouldSee(p, all)) {
				p.sendCustomPacket(getAddPacket(all));
			} else {
				p.sendCustomPacket(getRemovePacket(all));
			}
		}
	}
	
	public PacketPlayOutPlayerInfo getRemovePacket(TabPlayer p) {
		PlayerInfoData data = new PlayerInfoData(p.getTablistUUID());
		data.name = p.getName();
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, data);
	}
	
	public PacketPlayOutPlayerInfo getAddPacket(TabPlayer p) {
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, new PlayerInfoData(p.getName(), p.getTablistUUID(), p.getSkin(), (int)p.getPing(), EnumGamemode.CREATIVE, null));
	}
	
	@Override
	public void onPacketSend(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
		if (info.action == EnumPlayerInfoAction.REMOVE_PLAYER) {
			for (PlayerInfoData playerInfoData : info.entries) {
					//not preventing NPC removals
				if (tab.getPlayerByTablistUUID(playerInfoData.uniqueId) != null && (playerInfoData.name == null || playerInfoData.name.length() == 0)) {
					//remove packet not coming from tab
					//changing to random non-existing player, the easiest way to cancel the removal
					playerInfoData.uniqueId = UUID.randomUUID();
				}
			}
		}
		if (!displayAsSpectators) return;
		if (info.action == EnumPlayerInfoAction.ADD_PLAYER || info.action == EnumPlayerInfoAction.UPDATE_GAME_MODE) {
			for (PlayerInfoData playerInfoData : info.entries) {
				TabPlayer packetPlayer = tab.getPlayerByTablistUUID(playerInfoData.uniqueId);
				if (packetPlayer != null && !receiver.getWorldName().equals(packetPlayer.getWorldName())) {
					playerInfoData.gameMode = EnumGamemode.SPECTATOR;
				}
			}
		}
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.GLOBAL_PLAYERLIST;
	}
}