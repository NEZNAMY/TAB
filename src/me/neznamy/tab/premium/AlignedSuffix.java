package me.neznamy.tab.premium;

import java.util.HashMap;
import java.util.Map;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.features.SimpleFeature;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class AlignedSuffix implements SimpleFeature{

	private int maxWidth;
	private ITabPlayer maxPlayer;
	public static final Map<Character, Integer> widths = new HashMap<Character, Integer>();

	public AlignedSuffix() {
		//32-47
		widths.put(' ', 3);
		widths.put('!', 1);
		widths.put('"', 5);
		widths.put('#', 5);
		widths.put('$', 5);
		widths.put('%', 5);
		widths.put('&', 5);
		widths.put('\'', 1);
		widths.put('(', 4);
		widths.put(')', 4);
		widths.put('*', 3);
		widths.put('+', 5);
		widths.put(',', 1);
		widths.put('-', 5);
		widths.put('.', 1);
		widths.put('/', 5);
		
		//48-57
		widths.put('0', 5);
		widths.put('1', 5);
		widths.put('2', 5);
		widths.put('3', 5);
		widths.put('4', 5);
		widths.put('5', 5);
		widths.put('6', 5);
		widths.put('7', 5);
		widths.put('8', 5);
		widths.put('9', 5);
		
		//58-64
		widths.put(':', 1);
		widths.put(';', 1);
		widths.put('<', 4);
		widths.put('=', 5);
		widths.put('>', 4);
		widths.put('?', 5);
		widths.put('@', 6);
		
		//65-90
		widths.put('A', 5);
		widths.put('B', 5);
		widths.put('C', 5);
		widths.put('D', 5);
		widths.put('E', 5);
		widths.put('F', 5);
		widths.put('G', 5);
		widths.put('H', 5);
		widths.put('I', 3);
		widths.put('İ', 3);
		widths.put('J', 5);
		widths.put('K', 5);
		widths.put('L', 5);
		widths.put('M', 5);
		widths.put('N', 5);
		widths.put('O', 5);
		widths.put('P', 5);
		widths.put('Q', 5);
		widths.put('R', 5);
		widths.put('S', 5);
		widths.put('T', 5);
		widths.put('U', 5);
		widths.put('V', 5);
		widths.put('W', 5);
		widths.put('X', 5);
		widths.put('Y', 5);
		widths.put('Z', 5);
		
		//91-96
		widths.put('[', 3);
		widths.put('\\', 5);
		widths.put(']', 3);
		widths.put('^', 5);
		widths.put('_', 5);
		widths.put('`', 2);

		//97-122
		widths.put('a', 5);
		widths.put('b', 5);
		widths.put('c', 5);
		widths.put('d', 5);
		widths.put('e', 5);
		widths.put('f', 4);
		widths.put('g', 5);
		widths.put('h', 5);
		widths.put('i', 1);
		widths.put('j', 5);
		widths.put('k', 4);
		widths.put('l', 2);
		widths.put('m', 5);
		widths.put('n', 5);
		widths.put('o', 5);
		widths.put('p', 5);
		widths.put('q', 5);
		widths.put('r', 5);
		widths.put('s', 5);
		widths.put('t', 3);
		widths.put('u', 5);
		widths.put('v', 5);
		widths.put('w', 5);
		widths.put('x', 5);
		widths.put('y', 5);
		widths.put('z', 5);
		
		//123-126
		widths.put('{', 4);
		widths.put('|', 1);
		widths.put('}', 4);
		widths.put('~', 6);
		
		//extra
		widths.put('á', 5);
		widths.put('ä', 5);
		widths.put('č', 3);
		widths.put('ç', 5);
		widths.put('ď', 5);
		widths.put('é', 5);
		widths.put('ě', 5);
		widths.put('í', 1);
		widths.put('ı', 2); //more like 2.5
		widths.put('ľ', 2);
		widths.put('ĺ', 2);
		widths.put('ň', 5);
		widths.put('ó', 5);
		widths.put('ö', 5);
		widths.put('ô', 5);
		widths.put('ŕ', 5);
		widths.put('ř', 5);
		widths.put('š', 5);
		widths.put('ş', 5);
		widths.put('ť', 3);
		widths.put('ü', 5);
		widths.put('ú', 5);
		widths.put('ů', 5);
		widths.put('ý', 5);
		widths.put('ž', 5);
		widths.put((char)9876, 7); //crossed swords
		widths.put((char)12304, 8);
		widths.put((char)12305, 8);
		widths.put((char)10084, 7);
		widths.put((char)10004, 8);
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
			for (ITabPlayer all : Shared.getPlayers()) {
				all.updatePlayerListName();
			}
		} else if (playerNameWidth > maxWidth) {
			maxWidth = playerNameWidth;
			maxPlayer = player;
			for (ITabPlayer all : Shared.getPlayers()) {
				if (all == player) continue;
				all.updatePlayerListName();
			}
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
					Shared.errorManager.oneTimeConsoleError("Unknown character " + c + " (" + ((int)c) + ") found when aligning tabsuffix. Configure it in premiumconfig.yml in extra-character-widths section.");
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
		String format = p.properties.get("tabprefix").get() + 
				p.properties.get("customtabname").get() +
				p.properties.get("tabsuffix").get();
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
			for (ITabPlayer all : Shared.getPlayers()) {
				all.updatePlayerListName();
			}
		}
	}
	@Override
	public void onQuit(ITabPlayer p) {
		if (maxPlayer == p) {
			if (recalculateMaxWidth(p)) {
				for (ITabPlayer all : Shared.getPlayers()) {
					all.updatePlayerListName();
				}
			}
		}
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		if (maxPlayer == p) {
			if (recalculateMaxWidth(null)) {
				for (ITabPlayer all : Shared.getPlayers()) {
					all.updatePlayerListName();
				}
			}
		}
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