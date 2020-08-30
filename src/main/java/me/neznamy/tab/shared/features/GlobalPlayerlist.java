package me.neznamy.tab.shared.features;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
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

	private final String PREMIUMVANISH_SEE_VANISHED_PERMISSION = "pv.see";
	private List<String> spyServers;
	private Map<String, List<String>> sharedServers;
	private boolean displayAsSpectators;

	@SuppressWarnings("unchecked")
	@Override
	public void load() {
		spyServers = Configs.config.getStringList("global-playerlist.spy-servers", Arrays.asList("spaserver1"));
		sharedServers = Configs.config.getConfigurationSection("global-playerlist.server-groups");
		displayAsSpectators = Configs.config.getBoolean("global-playerlist.display-others-as-spectators", false);
		for (ITabPlayer displayed : Shared.getPlayers()) {
			Object displayedAddPacket = getAddPacket(displayed).build(ProtocolVersion.SERVER_VERSION);
			for (ITabPlayer viewer : Shared.getPlayers()) {
				if (viewer.getWorldName().equals(displayed.getWorldName())) continue;
				if (shouldSee(viewer, displayed)) viewer.sendPacket(displayedAddPacket);
			}
		}
	}
	
	private boolean shouldSee(ITabPlayer viewer, ITabPlayer displayed) {
		if (displayed == viewer) return true;
		if (displayed.isVanished() && !viewer.hasPermission(PREMIUMVANISH_SEE_VANISHED_PERMISSION)) return false;
		if (spyServers.contains(viewer.getWorldName())) {
			return true;
		}
		String viewerServerGroup = "null";
		for (String group : sharedServers.keySet()) {
			if (sharedServers.get(group).contains(viewer.getWorldName())) viewerServerGroup = group;
		}
		String displayedServerGroup = "null";
		for (String group : sharedServers.keySet()) {
			if (sharedServers.get(group).contains(displayed.getWorldName())) displayedServerGroup = group;
		}
		if (spyServers.contains(displayed.getWorldName()) && !spyServers.contains(viewer.getWorldName())) {
			return false;
		}
		if (viewerServerGroup.equals(displayedServerGroup)) {
			return true;
		}
		return false;
	}
	
	@Override
	public void unload() {
		for (ITabPlayer displayed : Shared.getPlayers()) {
			Object displayedRemovePacket = getRemovePacket(displayed).build(ProtocolVersion.SERVER_VERSION);
			for (ITabPlayer viewer : Shared.getPlayers()) {
				if (!displayed.getWorldName().equals(viewer.getWorldName())) viewer.sendPacket(displayedRemovePacket);
			}
		}
	}
	
	@Override
	public void onJoin(ITabPlayer connectedPlayer) {
		Object addConnected = getAddPacket(connectedPlayer).build(ProtocolVersion.SERVER_VERSION);
		for (ITabPlayer all : Shared.getPlayers()) {
			if (all == connectedPlayer) continue;
			if (all.getWorldName().equals(connectedPlayer.getWorldName())) continue;
			if (shouldSee(all, connectedPlayer)) {
				all.sendPacket(addConnected);
			}
			if (shouldSee(connectedPlayer, all)) {
				connectedPlayer.sendCustomPacket(getAddPacket(all));
			}
		}
	}
	
	@Override
	public void onQuit(ITabPlayer disconnectedPlayer) {
		Object remove = getRemovePacket(disconnectedPlayer).build(ProtocolVersion.SERVER_VERSION);
		for (ITabPlayer all : Shared.getPlayers()) {
			if (all == disconnectedPlayer) continue;
			all.sendPacket(remove);
		}
	}
	
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		//delay because VeLoCiTyPoWeReD
		Shared.cpu.runTaskLater(100, "processing server switch", getFeatureType(), UsageType.WORLD_SWITCH_EVENT, new Runnable() {

			@Override
			public void run() {
				Object addChanged = getAddPacket(p).build(ProtocolVersion.SERVER_VERSION);
				Object removeChanged = getRemovePacket(p).build(ProtocolVersion.SERVER_VERSION);
				for (ITabPlayer all : Shared.getPlayers()) {
					if (all == p) continue;
					if (shouldSee(all, p)) {
						all.sendPacket(addChanged);
					} else {
						all.sendPacket(removeChanged);
					}
					if (shouldSee(p, all)) {
						p.sendCustomPacket(getAddPacket(all));
					} else {
						p.sendCustomPacket(getRemovePacket(all));
					}
				}
			}
		});
	}
	
	public PacketPlayOutPlayerInfo getRemovePacket(ITabPlayer p) {
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, new PlayerInfoData(p.getName(), p.getUniqueId(), null, 0, null, null));
	}
	
	public PacketPlayOutPlayerInfo getAddPacket(ITabPlayer p) {
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, new PlayerInfoData(p.getName(), p.getUniqueId(), p.getSkin(), (int)p.getPing(), EnumGamemode.CREATIVE, null));
	}
	
	@Override
	public PacketPlayOutPlayerInfo onPacketSend(ITabPlayer receiver, PacketPlayOutPlayerInfo info) {
		if (receiver.getVersion().getMinorVersion() < 8) return info;
		if (info.action == EnumPlayerInfoAction.REMOVE_PLAYER) {
			for (PlayerInfoData playerInfoData : info.entries) {
					//not preventing NPC removals
				if (Shared.getPlayer(playerInfoData.uniqueId) != null && (playerInfoData.name == null || playerInfoData.name.length() == 0) && info.action == EnumPlayerInfoAction.REMOVE_PLAYER) {
					//remove packet sent by bungeecord
					//changing to random non-existing player, the easiest way to cancel the removal
					playerInfoData.uniqueId = UUID.randomUUID();
				}
			}
		}
		if (info.action == EnumPlayerInfoAction.ADD_PLAYER || info.action == EnumPlayerInfoAction.UPDATE_GAME_MODE) {
			for (PlayerInfoData playerInfoData : info.entries) {
				ITabPlayer packetPlayer = Shared.getPlayerByTablistUUID(playerInfoData.uniqueId);
				if (packetPlayer != null && displayAsSpectators && !receiver.getWorldName().equals(packetPlayer.getWorldName())) {
					playerInfoData.gameMode = EnumGamemode.SPECTATOR;
				}
			}
		}
		return info;
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.GLOBAL_PLAYERLIST;
	}
}