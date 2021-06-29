package me.neznamy.tab.shared.permission;

import java.util.Optional;
import java.util.stream.Collectors;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.PrefixSuffixProvider;
import net.luckperms.api.LuckPermsProvider;
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
			return "<null>";
		}
		User user = api.getUserManager().getUser(p.getUniqueId());
		if (user == null) return "<null>";
		return user.getPrimaryGroup();
	}

	@Override
	public String[] getAllGroups(TabPlayer p) {
		if (version.startsWith("4")) return new String[]{UPDATE_MESSAGE};
		net.luckperms.api.LuckPerms api = getAPI();
		if (api == null) {
			return new String[]{"<null>"};
		}
		User user = api.getUserManager().getUser(p.getUniqueId());
		if (user == null) return new String[] {"<null>"};
		return user.getNodes().stream().filter(NodeType.INHERITANCE::matches).map(NodeType.INHERITANCE::cast).map(InheritanceNode::getGroupName).collect(Collectors.toSet()).toArray(new String[0]);
	}

	@Override
	public String getPrefix(TabPlayer p) {
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
			String prefix = user.getCachedData().getMetaData(options.get()).getPrefix();
			return prefix == null ? "" : prefix;
		} catch (Exception e) {
			TAB.getInstance().getErrorManager().printError("LuckPerms v" + version + " threw an exception when retrieving player prefix of " + p.getName(), e);
			return "";
		}
	}

	@Override
	public String getSuffix(TabPlayer p) {
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
			String suffix = user.getCachedData().getMetaData(options.get()).getSuffix();
			return suffix == null ? "" : suffix;
		} catch (Exception e) {
			TAB.getInstance().getErrorManager().printError("LuckPerms v" + version + " threw an exception when retrieving player suffix of " + p.getName(), e);
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