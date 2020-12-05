package me.neznamy.tab.premium;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.Playerlist;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * Additional code for Playerlist class to secure alignment
 */
public class AlignedSuffix implements Loadable, JoinEventListener, QuitEventListener, WorldChangeListener{

	private int maxWidth;
	private TabPlayer maxPlayer;
	private Map<Character, Byte> widths = new HashMap<Character, Byte>();
	private Playerlist playerlist;

	public AlignedSuffix(Playerlist playerlist) {
		this.playerlist = playerlist;
		loadWidthsFromFile();
		boolean save = false;
		Map<Integer, Integer> extraWidths = Premium.premiumconfig.getConfigurationSection("extra-character-widths");
		for (Integer entry : new HashSet<>(extraWidths.keySet())) {
			char c = (char)(int)entry;
			int width = (int)extraWidths.get(entry);
			if (widths.containsKey(c)) {
				extraWidths.remove((int)c);
				Shared.print('2', "Deleting character width of " + (int)c + " from extra-character-widths because it already exists inside the plugin.");
				save = true;
			} else {
				widths.put(c, (byte)width);
			}
		}
		if (save) Premium.premiumconfig.save();
		Shared.debug("Loaded " + widths.size() + " character widths.");
	}
	private void loadWidthsFromFile() {
		try {
			InputStream input = getClass().getClassLoader().getResourceAsStream("widths.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.isEmpty()) continue;
				String[] arr = line.split(":");
				widths.put((char)Integer.parseInt(arr[0]), (byte)Integer.parseInt(arr[1]));
			}
			br.close();
		} catch (Exception ex) {
			Shared.errorManager.criticalError("Failed to read character widths from file", ex);
		}
	}
	public String fixTextWidth(TabPlayer player, String prefixAndName, String suffix) {
		int playerNameWidth = getTextWidth(IChatBaseComponent.fromColoredText(prefixAndName + suffix));
		if (player == maxPlayer && playerNameWidth < maxWidth) {
			maxWidth = playerNameWidth;
			for (TabPlayer all : Shared.getPlayers()) {
				int localWidth = getPlayerNameWidth(all);
				if (localWidth > maxWidth) {
					maxWidth = localWidth;
					maxPlayer = all;
				}
			}
			updateAllNames(null, UsageType.PACKET_READING);
		} else if (playerNameWidth > maxWidth) {
			maxWidth = playerNameWidth;
			maxPlayer = player;
			updateAllNames(player, UsageType.PACKET_READING);
		}
		String newFormat = prefixAndName + Placeholders.colorChar + "r";
		try {
			newFormat += buildSpaces(maxWidth + 12 - playerNameWidth);
		} catch (IllegalArgumentException e) {
			//will investigate later
			newFormat += buildSpaces(12);
		}
		newFormat += Placeholders.getLastColors(prefixAndName) + suffix;
		return newFormat;
	}
	private int getTextWidth(IChatBaseComponent component) {
		int width = 0;
		if (component.getText() != null) {
			for (Character c : component.getText().toCharArray()) {
				if (widths.containsKey(c)) {
					int localWidth = widths.get(c);
					if (component.isBold()) {
						width += localWidth+1;
					} else {
						width += localWidth;
					}
				} else {
					Shared.errorManager.oneTimeConsoleError("Unknown character " + c + " (" + ((int)c) + ") found when aligning tabsuffix. Configure it using /tab width <character|ID>.");
				}
			}
			//there is 1 pixel space between characters, but not after last one
			width += component.getText().length()-1;
		}

		if (component.getExtra() != null) {
			for (IChatBaseComponent extra : component.getExtra()) {
				int extraWidth = getTextWidth(extra);
				//ignoring empty components
				if (extraWidth > 0) {
					width += extraWidth + 1; //1 pixel space between characters
				}
			}
		}
		return width;
	}
	private int getPlayerNameWidth(TabPlayer p) {
		String format = p.getProperty("tabprefix").get() + p.getProperty("customtabname").get() + p.getProperty("tabsuffix").get();
		return getTextWidth(IChatBaseComponent.fromColoredText(format));
	}
	
	/**
	 * Returns a combination of normal and bold spaces to build exactly the requested amount of pixels.
	 * Must be at least 12 as lower numbers cannot always be built using numbers 4 (normal space + 1 pixel) and 5 (bold space + 1 pixel)
	 * Returns the result string with normal then bold spaces, such as "   &l   "
	 * @param pixelWidth - amount of pixels to be built
	 * @return string consisting of spaces and &l &r (if needed)
	 */
	private String buildSpaces(int pixelWidth) {
		if (pixelWidth < 12) throw new IllegalArgumentException("Cannot build space lower than 12 pixels wide");
		int pixelsLeft = pixelWidth;
		int boldSpaces = 0;
		int normalSpaces = 0;
		while (pixelsLeft % 4 != 0) {
			pixelsLeft -= 5;
			boldSpaces++;
		}
		while (pixelsLeft > 0) {
			pixelsLeft -= 4;
			normalSpaces++;
		}
		String output = "";
		for (int i=0; i<normalSpaces; i++) {
			output += " ";
		}
		if (boldSpaces > 0) {
			output += "&l";
			for (int i=0; i<boldSpaces; i++) {
				output += " ";
			}
			output += "&r";
		}
		return Placeholders.color(output);
	}

	@Override
	public void load() {
		recalculateMaxWidth(null);
	}
	@Override
	public void unload() {
		//nothing to do here, Playerlist feature handles unloading
	}
	@Override
	public void onJoin(TabPlayer p) {
		int width = getPlayerNameWidth(p);
		if (width > maxWidth) {
			maxWidth = width;
			maxPlayer = p;
			updateAllNames(null, UsageType.PLAYER_JOIN_EVENT);
		}
	}
	@Override
	public void onQuit(TabPlayer p) {
		if (maxPlayer == p && recalculateMaxWidth(p)) {
			updateAllNames(null, UsageType.PLAYER_QUIT_EVENT);
		}
	}
	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		if (maxPlayer == p && recalculateMaxWidth(null)) {
			updateAllNames(null, UsageType.WORLD_SWITCH_EVENT);
		}
	}

	private void updateAllNames(TabPlayer exception, UsageType usage) {
		Shared.cpu.runMeasuredTask("aligning tabsuffix", TabFeature.ALIGNED_TABSUFFIX, usage, new Runnable() {

			@Override
			public void run() {
				for (TabPlayer all : Shared.getPlayers()) {
					if (all == exception) continue;
					playerlist.refresh(all, true);
				}
			}
		});
	}

	// returns true if max changed, false if not
	private boolean recalculateMaxWidth(TabPlayer ignoredPlayer) {
		int oldMaxWidth = maxWidth;
		maxWidth = 0;
		maxPlayer = null;
		for (TabPlayer all : Shared.getPlayers()) {
			if (all == ignoredPlayer) continue;
			int localWidth = getPlayerNameWidth(all);
			if (localWidth > maxWidth) {
				maxWidth = localWidth;
				maxPlayer = all;
			}
		}
		return oldMaxWidth != maxWidth;
	}
	
	/**
	 * Returns name of the feature displayed in /tab cpu
	 * @return name of the feature displayed in /tab cpu
	 */
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.ALIGNED_TABSUFFIX;
	}
}