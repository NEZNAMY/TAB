package me.neznamy.tab.shared.features;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore.Action;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.PropertyUtils;
import me.neznamy.tab.shared.TAB;

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
		textRefresher = new TextRefresher();
		TAB.getInstance().debug(String.format("Loaded BelowName feature with parameters number=%s, text=%s, disabledWorlds=%s, disabledServers=%s", rawNumber, rawText, disabledWorlds, disabledServers));
		TAB.getInstance().getFeatureManager().registerFeature("belowname-text-refresher", textRefresher);
	}

	@Override
	public void load() {
		for (TabPlayer loaded : TAB.getInstance().getOnlinePlayers()){
			loaded.setProperty(this, PropertyUtils.BELOWNAME_NUMBER, rawNumber);
			loaded.setProperty(textRefresher, PropertyUtils.BELOWNAME_TEXT, rawText);
			if (isDisabled(loaded.getServer(), loaded.getWorld())) {
				disabledPlayers.add(loaded);
				continue;
			}
			PacketAPI.registerScoreboardObjective(loaded, OBJECTIVE_NAME, loaded.getProperty(PropertyUtils.BELOWNAME_TEXT).updateAndGet(), DISPLAY_SLOT, EnumScoreboardHealthDisplay.INTEGER, textRefresher);
		}
		for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()){
			for (TabPlayer target : TAB.getInstance().getOnlinePlayers()){
				viewer.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, target.getName(), getValue(target)), this);
			}
		}
	}

	@Override
	public void unload() {
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()){
			if (disabledPlayers.contains(p)) continue;
			p.sendCustomPacket(new PacketPlayOutScoreboardObjective(OBJECTIVE_NAME), textRefresher);
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
		PacketAPI.registerScoreboardObjective(connectedPlayer, OBJECTIVE_NAME, connectedPlayer.getProperty(PropertyUtils.BELOWNAME_TEXT).get(), DISPLAY_SLOT, EnumScoreboardHealthDisplay.INTEGER, textRefresher);
		int number = getValue(connectedPlayer);
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()){
			all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, connectedPlayer.getName(), number), this);
			if (all.isLoaded()) connectedPlayer.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, all.getName(), getValue(all)), this);
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
			p.sendCustomPacket(new PacketPlayOutScoreboardObjective(OBJECTIVE_NAME), textRefresher);
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
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, refreshed.getName(), number), this);
		}
	}

	@Override
	public void onLoginPacket(TabPlayer packetReceiver) {
		if (disabledPlayers.contains(packetReceiver) || !antiOverride) return;
		PacketAPI.registerScoreboardObjective(packetReceiver, OBJECTIVE_NAME, packetReceiver.getProperty(PropertyUtils.BELOWNAME_TEXT).get(), DISPLAY_SLOT, EnumScoreboardHealthDisplay.INTEGER, textRefresher);
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()){
			if (all.isLoaded()) packetReceiver.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, all.getName(), getValue(all)), this);
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
			refreshed.sendCustomPacket(new PacketPlayOutScoreboardObjective(2, OBJECTIVE_NAME, refreshed.getProperty(PropertyUtils.BELOWNAME_TEXT).updateAndGet(), EnumScoreboardHealthDisplay.INTEGER), textRefresher);
		}
	}
}