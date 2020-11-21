package me.neznamy.tab.shared.features;

import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * Feature handler for BelowName feature
 */
public class BelowName implements Loadable, JoinEventListener, WorldChangeListener, Refreshable {

	private final String ObjectiveName = "TAB-BelowName";
	private final int DisplaySlot = 2;
	private final String propertyName = "belowname-number";

	private String number;
	private Property textProperty;
	private List<String> usedPlaceholders;
	private List<String> disabledWorlds;

	public BelowName() {
		number = Configs.config.getString("classic-vanilla-belowname.number", "%health%");
		disabledWorlds = Configs.config.getStringList("disable-features-in-"+Shared.platform.getSeparatorType()+"s.belowname", Arrays.asList("disabled" + Shared.platform.getSeparatorType()));
		refreshUsedPlaceholders();
		String text = Configs.config.getString("classic-vanilla-belowname.text", "Health");
		textProperty = new Property(null, text, null);
		Shared.featureManager.registerFeature("belowname-text", new Refreshable() {

			private List<String> usedPlaceholders;

			{
				refreshUsedPlaceholders();
			}

			@Override
			public void refresh(TabPlayer refreshed, boolean force) {
				if (isDisabledWorld(disabledWorlds, refreshed.getWorldName())) return;
				refreshed.sendCustomPacket(PacketPlayOutScoreboardObjective.UPDATE_TITLE(ObjectiveName, textProperty.updateAndGet(), EnumScoreboardHealthDisplay.INTEGER));
			}

			@Override
			public List<String> getUsedPlaceholders() {
				return usedPlaceholders;
			}

			@Override
			public void refreshUsedPlaceholders() {
				usedPlaceholders = Placeholders.getUsedPlaceholderIdentifiersRecursive(text);
			}

			/**
			 * Returns name of the feature displayed in /tab cpu
			 * @return name of the feature displayed in /tab cpu
			 */
			@Override
			public TabFeature getFeatureType() {
				return TabFeature.BELOWNAME_TEXT;
			}
		});
	}

	@Override
	public void load() {
		for (TabPlayer loaded : Shared.getPlayers()){
			loaded.setProperty(propertyName, number);
			if (isDisabledWorld(disabledWorlds, loaded.getWorldName())) continue;
			PacketAPI.registerScoreboardObjective(loaded, ObjectiveName, textProperty.updateAndGet(), DisplaySlot, EnumScoreboardHealthDisplay.INTEGER);
		}
		for (TabPlayer viewer : Shared.getPlayers()){
			for (TabPlayer target : Shared.getPlayers()){
				PacketAPI.setScoreboardScore(viewer, target.getName(), ObjectiveName, getValue(target));
			}
		}
	}

	@Override
	public void unload() {
		Object unregister = PacketPlayOutScoreboardObjective.UNREGISTER(ObjectiveName).create(ProtocolVersion.SERVER_VERSION);
		for (TabPlayer p : Shared.getPlayers()){
			if (isDisabledWorld(disabledWorlds, p.getWorldName())) continue;
			p.sendPacket(unregister);
		}
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		connectedPlayer.setProperty(propertyName, number);
		if (isDisabledWorld(disabledWorlds, connectedPlayer.getWorldName())) return;
		PacketAPI.registerScoreboardObjective(connectedPlayer, ObjectiveName, textProperty.get(), DisplaySlot, EnumScoreboardHealthDisplay.INTEGER);
		int number = getValue(connectedPlayer);
		for (TabPlayer all : Shared.getPlayers()){
			PacketAPI.setScoreboardScore(all, connectedPlayer.getName(), ObjectiveName, number);
			if (all.isLoaded()) PacketAPI.setScoreboardScore(connectedPlayer, all.getName(), ObjectiveName, getValue(all));
		}
	}

	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		if (isDisabledWorld(disabledWorlds, p.getWorldName()) && !isDisabledWorld(disabledWorlds, from)) {
			p.sendCustomPacket(PacketPlayOutScoreboardObjective.UNREGISTER(ObjectiveName));
			return;
		}
		if (!isDisabledWorld(disabledWorlds, p.getWorldName()) && isDisabledWorld(disabledWorlds, from)) {
			onJoin(p);
			return;
		}
	}

	private int getValue(TabPlayer p) {
		return Shared.errorManager.parseInteger(p.getProperty(propertyName).updateAndGet(), 0, "belowname");
	}

	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (isDisabledWorld(disabledWorlds, refreshed.getWorldName())) return;
		int number = getValue(refreshed);
		for (TabPlayer all : Shared.getPlayers()) {
			PacketAPI.setScoreboardScore(all, refreshed.getName(), ObjectiveName, number);
		}
	}

	@Override
	public List<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}

	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = Placeholders.getUsedPlaceholderIdentifiersRecursive(number);
	}

	/**
	 * Returns name of the feature displayed in /tab cpu
	 * @return name of the feature displayed in /tab cpu
	 */
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.BELOWNAME_NUMBER;
	}
}