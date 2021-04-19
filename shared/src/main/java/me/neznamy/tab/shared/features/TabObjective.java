package me.neznamy.tab.shared.features;

import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.features.types.event.WorldChangeListener;
import me.neznamy.tab.shared.features.types.packet.DisplayObjectivePacketListener;
import me.neznamy.tab.shared.features.types.packet.LoginPacketListener;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore.Action;

/**
 * Feature handler for tablist objective feature
 */
public class TabObjective implements Loadable, JoinEventListener, WorldChangeListener, Refreshable, LoginPacketListener, DisplayObjectivePacketListener {

	public static final String ObjectiveName = "TAB-YellowNumber";
	public static final int DisplaySlot = 0;
	
	private final String propertyName = "tablist-objective";
	private final String title = "ms";

	private TAB tab;
	private String rawValue;
	private EnumScoreboardHealthDisplay displayType;
	private List<String> usedPlaceholders;
	private List<String> disabledWorlds;

	public TabObjective(TAB tab) {
		this.tab = tab;
		rawValue = tab.getConfiguration().config.getString("yellow-number-in-tablist", "%ping%");
		disabledWorlds = tab.getConfiguration().config.getStringList("disable-features-in-"+tab.getPlatform().getSeparatorType()+"s.yellow-number", Arrays.asList("disabled" + tab.getPlatform().getSeparatorType()));
		refreshUsedPlaceholders();
		if (rawValue.equals("%health%") || rawValue.equals("%player_health%") || rawValue.equals("%player_health_rounded%")) {
			displayType = EnumScoreboardHealthDisplay.HEARTS;
		} else {
			displayType = EnumScoreboardHealthDisplay.INTEGER;
		}
	}

	@Override
	public void load() {
		for (TabPlayer loaded : tab.getPlayers()){
			loaded.setProperty(propertyName, rawValue);
			if (isDisabledWorld(disabledWorlds, loaded.getWorldName())) continue;
			PacketAPI.registerScoreboardObjective(loaded, ObjectiveName, title, DisplaySlot, displayType, getFeatureType());
		}
		for (TabPlayer viewer : tab.getPlayers()){
			for (TabPlayer target : tab.getPlayers()){
				viewer.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, ObjectiveName, target.getName(), getValue(target)), getFeatureType());
			}
		}
	}

	@Override
	public void unload() {
		for (TabPlayer p : tab.getPlayers()){
			if (isDisabledWorld(disabledWorlds, p.getWorldName())) continue;
			p.sendCustomPacket(new PacketPlayOutScoreboardObjective(ObjectiveName), getFeatureType());
		}
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		connectedPlayer.setProperty(propertyName, rawValue);
		if (isDisabledWorld(disabledWorlds, connectedPlayer.getWorldName())) return;
		PacketAPI.registerScoreboardObjective(connectedPlayer, ObjectiveName, title, DisplaySlot, displayType, getFeatureType());
		int value = getValue(connectedPlayer);
		for (TabPlayer all : tab.getPlayers()){
			all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, ObjectiveName, connectedPlayer.getName(), value), getFeatureType());
			if (all.isLoaded()) connectedPlayer.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, ObjectiveName, all.getName(), getValue(all)), getFeatureType());
		}
	}

	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		if (isDisabledWorld(disabledWorlds, p.getWorldName()) && !isDisabledWorld(disabledWorlds, from)) {
			p.sendCustomPacket(new PacketPlayOutScoreboardObjective(ObjectiveName), getFeatureType());
			return;
		}
		if (!isDisabledWorld(disabledWorlds, p.getWorldName()) && isDisabledWorld(disabledWorlds, from)) {
			onJoin(p);
			return;
		}
	}

	private int getValue(TabPlayer p) {
		return tab.getErrorManager().parseInteger(p.getProperty(propertyName).updateAndGet(), 0, "yellow number");
	}

	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (isDisabledWorld(disabledWorlds, refreshed.getWorldName())) return;
		int value = getValue(refreshed);
		for (TabPlayer all : tab.getPlayers()) {
			all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, ObjectiveName, refreshed.getName(), value), getFeatureType());
		}
	}

	@Override
	public List<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}

	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = tab.getPlaceholderManager().getUsedPlaceholderIdentifiersRecursive(rawValue);
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.YELLOW_NUMBER;
	}

	@Override
	public void onLoginPacket(TabPlayer packetReceiver) {
		if (isDisabledWorld(disabledWorlds, packetReceiver.getWorldName())) return;
		PacketAPI.registerScoreboardObjective(packetReceiver, ObjectiveName, title, DisplaySlot, displayType, getFeatureType());
		for (TabPlayer all : tab.getPlayers()){
			if (all.isLoaded()) packetReceiver.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, ObjectiveName, all.getName(), getValue(all)), getFeatureType());
		}
	}

	@Override
	public boolean onPacketSend(TabPlayer receiver, PacketPlayOutScoreboardDisplayObjective packet) {
		if (isDisabledWorld(disabledWorlds, receiver.getWorldName())) return false;
		if (packet.slot == DisplaySlot && !packet.objectiveName.equals(ObjectiveName)) {
			tab.getErrorManager().printError("Something just tried to register objective \"" + packet.objectiveName + "\" in position " + packet.slot + " (playerlist)", null, false, tab.getErrorManager().antiOverrideLog);
			return true;
		}
		return false;
	}
}