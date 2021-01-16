package me.neznamy.tab.shared.permission;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

/**
 * UltraPermissions hook
 */
public class UltraPermissions implements PermissionPlugin {

	private String version;
	
	public UltraPermissions(String version) {
		this.version = version;
	}
	
	@Override
	public String getPrimaryGroup(TabPlayer p) throws Exception {
		String[] groups = getAllGroups(p);
		if (groups.length == 0) return "<null>";
		return groups[0];
	}

	@SuppressWarnings("unchecked")
	@Override
	public String[] getAllGroups(TabPlayer p) throws Exception {
		Object api = null;
		if (TAB.getInstance().getPlatform().getSeparatorType().equals("server")) { //meh solution but whatever
			api = Class.forName("me.TechsCode.UltraPermissions.bungee.UltraPermissionsBungee").getMethod("getAPI").invoke(null);
		} else {
			api = Class.forName("me.TechsCode.UltraPermissions.UltraPermissions").getMethod("getAPI").invoke(null);
		}
		if (api == null) {
			TAB.getInstance().getErrorManager().printError("UltraPermissions v" + version + " returned null API");
			return new String[]{"<null>"};
		}
		Object users = api.getClass().getMethod("getUsers").invoke(api);
		Optional<Object> optUser = (Optional<Object>) users.getClass().getMethod("name", String.class).invoke(users, p.getName());
		if (optUser == null || !optUser.isPresent()) {
			TAB.getInstance().getErrorManager().printError("UltraPermissions v" + version + " returned null user for " + p.getName() + " (" + p.getUniqueId() + ")");
			return new String[]{"<null>"};
		}
		Set<String> groups = new HashSet<String>();
		Object user = optUser.get();
		Object activeGroups = user.getClass().getMethod("getActiveGroups").invoke(user);
		Iterable<Object> bestToWorst = (Iterable<Object>) activeGroups.getClass().getMethod("bestToWorst").invoke(activeGroups);
		for (Object group : bestToWorst) {
			groups.add((String) group.getClass().getMethod("getName").invoke(group));
		}
		return groups.toArray(new String[0]);
	}

	@Override
	public String getVersion() {
		return version;
	}
}