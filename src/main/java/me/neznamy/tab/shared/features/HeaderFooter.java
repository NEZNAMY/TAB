package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * Feature handler for header and footer
 */
public class HeaderFooter implements Loadable, JoinEventListener, WorldChangeListener, Refreshable{

	private List<String> usedPlaceholders;
	private List<String> disabledWorlds;
	
	public HeaderFooter() {
		disabledWorlds = Configs.config.getStringList("disable-features-in-"+Shared.platform.getSeparatorType()+"s.header-footer", Arrays.asList("disabled" + Shared.platform.getSeparatorType()));
		refreshUsedPlaceholders();
	}
	
	@Override
	public void load() {
		for (TabPlayer p : Shared.getPlayers()) {
			refresh(p, true);
		}
	}
	
	@Override
	public void unload() {
		for (TabPlayer p : Shared.getPlayers()) {
			if (isDisabledWorld(disabledWorlds, p.getWorldName()) || p.getVersion().getMinorVersion() < 8) continue;
			p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter("",""));
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
			if (!isDisabledWorld(disabledWorlds, from)) p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter("", ""));
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
		p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter(p.getProperty("header").updateAndGet(), p.getProperty("footer").updateAndGet()));
	}
	
	@Override
	public List<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}

	private void updateRawValue(TabPlayer p, String name) {
		String worldGroup = Configs.getWorldGroupOf(p.getWorldName());
		StringBuilder rawValue = new StringBuilder();
		List<String> lines = Configs.config.getStringList("per-" + Shared.platform.getSeparatorType() + "-settings." + worldGroup + ".Users." + p.getName() + "." + name);
		if (lines == null) lines = Configs.config.getStringList("per-" + Shared.platform.getSeparatorType() + "-settings." + worldGroup + ".Users." + p.getUniqueId().toString() + "." + name);
		if (lines == null) lines = Configs.config.getStringList("Users." + p.getName() + "." + name);
		if (lines == null) lines = Configs.config.getStringList("Users." + p.getUniqueId().toString() + "." + name);
		if (lines == null) lines = Configs.config.getStringList("per-" + Shared.platform.getSeparatorType() + "-settings." + worldGroup + ".Groups." + p.getGroup() + "." + name);
		if (lines == null) lines = Configs.config.getStringList("per-" + Shared.platform.getSeparatorType() + "-settings." + worldGroup + "." + name);
		if (lines == null) lines = Configs.config.getStringList("Groups." + p.getGroup() + "." + name);
		if (lines == null) lines = Configs.config.getStringList(name);
		if (lines == null) lines = new ArrayList<String>();
		int i = 0;
		for (String line : lines) {
			if (++i > 1) rawValue.append("\n" + Placeholders.colorChar + "r");
			rawValue.append(line);
		}
		p.setProperty(name, rawValue.toString());
	}
	
	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = Configs.config.getUsedPlaceholderIdentifiersRecursive("header", "footer");
	}
	
	/**
	 * Returns name of the feature displayed in /tab cpu
	 * @return name of the feature displayed in /tab cpu
	 */
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.HEADER_FOOTER;
	}
}