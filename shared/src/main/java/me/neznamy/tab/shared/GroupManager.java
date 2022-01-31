package me.neznamy.tab.shared;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.permission.None;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;

/**
 * Permission group manager retrieving groups from permission plugin
 */
public class GroupManager extends TabFeature {

	/** Detected permission plugin to take groups from */
	private final PermissionPlugin plugin;

	/** If enabled, groups are assigned via permissions instead of permission plugin */
	private final boolean groupsByPermissions = TAB.getInstance().getConfiguration().getConfig().getBoolean("assign-groups-by-permissions", false);

	/** List of group permissions to iterate through if {@link #groupsByPermissions} is {@code true} */
	private final List<String> primaryGroupFindingList = TAB.getInstance().getConfiguration().getConfig().getStringList("primary-group-finding-list", Arrays.asList("Owner", "Admin", "Helper", "default"));

	/** UserDataRecalculateEvent LuckPerms event listener */
	private EventSubscription<UserDataRecalculateEvent> luckPermsSub;

	/** GroupDataRecalculateEvent LuckPerms event listener */
	private EventSubscription<GroupDataRecalculateEvent> luckPermsSub2;

	/**
	 * Constructs new instance with given permission plugin, loads
	 * event listeners and registers group placeholder.
	 *
	 * @param	plugin
	 * 			Detected permission plugin
	 */
	public GroupManager(PermissionPlugin plugin) {
		super("Permission group refreshing", "Refreshing group");
		this.plugin = plugin;
		if (plugin instanceof LuckPerms) {
			TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%group%", -1, TabPlayer::getGroup).enableTriggerMode();
			luckPermsSub = LuckPermsProvider.get().getEventBus().subscribe(UserDataRecalculateEvent.class, this::updatePlayer);
			luckPermsSub2 = LuckPermsProvider.get().getEventBus().subscribe(GroupDataRecalculateEvent.class, this::updateGroup);
		} else if (plugin instanceof None && !groupsByPermissions){
			TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%group%", -1, p -> TabConstants.DEFAULT_GROUP);
		} else {
			TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%group%", 1000, this::detectPermissionGroup);
			addUsedPlaceholders(Collections.singletonList("%group%"));
		}
	}

	/**
	 * Processes UserDataRecalculateEvent
	 *
	 * @param	event
	 * 			Event to process
	 */
	private void updatePlayer(UserDataRecalculateEvent event) {
		TAB.getInstance().getCPUManager().runTaskLater(50, this, TabConstants.CpuUsageCategory.LUCKPERMS_USER_RECALCULATE_EVENT, () -> {
			TabPlayer p = TAB.getInstance().getPlayer(event.getUser().getUniqueId());
			refresh(p, false);
			((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder("%group%")).updateValue(p, p.getGroup());
		});
	}

	/**
	 * Processes GroupDataRecalculateEvent
	 *
	 * @param	event
	 * 			Event to process
	 */
	private void updateGroup(GroupDataRecalculateEvent event) {
		TAB.getInstance().getCPUManager().runTaskLater(50, this, TabConstants.CpuUsageCategory.LUCKPERMS_GROUP_RECALCULATE_EVENT, () -> {
			for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
				refresh(player, false);
				((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder("%group%")).updateValue(player, player.getGroup());
			}
		});
	}

	/**
	 * Detects player's permission group using configured method and returns it
	 *
	 * @param	player
	 * 			Player to detect permission group of
	 * @return	Detected permission group
	 */
	public String detectPermissionGroup(TabPlayer player) {
		return groupsByPermissions ? getByPermission(player) : getByPrimary(player);
	}

	/**
	 * Returns player's permission group from detected permission plugin
	 *
	 * @param	player
	 * 			Player to get permission group of
	 * @return	Permission group from permission plugin
	 */
	private String getByPrimary(TabPlayer player) {
		try {
			return plugin.getPrimaryGroup(player);
		} catch (Exception e) {
			TAB.getInstance().getErrorManager().printError("Failed to get permission group of " + player.getName() + " using " + plugin.getName() + " v" + plugin.getVersion(), e);
			return TabConstants.DEFAULT_GROUP;
		}
	}

	/**
	 * Returns player's permission group based on highest permission
	 * or {@link TabConstants#DEFAULT_GROUP} if player has no permission.
	 *
	 * @param	player
	 * 			Player to get permission group of
	 * @return	Highest permission group player has permission for
	 * 			or {@link TabConstants#DEFAULT_GROUP} if player does not have any
	 */
	private String getByPermission(TabPlayer player) {
		for (String group : primaryGroupFindingList) {
			if (player.hasPermission(TabConstants.Permission.GROUP_PREFIX + group)) {
				return String.valueOf(group);
			}
		}
		return TabConstants.DEFAULT_GROUP;
	}

	/**
	 * Returns {@code true} if assigning by permissions is configured,
	 * {@code false} if not.
	 *
	 * @return	{@code true} if assigning by permissions, {@code false} if not
	 */
	public boolean isGroupsByPermissions() {
		return groupsByPermissions;
	}

	/**
	 * Returns detected permission plugin
	 *
	 * @return	detected permission plugin
	 */
	public PermissionPlugin getPlugin() {
		return plugin;
	}

	@Override
	public void refresh(TabPlayer p, boolean force) {
		((ITabPlayer)p).setGroup(detectPermissionGroup(p));
	}

	@Override
	public void unload() {
		if (luckPermsSub != null) {
			luckPermsSub.close();
			luckPermsSub2.close();
		}
	}
}