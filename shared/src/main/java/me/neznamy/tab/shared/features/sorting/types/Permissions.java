package me.neznamy.tab.shared.features.sorting.types;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.sorting.Sorting;
import org.jetbrains.annotations.NotNull;

/**
 * Sorting by permission nodes
 */
public class Permissions extends SortingType {

    /** Map of permissions with priorities */
    private final LinkedHashMap<String, Integer> sortedGroups;

    /**
     * Constructs new instance with given parameters and registers internal permission placeholders.
     *
     * @param   sorting
     *          Sorting feature
     * @param   options
     *          Permission nodes separated with ","
     */
    public Permissions(Sorting sorting, String options) {
        super(sorting, "PERMISSIONS");
        sortedGroups = convertSortingElements(options.split(","));
        List<String> placeholders = new ArrayList<>();
        for (String permission : sortedGroups.keySet()) {
            String placeholder = "%permission:" + permission + "%";
            placeholders.add(placeholder);
            TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(placeholder, 1000,
                    p -> ((TabPlayer)p).hasPermission(permission));
        }
        sorting.addUsedPlaceholders(placeholders);
    }

    @Override
    public String getChars(@NotNull TabPlayer p) {
        int position = 0;
        for (String permission : sortedGroups.keySet()) {
            if (p.hasPermission(permission)) {
                position = sortedGroups.get(permission.toLowerCase());
                sorting.setTeamNameNote(p, sorting.getTeamNameNote(p) + "\n-> Highest sorting permission: &e" + permission + " &a(#" + position + " in list). &r");
                if (p.hasPermission(TabConstants.Permission.TEST_PERMISSION)) {
                    sorting.setTeamNameNote(p, sorting.getTeamNameNote(p) + "&cThis user appears to have all permissions. Are they OP? &r");
                }
                break;
            }
        }
        if (position == 0) {
            TAB.getInstance().getConfigHelper().runtime().noPermissionFromSortingList(sortedGroups.keySet(), p);
            position = sortedGroups.size()+1;
            sorting.setTeamNameNote(p, sorting.getTeamNameNote(p) + "\n-> &cPlayer does not have any of the defined permissions. &r");
        }
        return String.valueOf((char) (position + 47));
    }
}