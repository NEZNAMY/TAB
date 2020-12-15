package me.neznamy.tab.shared.features.sorting.types;

import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.placeholders.Placeholder;

public abstract class SortingType {

	protected final int DEFAULT_NUMBER = 500000000;
	protected String sortingPlaceholder;
	private List<String> usedPlaceholders;
	
	public SortingType(String sortingPlaceholder){
		this.sortingPlaceholder = sortingPlaceholder;
		usedPlaceholders = PlaceholderManager.getUsedPlaceholderIdentifiersRecursive(sortingPlaceholder);
	}
	
	protected String setPlaceholders(String string, TabPlayer player) {
		String replaced = string;
		if (string.contains("%")) {
			for (String identifier : usedPlaceholders) {
				Placeholder pl = ((PlaceholderManager) Shared.featureManager.getFeature("placeholders")).getPlaceholder(identifier);
				if (pl != null && replaced.contains(pl.getIdentifier())) {
					replaced = pl.set(replaced, player);
				}
			}
		}
		return replaced;
	}
	
	public abstract String getChars(TabPlayer p);
}
