package me.neznamy.tab.shared.features.sorting.types;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.sorting.Sorting;

/**
 * Sorting by permission nodes tab.sort.<name>, where names are defined in sorting list
 */
public class Permissions extends SortingType {

    //map of sorted groups in config
    private final LinkedHashMap<String, String> sortedGroups;

    /**
     * Constructs new instance
     */
    public Permissions(Sorting sorting, String options) {
        sortedGroups = convertSortingElements(options.split(","));
        List<String> placeholders = new ArrayList<>();
        for (String permission : sortedGroups.keySet()) {
            String placeholder = "%permission:" + permission + "%";
            placeholders.add(placeholder);
            TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(placeholder, 1000, p -> p.hasPermission(permission));
        }
        sorting.addUsedPlaceholders(placeholders);
    }

    @Override
    public String getChars(ITabPlayer p) {
        String chars = null;
        for (String permission : sortedGroups.keySet()) {
            if (p.hasPermission(permission)) {
                chars = sortedGroups.get(permission.toLowerCase());
                p.setTeamNameNote("Highest sorting permission: &e" + permission + " &a(#" + Integer.parseInt(chars) + " in list). &r");
                if (p.hasPermission(TabConstants.Permission.TEST_PERMISSION)) {
                    p.setTeamNameNote(p.getTeamNameNote() + "&cThis user appears to have all permissions. Is he OP? &r");
                }
                break;
            }
        }
        if (chars == null) {
            chars = String.valueOf(sortedGroups.size()+1);
            p.setTeamNameNote(p.getTeamNameNote() + "&cPlayer does not have any of the defined permissions. &r");
        }
        return chars;
    }

    @Override
    public String toString() {
        return "PERMISSIONS";
    }
}