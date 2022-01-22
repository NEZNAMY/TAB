package me.neznamy.tab.shared.features;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;

/**
 * Additional code for PlayerList class to secure alignment
 */
public class AlignedPlayerList extends PlayerList {

	private int maxWidth;
	private TabPlayer maxPlayer;
	private final WeakHashMap<TabPlayer, Integer> playerWidths = new WeakHashMap<>();
	
	private final byte[] widths = new byte[65536];

	public AlignedPlayerList() {
		loadWidths();
	}

	/**
	 * Loads widths from included widths.txt file as well as width overrides from config
	 */
	private void loadWidths() {
		InputStream file = getClass().getClassLoader().getResourceAsStream("widths.txt");
		if (file == null) {
			TAB.getInstance().getErrorManager().criticalError("Failed to load widths.txt file. Is it inside the jar? Aligned suffix will not work.", null);
			return;
		}
		int characterId = 1;
		for (String line : new BufferedReader(new InputStreamReader(file)).lines().collect(Collectors.toList())) {
			widths[characterId++] = (byte) Float.parseFloat(line);
		}
		Map<Integer, Integer> widthOverrides = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("tablist-name-formatting.character-width-overrides");
		for (Entry<Integer, Integer> entry : widthOverrides.entrySet()) {
			widths[entry.getKey()] = (byte)(int)entry.getValue();
		}
	}

	public String formatNameAndUpdateLeader(TabPlayer player, TabPlayer viewer) {
		int playerNameWidth = getPlayerNameWidth(player);
		playerWidths.put(player, playerNameWidth);
		if (player == maxPlayer && playerNameWidth < maxWidth) {
			//name became shorter, finding new max player
			playerWidths.put(player, playerNameWidth);
			recalculateMaxWidth(null);
			updateAllNames(null);
		} else if (playerNameWidth > maxWidth) {
			maxWidth = playerNameWidth;
			maxPlayer = player;
			updateAllNames(player);
		}
		return formatName(player.getProperty(TabConstants.Property.TABPREFIX).getFormat(viewer) + player.getProperty(TabConstants.Property.CUSTOMTABNAME).getFormat(viewer), player.getProperty(TabConstants.Property.TABSUFFIX).getFormat(viewer));
	}
	
	public synchronized String formatName(String prefixAndName, String suffix) {
		if (suffix.length() == 0) return prefixAndName;
		int playerNameWidth = getTextWidth(IChatBaseComponent.fromColoredText(prefixAndName + suffix));
		StringBuilder newFormat = new StringBuilder(prefixAndName).append(EnumChatFormat.RESET.getFormat());
		int length = maxWidth + 12 - playerNameWidth;
		try {
			newFormat.append(buildSpaces(length));
		} catch (IllegalArgumentException e) {
			//will investigate later
			newFormat.append(buildSpaces(12));
			TAB.getInstance().getErrorManager().printError("Could not build space consisting of " + length + " pixels", e);
		}
		return newFormat.append(EnumChatFormat.getLastColors(prefixAndName)).append(suffix).toString();
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
	 * Returns width of player's TabList name format
	 * @param p - player to get width for
	 * @return width of player's TabList name format
	 */
	private int getPlayerNameWidth(TabPlayer p) {
		String format = p.getProperty(TabConstants.Property.TABPREFIX).getFormat(p) + p.getProperty(TabConstants.Property.CUSTOMTABNAME).getFormat(p) + p.getProperty(TabConstants.Property.TABSUFFIX).getFormat(p);
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
	public void load(){
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			updateProperties(all);
			playerWidths.put(all, getPlayerNameWidth(all));
			if (isDisabled(all.getServer(), all.getWorld())) {
				addDisabledPlayer(all);
			}
		}
		recalculateMaxWidth(null);
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			refresh(all, true);
		}
	}
	
	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		updateProperties(connectedPlayer);
		int width = getPlayerNameWidth(connectedPlayer);
		playerWidths.put(connectedPlayer, width);
		if (width > maxWidth) {
			maxWidth = width;
			maxPlayer = connectedPlayer;
			updateAllNames(connectedPlayer);
		}
		if (isDisabled(connectedPlayer.getServer(), connectedPlayer.getWorld())) {
			addDisabledPlayer(connectedPlayer);
			return;
		}
		Runnable r = () -> {
			refresh(connectedPlayer, true);
			if (connectedPlayer.getVersion().getMinorVersion() < 8) return;
			List<PlayerInfoData> list = new ArrayList<>();
			for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
				if (all == connectedPlayer) continue; //already sent 4 lines above
				list.add(new PlayerInfoData(getTablistUUID(all, connectedPlayer), getTabFormat(all, connectedPlayer, false)));
			}
			if (!list.isEmpty()) connectedPlayer.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, list), this);
		};
		r.run();
		//add packet might be sent after tab's refresh packet, resending again when anti-override is disabled
		if (!antiOverrideTabList || !TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.PIPELINE_INJECTION))
			TAB.getInstance().getCPUManager().runTaskLater(300, "processing PlayerJoinEvent", this, TabConstants.CpuUsageCategory.PLAYER_JOIN, r);
	}
	
	@Override
	public void onQuit(TabPlayer p) {
		super.onQuit(p);
		if (maxPlayer == p && recalculateMaxWidth(p)) {
			updateAllNames(p);
		}
	}

	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		super.onWorldChange(p, from, to);
		int width = getPlayerNameWidth(p);
		if (width != playerWidths.get(p)) {
			playerWidths.put(p, width);
			refresh(p, true);
		}
		if (maxPlayer == p && recalculateMaxWidth(null)) {
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
		int newMaxWidth = 0;
		TabPlayer newMaxPlayer = null;
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			if (all == ignoredPlayer) continue;
			int localWidth = playerWidths.get(all);
			if (localWidth > newMaxWidth) {
				newMaxWidth = localWidth;
				newMaxPlayer = all;
			}
		}
		boolean changed = newMaxWidth != maxWidth;
		maxPlayer = newMaxPlayer;
		maxWidth = newMaxWidth;
		return changed;
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