package me.neznamy.tab.shared.features.sorting.types;

import me.neznamy.tab.api.TabPlayer;

public class PlaceholderZtoA extends SortingType {

	public PlaceholderZtoA(String sortingPlaceholder) {
		super(sortingPlaceholder);
	}
	
	@Override
	public String getChars(TabPlayer p) {
		char[] chars = setPlaceholders(sortingPlaceholder, p).toCharArray();
		for (int i=0; i<chars.length; i++) {
			char c = chars[i];
			if (c >= 65 && c <= 90) {
				chars[i] = (char) (155 - c);
			}
			if (c >= 97 && c <= 122) {
				chars[i] = (char) (219 - c);
			}
		}
		return new String(chars);
	}
	
	@Override
	public String toString() {
		return "PLACEHOLDER_Z_TO_A";
	}
}