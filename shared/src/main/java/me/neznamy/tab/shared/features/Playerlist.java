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
import me.neznamy.tab.shared.CpuConstants;
import me.neznamy.tab.shared.PropertyUtils;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.layout.Layout;
import me.neznamy.tab.shared.features.layout.LayoutManager;
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
				addDisabledPlayer(all);
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
			if (!isDisabledPlayer(p)) updatedPlayers.add(new PlayerInfoData(getTablistUUID(p, p)));
		}
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			if (all.getVersion().getMinorVersion() >= 8) all.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, updatedPlayers), this);
		}
	}

	@Override
	public void onServerChange(TabPlayer p, String from, String to) {
		onWorldChange(p, null, null);
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PlayerInfoData(getTablistUUID(all, p), getTabFormat(all, p, false))), this);
		}
	}
	
	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		boolean disabledBefore = isDisabledPlayer(p);
		boolean disabledNow = false;
		if (isDisabled(p.getServer(), p.getWorld())) {
			disabledNow = true;
			addDisabledPlayer(p);
		} else {
			removeDisabledPlayer(p);
		}
		if (disabledNow) {
			if (!disabledBefore) {
				for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
					if (viewer.getVersion().getMinorVersion() < 8) continue;
					viewer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PlayerInfoData(getTablistUUID(p, viewer))), this);
				}
				RedisSupport redis = (RedisSupport) TAB.getInstance().getFeatureManager().getFeature("redisbungee");
				if (redis != null) redis.updateTabFormat(p, p.getProperty(PropertyUtils.TABPREFIX).get() + p.getProperty(PropertyUtils.CUSTOMTABNAME).get() + p.getProperty(PropertyUtils.TABSUFFIX).get());
			}
		} else {
			refresh(p, true);
		}
	}

	public IChatBaseComponent getTabFormat(TabPlayer p, TabPlayer viewer, boolean updateWidths) {
		Property prefix = p.getProperty(PropertyUtils.TABPREFIX);
		Property name = p.getProperty(PropertyUtils.CUSTOMTABNAME);
		Property suffix = p.getProperty(PropertyUtils.TABSUFFIX);
		if (prefix == null || name == null || suffix == null) {
			return null;
		}
		return IChatBaseComponent.optimizedComponent(prefix.getFormat(viewer) + name.getFormat(viewer) + suffix.getFormat(viewer));
	}
	
	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (isDisabledPlayer(refreshed)) return;
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
			for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
				if (viewer.getVersion().getMinorVersion() < 8) continue;
				viewer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PlayerInfoData(getTablistUUID(refreshed, viewer), getTabFormat(refreshed, viewer, true))), this);
			}
			RedisSupport redis = (RedisSupport) TAB.getInstance().getFeatureManager().getFeature("redisbungee");
			if (redis != null) redis.updateTabFormat(refreshed, refreshed.getProperty(PropertyUtils.TABPREFIX).get() + refreshed.getProperty(PropertyUtils.CUSTOMTABNAME).get() + refreshed.getProperty(PropertyUtils.TABSUFFIX).get());
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
			addDisabledPlayer(connectedPlayer);
			updateProperties(connectedPlayer);
			return;
		}
		Runnable r = () -> {
			refresh(connectedPlayer, true);
			if (connectedPlayer.getVersion().getMinorVersion() < 8) return;
			List<PlayerInfoData> list = new ArrayList<>();
			for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
				if (all == connectedPlayer) continue; //already sent 4 lines above
				list.add(new PlayerInfoData(getTablistUUID(all, connectedPlayer), getTabFormat(all, connectedPlayer, false)));
			}
			connectedPlayer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, list), this);
		};
		r.run();
		//add packet might be sent after tab's refresh packet, resending again when anti-override is disabled
		if (!antiOverrideTablist) TAB.getInstance().getCPUManager().runTaskLater(100, "processing PlayerJoinEvent", this, CpuConstants.UsageCategory.PLAYER_JOIN, r);
	}
	
	private UUID getTablistUUID(TabPlayer p, TabPlayer viewer) {
		LayoutManager manager = (LayoutManager) TAB.getInstance().getFeatureManager().getFeature("layout");
		if (manager != null) {
			Layout layout = manager.getPlayerViews().get(viewer);
			if (layout != null) {
				PlayerSlot slot = layout.getSlot(p);
				if (slot != null) {
					return slot.getUUID();
				}
			}
		}
		return p.getTablistUUID(); //layout not enabled or player not visible to viewer
	}

	@Override
	public void onPlayerInfo(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
		if (disabling || !antiOverrideTablist) return;
		if (info.getAction() != EnumPlayerInfoAction.UPDATE_DISPLAY_NAME && info.getAction() != EnumPlayerInfoAction.ADD_PLAYER) return;
		for (PlayerInfoData playerInfoData : info.getEntries()) {
			TabPlayer packetPlayer = TAB.getInstance().getPlayerByTablistUUID(playerInfoData.getUniqueId());
			if (packetPlayer != null && !isDisabledPlayer(packetPlayer)) {
				playerInfoData.setDisplayName(getTabFormat(packetPlayer, receiver, false));
			}
		}
	}
}