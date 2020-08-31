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
	
	//name of this subcommand
	private String name;
	
	//permission required to run this command
	private String permission;
	
	//subcommands of this command
	public Map<String, SubCommand> subcommands = new HashMap<String, SubCommand>();
	
	public SubCommand(String name, String permission) {
		this.name = name;
		this.permission = permission;
	}
	
	/**
	 * Registers new subcommand
	 * @param subcommand - subcommand to register
	 */
	public void registerSubCommand(SubCommand subcommand) {
		subcommands.put(subcommand.getName(), subcommand);
	}
	
	/**
	 * Returns name of this command
	 * @return name of this command
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns whether player has permission to run this command or not
	 * @param sender - player who ran command or null if console
	 * @return true if has permission or is console, false otherwise
	 */
	public boolean hasPermission(TabPlayer sender) {
		return hasPermission(sender, permission);
	}
	
	/**
	 * Returns whether player has given permission or not
	 * @param sender - player who ran command or null if console
	 * @param permission - permission to check for
	 * @returntrue if has permission or is console, false otherwise
	 */
	public boolean hasPermission(TabPlayer sender, String permission) {
		if (permission == null) return true; //no permission required
		if (sender == null) return true; //console
		if (sender.hasPermission("tab.admin")) return true;
		return sender.hasPermission(permission);
	}
	
	/**
	 * Sends message to the command sender with colors translated
	 * @param sender - player or console to send the message to
	 * @param message - the message to sent
	 */
	public void sendMessage(TabPlayer sender, String message) {
		if (sender != null) {
			sender.sendMessage(message, true);
		} else {
			Shared.platform.sendConsoleMessage(message, true);
		}
	}
	
	/**
	 * Sends message to the command sender without colors translated
	 * @param sender - player or console to send the message to
	 * @param message - the message to sent
	 */
	public void sendRawMessage(TabPlayer sender, String message) {
		if (sender != null) {
			sender.sendMessage(message, false);
		} else {
			Shared.platform.sendConsoleMessage(message, false);
		}
	}
	
	/**
	 * Returns all players whose name start with given string
	 * @param nameStart - beginning of the name
	 * @return List of compatible players
	 */
	public List<String> getPlayers(String nameStart){
		List<String> suggestions = new ArrayList<String>();
		for (TabPlayer all : Shared.getPlayers()) {
			if (all.getName().toLowerCase().startsWith(nameStart.toLowerCase())) suggestions.add(all.getName());
		}
		return suggestions;
	}
	
	/**
	 * Performs command complete and returns list of arguments to be shown
	 * @param sender - command sender
	 * @param arguments - arguments inserted in chat so far
	 * @return List of possible arguments
	 */
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
	
	/**
	 * Performs the command
	 * @param sender - command sender or null if console
	 * @param args - arguments of the command
	 */
	public abstract void execute(TabPlayer sender, String[] args);
}