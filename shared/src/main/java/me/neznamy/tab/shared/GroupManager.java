package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.permission.None;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.user.UserDataRecalculateEvent;

/**
 * Permission group refresher
 */
public class GroupManager extends TabFeature {

	public static final String DEFAULT_GROUP = "NONE";
	
	private Object luckPermsSub;
	private PermissionPlugin plugin;
	private boolean groupsByPermissions;
	private List<String> primaryGroupFindingList = new ArrayList<>();
	
	public GroupManager(PermissionPlugin plugin) {
		super("Permission group refreshing");
		this.plugin = plugin;
		groupsByPermissions = TAB.getInstance().getConfiguration().getConfig().getBoolean("assign-groups-by-permissions", false);
		for (Object group : TAB.getInstance().getConfiguration().getConfig().getStringList("primary-group-finding-list", Arrays.asList("Owner", "Admin", "Helper", "default"))){
			primaryGroupFindingList.add(group.toString());
		}
		if (plugin instanceof LuckPerms) {
			TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%group%", 1000000000, TabPlayer::getGroup);
			luckPermsSub = LuckPermsProvider.get().getEventBus().subscribe(UserDataRecalculateEvent.class, event -> {
				long time = System.nanoTime();
				TabPlayer p = TAB.getInstance().getPlayer(event.getUser().getUniqueId());
				if (p == null) return; //server still starting up and users connecting already (LP loading them)
				refresh(p, false);
				((PlayerPlaceholder) TAB.getInstance().getPlaceholderManager().getPlaceholder("%group%")).getLastValues().put(p.getName(), p.getGroup());
				TAB.getInstance().getCPUManager().addTime("Permission group refreshing", TabConstants.CpuUsageCategory.LUCKPERMS_RECALCULATE_EVENT, System.nanoTime()-time);
			});
		} else if (!(plugin instanceof None)){
			TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%group%", 1000, this::detectPermissionGroup);
		} else {
			TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%group%", 1000000000, p -> DEFAULT_GROUP);
		}
		addUsedPlaceholders(Arrays.asList("%group%"));
	}
	
	@Override
	public void refresh(TabPlayer p, boolean force) {
		((ITabPlayer)p).setGroup(detectPermissionGroup(p), true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void unload() {
		if (luckPermsSub != null) ((EventSubscription<UserDataRecalculateEvent>)luckPermsSub).close();
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
			if (p.hasPermission("tab.group." + group)) {
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