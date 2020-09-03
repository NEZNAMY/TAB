package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.premium.AlignedSuffix;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.PlayerInfoPacketListener;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;

/**
 * Feature handler for tablist prefix/name/suffix
 */
public class Playerlist implements JoinEventListener, Loadable, WorldChangeListener, PlayerInfoPacketListener, Refreshable {

	private Set<String> usedPlaceholders;
	
	public Playerlist() {
		refreshUsedPlaceholders();
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
		Object packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, updatedPlayers).create(ProtocolVersion.SERVER_VERSION);
		for (ITabPlayer all : Shared.getPlayers()) {
			if (all.getVersion().getMinorVersion() >= 8) all.sendPacket(packet);
		}
	}
	
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		refresh(p, true);
	}
	
	@Override
	public PacketPlayOutPlayerInfo onPacketSend(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
		if (receiver.getVersion().getMinorVersion() < 8) return info;
		boolean UPDATE_NAME = info.action == EnumPlayerInfoAction.UPDATE_DISPLAY_NAME;
		boolean ADD = info.action == EnumPlayerInfoAction.ADD_PLAYER;
		if (!UPDATE_NAME && !ADD) return info;
		List<PlayerInfoData> v180PrefixBugFixList = new ArrayList<PlayerInfoData>();
		for (PlayerInfoData playerInfoData : info.entries) {
			TabPlayer packetPlayer = Shared.getPlayerByTablistUUID(playerInfoData.uniqueId);
			if (packetPlayer == receiver && ADD) ((ITabPlayer)packetPlayer).correctId = playerInfoData.uniqueId;
			if (packetPlayer != null && !((ITabPlayer)packetPlayer).disabledTablistNames) {
				playerInfoData.displayName = getTabFormat(packetPlayer, receiver);
				//preventing plugins from changing player name as nametag feature would not work correctly
				if (ADD && (Shared.featureManager.isFeatureEnabled("nametag16") || Shared.featureManager.isFeatureEnabled("nametagx")) && !playerInfoData.name.equals(packetPlayer.getName())) {
					Shared.debug("Blocking name change of player " +  packetPlayer.getName() + " to " + playerInfoData.name + " for " + receiver.getName());
					playerInfoData.name = packetPlayer.getName();
				}
			}
			if (ADD && packetPlayer != null && receiver.getVersion() == ProtocolVersion.v1_8) v180PrefixBugFixList.add(playerInfoData.clone());
		}
		if (ADD && receiver.getVersion() == ProtocolVersion.v1_8) {
			//1.8.0 bug, sending to all 1.8.x clients as there is no way to find out if they use 1.8.0
			Shared.cpu.runTaskLater(50, "sending PacketPlayOutPlayerInfo", getFeatureType(), UsageType.OTHER, new Runnable() {

				@Override
				public void run() {
					receiver.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, v180PrefixBugFixList));
				}
			});
		}
		return info;
	}

	public IChatBaseComponent getTabFormat(TabPlayer p, TabPlayer viewer) {
		Property prefix = p.getProperty("tabprefix");
		Property name = p.getProperty("customtabname");
		Property suffix = p.getProperty("tabsuffix");
		if (prefix == null || name == null || suffix == null) {
//			Shared.errorManager.printError("TabFormat not initialized for " + p.getName());
			return null;
		}
		String format;
		if (Premium.alignTabsuffix) {
			format = ((AlignedSuffix)Shared.featureManager.getFeature("alignedsuffix")).fixTextWidth(p, prefix.getFormat(viewer) + name.getFormat(viewer), suffix.getFormat(viewer));
		} else {
			format = prefix.getFormat(viewer) + name.getFormat(viewer) + suffix.getFormat(viewer);
		}
		if (viewer.getVersion().getMinorVersion() >= 9) {
			return IChatBaseComponent.optimizedComponent(format);
		} else {
			//fucking lunar client
			return new IChatBaseComponent(IChatBaseComponent.fromColoredText(format).toColoredText());
		}
	}
	@Override
	public void refresh(ITabPlayer refreshed, boolean force) {
//		if (refreshed.disabledTablistNames) return; //prevented unloading when switching to disabled world, will find a better fix later
		boolean refresh;
		if (force) {
			updateProperties(refreshed);
			refresh = true;
		} else {
			boolean prefix = refreshed.getProperty("tabprefix").update();
			boolean name = refreshed.getProperty("customtabname").update();
			boolean suffix = refreshed.getProperty("tabsuffix").update();
			refresh = prefix || name || suffix;
		}
		if (refresh) {
			Object packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, refreshed.getInfoData()).create(ProtocolVersion.SERVER_VERSION);
			for (ITabPlayer all : Shared.getPlayers()) {
				if (all.getVersion().getMinorVersion() >= 8) all.sendPacket(packet);
			}
		}
	}
	private void updateProperties(ITabPlayer p) {
		p.updateProperty("tabprefix");
		p.updateProperty("customtabname", p.getName());
		p.updateProperty("tabsuffix");
	}
	
	@Override
	public Set<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}

	@Override
	public void onJoin(ITabPlayer connectedPlayer) {
		refresh(connectedPlayer, true);
	}
	
	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = Configs.config.getUsedPlaceholderIdentifiersRecursive("tabprefix", "customtabname", "tabsuffix");
	}
	
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.TABLIST_NAMES;
	}
}