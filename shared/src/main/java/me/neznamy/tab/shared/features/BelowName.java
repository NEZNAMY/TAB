package me.neznamy.tab.shared.features;

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
	private boolean antiOverride;
	private TabFeature textRefresher;

	public BelowName() {
		super("Belowname number", TAB.getInstance().getConfiguration().getConfig().getStringList("belowname-objective.disable-in-servers"),
				TAB.getInstance().getConfiguration().getConfig().getStringList("belowname-objective.disable-in-worlds"));
		rawNumber = TAB.getInstance().getConfiguration().getConfig().getString("belowname-objective.number", "%health%");
		rawText = TAB.getInstance().getConfiguration().getConfig().getString("belowname-objective.text", "Health");
		antiOverride = TAB.getInstance().getConfiguration().getConfig().getBoolean("belowname-objective.anti-override", true);
		TAB.getInstance().debug(String.format("Loaded BelowName feature with parameters number=%s, text=%s, disabledWorlds=%s, disabledServers=%s", rawNumber, rawText, disabledWorlds, disabledServers));
		TAB.getInstance().getFeatureManager().registerFeature("belowname-text-refresher", textRefresher = new TextRefresher());
	}

	@Override
	public void load() {
		for (TabPlayer loaded : TAB.getInstance().getPlayers()){
			loaded.setProperty(this, PropertyUtils.BELOWNAME_NUMBER, rawNumber);
			loaded.setProperty(textRefresher, PropertyUtils.BELOWNAME_TEXT, rawText);
			if (isDisabled(loaded.getServer(), loaded.getWorld())) {
				disabledPlayers.add(loaded);
				continue;
			}
			PacketAPI.registerScoreboardObjective(loaded, OBJECTIVE_NAME, loaded.getProperty(PropertyUtils.BELOWNAME_TEXT).updateAndGet(), DISPLAY_SLOT, EnumScoreboardHealthDisplay.INTEGER, TEXT_USAGE);
		}
		for (TabPlayer viewer : TAB.getInstance().getPlayers()){
			for (TabPlayer target : TAB.getInstance().getPlayers()){
				viewer.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, target.getName(), getValue(target)), getFeatureName());
			}
		}
	}

	@Override
	public void unload() {
		for (TabPlayer p : TAB.getInstance().getPlayers()){
			if (disabledPlayers.contains(p)) continue;
			p.sendCustomPacket(new PacketPlayOutScoreboardObjective(OBJECTIVE_NAME), TEXT_USAGE);
		}
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		connectedPlayer.setProperty(this, PropertyUtils.BELOWNAME_NUMBER, rawNumber);
		connectedPlayer.setProperty(textRefresher, PropertyUtils.BELOWNAME_TEXT, rawText);
		if (isDisabled(connectedPlayer.getServer(), connectedPlayer.getWorld())) {
			disabledPlayers.add(connectedPlayer);
			return;
		}
		PacketAPI.registerScoreboardObjective(connectedPlayer, OBJECTIVE_NAME, connectedPlayer.getProperty(PropertyUtils.BELOWNAME_TEXT).get(), DISPLAY_SLOT, EnumScoreboardHealthDisplay.INTEGER, TEXT_USAGE);
		int number = getValue(connectedPlayer);
		for (TabPlayer all : TAB.getInstance().getPlayers()){
			all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, connectedPlayer.getName(), number), getFeatureName());
			if (all.isLoaded()) connectedPlayer.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, all.getName(), getValue(all)), getFeatureName());
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
		if (disabledPlayers.contains(refreshed)) return;
		int number = getValue(refreshed);
		for (TabPlayer all : TAB.getInstance().getPlayers()) {
			all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, refreshed.getName(), number), getFeatureName());
		}
	}

	@Override
	public void onLoginPacket(TabPlayer packetReceiver) {
		if (disabledPlayers.contains(packetReceiver) || !antiOverride) return;
		PacketAPI.registerScoreboardObjective(packetReceiver, OBJECTIVE_NAME, packetReceiver.getProperty(PropertyUtils.BELOWNAME_TEXT).get(), DISPLAY_SLOT, EnumScoreboardHealthDisplay.INTEGER, TEXT_USAGE);
		for (TabPlayer all : TAB.getInstance().getPlayers()){
			if (all.isLoaded()) packetReceiver.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, all.getName(), getValue(all)), getFeatureName());
		}
	}

	@Override
	public boolean onPacketSend(TabPlayer receiver, PacketPlayOutScoreboardDisplayObjective packet) {
		if (disabledPlayers.contains(receiver) || !antiOverride) return false;
		if (packet.getSlot() == DISPLAY_SLOT && !packet.getObjectiveName().equals(OBJECTIVE_NAME)) {
			TAB.getInstance().getErrorManager().printError("Something just tried to register objective \"" + packet.getObjectiveName() + "\" in position " + packet.getSlot() + " (belowname)", null, false, TAB.getInstance().getErrorManager().getAntiOverrideLog());
			return true;
		}
		return false;
	}

	public class TextRefresher extends TabFeature {

		public TextRefresher(){
			super(TEXT_USAGE);
		}

		@Override
		public void refresh(TabPlayer refreshed, boolean force) {
			if (disabledPlayers.contains(refreshed)) return;
			refreshed.sendCustomPacket(new PacketPlayOutScoreboardObjective(2, OBJECTIVE_NAME, refreshed.getProperty(PropertyUtils.BELOWNAME_TEXT).updateAndGet(), EnumScoreboardHealthDisplay.INTEGER), TEXT_USAGE);
		}
	}
}