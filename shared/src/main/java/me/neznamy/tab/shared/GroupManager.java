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
 * Permission group refresher
 */
public class GroupManager extends TabFeature {

	public static final String DEFAULT_GROUP = "NONE";
	
	private Object luckPermsSub;
	private Object luckPermsSub2;
	private final PermissionPlugin plugin;
	private final boolean groupsByPermissions = TAB.getInstance().getConfiguration().getConfig().getBoolean("assign-groups-by-permissions", false);
	private final List<String> primaryGroupFindingList = TAB.getInstance().getConfiguration().getConfig().getStringList("primary-group-finding-list", Arrays.asList("Owner", "Admin", "Helper", "default"));
	private PlayerPlaceholder groupPlaceholder;
	
	public GroupManager(PermissionPlugin plugin) {
		super("Permission group refreshing", "Refreshing group");
		this.plugin = plugin;
		if (plugin instanceof LuckPerms) {
			groupPlaceholder = TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%group%", 1000000000, TabPlayer::getGroup);
			groupPlaceholder.enableTriggerMode();
			registerLuckPermsSub();
		} else if (plugin instanceof None && !groupsByPermissions){
			TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%group%", 1000000000, p -> DEFAULT_GROUP);
		} else {
			TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%group%", 1000, this::detectPermissionGroup);
			addUsedPlaceholders(Collections.singletonList("%group%"));
		}
	}
	
	@Override
	public void refresh(TabPlayer p, boolean force) {
		((ITabPlayer)p).setGroup(detectPermissionGroup(p), true);
	}
	
	private void registerLuckPermsSub() {
		luckPermsSub = LuckPermsProvider.get().getEventBus().subscribe(UserDataRecalculateEvent.class, this::updatePlayer);
		luckPermsSub2 = LuckPermsProvider.get().getEventBus().subscribe(GroupDataRecalculateEvent.class, this::updateGroup);
	}

	private void updatePlayer(UserDataRecalculateEvent event) {
		TAB.getInstance().getCPUManager().runMeasuredTask("Processing UserDataRecalculateEvent", this, "Processing UserDataRecalculateEvent", () -> {
			long time = System.nanoTime();
			TabPlayer p = TAB.getInstance().getPlayer(event.getUser().getUniqueId());
			if (p == null) return; //server still starting up and users connecting already (LP loading them)
			refresh(p, false);
			TAB.getInstance().getCPUManager().addTime("Permission group refreshing", TabConstants.CpuUsageCategory.LUCKPERMS_RECALCULATE_EVENT, System.nanoTime()-time);
			groupPlaceholder.updateValue(p, p.getGroup());
		});
	}

	private void updateGroup(GroupDataRecalculateEvent event) {
		TAB.getInstance().getCPUManager().runTaskLater(50, "Processing GroupDataRecalculateEvent", this, "Processing GroupDataRecalculateEvent", () -> {
			long time = System.nanoTime();
			for (TabPlayer player : TAB.getInstance().getOnlinePlayers()) {
				refresh(player, false);
				groupPlaceholder.updateValue(player, player.getGroup());
			}
			TAB.getInstance().getCPUManager().addTime("Permission group refreshing", TabConstants.CpuUsageCategory.LUCKPERMS_RECALCULATE_EVENT, System.nanoTime()-time);
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public void unload() {
		if (luckPermsSub != null) ((EventSubscription<UserDataRecalculateEvent>)luckPermsSub).close();
		if (luckPermsSub2 != null) ((EventSubscription<UserDataRecalculateEvent>)luckPermsSub2).close();
	}

	public String detectPermissionGroup(TabPlayer p) {
		if (isGroupsByPermissions()) {
			return getByPermission(p);
		}
		return getByPrimary(p);
	}

	private String getByPrimary(TabPlayer p) {
		try {
			return plugin.getPrimaryGroup(p);
		} catch (Exception e) {
			TAB.getInstance().getErrorManager().printError("Failed to get permission group of " + p.getName() + " using " + plugin.getName() + " v" + plugin.getVersion(), e);
			return DEFAULT_GROUP;
		}
	}

	private String getByPermission(TabPlayer p) {
		for (Object group : primaryGroupFindingList) {
			if (p.hasPermission(TabConstants.Permission.GROUP_PREFIX + group)) {
				return String.valueOf(group);
			}
		}
		return DEFAULT_GROUP;
	}

	public boolean isGroupsByPermissions() {
		return groupsByPermissions;
	}
	
	public PermissionPlugin getPlugin() {
		return plugin;
	}
}