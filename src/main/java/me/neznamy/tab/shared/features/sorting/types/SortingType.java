package me.neznamy.tab.shared.features.sorting.types;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.Placeholder;

public abstract class SortingType {

	protected final int DEFAULT_NUMBER = 500000000;
	protected String sortingPlaceholder;
	private List<String> usedPlaceholders;
	
	public SortingType(String sortingPlaceholder){
		this.sortingPlaceholder = sortingPlaceholder;
		usedPlaceholders = TAB.getInstance().getPlaceholderManager().getUsedPlaceholderIdentifiersRecursive(sortingPlaceholder);
	}
	
	protected String setPlaceholders(String string, TabPlayer player) {
		String replaced = string;
		if (string.contains("%")) {
			for (String identifier : usedPlaceholders) {
				Placeholder pl = TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier);
				if (pl != null && replaced.contains(pl.getIdentifier())) {
					replaced = pl.set(replaced, player);
				}
			}
		}
		return replaced;
	}
	
	protected LinkedHashMap<String, String> loadSortingList() {
		LinkedHashMap<String, String> sortedGroups = new LinkedHashMap<String, String>();
		int index = 1;
		List<String> configList = TAB.getInstance().getConfiguration().config.getStringList("group-sorting-priority-list", Arrays.asList("Owner", "Admin", "Mod", "Helper", "Builder", "Premium", "Player", "default"));
		int charCount = String.valueOf(configList.size()).length(); //1 char for <10 groups, 2 chars for <100 etc
		for (Object group : configList){
			String sort = index+"";
			while (sort.length() < charCount) { 
				sort = "0" + sort;
			}
			for (String group0 : String.valueOf(group).toLowerCase().split(" ")) {
				sortedGroups.put(group0, sort);
			}
			index++;
		}
		return sortedGroups;
	}
	
	public abstract String getChars(TabPlayer p);
}
