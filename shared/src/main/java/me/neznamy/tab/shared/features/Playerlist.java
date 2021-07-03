package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.PropertyUtils;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.features.types.event.QuitEventListener;
import me.neznamy.tab.shared.features.types.event.WorldChangeListener;
import me.neznamy.tab.shared.features.types.packet.PlayerInfoPacketListener;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;

/**
 * Feature handler for tablist prefix/name/suffix
 */
public class Playerlist implements JoinEventListener, QuitEventListener, Loadable, WorldChangeListener, Refreshable {

	private TAB tab;
	private Set<String> usedPlaceholders;
	private List<String> disabledWorlds;
	private boolean antiOverrideNames;
	private boolean antiOverrideTablist;
	private boolean disabling = false;
	private Set<TabPlayer> playersInDisabledWorlds = new HashSet<>();

	public Playerlist(TAB tab) {
		this.tab = tab;
		disabledWorlds = tab.getConfiguration().getConfig().getStringList("disable-features-in-"+tab.getPlatform().getSeparatorType()+"s.tablist-names", Arrays.asList("disabled" + tab.getPlatform().getSeparatorType()));
		antiOverrideNames = tab.getConfiguration().getConfig().getBoolean("anti-override.usernames", true) && tab.getFeatureManager().isFeatureEnabled("injection");
		refreshUsedPlaceholders();
		antiOverrideTablist = tab.getConfiguration().getConfig().getBoolean("anti-override.tablist-names", true) && tab.getFeatureManager().isFeatureEnabled("injection");
		if (antiOverrideTablist) {
			tab.getFeatureManager().registerFeature("playerlist_info", new PlayerInfoPacketListener() {

				@Override
				public TabFeature getFeatureType() {
					return TabFeature.TABLIST_NAMES;
				}

				@Override
				public void onPacketSend(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
					if (disabling) return;
					if (info.getAction() != EnumPlayerInfoAction.UPDATE_DISPLAY_NAME && info.getAction() != EnumPlayerInfoAction.ADD_PLAYER) return;
					for (PlayerInfoData playerInfoData : info.getEntries()) {
						TabPlayer packetPlayer = tab.getPlayerByTablistUUID(playerInfoData.getUniqueId());
						if (packetPlayer != null && !playersInDisabledWorlds.contains(packetPlayer)) {
							playerInfoData.setDisplayName(getTabFormat(packetPlayer, receiver));
							//preventing plugins from changing player name as nametag feature would not work correctly
							if (info.getAction() == EnumPlayerInfoAction.ADD_PLAYER && tab.getFeatureManager().getNameTagFeature() != null && !playerInfoData.getName().equals(packetPlayer.getName()) && antiOverrideNames) {
								tab.getErrorManager().printError("A plugin tried to change name of " +  packetPlayer.getName() + " to \"" + playerInfoData.getName() + "\" for viewer " + receiver.getName(), null, false, tab.getErrorManager().getAntiOverrideLog());
								playerInfoData.setName(packetPlayer.getName());
							}
						}
					}
				}
				
			});
		}
		tab.debug(String.format("Loaded Playerlist feature with parameters disabledWorlds=%s, antiOverrideTablist=%s", getDisabledWorlds(), antiOverrideTablist));
	}

	@Override
	public void load(){
		for (TabPlayer all : tab.getPlayers()) {
			if (isDisabledWorld(disabledWorlds, all.getWorldName())) {
				playersInDisabledWorlds.add(all);
				updateProperties(all);
				return;
			}
			refresh(all, true);
		}
	}

