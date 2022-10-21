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
    private final LinkedHashMap<String, String> sortedGroups;

    /**
     * Constructs new instance
     */
    public Groups(Sorting sorting, String options) {
        super(sorting, TabConstants.Placeholder.GROUP);
        sortedGroups = convertSortingElements(options.split(","));
    }

    @Override
    public String getChars(ITabPlayer p) {
        String group = p.getGroup();
        String chars = sortedGroups.get(group.toLowerCase());
        if (chars == null) {
            chars = String.valueOf(sortedGroups.size()+1);
            sorting.setTeamNameNote(p, sorting.getTeamNameNote(p) + "&cPlayer's primary group is not in sorting list. &r");
        } else {
            sorting.setTeamNameNote(p, sorting.getTeamNameNote(p) + String.format("Primary group is #%s in sorting list", Integer.parseInt(chars)) + ". &r");
        }
        return chars;
    }

    @Override
    public String toString() {
        return "GROUPS";
    }
}