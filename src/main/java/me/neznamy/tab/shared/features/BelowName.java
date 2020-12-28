package me.neznamy.tab.shared.features;

import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.LoginPacketListener;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore.Action;

/**
 * Feature handler for BelowName feature
 */
public class BelowName implements Loadable, JoinEventListener, WorldChangeListener, Refreshable, LoginPacketListener {

	private final String ObjectiveName = "TAB-BelowName";
	private final int DisplaySlot = 2;
	private final String numberPropertyName = "belowname-number";
	private final String textPropertyName = "belowname-text";

	private String rawNumber;
	private String rawText;
	private List<String> usedPlaceholders;
	private List<String> disabledWorlds;

	public BelowName() {
		rawNumber = Configs.config.getString("classic-vanilla-belowname.number", "%health%");
		rawText = Configs.config.getString("classic-vanilla-belowname.text", "Health");
		disabledWorlds = Configs.config.getStringList("disable-features-in-"+Shared.platform.getSeparatorType()+"s.belowname", Arrays.asList("disabled" + Shared.platform.getSeparatorType()));
		refreshUsedPlaceholders();
		Shared.featureManager.registerFeature("belowname-text", new Refreshable() {

			private List<String> usedPlaceholders;

			{
				refreshUsedPlaceholders();
			}

			@Override
			public void refresh(TabPlayer refreshed, boolean force) {
				if (isDisabledWorld(disabledWorlds, refreshed.getWorldName())) return;
				refreshed.sendCustomPacket(new PacketPlayOutScoreboardObjective(2, ObjectiveName, refreshed.getProperty(textPropertyName).updateAndGet(), EnumScoreboardHealthDisplay.INTEGER));
			}

			@Override
			public List<String> getUsedPlaceholders() {
				return usedPlaceholders;
			}

			@Override
			public void refreshUsedPlaceholders() {
				usedPlaceholders = PlaceholderManager.getUsedPlaceholderIdentifiersRecursive(rawText);
			}

			@Override
			public TabFeature getFeatureType() {
				return TabFeature.BELOWNAME_TEXT;
			}
		});
	}

	@Override
	public void load() {
		for (TabPlayer loaded : Shared.getPlayers()){
			loaded.setProperty(numberPropertyName, rawNumber);
			loaded.setProperty(textPropertyName, rawText);
			if (isDisabledWorld(disabledWorlds, loaded.getWorldName())) continue;
			PacketAPI.registerScoreboardObjective(loaded, ObjectiveName, loaded.getProperty(textPropertyName).updateAndGet(), DisplaySlot, EnumScoreboardHealthDisplay.INTEGER);
		}
		for (TabPlayer viewer : Shared.getPlayers()){
			for (TabPlayer target : Shared.getPlayers()){
				viewer.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, ObjectiveName, target.getName(), getValue(target)));
			}
		}
	}

	@Override
	public void unload() {
		Object unregister = new PacketPlayOutScoreboardObjective(ObjectiveName).create(ProtocolVersion.SERVER_VERSION);
		for (TabPlayer p : Shared.getPlayers()){
			if (isDisabledWorld(disabledWorlds, p.getWorldName())) continue;
			p.sendPacket(unregister);
		}
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		connectedPlayer.setProperty(numberPropertyName, rawNumber);
		connectedPlayer.setProperty(textPropertyName, rawText);
		if (isDisabledWorld(disabledWorlds, connectedPlayer.getWorldName())) return;
		PacketAPI.registerScoreboardObjective(connectedPlayer, ObjectiveName, connectedPlayer.getProperty(textPropertyName).get(), DisplaySlot, EnumScoreboardHealthDisplay.INTEGER);
		int number = getValue(connectedPlayer);
		for (TabPlayer all : Shared.getPlayers()){
			all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, ObjectiveName, connectedPlayer.getName(), number));
			if (all.isLoaded()) connectedPlayer.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, ObjectiveName, all.getName(), getValue(all)));
		}
	}

	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		if (isDisabledWorld(disabledWorlds, p.getWorldName()) && !isDisabledWorld(disabledWorlds, from)) {
			p.sendCustomPacket(new PacketPlayOutScoreboardObjective(ObjectiveName));
			return;
		}
		if (!isDisabledWorld(disabledWorlds, p.getWorldName()) && isDisabledWorld(disabledWorlds, from)) {
			onJoin(p);
			return;
		}
	}

	private int getValue(TabPlayer p) {
		return Shared.errorManager.parseInteger(p.getProperty(numberPropertyName).updateAndGet(), 0, "belowname");
	}

	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (isDisabledWorld(disabledWorlds, refreshed.getWorldName())) return;
		int number = getValue(refreshed);
		for (TabPlayer all : Shared.getPlayers()) {
			all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, ObjectiveName, refreshed.getName(), number));
		}
	}

	@Override
	public List<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}

	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = PlaceholderManager.getUsedPlaceholderIdentifiersRecursive(rawNumber);
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.BELOWNAME_NUMBER;
	}

	@Override
	public void onLoginPacket(TabPlayer packetReceiver) {
		if (isDisabledWorld(disabledWorlds, packetReceiver.getWorldName())) return;
		PacketAPI.registerScoreboardObjective(packetReceiver, ObjectiveName, packetReceiver.getProperty(textPropertyName).get(), DisplaySlot, EnumScoreboardHealthDisplay.INTEGER);
		for (TabPlayer all : Shared.getPlayers()){
			if (all.isLoaded()) packetReceiver.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, ObjectiveName, all.getName(), getValue(all)));
		}
	}
}