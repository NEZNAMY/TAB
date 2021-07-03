package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.PropertyUtils;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.features.types.event.QuitEventListener;
import me.neznamy.tab.shared.features.types.event.WorldChangeListener;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;

/**
 * Feature handler for header and footer
 */
public class HeaderFooter implements Loadable, JoinEventListener, QuitEventListener, WorldChangeListener, Refreshable {

	private static final String LINE_SEPARATOR = "\n\u00a7r";
	private TAB tab;
	private List<String> usedPlaceholders;
	private List<String> disabledWorlds;
	private Set<TabPlayer> playersInDisabledWorlds = new HashSet<>();
	
	public HeaderFooter(TAB tab) {
		this.tab = tab;
		disabledWorlds = tab.getConfiguration().getConfig().getStringList("disable-features-in-"+tab.getPlatform().getSeparatorType()+"s.header-footer", Arrays.asList("disabled" + tab.getPlatform().getSeparatorType()));
		refreshUsedPlaceholders();
		tab.debug(String.format("Loaded HeaderFooter feature with parameters disabledWorlds=%s", disabledWorlds));
	}
	
	@Override
	public void load() {
		for (TabPlayer p : tab.getPlayers()) {
			onJoin(p);
		}
	}
	
	@Override
	public void unload() {
		for (TabPlayer p : tab.getPlayers()) {
			if (playersInDisabledWorlds.contains(p) || p.getVersion().getMinorVersion() < 8) continue;
			p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter("",""), getFeatureType());
		}
	}
	
	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		if (isDisabledWorld(disabledWorlds, connectedPlayer.getWorldName())) {
			playersInDisabledWorlds.add(connectedPlayer);
			return;
		}
		refresh(connectedPlayer, true);
	}
	
	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		boolean disabledBefore = playersInDisabledWorlds.contains(p);
		boolean disabledNow = false;
		if (isDisabledWorld(disabledWorlds, p.getWorldName())) {
			disabledNow = true;
			playersInDisabledWorlds.add(p);
		} else {
			playersInDisabledWorlds.remove(p);
		}
		if (p.getVersion().getMinorVersion() < 8) return;
		if (disabledNow) {
			if (!disabledBefore) p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter("", ""), getFeatureType());
		} else {
			if (disabledBefore) {
				refresh(p, true);
				return;
			}
			boolean refresh = false;
			String headerAppend = getValue(p, "headerappend");
			if (headerAppend.length() > 0) headerAppend = LINE_SEPARATOR + headerAppend;
			String header = getValue(p, PropertyUtils.HEADER) + headerAppend;
			if (!p.getProperty(PropertyUtils.HEADER).getOriginalRawValue().equals(header)) {
				p.setProperty(PropertyUtils.HEADER, header);
				refresh = true;
			}
			
			String footerAppend = getValue(p, "footerappend");
			if (footerAppend.length() > 0) footerAppend = LINE_SEPARATOR + footerAppend;
			String footer = getValue(p, PropertyUtils.FOOTER) + footerAppend;
			if (!p.getProperty(PropertyUtils.FOOTER).getOriginalRawValue().equals(footer)) {
				p.setProperty(PropertyUtils.FOOTER, footer);
				refresh = true;
			}
			if (refresh) {
				p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter(IChatBaseComponent.optimizedComponent(p.getProperty(PropertyUtils.HEADER).updateAndGet()), 
						IChatBaseComponent.optimizedComponent(p.getProperty(PropertyUtils.FOOTER).updateAndGet())), getFeatureType());
			}
		}
	}
	
	@Override
	public void refresh(TabPlayer p, boolean force) {
		if (force) {
			String headerAppend = getValue(p, "headerappend");
			if (headerAppend.length() > 0) headerAppend = LINE_SEPARATOR + headerAppend;
			p.setProperty(PropertyUtils.HEADER, getValue(p, PropertyUtils.HEADER) + headerAppend);
			
			String footerAppend = getValue(p, "footerappend");
			if (footerAppend.length() > 0) footerAppend = LINE_SEPARATOR + footerAppend;
			p.setProperty(PropertyUtils.FOOTER, getValue(p, PropertyUtils.FOOTER) + footerAppend);
		}
		if (playersInDisabledWorlds.contains(p) || p.getVersion().getMinorVersion() < 8) return;
		p.sendCustomPacket(new PacketPlayOutPlayerListHeaderFooter(IChatBaseComponent.optimizedComponent(p.getProperty(PropertyUtils.HEADER).updateAndGet()), 
				IChatBaseComponent.optimizedComponent(p.getProperty(PropertyUtils.FOOTER).updateAndGet())), getFeatureType());
	}
	
	@Override
	public List<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}

	private String getValue(TabPlayer p, String property) {
		String worldGroup = tab.getConfiguration().getWorldGroupOf(tab.getConfiguration().getConfig().getConfigurationSection("per-" + tab.getPlatform().getSeparatorType() + "-settings").keySet(), p.getWorldName());
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
			lines = tab.getConfiguration().getConfig().getStringList(path);
			if (lines != null) break;
		}
		if (lines == null) lines = new ArrayList<>();
		return String.join(LINE_SEPARATOR, lines);
	}
	
	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = tab.getConfiguration().getConfig().getUsedPlaceholderIdentifiersRecursive(PropertyUtils.HEADER, PropertyUtils.FOOTER);
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.HEADER_FOOTER;
	}

	@Override
	public void onQuit(TabPlayer disconnectedPlayer) {
		playersInDisabledWorlds.remove(disconnectedPlayer);
	}
}