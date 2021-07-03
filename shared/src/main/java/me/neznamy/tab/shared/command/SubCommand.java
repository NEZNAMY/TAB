package me.neznamy.tab.shared.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.PropertyUtils;
import me.neznamy.tab.shared.TAB;
/**
 * Abstract class representing a subcommand of "/tab" command
 */
public abstract class SubCommand {

	//all properties assignable with a command
	private static String[] allProperties = {PropertyUtils.TABPREFIX, PropertyUtils.TABSUFFIX, PropertyUtils.TAGPREFIX, PropertyUtils.TAGSUFFIX, PropertyUtils.CUSTOMTABNAME, PropertyUtils.ABOVENAME, PropertyUtils.BELOWNAME, PropertyUtils.CUSTOMTAGNAME};
	
	//properties that require unlimited nametag mode
	protected final List<String> extraProperties = Arrays.asList(PropertyUtils.ABOVENAME, PropertyUtils.BELOWNAME, PropertyUtils.CUSTOMTAGNAME);
	
	//name of this subcommand
	private String name;
	
	//permission required to run this command
	private String permission;
	
	//subcommands of this command
	private Map<String, SubCommand> subcommands = new HashMap<>();
	
	/**
	 * Constructs new instance with given parameters
	 * @param name - command name
	 * @param permission - permission requirement
	 */
	protected SubCommand(String name, String permission) {
		this.name = name;
		this.permission = permission;
	}
	
	/**
	 * Registers new subcommand
	 * @param subcommand - subcommand to register
	 */
	public void registerSubCommand(SubCommand subcommand) {
		getSubcommands().put(subcommand.getName(), subcommand);
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
		if (message == null || message.length() == 0) return;
		if (sender != null) {
			sender.sendMessage(message, true);
		} else {
			TAB.getInstance().getPlatform().sendConsoleMessage(message, true);
		}
	}
	
	/**
	 * Sends message to the command sender without colors translated
	 * @param sender - player or console to send the message to
	 * @param message - the message to sent
	 */
	public void sendRawMessage(TabPlayer sender, String message) {
		if (message == null || message.length() == 0) return;
		if (sender != null) {
			sender.sendMessage(message, false);
		} else {
			TAB.getInstance().getPlatform().sendConsoleMessage(message, false);
		}
	}
	
	/**
	 * Returns all players whose name start with given string
	 * @param nameStart - beginning of the name
	 * @return List of compatible players
	 */
	public List<String> getPlayers(String nameStart){
		List<String> suggestions = new ArrayList<>();
		for (TabPlayer all : TAB.getInstance().getPlayers()) {
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
		String argument;
		if (arguments.length == 0) {
			argument = "";
		} else {
			argument = arguments[0].toLowerCase();
		}
		if (arguments.length < 2) {
			List<String> suggestions = new ArrayList<>();
			for (String subcommand : getSubcommands().keySet()) {
				if (subcommand.startsWith(argument)) suggestions.add(subcommand);
			}
			return suggestions;
		}
		SubCommand subcommand = getSubcommands().get(argument);
		if (subcommand != null) {
			return subcommand.complete(sender, Arrays.copyOfRange(arguments, 1, arguments.length));
		}
		return new ArrayList<>();
	}
	
	/**
	 * Converts string array into a single string with "" and '' support for spaces
	 * @param args - array of arguments
	 * @return argument from array
	 */
	public String buildArgument(String[] args) {
		String value = String.join(" ", args);
		if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
			value = value.substring(1, value.length()-1);
		}
		return value;
	}
	
	/**
	 * Returns message with given key
	 * @param key - message key
	 * @return translation from file
	 */
	public String getTranslation(String key) {
		return TAB.getInstance().getConfiguration().getTranslation().getString(key);
	}
	
	/**
	 * Performs the command
	 * @param sender - command sender or null if console
	 * @param args - arguments of the command
	 */
	public abstract void execute(TabPlayer sender, String[] args);

	public static String[] getAllProperties() {
		return allProperties;
	}

	public static void setAllProperties(String[] allProperties) {
		SubCommand.allProperties = allProperties;
	}

	public Map<String, SubCommand> getSubcommands() {
		return subcommands;
	}

	public void setSubcommands(Map<String, SubCommand> subcommands) {
		this.subcommands = subcommands;
	}
}