package me.neznamy.tab.shared.permission;

import java.util.Optional;
import java.util.stream.Collectors;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.GroupRefresher;
import me.neznamy.tab.shared.placeholders.PrefixSuffixProvider;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.QueryOptions;

/**
 * LuckPerms hook
 */
public class LuckPerms implements PermissionPlugin, PrefixSuffixProvider {

	private static final String UPDATE_MESSAGE = "Upgrade to LuckPerms 5";
	//luckperms version
	private String version;

	/**
	 * Constructs new instance with given parameter
	 * @param version - luckperms version
	 */
	public LuckPerms(String version) {
		this.version = version;
	}

	@Override
	public String getPrimaryGroup(TabPlayer p) {
		if (version.startsWith("4")) return UPDATE_MESSAGE;
		net.luckperms.api.LuckPerms api = getAPI();
		if (api == null) {
			return GroupRefresher.DEFAULT_GROUP;
		}
		User user = api.getUserManager().getUser(p.getUniqueId());
		if (user == null) return GroupRefresher.DEFAULT_GROUP;
		return user.getPrimaryGroup();
	}

	@Override
	public String[] getAllGroups(TabPlayer p) {
		if (version.startsWith("4")) return new String[]{UPDATE_MESSAGE};
		net.luckperms.api.LuckPerms api = getAPI();
		if (api == null) {
			return new String[]{GroupRefresher.DEFAULT_GROUP};
		}
		User user = api.getUserManager().getUser(p.getUniqueId());
		if (user == null) return new String[] {GroupRefresher.DEFAULT_GROUP};
		return user.getNodes().stream().filter(NodeType.INHERITANCE::matches).map(NodeType.INHERITANCE::cast).map(InheritanceNode::getGroupName).collect(Collectors.toSet()).toArray(new String[0]);
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
			net.luckperms.api.LuckPerms api = getAPI();
			if (api == null) {
				return "";
			}
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
			TAB.getInstance().getErrorManager().printError("LuckPerms v" + version + " threw an exception when retrieving player " + (prefix ? "prefix" : "suffix") + " of " + p.getName(), e);
			return "";
		}
	}
	
	private net.luckperms.api.LuckPerms getAPI() {
		try {
			return LuckPermsProvider.get();
		} catch (Exception e) {
			TAB.getInstance().getErrorManager().printError("LuckPerms v" + version + " threw an exception when retrieving API instance: " + e.getMessage());
			TAB.getInstance().getErrorManager().printError("Just a side note: LuckPerms is installed, otherwise server would not say it is. LuckPerms is declared as softdependecy and all code runs at onEnable or later, constructor is unused.");
			return null;
		}
	}

	@Override
	public String getVersion() {
		return version;
	}
}