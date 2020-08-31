package me.neznamy.tab.shared.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;

/**
 * Abstract class representing a subcommand of "/tab" command
 */
public abstract class SubCommand {

	protected final String[] usualProperties = {"tabprefix", "tabsuffix", "tagprefix", "tagsuffix", "customtabname"};
	protected final String[] extraProperties = {"abovename", "belowname", "customtagname"};
	
	private String name;
	private String permission;
	public Map<String, SubCommand> subcommands = new HashMap<String, SubCommand>();
	
	public SubCommand(String name, String permission) {
		this.name = name;
		this.permission = permission;
	}
	public void registerSubCommand(SubCommand subcommand) {
		subcommands.put(subcommand.getName(), subcommand);
	}
	public String getName() {
		return name;
	}
	public boolean hasPermission(TabPlayer sender) {
		return hasPermission(sender, permission);
	}
	public boolean hasPermission(TabPlayer sender, String permission) {
		if (permission == null) return true; //no permission required
		if (sender == null) return true; //console
		if (sender.hasPermission("tab.admin")) return true;
		return sender.hasPermission(permission);
	}
	public void sendMessage(TabPlayer sender, String message) {
		if (sender != null) {
			sender.sendMessage(message, true);
		} else {
			Shared.platform.sendConsoleMessage(message, true);
		}
	}
	public void sendRawMessage(TabPlayer sender, String message) {
		if (sender != null) {
			sender.sendMessage(message, false);
		} else {
			Shared.platform.sendConsoleMessage(message, false);
		}
	}
	public List<String> getPlayers(String nameStart){
		List<String> suggestions = new ArrayList<String>();
		for (TabPlayer all : Shared.getPlayers()) {
			if (all.getName().toLowerCase().startsWith(nameStart.toLowerCase())) suggestions.add(all.getName());
		}
		return suggestions;
	}
	public List<String> complete(TabPlayer sender, String[] arguments) {
		String argument = arguments[0].toLowerCase();
		if (arguments.length == 1) {
			List<String> suggestions = new ArrayList<String>();
			for (String subcommand : subcommands.keySet()) {
				if (subcommand.startsWith(argument)) suggestions.add(subcommand);
			}
			return suggestions;
		}
		SubCommand subcommand = subcommands.get(argument);
		if (subcommand != null) {
			return subcommand.complete(sender, Arrays.copyOfRange(arguments, 1, arguments.length));
		}
		return new ArrayList<String>();
	}
	
	public abstract void execute(TabPlayer sender, String[] args);
}