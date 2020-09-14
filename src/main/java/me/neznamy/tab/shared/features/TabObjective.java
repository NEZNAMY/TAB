package me.neznamy.tab.shared.features;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import me.neznamy.tab.api.TabPlayer;
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
	private List<String> disabledWorlds;

	public TabObjective() {
		rawValue = Configs.config.getString("yellow-number-in-tablist", "%ping%");
		disabledWorlds = Configs.config.getStringList("disable-features-in-"+Shared.platform.getSeparatorType()+"s.tablist-objective", Arrays.asList("disabled" + Shared.platform.getSeparatorType()));
		refreshUsedPlaceholders();
		if (rawValue.equals("%health%") || rawValue.equals("%player_health%") || rawValue.equals("%player_health_rounded%")) {
			displayType = EnumScoreboardHealthDisplay.HEARTS;
		} else {
			displayType = EnumScoreboardHealthDisplay.INTEGER;
		}
	}
	
	@Override
	public void load() {
		for (TabPlayer loaded : Shared.getPlayers()){
			loaded.setProperty(propertyName, rawValue);
			if (isDisabledWorld(disabledWorlds, loaded.getWorldName())) continue;
			PacketAPI.registerScoreboardObjective(loaded, ObjectiveName, title, DisplaySlot, displayType);
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
		connectedPlayer.setProperty(propertyName, rawValue);
		if (isDisabledWorld(disabledWorlds, connectedPlayer.getWorldName())) return;
		PacketAPI.registerScoreboardObjective(connectedPlayer, ObjectiveName, title, DisplaySlot, displayType);
		int value = getValue(connectedPlayer);
		for (TabPlayer all : Shared.getPlayers()){
			PacketAPI.setScoreboardScore(all, connectedPlayer.getName(), ObjectiveName, value);
			PacketAPI.setScoreboardScore(connectedPlayer, all.getName(), ObjectiveName, getValue(all));
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
	
	public int getValue(TabPlayer p) {
		return Shared.errorManager.parseInteger(p.getProperty(propertyName).updateAndGet(), 0, "Yellow number in tablist");
	}
	
	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (isDisabledWorld(disabledWorlds, refreshed.getWorldName())) return;
		int value = getValue(refreshed);
		for (TabPlayer all : Shared.getPlayers()) {
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