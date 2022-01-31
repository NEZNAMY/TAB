package me.neznamy.tab.shared.permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;

/**
 * UltraPermissions hook
 */
public class UltraPermissions extends PermissionPlugin {

	/**
	 * Constructs new instance with given version
	 *
	 * @param	version
	 * 			UltraPermissions version
	 */
	public UltraPermissions(String version) {
		super(version);
	}
	
	@Override
	public String getPrimaryGroup(TabPlayer p) throws ReflectiveOperationException {
		String[] groups = getAllGroups(p);
		if (groups.length == 0) return TabConstants.DEFAULT_GROUP;
		return groups[0];
	}

	@SuppressWarnings("unchecked")
	public String[] getAllGroups(TabPlayer p) throws ReflectiveOperationException {
		Object api;
		if (TAB.getInstance().getPlatform().isProxy()) {
			api = Class.forName("me.TechsCode.UltraPermissions.bungee.UltraPermissionsBungee").getMethod("getAPI").invoke(null);
		} else {
			api = Class.forName("me.TechsCode.UltraPermissions.UltraPermissions").getMethod("getAPI").invoke(null);
		}
		if (api == null) {
			TAB.getInstance().getErrorManager().printError("UltraPermissions v" + getVersion() + " returned null API");
			return new String[]{TabConstants.DEFAULT_GROUP};
		}
		Object users = api.getClass().getMethod("getUsers").invoke(api);
		Optional<Object> optUser = (Optional<Object>) users.getClass().getMethod("name", String.class).invoke(users, p.getName());
		if (!optUser.isPresent()) {
			TAB.getInstance().getErrorManager().printError("UltraPermissions v" + getVersion() + " returned null user for " + p.getName() + " (" + p.getUniqueId() + ")");
			return new String[]{TabConstants.DEFAULT_GROUP};
		}
		List<String> groups = new ArrayList<>();
		Object user = optUser.get();
		Object activeGroups = user.getClass().getMethod("getActiveGroups").invoke(user);
		Iterable<Object> bestToWorst = (Iterable<Object>) activeGroups.getClass().getMethod("bestToWorst").invoke(activeGroups);
		for (Object group : bestToWorst) {
			groups.add((String) group.getClass().getMethod("getName").invoke(group));
		}
		return groups.toArray(new String[0]);
	}
}