	@Override
	public void unload(){
		disabling = true;
		List<PlayerInfoData> updatedPlayers = new ArrayList<>();
		for (TabPlayer p : tab.getPlayers()) {
			if (!playersInDisabledWorlds.contains(p)) updatedPlayers.add(new PlayerInfoData(p.getTablistUUID()));
		}
		for (TabPlayer all : tab.getPlayers()) {
			if (all.getVersion().getMinorVersion() >= 8) all.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, updatedPlayers), getFeatureType());
		}
	}

	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		boolean disabledBefore = playersInDisabledWorlds.contains(p);
		boolean disabledNow = false;
		if (isDisabledWorld(disabledWorlds, p.getWorldName())) {
			disabledNow = true;
			playersInDisabledWorlds.add(p);
		} else {
			playersInDisabledWorlds.remove(p);
		}
		if (disabledNow) {
			if (!disabledBefore) {
				for (TabPlayer viewer : tab.getPlayers()) {
					if (viewer.getVersion().getMinorVersion() < 8) continue;
					viewer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PlayerInfoData(p.getTablistUUID())));
				}
			}
		} else {
			refresh(p, true);
		}
	}

	public IChatBaseComponent getTabFormat(TabPlayer p, TabPlayer viewer) {
		Property prefix = p.getProperty(PropertyUtils.TABPREFIX);
		Property name = p.getProperty(PropertyUtils.CUSTOMTABNAME);
		Property suffix = p.getProperty(PropertyUtils.TABSUFFIX);
		if (prefix == null || name == null || suffix == null) {
			return null;
		}
		String format;
		AlignedSuffix alignedSuffix = (AlignedSuffix) tab.getFeatureManager().getFeature("alignedsuffix");
		if (alignedSuffix != null) {
			format = alignedSuffix.formatName(prefix.getFormat(viewer) + name.getFormat(viewer), suffix.getFormat(viewer));
		} else {
			format = prefix.getFormat(viewer) + name.getFormat(viewer) + suffix.getFormat(viewer);
		}
		return IChatBaseComponent.optimizedComponent(format);
	}
	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (playersInDisabledWorlds.contains(refreshed)) return;
		boolean refresh;
		if (force) {
			updateProperties(refreshed);
			refresh = true;
		} else {
			boolean prefix = refreshed.getProperty(PropertyUtils.TABPREFIX).update();
			boolean name = refreshed.getProperty(PropertyUtils.CUSTOMTABNAME).update();
			boolean suffix = refreshed.getProperty(PropertyUtils.TABSUFFIX).update();
			refresh = prefix || name || suffix;
		}
		if (refresh) {
			Property prefix = refreshed.getProperty(PropertyUtils.TABPREFIX);
			Property name = refreshed.getProperty(PropertyUtils.CUSTOMTABNAME);
			Property suffix = refreshed.getProperty(PropertyUtils.TABSUFFIX);
			for (TabPlayer viewer : tab.getPlayers()) {
				if (viewer.getVersion().getMinorVersion() < 8) continue;
				String format;
				AlignedSuffix alignedSuffix = (AlignedSuffix) tab.getFeatureManager().getFeature("alignedsuffix");
				if (alignedSuffix != null) {
					format = alignedSuffix.formatNameAndUpdateLeader(refreshed, viewer);
				} else {
					format = prefix.getFormat(viewer) + name.getFormat(viewer) + suffix.getFormat(viewer);
				}
				viewer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PlayerInfoData(refreshed.getTablistUUID(), IChatBaseComponent.optimizedComponent(format))), getFeatureType());
			}
		}
	}
	private void updateProperties(TabPlayer p) {
		p.loadPropertyFromConfig(PropertyUtils.TABPREFIX);
		p.loadPropertyFromConfig(PropertyUtils.CUSTOMTABNAME, p.getName());
		p.loadPropertyFromConfig(PropertyUtils.TABSUFFIX);
	}

	@Override
	public List<String> getUsedPlaceholders() {
		return new ArrayList<>(usedPlaceholders);
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		if (isDisabledWorld(disabledWorlds, connectedPlayer.getWorldName())) {
			playersInDisabledWorlds.add(connectedPlayer);
			updateProperties(connectedPlayer);
			return;
		}
		Runnable r = () -> {
			refresh(connectedPlayer, true);
			if (connectedPlayer.getVersion().getMinorVersion() < 8) return;
			List<PlayerInfoData> list = new ArrayList<>();
			for (TabPlayer all : tab.getPlayers()) {
				if (all == connectedPlayer) continue; //already sent 4 lines above
				list.add(new PlayerInfoData(all.getTablistUUID(), getTabFormat(all, connectedPlayer)));
			}
			connectedPlayer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, list), getFeatureType());
		};
		r.run();
		//add packet might be sent after tab's refresh packet, resending again when anti-override is disabled
		if (!antiOverrideTablist) tab.getCPUManager().runTaskLater(100, "processing PlayerJoinEvent", getFeatureType(), UsageType.PLAYER_JOIN_EVENT, r);
	}

	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = new HashSet<>(tab.getConfiguration().getConfig().getUsedPlaceholderIdentifiersRecursive(PropertyUtils.TABPREFIX, PropertyUtils.CUSTOMTABNAME, PropertyUtils.TABSUFFIX));
		for (TabPlayer p : tab.getPlayers()) {
			usedPlaceholders.addAll(tab.getPlaceholderManager().getUsedPlaceholderIdentifiersRecursive(p.getProperty(PropertyUtils.TABPREFIX).getCurrentRawValue(),
					p.getProperty(PropertyUtils.CUSTOMTABNAME).getCurrentRawValue(), p.getProperty(PropertyUtils.TABSUFFIX).getCurrentRawValue()));
		}
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.TABLIST_NAMES;
	}

	public List<String> getDisabledWorlds() {
		return disabledWorlds;
	}

	@Override
	public void onQuit(TabPlayer disconnectedPlayer) {
		playersInDisabledWorlds.remove(disconnectedPlayer);
	}
}