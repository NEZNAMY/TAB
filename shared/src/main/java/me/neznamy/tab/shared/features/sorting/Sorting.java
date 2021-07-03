package me.neznamy.tab.shared.features.sorting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.NameTag;
import me.neznamy.tab.shared.features.sorting.types.GroupPermission;
import me.neznamy.tab.shared.features.sorting.types.Groups;
import me.neznamy.tab.shared.features.sorting.types.Placeholder;
import me.neznamy.tab.shared.features.sorting.types.PlaceholderAtoZ;
import me.neznamy.tab.shared.features.sorting.types.PlaceholderHighToLow;
import me.neznamy.tab.shared.features.sorting.types.PlaceholderLowToHigh;
import me.neznamy.tab.shared.features.sorting.types.PlaceholderZtoA;
import me.neznamy.tab.shared.features.sorting.types.SortingType;

/**
 * Class for handling player sorting rules
 */
public class Sorting {

	//tab instance
	private TAB tab;

	//map of all registered sorting types
	private Map<String, SortingType> types = new HashMap<>();
	
	//placeholder to sort by, if sorting type uses it
	private String sortingPlaceholder;
	
	//if sorting is case senstitive or not
	private boolean caseSensitiveSorting = true;
	
	//active sorting types
	private List<SortingType> usedSortingTypes;
	
	/**
	 * Constructs new instance, loads data from configuration and starts repeating task
	 * @param tab - tab instance
	 * @param nametags - nametag feature
	 */
	public Sorting(TAB tab, NameTag nametags) {
		this.tab = tab;
		types.put("GROUPS", new Groups());
		types.put("GROUP_PERMISSIONS", new GroupPermission());
		if (tab.getConfiguration().getPremiumConfig() != null) {
			sortingPlaceholder = tab.getConfiguration().getPremiumConfig().getString("sorting-placeholder", "%some_level_maybe?%");
			caseSensitiveSorting = tab.getConfiguration().getPremiumConfig().getBoolean("case-sentitive-sorting", true);
			types.put("PLACEHOLDER", new Placeholder(getSortingPlaceholder()));
			types.put("PLACEHOLDER_A_TO_Z", new PlaceholderAtoZ(getSortingPlaceholder()));
			types.put("PLACEHOLDER_Z_TO_A", new PlaceholderZtoA(getSortingPlaceholder()));
			types.put("PLACEHOLDER_LOW_TO_HIGH", new PlaceholderLowToHigh(getSortingPlaceholder()));
			types.put("PLACEHOLDER_HIGH_TO_LOW", new PlaceholderHighToLow(getSortingPlaceholder()));
			usedSortingTypes = compile(tab.getConfiguration().getPremiumConfig().getString("sorting-type", "GROUPS"));
		} else {
			usedSortingTypes = new ArrayList<>();
			if (tab.getConfiguration().getConfig().getBoolean("sort-players-by-permissions", false)) {
				getSorting().add(types.get("GROUP_PERMISSIONS"));
			} else {
				getSorting().add(new Groups());
			}
		}
		
		tab.getCPUManager().startRepeatingMeasuredTask(1000, "refreshing team names", TabFeature.SORTING, UsageType.REFRESHING_TEAM_NAME, () -> {

			for (TabPlayer p : tab.getPlayers()) {
				if (!p.isLoaded() || p.getForcedTeamName() != null || p.hasTeamHandlingPaused()) continue;
				String newName = getTeamName(p);
				if (!p.getTeamName().equals(newName)) {
					nametags.unregisterTeam(p);
					((ITabPlayer) p).setTeamName(newName);
					nametags.registerTeam(p);
				}
			}
		});
	}
	
	/**
	 * Compiles sorting type chain into classes
	 * @param string - sorting types separated by "_THEN_"
	 * @return list of compiled sorting types
	 */
	private List<SortingType> compile(String string){
		List<SortingType> list = new ArrayList<>();
		for (String element : string.split("_THEN_")) {
			SortingType type = types.get(element.toUpperCase());
			if (type == null) {
				tab.getErrorManager().startupWarn("\"&e" + type + "&c\" is not a valid sorting type element. Valid options are: &e" + types.keySet() + ". &bUsing GROUPS");
			} else {
				list.add(type);
			}
		}
		if (list.isEmpty()) list.add(new Groups());
		return list;
	}
	
	/**
	 * Constructs team name for specified player
	 * @param p - player to build team name for
	 * @return unique up to 16 character long sequence that sorts the player
	 */
	public String getTeamName(TabPlayer p) {
		((ITabPlayer) p).setTeamNameNote("");
		StringBuilder sb = new StringBuilder();
		for (SortingType type : getSorting()) {
			sb.append(type.getChars((ITabPlayer) p));
		}
		if (sb.length() > 12) {
			sb.setLength(12);
		}
		sb.append(p.getName());
		if (sb.length() > 15) {
			sb.setLength(15);
		}
		return checkTeamName(p, sb, 65);
	}
	
	/**
	 * Checks if team name is available and proceeds to try new values until free name is found
	 * @param p - player to build team name for
	 * @param currentName - current up to 15 character long teamname start
	 * @param id - current character to check as 16th character
	 * @return first available full team name
	 */
	private String checkTeamName(TabPlayer p, StringBuilder currentName, int id) {
		String potentialTeamName = currentName.toString();
		if (!caseSensitiveSorting) potentialTeamName = potentialTeamName.toLowerCase();
		potentialTeamName += (char)id;
		for (TabPlayer all : tab.getPlayers()) {
			if (all == p) continue;
			if (all.getTeamName() != null && all.getTeamName().equals(potentialTeamName)) {
				return checkTeamName(p, currentName, id+1);
			}
		}
		return potentialTeamName;
	}
	
	/**
	 * Converts sorting types into user-friendly sorting types into /tab debug
	 * @return user-friendly representation of sorting types
	 */
	public String typesToString() {
		String[] elements = new String[getSorting().size()];
		for (int i=0; i<getSorting().size(); i++) {
			elements[i] = getSorting().get(i).toString();
		}
		return String.join(" then ", elements);
	}

	public String getSortingPlaceholder() {
		return sortingPlaceholder;
	}

	public List<SortingType> getSorting() {
		return usedSortingTypes;
	}
}