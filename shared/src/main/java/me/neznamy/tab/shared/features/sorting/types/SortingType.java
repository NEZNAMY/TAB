package me.neznamy.tab.shared.features.sorting.types;

import java.util.Collections;
import java.util.LinkedHashMap;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.sorting.Sorting;

@RequiredArgsConstructor
public abstract class SortingType {

    protected final Sorting sorting;

    private final String displayName;

    //number to add to / subtract from to prevent incorrect sorting with negative values
    protected final int DEFAULT_NUMBER = 5000000;
    
    //placeholder to sort by, if sorting type uses it
    protected String sortingPlaceholder;

    /**
     * Constructs new instance with given parameter
     *
     * @param   sortingPlaceholder
     *          placeholder to sort by
     */
    protected SortingType(Sorting sorting, String displayName, String sortingPlaceholder) {
        this.sorting = sorting;
        this.displayName = displayName;
        if (!sortingPlaceholder.startsWith("%") || !sortingPlaceholder.endsWith("%")) {
            TAB.getInstance().getErrorManager().startupWarn("\"" + sortingPlaceholder + "\" is not a valid placeholder for " + this + " sorting type");
        } else {
            sorting.addUsedPlaceholders(Collections.singletonList(sortingPlaceholder));
            this.sortingPlaceholder = sortingPlaceholder;
        }
    }
    
    /**
     * Applies all placeholders for specified player
     *
     * @param   player
     *          player to set placeholders for
     * @return  text with replaced placeholders
     */
    protected String setPlaceholders(TabPlayer player) {
        if (sortingPlaceholder == null) return "";
        return TAB.getInstance().getPlaceholderManager().getPlaceholder(sortingPlaceholder).set(sortingPlaceholder, player);
    }
    
    protected LinkedHashMap<String, Integer> convertSortingElements(String[] elements) {
        LinkedHashMap<String, Integer> sortedGroups = new LinkedHashMap<>();
        int index = 1;
        for (String element : elements) {
            for (String element0 : element.split("\\|")) {
                sortedGroups.put(EnumChatFormat.color(element0.trim().toLowerCase()), index++);
            }
        }
        return sortedGroups;
    }

    @Override
    public final String toString() {
        return displayName;
    }
    
    /**
     * Returns current sorting characters of this sorting type for specified player
     *
     * @param   p
     *          player to get chars for
     * @return  an as-short-as-possible character sequence for unique sorting
     */
    public abstract String getChars(ITabPlayer p);
}