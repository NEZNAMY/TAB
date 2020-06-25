package me.neznamy.tab.shared.features;

import java.util.Set;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class BelowName implements Loadable, JoinEventListener, WorldChangeListener, Refreshable {

	private final String ObjectiveName = "TAB-BelowName";
	private final int DisplaySlot = 2;
	private final String propertyName = "belowname-number";
	
	private String number;
	private Property textProperty;
	private Set<String> usedPlaceholders;
	
	public BelowName() {
		number = Configs.config.getString("classic-vanilla-belowname.number", "%health%");
		usedPlaceholders = Placeholders.getUsedPlaceholderIdentifiersRecursive(number);
		String text = Configs.config.getString("classic-vanilla-belowname.text", "Health");
		textProperty = new Property(null, text, null);
		Shared.registerFeature("belowname-text", new Refreshable() {
			
			private Set<String> usedPlaceholders = Placeholders.getUsedPlaceholderIdentifiersRecursive(text);
			
			@Override
			public void refresh(ITabPlayer refreshed) {
				if (refreshed.disabledBelowname) return;
				refreshed.sendCustomPacket(PacketPlayOutScoreboardObjective.UPDATE_TITLE(ObjectiveName, textProperty.updateAndGet(), EnumScoreboardHealthDisplay.INTEGER));
			}

			@Override
			public Set<String> getUsedPlaceholders() {
				return usedPlaceholders;
			}

			@Override
			public CPUFeature getRefreshCPU() {
				return CPUFeature.BELOWNAME_TEXT;
			}
		});
	}
	@Override
	public void load() {
		for (ITabPlayer p : Shared.getPlayers()){
			p.setProperty(propertyName, number, null);
			if (p.disabledBelowname) continue;
			PacketAPI.registerScoreboardObjective(p, ObjectiveName, textProperty.updateAndGet(), DisplaySlot, EnumScoreboardHealthDisplay.INTEGER);
		}
	}
	@Override
	public void unload() {
		for (ITabPlayer p : Shared.getPlayers()){
			if (p.disabledBelowname) continue;
			p.sendCustomPacket(PacketPlayOutScoreboardObjective.UNREGISTER(ObjectiveName));
		}
	}
	@Override
	public void onJoin(ITabPlayer connectedPlayer) {
		connectedPlayer.setProperty(propertyName, number, null);
		if (connectedPlayer.disabledBelowname) return;
		PacketAPI.registerScoreboardObjective(connectedPlayer, ObjectiveName, textProperty.get(), DisplaySlot, EnumScoreboardHealthDisplay.INTEGER);
		for (ITabPlayer all : Shared.getPlayers()){
			PacketAPI.setScoreboardScore(all, connectedPlayer.getName(), ObjectiveName, getNumber(connectedPlayer));
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
		p.sendCustomPacket(PacketPlayOutScoreboardObjective.UNREGISTER(ObjectiveName));
		onJoin(p);
	}
	private int getNumber(ITabPlayer p) {
		return Shared.errorManager.parseInteger(p.properties.get(propertyName).updateAndGet(), 0, "BelowName");
	}
	@Override
	public void refresh(ITabPlayer refreshed) {
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
	public CPUFeature getRefreshCPU() {
		return CPUFeature.BELOWNAME_NUMBER;
	}
}