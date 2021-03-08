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
		if (version.startsWith("4")) return "Upgrade to LuckPerms 5";
		User user;
		try {
			user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
		} catch (IllegalStateException e) {
			return e.getMessage();
		}
		if (user == null) {
			TAB.getInstance().getErrorManager().printError("LuckPerms v" + version + " returned null user for " + p.getName() + ", uuid=" + p.getUniqueId() + ", online=" + p.isOnline() + ", func=getPrimaryGroup");
			return "<null>";
		}
		return user.getPrimaryGroup();
	}

	@Override
	public String[] getAllGroups(TabPlayer p) {
		if (version.startsWith("4")) return new String[]{"Upgrade to LuckPerms 5"};
		User user;
		try {
			user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
		} catch (IllegalStateException e) {
			return new String[] {e.getMessage()};
		}
		if (user == null) {
			TAB.getInstance().getErrorManager().printError("LuckPerms v" + version + " returned null user for " + p.getName() + ", uuid=" + p.getUniqueId() + ", online=" + p.isOnline() + ", func=getAllGroups");
			return new String[] {"<null>"};
		}
		return user.getNodes().stream().filter(NodeType.INHERITANCE::matches).map(NodeType.INHERITANCE::cast).map(InheritanceNode::getGroupName).collect(Collectors.toSet()).toArray(new String[0]);
	}

	@Override
	public String getPrefix(TabPlayer p) {
		if (version.startsWith("4")) return "Upgrade to LuckPerms 5";
		User user;
		try {
			user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
		} catch (IllegalStateException e) {
			return e.getMessage();
		}
		if (user == null) {
			TAB.getInstance().getErrorManager().printError("LuckPerms v" + version + " returned null user for " + p.getName() + ", uuid=" + p.getUniqueId() + ", online=" + p.isOnline() + ", func=getPrefix");
			return "";
		}
		Optional<QueryOptions> options = LuckPermsProvider.get().getContextManager().getQueryOptions(user);
		if (!options.isPresent()) return "";
		String prefix = user.getCachedData().getMetaData(options.get()).getPrefix();
		return prefix == null ? "" : prefix;
	}

	@Override
	public String getSuffix(TabPlayer p) {
		if (version.startsWith("4")) return "Upgrade to LuckPerms 5";
		User user;
		try {
			user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
		} catch (IllegalStateException e) {
			return e.getMessage();
		}
		if (user == null) {
			TAB.getInstance().getErrorManager().printError("LuckPerms v" + version + " returned null user for " + p.getName() + ", uuid=" + p.getUniqueId() + ", online=" + p.isOnline() + ", func=getSuffix");
			return "";
		}
		Optional<QueryOptions> options = LuckPermsProvider.get().getContextManager().getQueryOptions(user);
		if (!options.isPresent()) return "";
		String suffix = user.getCachedData().getMetaData(options.get()).getSuffix();
		return suffix == null ? "" : suffix;
	}

	@Override
	public String getVersion() {
		return version;
	}
}