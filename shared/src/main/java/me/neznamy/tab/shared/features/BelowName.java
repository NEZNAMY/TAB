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
 * Feature handler for BelowName feature
 */
public class BelowName extends TabFeature {

	public static final String OBJECTIVE_NAME = "TAB-BelowName";
	public static final int DISPLAY_SLOT = 2;
	private static final String TEXT_USAGE = "Belowname text";

	private String rawNumber;
	private String rawText;

	public BelowName() {
		rawNumber = TAB.getInstance().getConfiguration().getConfig().getString("classic-vanilla-belowname.number", "%health%");
		rawText = TAB.getInstance().getConfiguration().getConfig().getString("classic-vanilla-belowname.text", "Health");
		disabledWorlds = TAB.getInstance().getConfiguration().getConfig().getStringList("disable-features-in-"+TAB.getInstance().getPlatform().getSeparatorType()+"s.belowname", Arrays.asList("disabled" + TAB.getInstance().getPlatform().getSeparatorType()));
		refreshUsedPlaceholders();
		TAB.getInstance().debug(String.format("Loaded BelowName feature with parameters number=%s, text=%s, disabledWorlds=%s", rawNumber, rawText, disabledWorlds));
		TAB.getInstance().getFeatureManager().registerFeature("belowname-text-refresher", new TextRefresher());
	}

	@Override
	public void load() {
		for (TabPlayer loaded : TAB.getInstance().getPlayers()){
			loaded.setProperty(PropertyUtils.BELOWNAME_NUMBER, rawNumber);
			loaded.setProperty(PropertyUtils.BELOWNAME_TEXT, rawText);
			if (isDisabledWorld(disabledWorlds, loaded.getWorldName())) {
				playersInDisabledWorlds.add(loaded);
				continue;
			}
			PacketAPI.registerScoreboardObjective(loaded, OBJECTIVE_NAME, loaded.getProperty(PropertyUtils.BELOWNAME_TEXT).updateAndGet(), DISPLAY_SLOT, EnumScoreboardHealthDisplay.INTEGER, TEXT_USAGE);
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
			p.sendCustomPacket(new PacketPlayOutScoreboardObjective(OBJECTIVE_NAME), TEXT_USAGE);
		}
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		connectedPlayer.setProperty(PropertyUtils.BELOWNAME_NUMBER, rawNumber);
		connectedPlayer.setProperty(PropertyUtils.BELOWNAME_TEXT, rawText);
		if (isDisabledWorld(disabledWorlds, connectedPlayer.getWorldName())) {
			playersInDisabledWorlds.add(connectedPlayer);
			return;
		}
		PacketAPI.registerScoreboardObjective(connectedPlayer, OBJECTIVE_NAME, connectedPlayer.getProperty(PropertyUtils.BELOWNAME_TEXT).get(), DISPLAY_SLOT, EnumScoreboardHealthDisplay.INTEGER, TEXT_USAGE);
		int number = getValue(connectedPlayer);
		for (TabPlayer all : TAB.getInstance().getPlayers()){
			all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, connectedPlayer.getName(), number), getFeatureType());
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
			p.sendCustomPacket(new PacketPlayOutScoreboardObjective(OBJECTIVE_NAME), TEXT_USAGE);
		}
		if (!disabledNow && disabledBefore) {
			onJoin(p);
		}
	}

	private int getValue(TabPlayer p) {
		return TAB.getInstance().getErrorManager().parseInteger(p.getProperty(PropertyUtils.BELOWNAME_NUMBER).updateAndGet(), 0, "belowname number");
	}

	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (playersInDisabledWorlds.contains(refreshed)) return;
		int number = getValue(refreshed);
		for (TabPlayer all : TAB.getInstance().getPlayers()) {
			all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, refreshed.getName(), number), getFeatureType());
		}
	}

	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = TAB.getInstance().getPlaceholderManager().getUsedPlaceholderIdentifiersRecursive(rawNumber);
	}

	@Override
	public Object getFeatureType() {
		return "Belowname number";
	}

	@Override
	public void onLoginPacket(TabPlayer packetReceiver) {
		if (playersInDisabledWorlds.contains(packetReceiver)) return;
		PacketAPI.registerScoreboardObjective(packetReceiver, OBJECTIVE_NAME, packetReceiver.getProperty(PropertyUtils.BELOWNAME_TEXT).get(), DISPLAY_SLOT, EnumScoreboardHealthDisplay.INTEGER, TEXT_USAGE);
		for (TabPlayer all : TAB.getInstance().getPlayers()){
			if (all.isLoaded()) packetReceiver.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, all.getName(), getValue(all)), getFeatureType());
		}
	}

	@Override
	public boolean onPacketSend(TabPlayer receiver, PacketPlayOutScoreboardDisplayObjective packet) {
		if (playersInDisabledWorlds.contains(receiver)) return false;
		if (packet.getSlot() == DISPLAY_SLOT && !packet.getObjectiveName().equals(OBJECTIVE_NAME)) {
			TAB.getInstance().getErrorManager().printError("Something just tried to register objective \"" + packet.getObjectiveName() + "\" in position " + packet.getSlot() + " (belowname)", null, false, TAB.getInstance().getErrorManager().getAntiOverrideLog());
			return true;
		}
		return false;
	}

	public class TextRefresher extends TabFeature {

		public TextRefresher(){
			refreshUsedPlaceholders();
		}

		@Override
		public void refresh(TabPlayer refreshed, boolean force) {
			if (playersInDisabledWorlds.contains(refreshed)) return;
			refreshed.sendCustomPacket(new PacketPlayOutScoreboardObjective(2, OBJECTIVE_NAME, refreshed.getProperty(PropertyUtils.BELOWNAME_TEXT).updateAndGet(), EnumScoreboardHealthDisplay.INTEGER), TEXT_USAGE);
		}

		@Override
		public void refreshUsedPlaceholders() {
			usedPlaceholders = TAB.getInstance().getPlaceholderManager().getUsedPlaceholderIdentifiersRecursive(rawText);
		}

		@Override
		public String getFeatureType() {
			return TEXT_USAGE;
		}
	}

	@Override
	public void onQuit(TabPlayer disconnectedPlayer) {
		playersInDisabledWorlds.remove(disconnectedPlayer);
	}
}