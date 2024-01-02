package me.neznamy.tab.shared.features.sorting.types;

import java.util.LinkedHashMap;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.sorting.Sorting;
import org.jetbrains.annotations.NotNull;

/**
 * Sorting by primary permission groups.
 */
public class Groups extends SortingType {

    /** Map of sorted groups */
    private final LinkedHashMap<String, Integer> sortedGroups;

    /**
     * Constructs new instance with given parameters.
     *
     * @param   sorting
     *          Sorting feature
     * @param   options
     *          List of groups separated with ","
     */
    public Groups(Sorting sorting, String options) {
        super(sorting, "GROUPS", TabConstants.Placeholder.GROUP);
        sortedGroups = convertSortingElements(options.split(","));
    }

    @Override
    public String getChars(@NotNull TabPlayer p) {
        String group = p.getGroup().toLowerCase();
        int position;
        if (!sortedGroups.containsKey(group)) {
            TAB.getInstance().getConfigHelper().runtime().groupNotInSortingList(sortedGroups.keySet(), group, p);
            position = sortedGroups.size() + 1;
            sorting.setTeamNameNote(p, sorting.getTeamNameNote(p) + "\n-> &cPrimary group (&e" + p.getGroup() + "&c) is not in sorting list. &r");
        } else {
            position = sortedGroups.get(group);
            sorting.setTeamNameNote(p, sorting.getTeamNameNote(p) + "\n-> Primary group (&e" + p.getGroup() + "&r) is &a#" + position + "&r in sorting list.");
        }
        return String.valueOf((char) (position + 47));
    }
}