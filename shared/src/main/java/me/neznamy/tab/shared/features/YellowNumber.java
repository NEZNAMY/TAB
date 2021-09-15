package me.neznamy.tab.shared.features;

import java.util.Arrays;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore.Action;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.PropertyUtils;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.layout.Layout;
import me.neznamy.tab.shared.features.layout.LayoutManager;
import me.neznamy.tab.shared.features.layout.PlayerSlot;

/**
 * Feature handler for tablist objective feature
 */
public class YellowNumber extends TabFeature {

	public static final String OBJECTIVE_NAME = "TAB-YellowNumber";
	public static final int DISPLAY_SLOT = 0;
	private static final String TITLE = "PlayerlistObjectiveTitle";

	private String rawValue;
	private EnumScoreboardHealthDisplay displayType;

	public YellowNumber() {
		super("Yellow number", TAB.getInstance().getConfiguration().getConfig().getStringList("yellow-number-in-tablist.disable-in-servers"),
				TAB.getInstance().getConfiguration().getConfig().getStringList("yellow-number-in-tablist.disable-in-worlds"));
		rawValue = TAB.getInstance().getConfiguration().getConfig().getString("yellow-number-in-tablist.value", "%ping%");
		if (rawValue.equals("%health%") || rawValue.equals("%player_health%") || rawValue.equals("%player_health_rounded%")) {
			displayType = EnumScoreboardHealthDisplay.HEARTS;
		} else {
			displayType = EnumScoreboardHealthDisplay.INTEGER;
		}
		TAB.getInstance().debug(String.format("Loaded YellowNumber feature with parameters value=%s, disabledWorlds=%s, disabledServers=%s, displayType=%s", rawValue, Arrays.toString(disabledWorlds), Arrays.toString(disabledServers), displayType));
	}

	@Override
	public void load() {
		for (TabPlayer loaded : TAB.getInstance().getOnlinePlayers()){
			loaded.setProperty(this, PropertyUtils.YELLOW_NUMBER, rawValue);
			if (isDisabled(loaded.getServer(), loaded.getWorld())) {
				addDisabledPlayer(loaded);
				continue;
			}
			PacketAPI.registerScoreboardObjective(loaded, OBJECTIVE_NAME, TITLE, DISPLAY_SLOT, displayType, this);
		}
		for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()){
			for (TabPlayer target : TAB.getInstance().getOnlinePlayers()){
				viewer.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, getName(target, viewer), getValue(target)), this);
			}
		}
	}

	@Override
	public void unload() {
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()){
			if (isDisabledPlayer(p)) continue;
			p.sendCustomPacket(new PacketPlayOutScoreboardObjective(OBJECTIVE_NAME), this);
		}
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		connectedPlayer.setProperty(this, PropertyUtils.YELLOW_NUMBER, rawValue);
		if (isDisabled(connectedPlayer.getServer(), connectedPlayer.getWorld())) {
			addDisabledPlayer(connectedPlayer);
			return;
		}
		PacketAPI.registerScoreboardObjective(connectedPlayer, OBJECTIVE_NAME, TITLE, DISPLAY_SLOT, displayType, this);
		int value = getValue(connectedPlayer);
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()){
			all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, getName(connectedPlayer, all), value), this);
			if (all.isLoaded()) connectedPlayer.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, getName(all, connectedPlayer), getValue(all)), this);
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
		if (disabledNow && !disabledBefore) {
			p.sendCustomPacket(new PacketPlayOutScoreboardObjective(OBJECTIVE_NAME), this);
		}
		if (!disabledNow && disabledBefore) {
			onJoin(p);
		}
	}

	private int getValue(TabPlayer p) {
		return TAB.getInstance().getErrorManager().parseInteger(p.getProperty(PropertyUtils.YELLOW_NUMBER).updateAndGet(), 0, "yellow number");
	}

	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (isDisabledPlayer(refreshed)) return;
		int value = getValue(refreshed);
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, getName(refreshed, all), value), this);
		}
	}

	private String getName(TabPlayer p, TabPlayer viewer) {
		LayoutManager manager = (LayoutManager) TAB.getInstance().getFeatureManager().getFeature("layout");
		if (manager != null) {
			Layout layout = manager.getPlayerViews().get(viewer);
			if (layout != null) {
				PlayerSlot slot = layout.getSlot(p);
				if (slot != null) {
					return slot.getFakePlayer();
				}
			}
		}
		return p.getName(); //layout not enabled or player not visible to viewer
	}
}