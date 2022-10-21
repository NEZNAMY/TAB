package me.neznamy.tab.shared.features.sorting.types;

import java.util.Collections;
import java.util.LinkedHashMap;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.sorting.Sorting;

public abstract class SortingType {

    //number to add to / subtract from to prevent incorrect sorting with negative values
    protected final int DEFAULT_NUMBER = 5000000;
    
    //placeholder to sort by, if sorting type uses it
    protected String sortingPlaceholder;

    protected Sorting sorting;

    protected SortingType(Sorting sorting) {
        this.sorting = sorting;
    }
    
    /**
     * Constructs new instance with given parameter
     *
     * @param   sortingPlaceholder
     *          placeholder to sort by
     */
    protected SortingType(Sorting sorting, String sortingPlaceholder){
        this.sorting = sorting;
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
    
    protected LinkedHashMap<String, String> convertSortingElements(String[] elements) {
        LinkedHashMap<String, String> sortedGroups = new LinkedHashMap<>();
        int index = 1;
        int charCount = String.valueOf(elements.length).length()+1; //1 char for <10 values, 2 chars for <100 etc
        for (String element : elements){
            StringBuilder sb = new StringBuilder();
            sb.append(index);
            while (sb.length() < charCount) { 
                sb.insert(0, "0");
            }
            for (String element0 : element.split("\\|")) {
                while (element0.startsWith(" ")) element0 = element0.substring(1);
                while (element0.endsWith(" ")) element0 = element0.substring(0, element0.length()-1);
                sortedGroups.put(EnumChatFormat.color(element0.toLowerCase()), sb.toString());
            }
            index++;
        }
        return sortedGroups;
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