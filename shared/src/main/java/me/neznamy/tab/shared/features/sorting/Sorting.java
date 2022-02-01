package me.neznamy.tab.shared.features.sorting;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.layout.LayoutManager;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.sorting.types.Groups;
import me.neznamy.tab.shared.features.sorting.types.Permissions;
import me.neznamy.tab.shared.features.sorting.types.Placeholder;
import me.neznamy.tab.shared.features.sorting.types.PlaceholderAtoZ;
import me.neznamy.tab.shared.features.sorting.types.PlaceholderHighToLow;
import me.neznamy.tab.shared.features.sorting.types.PlaceholderLowToHigh;
import me.neznamy.tab.shared.features.sorting.types.PlaceholderZtoA;
import me.neznamy.tab.shared.features.sorting.types.SortingType;

/**
 * Class for handling player sorting rules
 */
public class Sorting extends TabFeature {

	private final NameTag nameTags;
	
	//map of all registered sorting types
	private final Map<String, BiFunction<Sorting, String, SortingType>> types = new LinkedHashMap<>();
	
	//if sorting is case-sensitive or not
	private final boolean caseSensitiveSorting = TAB.getInstance().getConfiguration().getConfig().getBoolean("scoreboard-teams.case-sensitive-sorting", true);
	
	//active sorting types
	private final SortingType[] usedSortingTypes;
	
	/**
	 * Constructs new instance, loads data from configuration and starts repeating task
	 * @param nameTags - NameTag feature
	 */
	public Sorting(NameTag nameTags) {
		super("Team name refreshing", "Refreshing team name");
		this.nameTags = nameTags;
		types.put("GROUPS", Groups::new);
		types.put("PERMISSIONS", Permissions::new);
		types.put("PLACEHOLDER", Placeholder::new);
		types.put("PLACEHOLDER_A_TO_Z", PlaceholderAtoZ::new);
		types.put("PLACEHOLDER_Z_TO_A", PlaceholderZtoA::new);
		types.put("PLACEHOLDER_LOW_TO_HIGH", PlaceholderLowToHigh::new);
		types.put("PLACEHOLDER_HIGH_TO_LOW", PlaceholderHighToLow::new);
		usedSortingTypes = compile(TAB.getInstance().getConfiguration().getConfig().getStringList("scoreboard-teams.sorting-types", new ArrayList<>()));
	}
	
	@Override
	public void refresh(TabPlayer p, boolean force) {
		if (nameTags != null && (nameTags.getForcedTeamName(p) != null || nameTags.hasTeamHandlingPaused(p))) return;
		String newName = getTeamName(p);
		if (!p.getTeamName().equals(newName)) {
			if (nameTags != null) nameTags.unregisterTeam(p);
			LayoutManager layout = (LayoutManager) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.LAYOUT);
			if (layout != null) layout.updateTeamName(p, newName);
			((ITabPlayer) p).setTeamName(newName);
			if (nameTags != null) nameTags.registerTeam(p);
		}
	}
	
	@Override
	public void load(){
		if (nameTags != null) return; //handled by NameTag feature
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			((ITabPlayer) all).setTeamName(getTeamName(all));
		}
	}
	
	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		if (nameTags != null) return; //handled by NameTag feature
		((ITabPlayer) connectedPlayer).setTeamName(getTeamName(connectedPlayer));
	}
	
	/**
	 * Compiles sorting type list into classes
	 * @return list of compiled sorting types
	 */
	private SortingType[] compile(List<String> options){
		List<SortingType> list = new ArrayList<>();
		for (String element : options) {
			String[] arr = element.split(":");
			if (!types.containsKey(arr[0].toUpperCase())) {
				TAB.getInstance().getErrorManager().startupWarn("\"&e" + arr[0].toUpperCase() + "&c\" is not a valid sorting type element. Valid options are: &e" + types.keySet() + ".");
			} else {
				list.add(types.get(arr[0].toUpperCase()).apply(this, arr.length == 1 ? "" : element.substring(arr[0].length() + 1)));
			}
		}
		return list.toArray(new SortingType[0]);
	}
	
	/**
	 * Constructs team name for specified player
	 * @param p - player to build team name for
	 * @return unique up to 16 character long sequence that sorts the player
	 */
	public String getTeamName(TabPlayer p) {
		((ITabPlayer) p).setTeamNameNote("");
		StringBuilder sb = new StringBuilder();
		for (SortingType type : usedSortingTypes) {
			sb.append(type.getChars((ITabPlayer) p));
		}
		if (sb.length() > 15) {
			sb.setLength(15);
		}
		return checkTeamName(p, sb, 65);
	}
	
	/**
	 * Checks if team name is available and proceeds to try new values until free name is found
	 * @param p - player to build team name for
	 * @param currentName - current up to 15 character long team name start
	 * @param id - current character to check as 16th character
	 * @return first available full team name
	 */
	private String checkTeamName(TabPlayer p, StringBuilder currentName, int id) {
		String potentialTeamName = currentName.toString();
		if (!caseSensitiveSorting) potentialTeamName = potentialTeamName.toLowerCase();
		potentialTeamName += (char)id;
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
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
		return Arrays.stream(usedSortingTypes).map(Object::toString).collect(Collectors.joining(" then "));
	}
}