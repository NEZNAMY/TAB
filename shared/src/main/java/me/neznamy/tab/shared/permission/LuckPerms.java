package me.neznamy.tab.shared.permission;

import java.util.Optional;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.GroupManager;
import me.neznamy.tab.shared.placeholders.PrefixSuffixProvider;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;

/**
 * LuckPerms hook
 */
public class LuckPerms implements PermissionPlugin, PrefixSuffixProvider {

	private static final String UPDATE_MESSAGE = "Upgrade to LuckPerms 5";
	//LuckPerms version
	private final String version;

	/**
	 * Constructs new instance with given parameter
	 * @param version - LuckPerms version
	 */
	public LuckPerms(String version) {
		this.version = version;
	}

	@Override
	public String getPrimaryGroup(TabPlayer p) {
		try {
			if (version.startsWith("4")) return UPDATE_MESSAGE;
			net.luckperms.api.LuckPerms api = LuckPermsProvider.get();
			User user = api.getUserManager().getUser(p.getUniqueId());
			if (user == null) return GroupManager.DEFAULT_GROUP; //pretend like nothing is wrong
			return user.getPrimaryGroup();
		} catch (Exception e) {
			return GroupManager.DEFAULT_GROUP;
		}
	}

	@Override
	public String getPrefix(TabPlayer p) {
		return getValue(p, true);
	}

	@Override
	public String getSuffix(TabPlayer p) {
		return getValue(p, false);
	}
	
	private String getValue(TabPlayer p, boolean prefix) {
		try {
			if (version.startsWith("4")) return UPDATE_MESSAGE;
			net.luckperms.api.LuckPerms api = LuckPermsProvider.get();
			User user = api.getUserManager().getUser(p.getUniqueId());
			if (user == null) return "";
			Optional<QueryOptions> options = LuckPermsProvider.get().getContextManager().getQueryOptions(user);
			if (!options.isPresent()) return "";
			CachedMetaData data = user.getCachedData().getMetaData(options.get());
			String value;
			if (prefix) {
				value = data.getPrefix();
			} else {
				value = data.getSuffix();
			}
			return value == null ? "" : value;
		} catch (Exception e) {
			//pretend like nothing is wrong
			return "";
		}
	}

	@Override
	public String getVersion() {
		return version;
	}
}