package me.neznamy.tab.premium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.features.SimpleFeature;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class AlignedSuffix implements SimpleFeature{

	private int maxWidth;
	private static final Map<Character, Integer> widths = new HashMap<Character, Integer>();
	private Map<ITabPlayer, Integer> playerWidths = new HashMap<ITabPlayer, Integer>();
	
	public AlignedSuffix() {
		widths.put('A', 5);
		widths.put('B', 5);
		widths.put('C', 5);
		widths.put('D', 5);
		widths.put('E', 5);
		widths.put('F', 5);
		widths.put('G', 5);
		widths.put('H', 5);
		widths.put('I', 3);
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

		widths.put('a', 5);
		widths.put('á', 5);
		widths.put('ä', 5);
		widths.put('b', 5);
		widths.put('c', 5);
		widths.put('č', 3);
		widths.put('d', 5);
		widths.put('ď', 5);
		widths.put('e', 5);
		widths.put('é', 5);
		widths.put('ě', 5);
		widths.put('f', 4);
		widths.put('g', 5);
		widths.put('h', 5);
		widths.put('i', 1);
		widths.put('í', 1);
		widths.put('j', 5);
		widths.put('k', 4);
		widths.put('l', 2);
		widths.put('ľ', 2);
		widths.put('ĺ', 2);
		widths.put('m', 5);
		widths.put('n', 5);
		widths.put('ň', 5);
		widths.put('o', 5);
		widths.put('ó', 5);
		widths.put('ô', 5);
		widths.put('p', 5);
		widths.put('q', 5);
		widths.put('r', 5);
		widths.put('ŕ', 5);
		widths.put('ř', 5);
		widths.put('s', 5);
		widths.put('š', 5);
		widths.put('t', 3);
		widths.put('ť', 3);
		widths.put('u', 5);
		widths.put('ú', 5);
		widths.put('ů', 5);
		widths.put('v', 5);
		widths.put('w', 5);
		widths.put('x', 5);
		widths.put('y', 5);
		widths.put('ý', 5);
		widths.put('z', 5);
		widths.put('ž', 5);

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

		widths.put('+', 5);
		widths.put('*', 4);
		widths.put('%', 5);
		widths.put('(', 4);
		widths.put(')', 4);
		widths.put('[', 3);
		widths.put(']', 3);
		widths.put(',', 1);
		widths.put('.', 1);
		widths.put('-', 5);
		widths.put('=', 5);
		widths.put('_', 5);
		widths.put(' ', 3);
		widths.put((char)9876, 7); //crossed swords
		widths.put((char)12304, 8);
		widths.put((char)12305, 8);
		widths.put((char)10084, 7);
	}
	public String fixTextWidth(String prefixAndName, String suffix) {
		int currentWidth = getTextWidth(IChatBaseComponent.fromColoredText(prefixAndName + suffix));
		if (currentWidth > maxWidth) {
			maxWidth = currentWidth;
			for (ITabPlayer all : Shared.getPlayers()) {
				all.updatePlayerListName();
			}
		}
		String newFormat = prefixAndName;
		newFormat += buildSpaces(maxWidth + 12 - currentWidth);
		newFormat += Placeholders.getLastColors(prefixAndName);
		newFormat += suffix;
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
					Shared.errorManager.oneTimeConsoleError("Unknown character " + c + " (" + ((int)c) + ")");
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
		int newMax = 0;
		for (ITabPlayer all : Shared.getPlayers()) {
			int localWidth = getPlayerNameWidth(all);
			playerWidths.put(all, localWidth);
			if (localWidth > newMax) newMax = localWidth;
		}
		maxWidth = newMax;
	}
	@Override
	public void unload() {
		playerWidths.clear();
	}
	@Override
	public void onJoin(ITabPlayer p) {
		int width = getPlayerNameWidth(p);
		playerWidths.put(p, width);
		if (width > maxWidth) {
			maxWidth = width;
			for (ITabPlayer all : Shared.getPlayers()) {
				all.updatePlayerListName();
			}
		}
	}
	@Override
	public void onQuit(ITabPlayer p) {
		if (getPlayerNameWidth(p) == maxWidth) {
			List<ITabPlayer> players = new ArrayList<ITabPlayer>();
			players.addAll(Shared.getPlayers());
			players.remove(p);
			int newMax = 0;
			for (ITabPlayer all : players) {
				int localWidth = getPlayerNameWidth(all);
				if (localWidth > newMax) newMax = localWidth;
			}
			maxWidth = newMax;
			for (ITabPlayer all : players) {
				all.updatePlayerListName();
			}
		}
		playerWidths.remove(p);
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		// TODO Auto-generated method stub
	}
}