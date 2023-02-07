package me.neznamy.tab.shared;

import lombok.Getter;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.permission.PermissionPlugin;

import java.util.Arrays;
import java.util.List;

/**
 * Permission group manager retrieving groups from permission plugin
 */
public class GroupManager extends TabFeature {

    /** Detected permission plugin to take groups from */
    @Getter private final PermissionPlugin plugin;

    /** If enabled, groups are assigned via permissions instead of permission plugin */
    @Getter private final boolean groupsByPermissions = TAB.getInstance().getConfiguration().getConfig().getBoolean("assign-groups-by-permissions", false);

    /** List of group permissions to iterate through if {@link #groupsByPermissions} is {@code true} */
    private final List<String> primaryGroupFindingList = TAB.getInstance().getConfiguration().getConfig().getStringList("primary-group-finding-list", Arrays.asList("Owner", "Admin", "Helper", "default"));

    /**
     * Constructs new instance with given permission plugin, loads
     * event listeners and registers group placeholder.
     *
     * @param   plugin
     *          Detected permission plugin
     */
    public GroupManager(PermissionPlugin plugin) {
        super("Permission group refreshing", null);
        this.plugin = plugin;
        TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(1000, this, TabConstants.CpuUsageCategory.GROUP_REFRESHING, () -> {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                ((ITabPlayer)all).setGroup(detectPermissionGroup(all));
            }
        });
    }

    /**
     * Detects player's permission group using configured method and returns it
     *
     * @param   player
     *          Player to detect permission group of
     * @return  Detected permission group
     */
    public String detectPermissionGroup(TabPlayer player) {
        return groupsByPermissions ? getByPermission(player) : getByPrimary(player);
    }

    /**
     * Returns player's permission group from detected permission plugin
     *
     * @param   player
     *          Player to get permission group of
     * @return  Permission group from permission plugin
     */
    private String getByPrimary(TabPlayer player) {
        try {
            String group = plugin.getPrimaryGroup(player);
            return group == null ? TabConstants.NO_GROUP : group;
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().printError("Failed to get permission group of " + player.getName() + " using " + plugin.getName() + " v" + plugin.getVersion(), e);
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
    private String getByPermission(TabPlayer player) {
        for (String group : primaryGroupFindingList) {
            if (player.hasPermission(TabConstants.Permission.GROUP_PREFIX + group)) {
                return group;
            }
        }
        return TabConstants.NO_GROUP;
    }
}