package me.neznamy.tab.shared.features.sorting.types;

import java.util.LinkedHashMap;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.features.sorting.Sorting;

/**
 * Sorting by primary permission groups
 */
public class Groups extends SortingType {

    //map of sorted groups in config
    private final LinkedHashMap<String, Integer> sortedGroups;

    /**
     * Constructs new instance
     */
    public Groups(Sorting sorting, String options) {
        super(sorting, "GROUPS", TabConstants.Placeholder.GROUP);
        sortedGroups = convertSortingElements(options.split(","));
    }

    @Override
    public String getChars(ITabPlayer p) {
        String group = p.getGroup().toLowerCase();
        int position;
        if (!sortedGroups.containsKey(group)) {
            position = sortedGroups.size() + 1;
            sorting.setTeamNameNote(p, sorting.getTeamNameNote(p) + "\n-> &cPrimary group (&e" + p.getGroup() + "&c) is not in sorting list. &r");
        } else {
            position = sortedGroups.get(group);
            sorting.setTeamNameNote(p, sorting.getTeamNameNote(p) + "\n-> Primary group (&e" + p.getGroup() + "&r) is &a#" + position + "&r in sorting list.");
        }
        return String.valueOf((char) (position + 47));
    }
}