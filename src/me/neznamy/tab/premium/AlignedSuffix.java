package me.neznamy.tab.premium;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.Playerlist;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class AlignedSuffix implements Loadable, JoinEventListener, QuitEventListener, WorldChangeListener{

	private int maxWidth;
	private ITabPlayer maxPlayer;
	private Map<Character, Integer> widths = new HashMap<Character, Integer>();
	private Playerlist playerlist;

	@SuppressWarnings("unchecked")
	public AlignedSuffix(Playerlist playerlist) {
		this.playerlist = playerlist;
		loadWidthsFromFile();
		boolean save = false;
		Map<Integer, ?> extraWidths = Premium.premiumconfig.getConfigurationSection("extra-character-widths");
		for (Integer entry : new HashSet<>(extraWidths.keySet())) {
			char c = (char)(int)entry;
			int width = (int)extraWidths.get(entry);
			if (widths.containsKey(c) && widths.get(c) == width) {
				extraWidths.remove((int)c);
				Shared.print('2', "Deleting character width of " + (int)c + " from extra-character-widths because it already exists inside the plugin with the same value.");
				save = true;
				continue;
			}
			widths.put(c, width);
		}
		if (save) Premium.premiumconfig.save();
		Shared.debug("Loaded " + widths.size() + " character widths.");
	}
	private void loadWidthsFromFile() {
		try {
			InputStream input = getClass().getClassLoader().getResourceAsStream("resources/widths.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.isEmpty()) continue;
				String[] arr = line.split(":");
				widths.put((char)Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
			}
			br.close();
		} catch (Exception ex) {
			Shared.errorManager.criticalError("Failed to read character widths from file", ex);
		}
	}
	public String fixTextWidth(ITabPlayer player, String prefixAndName, String suffix) {
		int playerNameWidth = getTextWidth(IChatBaseComponent.fromColoredText(prefixAndName + suffix));
		if (player == maxPlayer && playerNameWidth < maxWidth) {
			maxWidth = playerNameWidth;
			for (ITabPlayer all : Shared.getPlayers()) {
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
			width += component.getText().length()-1;
		}

		if (component.getExtra() != null) {
			for (IChatBaseComponent extra : component.getExtra()) {
				width += getTextWidth(extra);
			}
			width += component.getExtra().size()-1;
		}
		return width;
	}
	private int getPlayerNameWidth(ITabPlayer p) {
		String format = p.properties.get("tabprefix").get() + p.properties.get("customtabname").get() + p.properties.get("tabsuffix").get();
		return getTextWidth(IChatBaseComponent.fromColoredText(format));
	}
	private String buildSpaces(int pixelWidth) {
		if (pixelWidth < 12) throw new IllegalArgumentException("Cannot build space lower than 12 pixels wide");
		int boldSpaces = 0;
		int normalSpaces = 0;
		while (pixelWidth % 4 != 0) {
			pixelWidth -= 5;
			boldSpaces++;
		}
		while (pixelWidth > 0) {
			pixelWidth -= 4;
			normalSpaces++;
		}
		String output = "";
		for (int i=0; i<normalSpaces; i++) {
			output += " ";
		}
		if (boldSpaces > 0) {
			output += Placeholders.colorChar + "l";
			for (int i=0; i<boldSpaces; i++) {
				output += " ";
			}
			output += Placeholders.colorChar + "r";
		}
		return output;
	}

	@Override
	public void load() {
		recalculateMaxWidth(null);
	}
	@Override
	public void unload() {

	}
	@Override
	public void onJoin(ITabPlayer p) {
		int width = getPlayerNameWidth(p);
		if (width > maxWidth) {
			maxWidth = width;
			maxPlayer = p;
			updateAllNames(null);
		}
	}
	@Override
	public void onQuit(ITabPlayer p) {
		if (maxPlayer == p && recalculateMaxWidth(p)) {
			updateAllNames(null);
		}
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		if (maxPlayer == p && recalculateMaxWidth(null)) {
			updateAllNames(null);
		}
	}

	private void updateAllNames(ITabPlayer exception) {
		Shared.featureCpu.runMeasuredTask("aligning tabsuffix", CPUFeature.ALIGNED_TABSUFFIX, new Runnable() {

			@Override
			public void run() {
				for (ITabPlayer all : Shared.getPlayers()) {
					if (all == exception) continue;
					playerlist.refresh(all, true);
				}
			}
		});
	}

	// returns true if max changed, false if not
	private boolean recalculateMaxWidth(ITabPlayer ignoredPlayer) {
		int oldMaxWidth = maxWidth;
		maxWidth = 0;
		maxPlayer = null;
		for (ITabPlayer all : Shared.getPlayers()) {
			if (all == ignoredPlayer) continue;
			int localWidth = getPlayerNameWidth(all);
			if (localWidth > maxWidth) {
				maxWidth = localWidth;
				maxPlayer = all;
			}
		}
		return oldMaxWidth != maxWidth;
	}
}