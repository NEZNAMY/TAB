package me.neznamy.tab.shared.features.sorting.types;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;

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
        } else {
            position = sortedGroups.get(group);
        }
        return String.valueOf((char) (position + 47));
    }

    @Override
    @NotNull
    public String getReturnedValue(@NotNull TabPlayer p) {
        String group = p.getGroup().toLowerCase();
        if (!sortedGroups.containsKey(group)) {
            return p.getGroup() + " (not in list)";
        } else {
            return p.getGroup() + " (#" +  sortedGroups.get(group) + " in list)";
        }
    }
}