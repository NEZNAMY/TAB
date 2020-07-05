package me.neznamy.tab.shared.permission;

import java.util.stream.Collectors;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;

public class LuckPerms implements PermissionPlugin {

	private String version;

	public LuckPerms(String version) {
		this.version = version;
	}

	@Override
	public String getPrimaryGroup(ITabPlayer p) {
		try {
			//LuckPerms API v5
			User user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
			if (user == null) {
				Shared.errorManager.printError("LuckPerms v" + version + " returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getPrimaryGroup)");
				return "null";
			}
			return user.getPrimaryGroup();
		} catch (NoClassDefFoundError e) {
			//LuckPerms API v4
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
		try {
			//LuckPerms API v5
			User user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
			if (user == null) {
				Shared.errorManager.printError("LuckPerms v" + version + "returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getAllGroups)");
				return new String[] {"null"};
			}
			return user.getNodes().stream().filter(NodeType.INHERITANCE::matches).map(NodeType.INHERITANCE::cast).map(InheritanceNode::getGroupName).collect(Collectors.toSet()).toArray(new String[0]);
		} catch (NoClassDefFoundError e) {
			//LuckPerms API v4
			me.lucko.luckperms.api.User user = me.lucko.luckperms.LuckPerms.getApi().getUser(p.getUniqueId());
			if (user == null) {
				Shared.errorManager.printError("LuckPerms v" + version + " returned null user for " + p.getName() + " (" + p.getUniqueId() + ") (func: getAllGroups)");
				return new String[] {"null"};
			}
			return user.getAllNodes().stream().filter(me.lucko.luckperms.api.Node::isGroupNode).map(me.lucko.luckperms.api.Node::getGroupName).collect(Collectors.toSet()).toArray(new String[0]);
		}
	}
}