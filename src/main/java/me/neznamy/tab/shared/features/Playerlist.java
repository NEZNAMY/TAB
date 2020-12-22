package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
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

	private List<String> usedPlaceholders;
	public List<String> disabledWorlds;
	
	
	public Playerlist() {
		disabledWorlds = Configs.config.getStringList("disable-features-in-"+Shared.platform.getSeparatorType()+"s.tablist-names", Arrays.asList("disabled" + Shared.platform.getSeparatorType()));
		refreshUsedPlaceholders();
	}
	
	@Override
	public void load(){
		for (TabPlayer all : Shared.getPlayers()) {
			refresh(all, true);
		}
	}
	
	@Override
	public void unload(){
		List<PlayerInfoData> updatedPlayers = new ArrayList<PlayerInfoData>();
		for (TabPlayer p : Shared.getPlayers()) {
			if (!isDisabledWorld(disabledWorlds, p.getWorldName())) updatedPlayers.add(new PlayerInfoData(p.getUniqueId()));
		}
		Object packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, updatedPlayers).create(ProtocolVersion.SERVER_VERSION);
		for (TabPlayer all : Shared.getPlayers()) {
			if (all.getVersion().getMinorVersion() >= 8) all.sendPacket(packet);
		}
	}
	
	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		refresh(p, true);
	}
	
	@Override
	public void onPacketSend(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
		boolean UPDATE_NAME = info.action == EnumPlayerInfoAction.UPDATE_DISPLAY_NAME;
		boolean ADD = info.action == EnumPlayerInfoAction.ADD_PLAYER;
		if (!UPDATE_NAME && !ADD) return;
		List<PlayerInfoData> v180PrefixBugFixList = new ArrayList<PlayerInfoData>();
		for (PlayerInfoData playerInfoData : info.entries) {
			TabPlayer packetPlayer = Shared.getPlayerByTablistUUID(playerInfoData.uniqueId);
			if (packetPlayer != null && !isDisabledWorld(disabledWorlds, packetPlayer.getWorldName())) {
				playerInfoData.displayName = getTabFormat(packetPlayer, receiver);
				//preventing plugins from changing player name as nametag feature would not work correctly
				if (ADD && Shared.featureManager.getNameTagFeature() != null && !playerInfoData.name.equals(packetPlayer.getName())) {
					Shared.debug("Blocking name change of player " +  packetPlayer.getName() + " to " + playerInfoData.name + " for " + receiver.getName());
					playerInfoData.name = packetPlayer.getName();
				}
			}
			if (ADD && packetPlayer != null && receiver.getVersion() == ProtocolVersion.v1_8) v180PrefixBugFixList.add(playerInfoData.clone());
		}
		if (ADD && receiver.getVersion() == ProtocolVersion.v1_8 && !v180PrefixBugFixList.isEmpty()) { //not sending empty packets due to NPCs
			//1.8.0 bug, sending to all 1.8.x clients as there is no way to find out if they use 1.8.0
			Shared.cpu.runTaskLater(50, "sending PacketPlayOutPlayerInfo", getFeatureType(), UsageType.v1_8_0_BUG_COMPENSATION, () -> receiver.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, v180PrefixBugFixList)));
		}
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
		AlignedSuffix alignedSuffix = (AlignedSuffix) Shared.featureManager.getFeature("alignedsuffix");
		if (alignedSuffix != null) {
			format = alignedSuffix.fixTextWidth(p, prefix.getFormat(viewer) + name.getFormat(viewer), suffix.getFormat(viewer));
		} else {
			format = prefix.getFormat(viewer) + name.getFormat(viewer) + suffix.getFormat(viewer);
		}
		if (viewer.getVersion().getMinorVersion() >= 9) {
			return IChatBaseComponent.optimizedComponent(format);
		} else {
			//fucking lunar client
			return new IChatBaseComponent(IChatBaseComponent.fromColoredText(format).toLegacyText());
		}
	}
	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
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
			Object packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PlayerInfoData(refreshed.getTablistUUID())).create(ProtocolVersion.SERVER_VERSION);
			for (TabPlayer all : Shared.getPlayers()) {
				if (all.getVersion().getMinorVersion() >= 8) all.sendPacket(packet);
			}
		}
	}
	private void updateProperties(TabPlayer p) {
		p.loadPropertyFromConfig("tabprefix");
		p.loadPropertyFromConfig("customtabname", p.getName());
		p.loadPropertyFromConfig("tabsuffix");
	}
	
	@Override
	public List<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		refresh(connectedPlayer, true);
		if (connectedPlayer.getVersion().getMinorVersion() < 8) return;
		List<PlayerInfoData> list = new ArrayList<PlayerInfoData>();
		for (TabPlayer all : Shared.getPlayers()) {
			list.add(new PlayerInfoData(all.getTablistUUID()));
		}
		connectedPlayer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, list));
	}
	
	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = Configs.config.getUsedPlaceholderIdentifiersRecursive("tabprefix", "customtabname", "tabsuffix");
	}
	
	/**
	 * Returns name of the feature displayed in /tab cpu
	 * @return name of the feature displayed in /tab cpu
	 */
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.TABLIST_NAMES;
	}
}