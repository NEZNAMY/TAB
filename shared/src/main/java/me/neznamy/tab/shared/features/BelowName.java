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
 * Feature handler for BelowName feature
 */
public class BelowName implements Loadable, JoinEventListener, WorldChangeListener, Refreshable, LoginPacketListener, DisplayObjectivePacketListener {

	public static final String OBJECTIVE_NAME = "TAB-BelowName";
	public static final int DISPLAY_SLOT = 2;
	private static final String NUMBER_PROPERTY = "belowname-number";
	private static final String TEXT_PROPERTY = "belowname-text";

	private TAB tab;
	private String rawNumber;
	private String rawText;
	private List<String> usedPlaceholders;
	private List<String> disabledWorlds;

	public BelowName(TAB tab) {
		this.tab = tab;
		rawNumber = tab.getConfiguration().getConfig().getString("classic-vanilla-belowname.number", "%health%");
		rawText = tab.getConfiguration().getConfig().getString("classic-vanilla-belowname.text", "Health");
		disabledWorlds = tab.getConfiguration().getConfig().getStringList("disable-features-in-"+tab.getPlatform().getSeparatorType()+"s.belowname", Arrays.asList("disabled" + tab.getPlatform().getSeparatorType()));
		refreshUsedPlaceholders();
		tab.debug(String.format("Loaded BelowName feature with parameters number=%s, text=%s, disabledWorlds=%s", rawNumber, rawText, disabledWorlds));
		tab.getFeatureManager().registerFeature("belowname-text-refresher", new TextRefresher());
	}

	@Override
	public void load() {
		for (TabPlayer loaded : tab.getPlayers()){
			loaded.setProperty(NUMBER_PROPERTY, rawNumber);
			loaded.setProperty(TEXT_PROPERTY, rawText);
			if (isDisabledWorld(disabledWorlds, loaded.getWorldName())) continue;
			PacketAPI.registerScoreboardObjective(loaded, OBJECTIVE_NAME, loaded.getProperty(TEXT_PROPERTY).updateAndGet(), DISPLAY_SLOT, EnumScoreboardHealthDisplay.INTEGER, TabFeature.BELOWNAME_TEXT);
		}
		for (TabPlayer viewer : tab.getPlayers()){
			for (TabPlayer target : tab.getPlayers()){
				viewer.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, target.getName(), getValue(target)), TabFeature.BELOWNAME_NUMBER);
			}
		}
	}

	@Override
	public void unload() {
		for (TabPlayer p : tab.getPlayers()){
			if (isDisabledWorld(disabledWorlds, p.getWorldName())) continue;
			p.sendCustomPacket(new PacketPlayOutScoreboardObjective(OBJECTIVE_NAME), TabFeature.BELOWNAME_TEXT);
		}
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		connectedPlayer.setProperty(NUMBER_PROPERTY, rawNumber);
		connectedPlayer.setProperty(TEXT_PROPERTY, rawText);
		if (isDisabledWorld(disabledWorlds, connectedPlayer.getWorldName())) return;
		PacketAPI.registerScoreboardObjective(connectedPlayer, OBJECTIVE_NAME, connectedPlayer.getProperty(TEXT_PROPERTY).get(), DISPLAY_SLOT, EnumScoreboardHealthDisplay.INTEGER, TabFeature.BELOWNAME_TEXT);
		int number = getValue(connectedPlayer);
		for (TabPlayer all : tab.getPlayers()){
			all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, connectedPlayer.getName(), number), TabFeature.BELOWNAME_NUMBER);
			if (all.isLoaded()) connectedPlayer.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, all.getName(), getValue(all)), TabFeature.BELOWNAME_NUMBER);
		}
	}

	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		if (isDisabledWorld(disabledWorlds, p.getWorldName()) && !isDisabledWorld(disabledWorlds, from)) {
			p.sendCustomPacket(new PacketPlayOutScoreboardObjective(OBJECTIVE_NAME), TabFeature.BELOWNAME_TEXT);
		}
		if (!isDisabledWorld(disabledWorlds, p.getWorldName()) && isDisabledWorld(disabledWorlds, from)) {
			onJoin(p);
		}
	}

	private int getValue(TabPlayer p) {
		return tab.getErrorManager().parseInteger(p.getProperty(NUMBER_PROPERTY).updateAndGet(), 0, "belowname number");
	}

	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (isDisabledWorld(disabledWorlds, refreshed.getWorldName())) return;
		int number = getValue(refreshed);
		for (TabPlayer all : tab.getPlayers()) {
			all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, refreshed.getName(), number), TabFeature.BELOWNAME_NUMBER);
		}
	}

	@Override
	public List<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}

	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = tab.getPlaceholderManager().getUsedPlaceholderIdentifiersRecursive(rawNumber);
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.BELOWNAME_NUMBER;
	}

	@Override
	public void onLoginPacket(TabPlayer packetReceiver) {
		if (isDisabledWorld(disabledWorlds, packetReceiver.getWorldName())) return;
		PacketAPI.registerScoreboardObjective(packetReceiver, OBJECTIVE_NAME, packetReceiver.getProperty(TEXT_PROPERTY).get(), DISPLAY_SLOT, EnumScoreboardHealthDisplay.INTEGER, TabFeature.BELOWNAME_TEXT);
		for (TabPlayer all : tab.getPlayers()){
			if (all.isLoaded()) packetReceiver.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, all.getName(), getValue(all)), TabFeature.BELOWNAME_NUMBER);
		}
	}
	
	@Override
	public boolean onPacketSend(TabPlayer receiver, PacketPlayOutScoreboardDisplayObjective packet) {
		if (isDisabledWorld(disabledWorlds, receiver.getWorldName())) return false;
		if (packet.getSlot() == DISPLAY_SLOT && !packet.getObjectiveName().equals(OBJECTIVE_NAME)) {
			tab.getErrorManager().printError("Something just tried to register objective \"" + packet.getObjectiveName() + "\" in position " + packet.getSlot() + " (belowname)", null, false, tab.getErrorManager().getAntiOverrideLog());
			return true;
		}
		return false;
	}
	
	public class TextRefresher implements Refreshable {
		
		private List<String> usedPlaceholders;

		public TextRefresher(){
			refreshUsedPlaceholders();
		}

		@Override
		public void refresh(TabPlayer refreshed, boolean force) {
			if (isDisabledWorld(disabledWorlds, refreshed.getWorldName())) return;
			refreshed.sendCustomPacket(new PacketPlayOutScoreboardObjective(2, OBJECTIVE_NAME, refreshed.getProperty(TEXT_PROPERTY).updateAndGet(), EnumScoreboardHealthDisplay.INTEGER), TabFeature.BELOWNAME_TEXT);
		}

		@Override
		public List<String> getUsedPlaceholders() {
			return usedPlaceholders;
		}

		@Override
		public void refreshUsedPlaceholders() {
			usedPlaceholders = tab.getPlaceholderManager().getUsedPlaceholderIdentifiersRecursive(rawText);
		}

		@Override
		public TabFeature getFeatureType() {
			return TabFeature.BELOWNAME_TEXT;
		}
	}
}