package me.neznamy.tab.premium.conditions.simple;

import me.neznamy.tab.api.TabPlayer;

/**
 * "permission:permission.node" condition where permission.node is the permission
 */
public class PermissionCondition extends SimpleCondition {

	private String permission;
	
	public PermissionCondition(String permission) {
		this.permission = permission;
	}
	
	@Override
	public boolean isMet(TabPlayer p) {
		return p.hasPermission(permission);
	}
	
	public static PermissionCondition compile(String line) {
		if (line.startsWith("permission:")) {
			return new PermissionCondition(line.split(":")[1]);
		} else {
			return null;
		}
	}
}