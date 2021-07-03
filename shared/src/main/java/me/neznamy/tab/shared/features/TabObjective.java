package me.neznamy.tab.shared.features;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.features.types.event.QuitEventListener;
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
public class TabObjective implements Loadable, JoinEventListener, QuitEventListener, WorldChangeListener, Refreshable, LoginPacketListener, DisplayObjectivePacketListener {

	public static final String OBJECTIVE_NAME = "TAB-YellowNumber";
	public static final int DISPLAY_SLOT = 0;
	private static final String PROPERTY_NAME = "tablist-objective";
	private static final String TITLE = "ms";

	private TAB tab;
	private String rawValue;
	private EnumScoreboardHealthDisplay displayType;
	private List<String> usedPlaceholders;
	private List<String> disabledWorlds;
	private Set<TabPlayer> playersInDisabledWorlds = new HashSet<>();

	public TabObjective(TAB tab) {
		this.tab = tab;
		rawValue = tab.getConfiguration().getConfig().getString("yellow-number-in-tablist", "%ping%");
		disabledWorlds = tab.getConfiguration().getConfig().getStringList("disable-features-in-"+tab.getPlatform().getSeparatorType()+"s.yellow-number", Arrays.asList("disabled" + tab.getPlatform().getSeparatorType()));
		refreshUsedPlaceholders();
		if (rawValue.equals("%health%") || rawValue.equals("%player_health%") || rawValue.equals("%player_health_rounded%")) {
			displayType = EnumScoreboardHealthDisplay.HEARTS;
		} else {
			displayType = EnumScoreboardHealthDisplay.INTEGER;
		}
		tab.debug(String.format("Loaded YellowNumber feature with parameters value=%s, disabledWorlds=%s, displayType=%s", rawValue, disabledWorlds, displayType));
	}

	@Override
	public void load() {
		for (TabPlayer loaded : tab.getPlayers()){
			loaded.setProperty(PROPERTY_NAME, rawValue);
			if (isDisabledWorld(disabledWorlds, loaded.getWorldName())) {
				playersInDisabledWorlds.add(loaded);
				continue;
			}
			PacketAPI.registerScoreboardObjective(loaded, OBJECTIVE_NAME, TITLE, DISPLAY_SLOT, displayType, getFeatureType());
		}
		for (TabPlayer viewer : tab.getPlayers()){
			for (TabPlayer target : tab.getPlayers()){
				viewer.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, target.getName(), getValue(target)), getFeatureType());
			}
		}
	}

	@Override
	public void unload() {
		for (TabPlayer p : tab.getPlayers()){
			if (playersInDisabledWorlds.contains(p)) continue;
			p.sendCustomPacket(new PacketPlayOutScoreboardObjective(OBJECTIVE_NAME), getFeatureType());
		}
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		connectedPlayer.setProperty(PROPERTY_NAME, rawValue);
		if (isDisabledWorld(disabledWorlds, connectedPlayer.getWorldName())) {
			playersInDisabledWorlds.add(connectedPlayer);
			return;
		}
		PacketAPI.registerScoreboardObjective(connectedPlayer, OBJECTIVE_NAME, TITLE, DISPLAY_SLOT, displayType, getFeatureType());
		int value = getValue(connectedPlayer);
		for (TabPlayer all : tab.getPlayers()){
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
		return tab.getErrorManager().parseInteger(p.getProperty(PROPERTY_NAME).updateAndGet(), 0, "yellow number");
	}

	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (playersInDisabledWorlds.contains(refreshed)) return;
		int value = getValue(refreshed);
		for (TabPlayer all : tab.getPlayers()) {
			all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, refreshed.getName(), value), getFeatureType());
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
		if (playersInDisabledWorlds.contains(packetReceiver)) return;
		PacketAPI.registerScoreboardObjective(packetReceiver, OBJECTIVE_NAME, TITLE, DISPLAY_SLOT, displayType, getFeatureType());
		for (TabPlayer all : tab.getPlayers()){
			if (all.isLoaded()) packetReceiver.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, all.getName(), getValue(all)), getFeatureType());
		}
	}

	@Override
	public boolean onPacketSend(TabPlayer receiver, PacketPlayOutScoreboardDisplayObjective packet) {
		if (playersInDisabledWorlds.contains(receiver)) return false;
		if (packet.getSlot() == DISPLAY_SLOT && !packet.getObjectiveName().equals(OBJECTIVE_NAME)) {
			tab.getErrorManager().printError("Something just tried to register objective \"" + packet.getObjectiveName() + "\" in position " + packet.getSlot() + " (playerlist)", null, false, tab.getErrorManager().getAntiOverrideLog());
			return true;
		}
		return false;
	}

	@Override
	public void onQuit(TabPlayer disconnectedPlayer) {
		playersInDisabledWorlds.remove(disconnectedPlayer);
	}
}