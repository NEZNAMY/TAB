package me.neznamy.tab.shared.permission;

import java.util.stream.Collectors;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.placeholders.PrefixSuffixProvider;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;

public class LuckPerms implements PermissionPlugin, PrefixSuffixProvider {

	private String version;

	public LuckPerms(String version) {
		this.version = version;
	}

	@Override
	public String getPrimaryGroup(ITabPlayer p) {
		if (version.startsWith("5")) {
			User user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
			if (user == null) {
				Shared.errorManager.printError("LuckPerms v" + version + " returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getPrimaryGroup)");
				return "null";
			}
			return user.getPrimaryGroup();
		} else {
			me.lucko.luckperms.api.User user = me.lucko.luckperms.LuckPerms.getApi().getUser(p.getUniqueId());
			if (user == null) {
				Shared.errorManager.printError("LuckPerms v" + version + " returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getPrimaryGroup)");
				return "null";
			}
			return user.getPrimaryGroup();
		}
	}

	@Override
	public String[] getAllGroups(ITabPlayer p) {
		if (version.startsWith("5")) {
			User user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
			if (user == null) {
				Shared.errorManager.printError("LuckPerms v" + version + "returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getAllGroups)");
				return new String[] {"null"};
			}
			return user.getNodes().stream().filter(NodeType.INHERITANCE::matches).map(NodeType.INHERITANCE::cast).map(InheritanceNode::getGroupName).collect(Collectors.toSet()).toArray(new String[0]);
		} else {
			me.lucko.luckperms.api.User user = me.lucko.luckperms.LuckPerms.getApi().getUser(p.getUniqueId());
			if (user == null) {
				Shared.errorManager.printError("LuckPerms v" + version + " returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getAllGroups)");
				return new String[] {"null"};
			}
			return user.getAllNodes().stream().filter(me.lucko.luckperms.api.Node::isGroupNode).map(me.lucko.luckperms.api.Node::getGroupName).collect(Collectors.toSet()).toArray(new String[0]);
		}
	}

	@Override
	public String getPrefix(ITabPlayer p) {
		String prefix;
		if (version.startsWith("5")) {
			User user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
			if (user == null) {
				Shared.errorManager.printError("LuckPerms v" + version + " returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getPrefix)");
				return "";
			}
			prefix = user.getCachedData().getMetaData(LuckPermsProvider.get().getContextManager().getQueryOptions(user).get()).getPrefix();
		} else {
			me.lucko.luckperms.api.User user = me.lucko.luckperms.LuckPerms.getApi().getUser(p.getUniqueId());
			if (user == null) {
				Shared.errorManager.printError("LuckPerms v" + version + " returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getPrefix)");
				return "";
			}
			prefix = user.getCachedData().getMetaData(me.lucko.luckperms.LuckPerms.getApi().getContextManager().getApplicableContexts(p instanceof me.neznamy.tab.platforms.bukkit.TabPlayer ? p.getBukkitEntity() : p.getBungeeEntity())).getPrefix();
		}
		return prefix == null ? "" : prefix;
	}

	@Override
	public String getSuffix(ITabPlayer p) {
		String suffix;
		if (version.startsWith("5")) {
			User user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
			if (user == null) {
				Shared.errorManager.printError("LuckPerms v" + version + " returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getSuffix)");
				return "";
			}
			suffix = user.getCachedData().getMetaData(LuckPermsProvider.get().getContextManager().getQueryOptions(user).get()).getSuffix();
		} else {
			me.lucko.luckperms.api.User user = me.lucko.luckperms.LuckPerms.getApi().getUser(p.getUniqueId());
			if (user == null) {
				Shared.errorManager.printError("LuckPerms v" + version + " returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getSuffix)");
				return "";
			}
			suffix = user.getCachedData().getMetaData(me.lucko.luckperms.LuckPerms.getApi().getContextManager().getApplicableContexts(p instanceof me.neznamy.tab.platforms.bukkit.TabPlayer ? p.getBukkitEntity() : p.getBungeeEntity())).getSuffix();
		}
		return suffix == null ? "" : suffix;
	}
}