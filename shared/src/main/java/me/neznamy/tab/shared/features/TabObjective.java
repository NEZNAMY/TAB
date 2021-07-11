package me.neznamy.tab.shared.features;

import java.util.Arrays;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.PropertyUtils;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore.Action;

/**
 * Feature handler for tablist objective feature
 */
public class TabObjective extends TabFeature {

	public static final String OBJECTIVE_NAME = "TAB-YellowNumber";
	public static final int DISPLAY_SLOT = 0;
	private static final String TITLE = "ms";

	private String rawValue;
	private EnumScoreboardHealthDisplay displayType;

	public TabObjective() {
		rawValue = TAB.getInstance().getConfiguration().getConfig().getString("yellow-number-in-tablist", "%ping%");
		disabledWorlds = TAB.getInstance().getConfiguration().getConfig().getStringList("disable-features-in-"+TAB.getInstance().getPlatform().getSeparatorType()+"s.yellow-number", Arrays.asList("disabled" + TAB.getInstance().getPlatform().getSeparatorType()));
		refreshUsedPlaceholders();
		if (rawValue.equals("%health%") || rawValue.equals("%player_health%") || rawValue.equals("%player_health_rounded%")) {
			displayType = EnumScoreboardHealthDisplay.HEARTS;
		} else {
			displayType = EnumScoreboardHealthDisplay.INTEGER;
		}
		TAB.getInstance().debug(String.format("Loaded YellowNumber feature with parameters value=%s, disabledWorlds=%s, displayType=%s", rawValue, disabledWorlds, displayType));
	}

	@Override
	public void load() {
		for (TabPlayer loaded : TAB.getInstance().getPlayers()){
			loaded.setProperty(PropertyUtils.YELLOW_NUMBER, rawValue);
			if (isDisabledWorld(disabledWorlds, loaded.getWorldName())) {
				playersInDisabledWorlds.add(loaded);
				continue;
			}
			PacketAPI.registerScoreboardObjective(loaded, OBJECTIVE_NAME, TITLE, DISPLAY_SLOT, displayType, getFeatureType());
		}
		for (TabPlayer viewer : TAB.getInstance().getPlayers()){
			for (TabPlayer target : TAB.getInstance().getPlayers()){
				viewer.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, target.getName(), getValue(target)), getFeatureType());
			}
		}
	}

	@Override
	public void unload() {
		for (TabPlayer p : TAB.getInstance().getPlayers()){
			if (playersInDisabledWorlds.contains(p)) continue;
			p.sendCustomPacket(new PacketPlayOutScoreboardObjective(OBJECTIVE_NAME), getFeatureType());
		}
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		connectedPlayer.setProperty(PropertyUtils.YELLOW_NUMBER, rawValue);
		if (isDisabledWorld(disabledWorlds, connectedPlayer.getWorldName())) {
			playersInDisabledWorlds.add(connectedPlayer);
			return;
		}
		PacketAPI.registerScoreboardObjective(connectedPlayer, OBJECTIVE_NAME, TITLE, DISPLAY_SLOT, displayType, getFeatureType());
		int value = getValue(connectedPlayer);
		for (TabPlayer all : TAB.getInstance().getPlayers()){
			all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, connectedPlayer.getName(), value), getFeatureType());
			if (all.isLoaded()) connectedPlayer.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, all.getName(), getValue(all)), getFeatureType());
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
		if (disabledNow && !disabledBefore) {
			p.sendCustomPacket(new PacketPlayOutScoreboardObjective(OBJECTIVE_NAME), getFeatureType());
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
		if (playersInDisabledWorlds.contains(refreshed)) return;
		int value = getValue(refreshed);
		for (TabPlayer all : TAB.getInstance().getPlayers()) {
			all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, refreshed.getName(), value), getFeatureType());
		}
	}

	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = TAB.getInstance().getPlaceholderManager().getUsedPlaceholderIdentifiersRecursive(rawValue);
	}

	@Override
	public String getFeatureType() {
		return "Yellow number";
	}

	@Override
	public void onLoginPacket(TabPlayer packetReceiver) {
		if (playersInDisabledWorlds.contains(packetReceiver)) return;
		PacketAPI.registerScoreboardObjective(packetReceiver, OBJECTIVE_NAME, TITLE, DISPLAY_SLOT, displayType, getFeatureType());
		for (TabPlayer all : TAB.getInstance().getPlayers()){
			if (all.isLoaded()) packetReceiver.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, all.getName(), getValue(all)), getFeatureType());
		}
	}

	@Override
	public boolean onPacketSend(TabPlayer receiver, PacketPlayOutScoreboardDisplayObjective packet) {
		if (playersInDisabledWorlds.contains(receiver)) return false;
		if (packet.getSlot() == DISPLAY_SLOT && !packet.getObjectiveName().equals(OBJECTIVE_NAME)) {
			TAB.getInstance().getErrorManager().printError("Something just tried to register objective \"" + packet.getObjectiveName() + "\" in position " + packet.getSlot() + " (playerlist)", null, false, TAB.getInstance().getErrorManager().getAntiOverrideLog());
			return true;
		}
		return false;
	}

	@Override
	public void onQuit(TabPlayer disconnectedPlayer) {
		playersInDisabledWorlds.remove(disconnectedPlayer);
	}
}