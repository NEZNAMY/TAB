package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.features.types.event.WorldChangeListener;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;

/**
 * Feature handler for header and footer
 */
public class HeaderFooter implements Loadable, JoinEventListener, WorldChangeListener, Refreshable {

	private TAB tab;
	private List<String> usedPlaceholders;
	public List<String> disabledWorlds;
	
	public HeaderFooter(TAB tab) {
		this.tab = tab;
		disabledWorlds = tab.getConfiguration().config.getStringList("disable-features-in-"+tab.getPlatform().getSeparatorType()+"s.header-footer", Arrays.asList("disabled" + tab.getPlatform().getSeparatorType()));
		refreshUsedPlaceholders();
		tab.debug(String.format("Loaded HeaderFooter feature with parameters disabledWorlds=%s", disabledWorlds));
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
			boolean refresh = false;
			String headerAppend = getValue(p, "headerappend");
			if (headerAppend.length() > 0) headerAppend = "\n\u00a7r" + headerAppend;
			String header = getValue(p, "header") + headerAppend;
			if (!p.getProperty("header").getOriginalRawValue().equals(header)) {
				p.setProperty("header", header);
				refresh = true;
			}
			
			String footerAppend = getValue(p, "footerappend");
			if (footerAppend.length() > 0) footerAppend = "\n\u00a7r" + footerAppend;
			String footer = getValue(p, "footer") + footerAppend;
			if (!p.getProperty("footer").getOriginalRawValue().equals(footer)) {
				p.setProperty("footer", footer);
				refresh = true;
			}
			if (refresh) {
				p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter(IChatBaseComponent.optimizedComponent(p.getProperty("header").updateAndGet()), 
						IChatBaseComponent.optimizedComponent(p.getProperty("footer").updateAndGet())), getFeatureType());
			}
		}
	}
	
	@Override
	public void refresh(TabPlayer p, boolean force) {
		if (force) {
			String headerAppend = getValue(p, "headerappend");
			if (headerAppend.length() > 0) headerAppend = "\n\u00a7r" + headerAppend;
			p.setProperty("header", getValue(p, "header") + headerAppend);
			
			String footerAppend = getValue(p, "footerappend");
			if (footerAppend.length() > 0) footerAppend = "\n\u00a7r" + footerAppend;
			p.setProperty("footer", getValue(p, "footer") + footerAppend);
		}
		if (isDisabledWorld(disabledWorlds, p.getWorldName()) || p.getVersion().getMinorVersion() < 8) return;
		p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter(IChatBaseComponent.optimizedComponent(p.getProperty("header").updateAndGet()), 
				IChatBaseComponent.optimizedComponent(p.getProperty("footer").updateAndGet())), getFeatureType());
	}
	
	@Override
	public List<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}

	private String getValue(TabPlayer p, String property) {
		String worldGroup = tab.getConfiguration().getWorldGroupOf(p.getWorldName());
		String[] priorities = {
				"per-" + tab.getPlatform().getSeparatorType() + "-settings." + worldGroup + ".Users." + p.getName() + "." + property,
				"per-" + tab.getPlatform().getSeparatorType() + "-settings." + worldGroup + ".Users." + p.getUniqueId().toString() + "." + property,
				"Users." + p.getName() + "." + property,
				"Users." + p.getUniqueId().toString() + "." + property,
				"per-" + tab.getPlatform().getSeparatorType() + "-settings." + worldGroup + ".Groups." + p.getGroup() + "." + property,
				"per-" + tab.getPlatform().getSeparatorType() + "-settings." + worldGroup + "." + property,
				"Groups." + p.getGroup() + "." + property,
				property
		};
		List<String> lines = null;
		for (String path : priorities) {
			lines = tab.getConfiguration().config.getStringList(path);
			if (lines != null) break;
		}
		if (lines == null) lines = new ArrayList<String>();
		return String.join("\n\u00a7r", lines);
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