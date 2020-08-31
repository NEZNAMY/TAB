package me.neznamy.tab.shared.permission;

import java.util.stream.Collectors;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.placeholders.PrefixSuffixProvider;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;

/**
 * LuckPerms hook
 */
public class LuckPerms implements PermissionPlugin, PrefixSuffixProvider {

	private String version;

	public LuckPerms(String version) {
		this.version = version;
	}

	@Override
	public String getPrimaryGroup(TabPlayer p) {
		if (version.startsWith("4")) return "Upgrade to LuckPerms 5";
		User user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
		if (user == null) {
			Shared.errorManager.printError("LuckPerms v" + version + " returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getPrimaryGroup)");
			return "null";
		}
		return user.getPrimaryGroup();
	}

	@Override
	public String[] getAllGroups(TabPlayer p) {
		if (version.startsWith("4")) return new String[]{"Upgrade to LuckPerms 5"};
		User user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
		if (user == null) {
			Shared.errorManager.printError("LuckPerms v" + version + "returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getAllGroups)");
			return new String[] {"null"};
		}
		return user.getNodes().stream().filter(NodeType.INHERITANCE::matches).map(NodeType.INHERITANCE::cast).map(InheritanceNode::getGroupName).collect(Collectors.toSet()).toArray(new String[0]);
	}

	@Override
	public String getPrefix(TabPlayer p) {
		if (version.startsWith("4")) return "Upgrade to LuckPerms 5";
		User user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
		if (user == null) {
			Shared.errorManager.printError("LuckPerms v" + version + " returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getPrefix)");
			return "";
		}
		String prefix = user.getCachedData().getMetaData(LuckPermsProvider.get().getContextManager().getQueryOptions(user).get()).getPrefix();
		return prefix == null ? "" : prefix;
	}

	@Override
	public String getSuffix(TabPlayer p) {
		if (version.startsWith("4")) return "Upgrade to LuckPerms 5";
		User user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
		if (user == null) {
			Shared.errorManager.printError("LuckPerms v" + version + " returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getSuffix)");
			return "";
		}
		String suffix = user.getCachedData().getMetaData(LuckPermsProvider.get().getContextManager().getQueryOptions(user).get()).getSuffix();
		return suffix == null ? "" : suffix;
	}
}