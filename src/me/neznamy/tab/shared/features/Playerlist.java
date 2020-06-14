package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.premium.AlignedSuffix;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.PluginHooks;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.PlayerInfoPacketListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;

public class Playerlist implements Loadable, WorldChangeListener, PlayerInfoPacketListener{

	public void load(){
		int refresh = Configs.config.getInt("tablist-refresh-interval-milliseconds", 1000);
		if (refresh < 50) Shared.errorManager.refreshTooLow("Tablist prefix/suffix", refresh);
		updateNames(true);
		Shared.featureCpu.startRepeatingMeasuredTask(refresh, "refreshing tablist prefix/suffix", CPUFeature.TABLIST_NAMES_1, new Runnable() {
			public void run() {
				updateNames(false);
			}
		});
	}
	public void unload(){
		updateNames(true);
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		if (!Configs.disabledTablistNames.contains("NORESET")) updatePlayerListName(p);
	}
	private void updateNames(boolean force){
		List<PlayerInfoData> updatedPlayers = new ArrayList<PlayerInfoData>();
		for (ITabPlayer p : Shared.getPlayers()) {
			if (!p.disabledTablistNames && (isListNameUpdateNeeded(p) || force)) updatedPlayers.add(p.getInfoData());
		}
		if (!updatedPlayers.isEmpty()) {
			for (ITabPlayer all : Shared.getPlayers()) {
				all.sendPacket(PacketAPI.buildPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, updatedPlayers), all.getVersion()));
			}
		}
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
	
	public boolean isListNameUpdateNeeded(ITabPlayer p) {
		p.getGroup();
		boolean tabprefix = p.properties.get("tabprefix").isUpdateNeeded();
		boolean customtabname = p.properties.get("customtabname").isUpdateNeeded();
		boolean tabsuffix = p.properties.get("tabsuffix").isUpdateNeeded();
		return (tabprefix || customtabname || tabsuffix);
	}

	public void updatePlayerListName(ITabPlayer p) {
		isListNameUpdateNeeded(p); //triggering updates to replaced values
		for (ITabPlayer all : Shared.getPlayers()) {
			all.sendPacket(PacketAPI.buildPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, p.getInfoData()), all.getVersion()));
		}
	}

	public IChatBaseComponent getTabFormat(ITabPlayer p, ITabPlayer viewer) {
		Property prefix = p.properties.get("tabprefix");
		Property name = p.properties.get("customtabname");
		Property suffix = p.properties.get("tabsuffix");
		if (prefix == null || name == null || suffix == null) {
			Shared.errorManager.printError("TabFormat not initialized for " + p.getName());
			return null;
		}
		String format;
		if (Premium.allignTabsuffix) {
			format = ((AlignedSuffix)Shared.features.get("alignedsuffix")).fixTextWidth(p, prefix.get() + name.get(), suffix.get());
		} else {
			format = prefix.get() + name.get() + suffix.get();
		}
		String text = (prefix.hasRelationalPlaceholders() || name.hasRelationalPlaceholders() || suffix.hasRelationalPlaceholders()) ? PluginHooks.PlaceholderAPI_setRelationalPlaceholders(viewer, p, format) : format;
		if (viewer.getVersion().getMinorVersion() >= 9) {
			return IChatBaseComponent.fromColoredText(text);
		} else {
			//fucking lunar client
			return new IChatBaseComponent(new IChatBaseComponent(text).toColoredText());
		}
	}
}