package me.neznamy.tab.shared.permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import me.TechsCode.UltraPermissions.UltraPermissionsAPI;
import me.TechsCode.UltraPermissions.bungee.UltraPermissionsBungee;
import me.TechsCode.UltraPermissions.storage.objects.Group;
import me.TechsCode.UltraPermissions.storage.objects.User;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

/**
 * UltraPermissions hook
 */
public class UltraPermissions implements PermissionPlugin {

	@Override
	public String getPrimaryGroup(ITabPlayer p) {
		String[] groups = getAllGroups(p);
		if (groups.length == 0) return "null";
		return groups[0];
	}

	@Override
	public String[] getAllGroups(ITabPlayer p) {
		UltraPermissionsAPI api = null;
		if (p instanceof me.neznamy.tab.platforms.bungee.TabPlayer) {
			api = UltraPermissionsBungee.getAPI();
		}
		if (p instanceof me.neznamy.tab.platforms.bukkit.TabPlayer) {
			api = me.TechsCode.UltraPermissions.UltraPermissions.getAPI();
		}
		if (api == null) {
			Shared.errorManager.printError("UltraPermissions getAPI returned null");
			return new String[]{"null"};
		}
		Optional<User> user = api.getUsers().name(p.getName());
		if (user == null) {
			Shared.errorManager.printError("UltraPermissions returned null user for " + p.getName() + " (" + p.getUniqueId() + ")");
			return new String[]{"null"};
		}
		List<String> groups = new ArrayList<String>();
		for (Group group : user.get().getActiveGroups().bestToWorst()) {
			groups.add(group.getName());
		}
		return groups.toArray(new String[0]);
	}
}