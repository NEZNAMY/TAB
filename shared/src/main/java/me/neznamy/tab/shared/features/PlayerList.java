package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.TablistFormatManager;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.layout.Layout;
import me.neznamy.tab.shared.features.layout.LayoutManager;
import me.neznamy.tab.shared.features.layout.PlayerSlot;

/**
 * Feature handler for TabList prefix/name/suffix
 */
public class PlayerList extends TabFeature implements TablistFormatManager {

	protected final boolean antiOverrideTabList = TAB.getInstance().getConfiguration().getConfig().getBoolean("tablist-name-formatting.anti-override", true);
	private boolean disabling = false;

	public PlayerList() {
		super("TabList prefix/suffix", "Updating TabList format", TAB.getInstance().getConfiguration().getConfig().getStringList("tablist-name-formatting.disable-in-servers"),
				TAB.getInstance().getConfiguration().getConfig().getStringList("tablist-name-formatting.disable-in-worlds"));
		TAB.getInstance().debug(String.format("Loaded PlayerList feature with parameters disabledWorlds=%s, disabledServers=%s, antiOverrideTabList=%s", Arrays.toString(disabledWorlds), Arrays.toString(disabledServers), antiOverrideTabList));
	}

	@Override
	public void load(){
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			if (isDisabled(all.getServer(), all.getWorld())) {
				addDisabledPlayer(all);
				updateProperties(all);
				continue;
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
		if (TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.PIPELINE_INJECTION)) return;
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			if (p.getVersion().getMinorVersion() >= 8) p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME,
					new PlayerInfoData(getTablistUUID(all, p), getTabFormat(all, p, false))), this);
			if (all.getVersion().getMinorVersion() >= 8) all.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME,
					new PlayerInfoData(getTablistUUID(p, all), getTabFormat(p, all, false))), this);
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
				updatePlayer(p, false);
			}
		} else if (updateProperties(p)) {
			updatePlayer(p, true);
		}
	}

	private void updatePlayer(TabPlayer p, boolean format) {
		for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
			if (viewer.getVersion().getMinorVersion() < 8) continue;
			viewer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME,
					new PlayerInfoData(getTablistUUID(p, viewer), format ? getTabFormat(p, viewer, true) : null)), this);
		}
		RedisSupport redis = (RedisSupport) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
		if (redis != null) redis.updateTabFormat(p, p.getProperty(TabConstants.Property.TABPREFIX).get() + p.getProperty(TabConstants.Property.CUSTOMTABNAME).get() + p.getProperty(TabConstants.Property.TABSUFFIX).get());
	}

	public IChatBaseComponent getTabFormat(TabPlayer p, TabPlayer viewer, boolean updateWidths) {
		Property prefix = p.getProperty(TabConstants.Property.TABPREFIX);
		Property name = p.getProperty(TabConstants.Property.CUSTOMTABNAME);
		Property suffix = p.getProperty(TabConstants.Property.TABSUFFIX);
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
			boolean prefix = refreshed.getProperty(TabConstants.Property.TABPREFIX).update();
			boolean name = refreshed.getProperty(TabConstants.Property.CUSTOMTABNAME).update();
			boolean suffix = refreshed.getProperty(TabConstants.Property.TABSUFFIX).update();
			refresh = prefix || name || suffix;
		}
		if (refresh) {
			updatePlayer(refreshed, true);
		}
	}
	
	protected boolean updateProperties(TabPlayer p) {
		boolean changed = p.loadPropertyFromConfig(this, TabConstants.Property.TABPREFIX);
		if (p.loadPropertyFromConfig(this, TabConstants.Property.CUSTOMTABNAME, p.getName())) changed = true;
		if (p.loadPropertyFromConfig(this, TabConstants.Property.TABSUFFIX)) changed = true;
		return changed;
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		updateProperties(connectedPlayer);
		if (isDisabled(connectedPlayer.getServer(), connectedPlayer.getWorld())) {
			addDisabledPlayer(connectedPlayer);
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
			if (!list.isEmpty()) connectedPlayer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, list), this);
		};
		r.run();
		//add packet might be sent after tab's refresh packet, resending again when anti-override is disabled
		if (!antiOverrideTabList || !TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.PIPELINE_INJECTION))
			TAB.getInstance().getCPUManager().runTaskLater(300, "processing PlayerJoinEvent", this, TabConstants.CpuUsageCategory.PLAYER_JOIN, r);
	}
	
	protected UUID getTablistUUID(TabPlayer p, TabPlayer viewer) {
		LayoutManager manager = (LayoutManager) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.LAYOUT);
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
		if (disabling || !antiOverrideTabList) return;
		if (info.getAction() != EnumPlayerInfoAction.UPDATE_DISPLAY_NAME && info.getAction() != EnumPlayerInfoAction.ADD_PLAYER) return;
		for (PlayerInfoData playerInfoData : info.getEntries()) {
			TabPlayer packetPlayer = TAB.getInstance().getPlayerByTablistUUID(playerInfoData.getUniqueId());
			if (packetPlayer != null && !isDisabledPlayer(packetPlayer)) {
				playerInfoData.setDisplayName(getTabFormat(packetPlayer, receiver, false));
			}
		}
	}

	@Override
	public void setPrefix(TabPlayer player, String prefix) {
		player.getProperty(TabConstants.Property.TABPREFIX).setTemporaryValue(prefix);
		player.forceRefresh();
	}

	@Override
	public void setName(TabPlayer player, String customName) {
		player.getProperty(TabConstants.Property.CUSTOMTABNAME).setTemporaryValue(customName);
		player.forceRefresh();
	}

	@Override
	public void setSuffix(TabPlayer player, String suffix) {
		player.getProperty(TabConstants.Property.TABSUFFIX).setTemporaryValue(suffix);
		player.forceRefresh();
	}

	@Override
	public void resetPrefix(TabPlayer player) {
		player.getProperty(TabConstants.Property.TABPREFIX).setTemporaryValue(null);
		player.forceRefresh();
	}

	@Override
	public void resetName(TabPlayer player) {
		player.getProperty(TabConstants.Property.CUSTOMTABNAME).setTemporaryValue(null);
		player.forceRefresh();
	}

	@Override
	public void resetSuffix(TabPlayer player) {
		player.getProperty(TabConstants.Property.TABSUFFIX).setTemporaryValue(null);
		player.forceRefresh();
	}

	@Override
	public String getCustomPrefix(TabPlayer player) {
		return player.getProperty(TabConstants.Property.TABPREFIX).getTemporaryValue();
	}

	@Override
	public String getCustomName(TabPlayer player) {
		return player.getProperty(TabConstants.Property.CUSTOMTABNAME).getTemporaryValue();
	}

	@Override
	public String getCustomSuffix(TabPlayer player) {
		return player.getProperty(TabConstants.Property.TABSUFFIX).getTemporaryValue();
	}

	@Override
	public String getOriginalPrefix(TabPlayer player) {
		return player.getProperty(TabConstants.Property.TABPREFIX).getOriginalRawValue();
	}

	@Override
	public String getOriginalName(TabPlayer player) {
		return player.getProperty(TabConstants.Property.CUSTOMTABNAME).getOriginalRawValue();
	}

	@Override
	public String getOriginalSuffix(TabPlayer player) {
		return player.getProperty(TabConstants.Property.TABSUFFIX).getOriginalRawValue();
	}
}