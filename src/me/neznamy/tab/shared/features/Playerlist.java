package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.neznamy.tab.premium.AlignedSuffix;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.PlayerInfoPacketListener;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;

public class Playerlist implements JoinEventListener, Loadable, WorldChangeListener, PlayerInfoPacketListener, Refreshable {

	private Set<String> usedPlaceholders;
	
	public Playerlist() {
		usedPlaceholders = Configs.config.getUsedPlaceholderIdentifiersRecursive("tabprefix", "customtabname", "tabsuffix");
	}
	@Override
	public void load(){
		for (ITabPlayer all : Shared.getPlayers()) {
			refresh(all, true);
		}
	}
	@Override
	public void unload(){
		List<PlayerInfoData> updatedPlayers = new ArrayList<PlayerInfoData>();
		for (ITabPlayer p : Shared.getPlayers()) {
			if (!p.disabledTablistNames) updatedPlayers.add(p.getInfoData());
		}
		Object packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, updatedPlayers).build(ProtocolVersion.SERVER_VERSION);
		for (ITabPlayer all : Shared.getPlayers()) {
			all.sendPacket(packet);
		}
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		refresh(p, true);
	}
	@Override
	public PacketPlayOutPlayerInfo onPacketSend(ITabPlayer receiver, PacketPlayOutPlayerInfo info) {
		if (receiver.getVersion().getMinorVersion() < 8) return info;
		boolean UPDATE_NAME = info.action == EnumPlayerInfoAction.UPDATE_DISPLAY_NAME;
		boolean ADD = info.action == EnumPlayerInfoAction.ADD_PLAYER;
		if (!UPDATE_NAME && !ADD) return info;
		List<PlayerInfoData> v180PrefixBugFixList = new ArrayList<PlayerInfoData>();
		for (PlayerInfoData playerInfoData : info.entries) {
			ITabPlayer packetPlayer = Shared.getPlayerByTablistUUID(playerInfoData.uniqueId);
			if (packetPlayer != null && !packetPlayer.disabledTablistNames) {
				playerInfoData.displayName = getTabFormat(packetPlayer, receiver);
				//preventing plugins from changing player name as nametag feature would not work correctly
				if (ADD && (Shared.features.containsKey("nametag16") || Shared.features.containsKey("nametagx")) && !playerInfoData.name.equals(packetPlayer.getName())) {
					Shared.debug("Blocking name change of player " +  packetPlayer.getName() + " to " + playerInfoData.name + " for " + receiver.getName());
					playerInfoData.name = packetPlayer.getName();
				}
			}
			if (ADD && packetPlayer != null && receiver.getVersion() == ProtocolVersion.v1_8) v180PrefixBugFixList.add(playerInfoData.clone());
		}
		if (ADD && receiver.getVersion() == ProtocolVersion.v1_8) {
			//1.8.0 bug, sending to all 1.8.x clients as there is no way to find out if they use 1.8.0
			Shared.featureCpu.runTaskLater(50, "sending PacketPlayOutPlayerInfo", CPUFeature.TABLIST_NAMES_3, new Runnable() {

				@Override
				public void run() {
					receiver.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, v180PrefixBugFixList));
				}
			});
		}
		return info;
	}
	@Override
	public CPUFeature getCPUName() {
		return CPUFeature.TABLIST_NAMES_2;
	}
	public IChatBaseComponent getTabFormat(ITabPlayer p, ITabPlayer viewer) {
		Property prefix = p.properties.get("tabprefix");
		Property name = p.properties.get("customtabname");
		Property suffix = p.properties.get("tabsuffix");
		if (prefix == null || name == null || suffix == null) {
//			Shared.errorManager.printError("TabFormat not initialized for " + p.getName());
			return null;
		}
		String format;
		if (Premium.alignTabsuffix) {
			format = ((AlignedSuffix)Shared.features.get("alignedsuffix")).fixTextWidth(p, prefix.getFormat(viewer) + name.getFormat(viewer), suffix.getFormat(viewer));
		} else {
			format = prefix.getFormat(viewer) + name.getFormat(viewer) + suffix.getFormat(viewer);
		}
		if (viewer.getVersion().getMinorVersion() >= 9) {
			return IChatBaseComponent.optimizedComponent(format);
		} else {
			//fucking lunar client
			return new IChatBaseComponent(new IChatBaseComponent(format).toColoredText());
		}
	}
	@Override
	public void refresh(ITabPlayer refreshed, boolean force) {
		if (refreshed.disabledTablistNames) return;
		boolean prefix = refreshed.properties.get("tabprefix").update();
		boolean name = refreshed.properties.get("customtabname").update();
		boolean suffix = refreshed.properties.get("tabsuffix").update();
		if (prefix || name || suffix || force) {
			Object packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, refreshed.getInfoData()).build(ProtocolVersion.SERVER_VERSION);
			for (ITabPlayer all : Shared.getPlayers()) {
				all.sendPacket(packet);
			}
		}
	}
	@Override
	public Set<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}
	@Override
	public CPUFeature getRefreshCPU() {
		return CPUFeature.TABLIST_NAMES_1;
	}
	@Override
	public void onJoin(ITabPlayer connectedPlayer) {
		refresh(connectedPlayer, true);
	}
}