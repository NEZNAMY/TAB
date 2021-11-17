package me.neznamy.tab.shared.features;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;

/**
 * Additional code for Playerlist class to secure alignment
 */
public class AlignedPlayerlist extends Playerlist {

	private int maxWidth;
	private TabPlayer maxPlayer;
	private byte[] widths = new byte[65536];

	public AlignedPlayerlist() {
		int characterId = 1;
		for (String line : new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("widths.txt"))).lines().collect(Collectors.toList())) {
			widths[characterId++] = (byte) Float.parseFloat(line);
		}
		Map<Integer, Integer> widthOverrides = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("tablist-name-formatting.character-width-overrides");
		for (Entry<Integer, Integer> entry : widthOverrides.entrySet()) {
			widths[entry.getKey()] = (byte)(int)entry.getValue();
		}
		TAB.getInstance().debug(String.format("Loaded AlignedSuffix feature with parameters widthOverrides=%s", widthOverrides));
	}

	public String formatNameAndUpdateLeader(TabPlayer player, TabPlayer viewer) {
		int playerNameWidth = getPlayerNameWidth(player);
		if (player == maxPlayer && playerNameWidth < maxWidth) {
			maxWidth = playerNameWidth;
			for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
				int localWidth = getPlayerNameWidth(all);
				if (localWidth > maxWidth) {
					maxWidth = localWidth;
					maxPlayer = all;
				}
			}
			updateAllNames(null);
		} else if (playerNameWidth > maxWidth) {
			maxWidth = playerNameWidth;
			maxPlayer = player;
			updateAllNames(player);
		}
		return formatName(player.getProperty(TabConstants.Property.TABPREFIX).getFormat(viewer) + player.getProperty(TabConstants.Property.CUSTOMTABNAME).getFormat(viewer), player.getProperty(TabConstants.Property.TABSUFFIX).getFormat(viewer));
	}
	
	public String formatName(String prefixAndName, String suffix) {
		int playerNameWidth = getTextWidth(IChatBaseComponent.fromColoredText(prefixAndName + suffix));
		StringBuilder newFormat = new StringBuilder(prefixAndName);
		newFormat.append(EnumChatFormat.COLOR_CHAR);
		newFormat.append("r");
		try {
			newFormat.append(buildSpaces(maxWidth + 12 - playerNameWidth));
		} catch (IllegalArgumentException e) {
			//will investigate later
			newFormat.append(buildSpaces(12));
		}
		newFormat.append(EnumChatFormat.getLastColors(prefixAndName));
		newFormat.append(suffix);
		return newFormat.toString();
	}
	
	/**
	 * Returns text width of characters in given component
	 * @param component - component to get width of
	 * @return text width of characters in given component
	 */
	private int getTextWidth(IChatBaseComponent component) {
		int width = 0;
		if (component.getText() != null) {
			for (Character c : component.getText().toCharArray()) {
				width += widths[c] + 1;
				if (component.getModifier().isBold()) {
					width += 1;
				}
			}
		}
		for (IChatBaseComponent extra : component.getExtra()) {
			width += getTextWidth(extra);
		}
		return width;
	}
	
	/**
	 * Returns width of player's tablist name format
	 * @param p - player to get width for
	 * @return width of player's tablist name format
	 */
	private int getPlayerNameWidth(TabPlayer p) {
		String format = p.getProperty(TabConstants.Property.TABPREFIX).getFormat(null) + p.getProperty(TabConstants.Property.CUSTOMTABNAME).getFormat(null) + p.getProperty(TabConstants.Property.TABSUFFIX).getFormat(null);
		return getTextWidth(IChatBaseComponent.fromColoredText(format));
	}
	
	/**
	 * Returns a combination of normal and bold spaces to build exactly the requested amount of pixels.
	 * Must be at least 12 as lower numbers cannot always be built using numbers 4 (normal space + 1 pixel) and 5 (bold space + 1 pixel)
	 * Returns the result string with normal then bold spaces, such as "   &l   &r"
	 * @param pixelWidth - amount of pixels to be built
	 * @return string consisting of spaces and &l &r
	 * @throws IllegalArgumentException if pixelWidth is < 12
	 */
	private String buildSpaces(int pixelWidth) {
		if (pixelWidth < 12) throw new IllegalArgumentException("Cannot build space lower than 12 pixels wide");
		int pixelsLeft = pixelWidth;
		StringBuilder output = new StringBuilder();
		while (pixelsLeft % 5 != 0) {
			pixelsLeft -= 4;
			output.append(' ');
		}
		output.append(EnumChatFormat.COLOR_CHAR);
		output.append('l');
		while (pixelsLeft > 0) {
			pixelsLeft -= 5;
			output.append(' ');
		}
		output.append(EnumChatFormat.COLOR_CHAR);
		output.append('r');
		return output.toString();
	}
	
	@Override
	public void onQuit(TabPlayer p) {
		super.onQuit(p);
		if (recalculateMaxWidth(p)) {
			updateAllNames(p);
		}
	}
	
	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		super.onWorldChange(p, from, to);
		if (recalculateMaxWidth(null)) {
			updateAllNames(null);
		}
	}

	private void updateAllNames(TabPlayer exception) {
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			if (all == exception) continue;
			refresh(all, true);
		}
	}

	// returns true if max changed, false if not
	private boolean recalculateMaxWidth(TabPlayer ignoredPlayer) {
		int oldMaxWidth = maxWidth;
		maxWidth = 0;
		maxPlayer = null;
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			if (all == ignoredPlayer) continue;
			int localWidth = getPlayerNameWidth(all);
			if (localWidth > maxWidth) {
				maxWidth = localWidth;
				maxPlayer = all;
			}
		}
		return oldMaxWidth != maxWidth;
	}
	
	@Override
	public IChatBaseComponent getTabFormat(TabPlayer p, TabPlayer viewer, boolean updateWidths) {
		Property prefix = p.getProperty(TabConstants.Property.TABPREFIX);
		Property name = p.getProperty(TabConstants.Property.CUSTOMTABNAME);
		Property suffix = p.getProperty(TabConstants.Property.TABSUFFIX);
		if (prefix == null || name == null || suffix == null) {
			return null;
		}
		String format;
		if (updateWidths) {
			format = formatNameAndUpdateLeader(p, viewer);
		} else {
			format = formatName(prefix.getFormat(viewer) + name.getFormat(viewer), suffix.getFormat(viewer));
		}
		return IChatBaseComponent.optimizedComponent(format);
	}
}