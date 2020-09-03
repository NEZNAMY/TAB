package me.neznamy.tab.shared.features;

import java.util.Set;

import me.neznamy.tab.shared.ITabPlayer;
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
	private Set<String> usedPlaceholders;
	
	public BelowName() {
		number = Configs.config.getString("classic-vanilla-belowname.number", "%health%");
		refreshUsedPlaceholders();
		String text = Configs.config.getString("classic-vanilla-belowname.text", "Health");
		textProperty = new Property(null, text, null);
		Shared.featureManager.registerFeature("belowname-text", new Refreshable() {
			
			private Set<String> usedPlaceholders;
			
			{
				refreshUsedPlaceholders();
			}
			
			@Override
			public void refresh(ITabPlayer refreshed, boolean force) {
				if (refreshed.disabledBelowname) return;
				refreshed.sendCustomPacket(PacketPlayOutScoreboardObjective.UPDATE_TITLE(ObjectiveName, textProperty.updateAndGet(), EnumScoreboardHealthDisplay.INTEGER));
			}

			@Override
			public Set<String> getUsedPlaceholders() {
				return usedPlaceholders;
			}

			@Override
			public void refreshUsedPlaceholders() {
				usedPlaceholders = Placeholders.getUsedPlaceholderIdentifiersRecursive(text);
			}

			@Override
			public TabFeature getFeatureType() {
				return TabFeature.BELOWNAME_TEXT;
			}
		});
	}
	@Override
	public void load() {
		for (ITabPlayer loaded : Shared.getPlayers()){
			loaded.setProperty(propertyName, number, null);
			if (loaded.disabledBelowname) continue;
			PacketAPI.registerScoreboardObjective(loaded, ObjectiveName, textProperty.updateAndGet(), DisplaySlot, EnumScoreboardHealthDisplay.INTEGER);
		}
		for (ITabPlayer viewer : Shared.getPlayers()){
			for (ITabPlayer target : Shared.getPlayers()){
				PacketAPI.setScoreboardScore(viewer, target.getName(), ObjectiveName, getNumber(target));
			}
		}
	}
	@Override
	public void unload() {
		Object unregister = PacketPlayOutScoreboardObjective.UNREGISTER(ObjectiveName).create(ProtocolVersion.SERVER_VERSION);
		for (ITabPlayer p : Shared.getPlayers()){
			if (p.disabledBelowname) continue;
			p.sendPacket(unregister);
		}
	}
	@Override
	public void onJoin(ITabPlayer connectedPlayer) {
		connectedPlayer.setProperty(propertyName, number, null);
		if (connectedPlayer.disabledBelowname) return;
		PacketAPI.registerScoreboardObjective(connectedPlayer, ObjectiveName, textProperty.get(), DisplaySlot, EnumScoreboardHealthDisplay.INTEGER);
		int number = getNumber(connectedPlayer);
		for (ITabPlayer all : Shared.getPlayers()){
			PacketAPI.setScoreboardScore(all, connectedPlayer.getName(), ObjectiveName, number);
			PacketAPI.setScoreboardScore(connectedPlayer, all.getName(), ObjectiveName, getNumber(all));
		}
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		if (p.disabledBelowname && !p.isDisabledWorld(Configs.disabledBelowname, from)) {
			p.sendCustomPacket(PacketPlayOutScoreboardObjective.UNREGISTER(ObjectiveName));
			return;
		}
		if (!p.disabledBelowname && p.isDisabledWorld(Configs.disabledBelowname, from)) {
			onJoin(p);
			return;
		}
	}
	private int getNumber(ITabPlayer p) {
		return Shared.errorManager.parseInteger(p.getProperty(propertyName).updateAndGet(), 0, "BelowName");
	}
	@Override
	public void refresh(ITabPlayer refreshed, boolean force) {
		if (refreshed.disabledBelowname) return;
		int number = getNumber(refreshed);
		for (ITabPlayer all : Shared.getPlayers()) {
			PacketAPI.setScoreboardScore(all, refreshed.getName(), ObjectiveName, number);
		}
	}
	@Override
	public Set<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}
	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = Placeholders.getUsedPlaceholderIdentifiersRecursive(number);
	}
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.BELOWNAME_NUMBER;
	}
}