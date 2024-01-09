package me.neznamy.tab.shared;

import lombok.Getter;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Permission group manager retrieving groups from permission plugin
 */
@Getter
public class GroupManager extends TabFeature implements Refreshable {

    /** Permission plugin's name */
    @NotNull private final String permissionPlugin;

    /** Group retrieve function */
    @NotNull private final Function<TabPlayer, String> groupFunction;

    /** If enabled, groups are assigned via permissions instead of permission plugin */
    private final boolean groupsByPermissions = config().getBoolean("assign-groups-by-permissions", false);

    /** List of group permissions to iterate through if {@link #groupsByPermissions} is {@code true} */
    private final List<String> primaryGroupFindingList = config().getStringList("primary-group-finding-list", Arrays.asList("Owner", "Admin", "Helper", "default"));

    private final String featureName = "Permission group refreshing";
    private final String refreshDisplayName = "Processing group change";

    /**
     * Constructs new instance with given permission plugin and registers group placeholder.
     *
     * @param   permissionPlugin
     *          Name of detected permission plugin
     * @param   groupFunction
     *          Function returning group of a player
     */
    public GroupManager(@NotNull String permissionPlugin, @NotNull Function<TabPlayer, String> groupFunction) {
        this.permissionPlugin = permissionPlugin;
        this.groupFunction = groupFunction;
        TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(TabConstants.Placeholder.GROUP, 1000,
                p -> detectPermissionGroup((TabPlayer) p));
        addUsedPlaceholder(TabConstants.Placeholder.GROUP);
    }

    /**
     * Detects player's permission group using configured method and returns it
     *
     * @param   player
     *          Player to detect permission group of
     * @return  Detected permission group
     */
    public @NotNull String detectPermissionGroup(@NotNull TabPlayer player) {
        return groupsByPermissions ? getByPermission(player) : getByPrimary(player);
    }

    /**
     * Returns player's permission group from detected permission plugin
     *
     * @param   player
     *          Player to get permission group of
     * @return  Permission group from permission plugin
     */
    private @NotNull String getByPrimary(@NotNull TabPlayer player) {
        try {
            String group = groupFunction.apply(player);
            return group == null ? TabConstants.NO_GROUP : group;
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().printError("Failed to get permission group of " + player.getName() + " using " + permissionPlugin, e);
            return TabConstants.NO_GROUP;
        }
    }

    /**
     * Returns player's permission group based on highest permission
     * or {@link TabConstants#NO_GROUP} if player has no permission.
     *
     * @param   player
     *          Player to get permission group of
     * @return  Highest permission group player has permission for
     *          or {@link TabConstants#NO_GROUP} if player does not have any
     */
    private @NotNull String getByPermission(@NotNull TabPlayer player) {
        for (String group : primaryGroupFindingList) {
            if (player.hasPermission(TabConstants.Permission.GROUP_PREFIX + group)) {
                return group;
            }
        }
        return TabConstants.NO_GROUP;
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        refreshed.setGroup(TAB.getInstance().getPlaceholderManager().getPlaceholder(TabConstants.Placeholder.GROUP).getLastValue(refreshed));
    }
}