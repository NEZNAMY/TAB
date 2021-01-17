package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;

/**
 * Feature handler for header and footer
 */
public class HeaderFooter implements Loadable, JoinEventListener, WorldChangeListener, Refreshable{

	private TAB tab;
	private List<String> usedPlaceholders;
	private List<String> disabledWorlds;
	
	public HeaderFooter(TAB tab) {
		this.tab = tab;
		disabledWorlds = tab.getConfiguration().config.getStringList("disable-features-in-"+tab.getPlatform().getSeparatorType()+"s.header-footer", Arrays.asList("disabled" + tab.getPlatform().getSeparatorType()));
		refreshUsedPlaceholders();
	}
	
	@Override
	public void load() {
		for (TabPlayer p : tab.getPlayers()) {
			refresh(p, true);
		}
	}
	
	@Override
	public void unload() {
		for (TabPlayer p : tab.getPlayers()) {
			if (isDisabledWorld(disabledWorlds, p.getWorldName()) || p.getVersion().getMinorVersion() < 8) continue;
			p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter("",""), getFeatureType());
		}
	}
	
	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		refresh(connectedPlayer, true);
	}
	
	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		if (p.getVersion().getMinorVersion() < 8) return;
		if (isDisabledWorld(disabledWorlds, p.getWorldName())) {
			if (!isDisabledWorld(disabledWorlds, from)) p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter("", ""), getFeatureType());
		} else {
			refresh(p, true);
		}
	}
	
	@Override
	public void refresh(TabPlayer p, boolean force) {
		if (force) {
			updateRawValue(p, "header");
			updateRawValue(p, "footer");
		}
		if (isDisabledWorld(disabledWorlds, p.getWorldName()) || p.getVersion().getMinorVersion() < 8) return;
		p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter(p.getProperty("header").updateAndGet(), p.getProperty("footer").updateAndGet()), getFeatureType());
	}
	
	@Override
	public List<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}

	private void updateRawValue(TabPlayer p, String name) {
		String worldGroup = tab.getConfiguration().getWorldGroupOf(p.getWorldName());
		StringBuilder rawValue = new StringBuilder();
		List<String> lines = tab.getConfiguration().config.getStringList("per-" + tab.getPlatform().getSeparatorType() + "-settings." + worldGroup + ".Users." + p.getName() + "." + name);
		if (lines == null) lines = tab.getConfiguration().config.getStringList("per-" + tab.getPlatform().getSeparatorType() + "-settings." + worldGroup + ".Users." + p.getUniqueId().toString() + "." + name);
		if (lines == null) lines = tab.getConfiguration().config.getStringList("Users." + p.getName() + "." + name);
		if (lines == null) lines = tab.getConfiguration().config.getStringList("Users." + p.getUniqueId().toString() + "." + name);
		if (lines == null) lines = tab.getConfiguration().config.getStringList("per-" + tab.getPlatform().getSeparatorType() + "-settings." + worldGroup + ".Groups." + p.getGroup() + "." + name);
		if (lines == null) lines = tab.getConfiguration().config.getStringList("per-" + tab.getPlatform().getSeparatorType() + "-settings." + worldGroup + "." + name);
		if (lines == null) lines = tab.getConfiguration().config.getStringList("Groups." + p.getGroup() + "." + name);
		if (lines == null) lines = tab.getConfiguration().config.getStringList(name);
		if (lines == null) lines = new ArrayList<String>();
		int i = 0;
		for (String line : lines) {
			if (++i > 1) rawValue.append("\n" + '\u00a7' + "r");
			rawValue.append(line);
		}
		p.setProperty(name, rawValue.toString());
	}
	
	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = tab.getConfiguration().config.getUsedPlaceholderIdentifiersRecursive("header", "footer");
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.HEADER_FOOTER;
	}
}