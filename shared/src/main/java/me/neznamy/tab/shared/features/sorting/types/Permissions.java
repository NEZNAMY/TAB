package me.neznamy.tab.shared.features.sorting.types;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;

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
        super(sorting, "PERMISSIONS", true);
        sortedGroups = convertSortingElements(options.split(","));
        for (String permission : sortedGroups.keySet()) {
            PlayerPlaceholderImpl pl = TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%permission:" + permission + "%",
                    p -> Boolean.toString(((TabPlayer)p).hasPermission(permission)));
            sorting.addUsedPlaceholder(pl.getIdentifier());
        }
    }

    @Override
    public String getChars(@NotNull TabPlayer p) {
        int position = 0;
        for (String permission : sortedGroups.keySet()) {
            if (p.hasPermission(permission)) {
                position = sortedGroups.get(permission.toLowerCase());
                break;
            }
        }
        if (position == 0) {
            TAB.getInstance().getConfigHelper().runtime().noPermissionFromSortingList(sortedGroups.keySet(), p);
            position = sortedGroups.size()+1;
        }
        return String.valueOf((char) (position + 47));
    }

    @Override
    @NotNull
    public String getReturnedValue(@NotNull TabPlayer p) {
        for (String permission : sortedGroups.keySet()) {
            if (p.hasPermission(permission)) {
                return permission + " (#" + sortedGroups.get(permission.toLowerCase()) + " in list)";
            }
        }
        return "NO PERMISSION";
    }
}