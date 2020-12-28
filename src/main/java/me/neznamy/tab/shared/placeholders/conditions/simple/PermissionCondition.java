package me.neznamy.tab.shared.placeholders.conditions.simple;

import me.neznamy.tab.api.TabPlayer;

/**
 * "permission:permission.node" condition where permission.node is the permission
 */
public class PermissionCondition extends SimpleCondition {

	private String permission;
	
	public PermissionCondition(String line) {
		permission = line.split(":")[1];
	}
	
	@Override
	public boolean isMet(TabPlayer p) {
		return p.hasPermission(permission);
	}
}