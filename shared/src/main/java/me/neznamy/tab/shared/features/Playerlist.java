package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.PropertyUtils;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.layout.Layout;
import me.neznamy.tab.shared.features.layout.PlayerSlot;

/**
 * Feature handler for tablist prefix/name/suffix
 */
public class Playerlist extends TabFeature {

	private boolean antiOverrideTablist;
	private boolean disabling = false;

	public Playerlist() {
		super("Tablist prefix/suffix", TAB.getInstance().getConfiguration().getConfig().getStringList("tablist-name-formatting.disable-in-servers"),
				TAB.getInstance().getConfiguration().getConfig().getStringList("tablist-name-formatting.disable-in-worlds"));
		antiOverrideTablist = TAB.getInstance().getConfiguration().getConfig().getBoolean("tablist-name-formatting.anti-override", true) && TAB.getInstance().getFeatureManager().isFeatureEnabled("injection");
		TAB.getInstance().debug(String.format("Loaded Playerlist feature with parameters disabledWorlds=%s, disabledServers=%s, antiOverrideTablist=%s", Arrays.toString(disabledWorlds), Arrays.toString(disabledServers), antiOverrideTablist));
	}

	@Override
	public void load(){
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			if (isDisabled(all.getServer(), all.getWorld())) {
				disabledPlayers.add(all);
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
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
			if (!disabledPlayers.contains(p)) updatedPlayers.add(new PlayerInfoData(getTablistUUID(p)));
		}
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			if (all.getVersion().getMinorVersion() >= 8) all.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, updatedPlayers), this);
		}
	}

	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		boolean disabledBefore = disabledPlayers.contains(p);
		boolean disabledNow = false;
		if (isDisabled(p.getServer(), p.getWorld())) {
			disabledNow = true;
			disabledPlayers.add(p);
		} else {
			disabledPlayers.remove(p);
		}
		if (disabledNow) {
			if (!disabledBefore) {
				for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
					if (viewer.getVersion().getMinorVersion() < 8) continue;
					viewer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PlayerInfoData(getTablistUUID(p))), this);
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
		AlignedSuffix alignedSuffix = (AlignedSuffix) TAB.getInstance().getFeatureManager().getFeature("alignedsuffix");
		if (alignedSuffix != null) {
			format = alignedSuffix.formatName(prefix.getFormat(viewer) + name.getFormat(viewer), suffix.getFormat(viewer));
		} else {
			format = prefix.getFormat(viewer) + name.getFormat(viewer) + suffix.getFormat(viewer);
		}
		return IChatBaseComponent.optimizedComponent(format);
	}
	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (disabledPlayers.contains(refreshed)) return;
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
			for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
				if (viewer.getVersion().getMinorVersion() < 8) continue;
				String format;
				AlignedSuffix alignedSuffix = (AlignedSuffix) TAB.getInstance().getFeatureManager().getFeature("alignedsuffix");
				if (alignedSuffix != null) {
					format = alignedSuffix.formatNameAndUpdateLeader(refreshed, viewer);
				} else {
					format = prefix.getFormat(viewer) + name.getFormat(viewer) + suffix.getFormat(viewer);
				}
				viewer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PlayerInfoData(getTablistUUID(refreshed), IChatBaseComponent.optimizedComponent(format))), this);
			}
		}
	}
	private void updateProperties(TabPlayer p) {
		p.loadPropertyFromConfig(this, PropertyUtils.TABPREFIX);
		p.loadPropertyFromConfig(this, PropertyUtils.CUSTOMTABNAME, p.getName());
		p.loadPropertyFromConfig(this, PropertyUtils.TABSUFFIX);
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		if (isDisabled(connectedPlayer.getServer(), connectedPlayer.getWorld())) {
			disabledPlayers.add(connectedPlayer);
			updateProperties(connectedPlayer);
			return;
		}
		Runnable r = () -> {
			refresh(connectedPlayer, true);
			if (connectedPlayer.getVersion().getMinorVersion() < 8) return;
			List<PlayerInfoData> list = new ArrayList<>();
			for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
				if (all == connectedPlayer) continue; //already sent 4 lines above
				list.add(new PlayerInfoData(getTablistUUID(all), getTabFormat(all, connectedPlayer)));
			}
			connectedPlayer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, list), this);
		};
		r.run();
		//add packet might be sent after tab's refresh packet, resending again when anti-override is disabled
		if (!antiOverrideTablist) TAB.getInstance().getCPUManager().runTaskLater(100, "processing PlayerJoinEvent", this, UsageType.PLAYER_JOIN_EVENT, r);
	}
	
	private UUID getTablistUUID(TabPlayer p) {
		Layout layout = (Layout) TAB.getInstance().getFeatureManager().getFeature("layout");
		if (layout != null) {
			PlayerSlot slot = layout.getSlot(p);
			if (slot != null) {
				return slot.getUUID();
			}
		}
		return p.getTablistUUID(); //layout not enabled or player not visible
	}

	@Override
	public void onPlayerInfo(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
		if (disabling || !antiOverrideTablist) return;
		if (info.getAction() != EnumPlayerInfoAction.UPDATE_DISPLAY_NAME && info.getAction() != EnumPlayerInfoAction.ADD_PLAYER) return;
		for (PlayerInfoData playerInfoData : info.getEntries()) {
			TabPlayer packetPlayer = TAB.getInstance().getPlayerByTablistUUID(playerInfoData.getUniqueId());
			if (packetPlayer != null && !disabledPlayers.contains(packetPlayer)) {
				playerInfoData.setDisplayName(getTabFormat(packetPlayer, receiver));
			}
		}
	}

	public String[] getDisabledWorlds() {
		return disabledWorlds;
	}
}