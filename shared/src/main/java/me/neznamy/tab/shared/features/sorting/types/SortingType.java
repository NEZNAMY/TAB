package me.neznamy.tab.shared.features.sorting.types;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.Placeholder;

public abstract class SortingType {

	//number to add to / subtract from to prevent incorrect sorting with negative values
	protected static final int DEFAULT_NUMBER = 500000000;
	
	//placeholder to sort by, if sorting type uses it
	protected String sortingPlaceholder;
	
	//used placeholders in sorting placeholder
	private List<String> usedPlaceholders;
	
	/**
	 * Constructs new instance
	 */
	protected SortingType() {
	}
	
	/**
	 * Constructs new instance with given parameter
	 * @param sortingPlaceholder - placeholder to sort by
	 */
	protected SortingType(String sortingPlaceholder){
		this.sortingPlaceholder = sortingPlaceholder;
		usedPlaceholders = TAB.getInstance().getPlaceholderManager().getUsedPlaceholderIdentifiersRecursive(sortingPlaceholder);
	}
	
	/**
	 * Applies all placeholders for specified player
	 * @param player - player to set placeholders for
	 * @return text with replaced placeholders
	 */
	protected String setPlaceholders(TabPlayer player) {
		String replaced = sortingPlaceholder;
		if (sortingPlaceholder.contains("%")) {
			for (String identifier : usedPlaceholders) {
				Placeholder pl = TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier);
				if (pl != null && replaced.contains(pl.getIdentifier())) {
					replaced = pl.set(replaced, player);
				}
			}
		}
		return replaced;
	}
	
	/**
	 * Loads sorting list from config and applies sorting numbers
	 * @return map of lowercased groups with their sorting characters
	 */
	protected LinkedHashMap<String, String> loadSortingList() {
		LinkedHashMap<String, String> sortedGroups = new LinkedHashMap<>();
		int index = 1;
		List<String> configList = TAB.getInstance().getConfiguration().getConfig().getStringList("group-sorting-priority-list", Arrays.asList("Owner", "Admin", "Mod", "Helper", "Builder", "Premium", "Player", "default"));
		int charCount = String.valueOf(configList.size()).length(); //1 char for <10 groups, 2 chars for <100 etc
		for (Object group : configList){
			String sort = String.valueOf(index);
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
	
	/**
	 * Returns current sorting characters of this sorting type for specified player
	 * @param p - player to get chars for
	 * @return an as-short-as-possible character sequence for unique sorting
	 */
	public abstract String getChars(ITabPlayer p);
}