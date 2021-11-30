package me.neznamy.tab.shared.features;

import java.util.Arrays;
import java.util.Objects;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore.Action;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;

/**
 * Feature handler for BelowName feature
 */
public class BelowName extends TabFeature {

	public static final String OBJECTIVE_NAME = "TAB-BelowName";
	public static final int DISPLAY_SLOT = 2;

	private final String rawNumber = TAB.getInstance().getConfiguration().getConfig().getString("belowname-objective.number", "%health%");
	private final String rawText = TAB.getInstance().getConfiguration().getConfig().getString("belowname-objective.text", "Health");
	private final TabFeature textRefresher = new TextRefresher();

	public BelowName() {
		super("Belowname", "Updating belowname number", TAB.getInstance().getConfiguration().getConfig().getStringList("belowname-objective.disable-in-servers"),
				TAB.getInstance().getConfiguration().getConfig().getStringList("belowname-objective.disable-in-worlds"));
		TAB.getInstance().getFeatureManager().registerFeature("belowname-text-refresher", textRefresher);
		TAB.getInstance().debug(String.format("Loaded BelowName feature with parameters number=%s, text=%s, disabledWorlds=%s, disabledServers=%s", rawNumber, rawText, Arrays.toString(disabledWorlds), Arrays.toString(disabledServers)));
	}

	@Override
	public void load() {
		for (TabPlayer loaded : TAB.getInstance().getOnlinePlayers()){
			loaded.setProperty(this, TabConstants.Property.BELOWNAME_NUMBER, rawNumber);
			loaded.setProperty(textRefresher, TabConstants.Property.BELOWNAME_TEXT, rawText);
			if (isDisabled(loaded.getServer(), loaded.getWorld())) {
				addDisabledPlayer(loaded);
				continue;
			}
			PacketAPI.registerScoreboardObjective(loaded, OBJECTIVE_NAME, loaded.getProperty(TabConstants.Property.BELOWNAME_TEXT).updateAndGet(), DISPLAY_SLOT, EnumScoreboardHealthDisplay.INTEGER, textRefresher);
		}
		for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()){
			for (TabPlayer target : TAB.getInstance().getOnlinePlayers()){
				viewer.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, getName(target), getValue(target)), this);
			}
		}
	}

	@Override
	public void unload() {
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()){
			if (isDisabledPlayer(p)) continue;
			p.sendCustomPacket(new PacketPlayOutScoreboardObjective(OBJECTIVE_NAME), textRefresher);
		}
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		connectedPlayer.setProperty(this, TabConstants.Property.BELOWNAME_NUMBER, rawNumber);
		connectedPlayer.setProperty(textRefresher, TabConstants.Property.BELOWNAME_TEXT, rawText);
		if (isDisabled(connectedPlayer.getServer(), connectedPlayer.getWorld())) {
			addDisabledPlayer(connectedPlayer);
			return;
		}
		PacketAPI.registerScoreboardObjective(connectedPlayer, OBJECTIVE_NAME, connectedPlayer.getProperty(TabConstants.Property.BELOWNAME_TEXT).get(), DISPLAY_SLOT, EnumScoreboardHealthDisplay.INTEGER, textRefresher);
		int number = getValue(connectedPlayer);
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()){
			if (!all.isLoaded()) continue; //objective not registered yet or player == connectedPlayer
			if (all.getWorld().equals(connectedPlayer.getWorld()) && Objects.equals(all.getServer(), connectedPlayer.getServer())) {
				all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, getName(connectedPlayer), number), this);
				connectedPlayer.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, getName(all), getValue(all)), this);
			}
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
			p.sendCustomPacket(new PacketPlayOutScoreboardObjective(OBJECTIVE_NAME), textRefresher);
			return;
		}
		if (!disabledNow && disabledBefore) {
			onJoin(p);
		}
		int number = getValue(p);
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()){
			if (!all.isLoaded()) continue; //objective not registered yet
			if (all.getWorld().equals(p.getWorld()) && Objects.equals(all.getServer(), p.getServer())) {
				all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, getName(p), number), this);
				p.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, getName(all), getValue(all)), this);
			}
		}
		RedisSupport redis = (RedisSupport) TAB.getInstance().getFeatureManager().getFeature("redisbungee");
		if (redis != null) redis.updateBelowname(p, p.getProperty(TabConstants.Property.BELOWNAME_NUMBER).get());
	}

	public int getValue(TabPlayer p) {
		return TAB.getInstance().getErrorManager().parseInteger(p.getProperty(TabConstants.Property.BELOWNAME_NUMBER).updateAndGet(), 0, "belowname number");
	}

	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (isDisabledPlayer(refreshed)) return;
		int number = getValue(refreshed);
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			if (!all.isLoaded()) continue; //objective not registered yet
			if (all.getWorld().equals(refreshed.getWorld()) && Objects.equals(all.getServer(), refreshed.getServer()))
				all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, OBJECTIVE_NAME, getName(refreshed), number), this);
		}
		RedisSupport redis = (RedisSupport) TAB.getInstance().getFeatureManager().getFeature("redisbungee");
		if (redis != null) redis.updateBelowname(refreshed, refreshed.getProperty(TabConstants.Property.BELOWNAME_NUMBER).get());
	}
	
	private String getName(TabPlayer p) {
		return ((NickCompatibility) TAB.getInstance().getFeatureManager().getFeature("nick")).getNickname(p);
	}

	public class TextRefresher extends TabFeature {

		public TextRefresher(){
			super("Belowname", "Updating belowname text");
		}

		@Override
		public void refresh(TabPlayer refreshed, boolean force) {
			if (isDisabledPlayer(refreshed)) return;
			refreshed.sendCustomPacket(new PacketPlayOutScoreboardObjective(2, OBJECTIVE_NAME, refreshed.getProperty(TabConstants.Property.BELOWNAME_TEXT).updateAndGet(), EnumScoreboardHealthDisplay.INTEGER), textRefresher);
		}
	}
}