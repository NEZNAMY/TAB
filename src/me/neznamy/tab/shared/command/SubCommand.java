package me.neznamy.tab.shared.command;

import java.util.HashMap;
import java.util.Map;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public abstract class SubCommand {

	private String name;
	private String permission;
	protected Map<String, SubCommand> subcommands = new HashMap<String, SubCommand>();
	
	public SubCommand(String name, String permission) {
		this.name = name;
		this.permission = permission;
	}
	public String getName() {
		return name;
	}
	public boolean hasPermission(ITabPlayer sender) {
		return hasPermission(sender, permission);
	}
	public boolean hasPermission(ITabPlayer sender, String permission) {
		if (permission == null) return true; //no permission required
		if (sender == null) return true; //console
		if (sender.hasPermission("tab.admin")) return true;
		return sender.hasPermission(permission);
	}
	public static void sendMessage(ITabPlayer sender, String message) {
		if (sender != null) {
			sender.sendMessage(message);
		} else {
			Shared.mainClass.sendConsoleMessage(message);
		}
	}
	public abstract void execute(ITabPlayer sender, String[] args);
	public abstract Object complete(ITabPlayer sender, String currentArgument);
}