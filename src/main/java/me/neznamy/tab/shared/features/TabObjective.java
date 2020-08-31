package me.neznamy.tab.shared.features;

import java.util.Set;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PacketAPI;
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
 * Feature handler for tablist objective feature
 */
public class TabObjective implements Loadable, JoinEventListener, WorldChangeListener, Refreshable {

	private final String ObjectiveName = "TAB-YellowNumber";
	private final int DisplaySlot = 0;
	private final String propertyName = "tablist-objective";
	
	private String rawValue;
	private final String title = "ms";
	private EnumScoreboardHealthDisplay displayType;
	private Set<String> usedPlaceholders;

	public TabObjective() {
		rawValue = Configs.config.getString("yellow-number-in-tablist", "%ping%");
		refreshUsedPlaceholders();
		if (rawValue.equals("%health%") || rawValue.equals("%player_health%") || rawValue.equals("%player_health_rounded%")) {
			displayType = EnumScoreboardHealthDisplay.HEARTS;
		} else {
			displayType = EnumScoreboardHealthDisplay.INTEGER;
		}
	}
	
	@Override
	public void load() {
		for (ITabPlayer loaded : Shared.getPlayers()){
			loaded.setProperty(propertyName, rawValue, null);
			if (loaded.disabledTablistObjective) continue;
			PacketAPI.registerScoreboardObjective(loaded, ObjectiveName, title, DisplaySlot, displayType);
		}
		for (ITabPlayer viewer : Shared.getPlayers()){
			for (ITabPlayer target : Shared.getPlayers()){
				PacketAPI.setScoreboardScore(viewer, target.getName(), ObjectiveName, getValue(target));
			}
		}
	}
	
	@Override
	public void unload() {
		Object unregister = PacketPlayOutScoreboardObjective.UNREGISTER(ObjectiveName).build(ProtocolVersion.SERVER_VERSION);
		for (ITabPlayer p : Shared.getPlayers()){
			if (p.disabledTablistObjective) continue;
			p.sendPacket(unregister);
		}
	}
	
	@Override
	public void onJoin(ITabPlayer connectedPlayer) {
		connectedPlayer.setProperty(propertyName, rawValue, null);
		if (connectedPlayer.disabledTablistObjective) return;
		PacketAPI.registerScoreboardObjective(connectedPlayer, ObjectiveName, title, DisplaySlot, displayType);
		int value = getValue(connectedPlayer);
		for (ITabPlayer all : Shared.getPlayers()){
			PacketAPI.setScoreboardScore(all, connectedPlayer.getName(), ObjectiveName, value);
			PacketAPI.setScoreboardScore(connectedPlayer, all.getName(), ObjectiveName, getValue(all));
		}
	}
	
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		if (p.disabledTablistObjective && !p.isDisabledWorld(Configs.disabledTablistObjective, from)) {
			p.sendCustomPacket(PacketPlayOutScoreboardObjective.UNREGISTER(ObjectiveName));
			return;
		}
		if (!p.disabledTablistObjective && p.isDisabledWorld(Configs.disabledTablistObjective, from)) {
			onJoin(p);
			return;
		}
	}
	
	public int getValue(ITabPlayer p) {
		return Shared.errorManager.parseInteger(p.getProperty(propertyName).updateAndGet(), 0, "Yellow number in tablist");
	}
	
	@Override
	public void refresh(ITabPlayer refreshed, boolean force) {
		if (refreshed.disabledTablistObjective) return;
		int value = getValue(refreshed);
		for (ITabPlayer all : Shared.getPlayers()) {
			PacketAPI.setScoreboardScore(all, refreshed.getName(), ObjectiveName, value);
		}
	}
	
	@Override
	public Set<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}

	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = Placeholders.getUsedPlaceholderIdentifiersRecursive(rawValue);
	}
	
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.BOSSBAR;
	}
}