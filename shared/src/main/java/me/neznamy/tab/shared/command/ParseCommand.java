package me.neznamy.tab.shared.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.PropertyImpl;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;

/**
 * Handler for "/tab parse <player> <placeholder>" subcommand
 */
public class ParseCommand extends SubCommand {

	/**
	 * Constructs new instance
	 */
	public ParseCommand() {
		super("parse", TabConstants.Permission.COMMAND_PARSE);
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		if (args.length < 2) {
			sendMessage(sender, getMessages().getParseCommandUsage());
			return;
		}
		TabPlayer target;
		if (args[0].equals("me") && sender != null) {
			target = sender;
		} else {
			target = TAB.getInstance().getPlayer(args[0]);
			if (target == null) {
				sendMessage(sender, getMessages().getPlayerNotFound(args[0]));
				return;
			}
		}
		String replaced = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
		String message = EnumChatFormat.color("&6Replacing placeholder &e%placeholder% &6for player &e" + target.getName()).replace("%placeholder%", replaced);
		sendRawMessage(sender, message);
		try {
			replaced = new PropertyImpl(null, target, replaced).get();
		} catch (Exception e) {
			sendMessage(sender, "&cThe placeholder threw an exception when parsing. Check console for more info.");
			TAB.getInstance().getErrorManager().printError("Placeholder " + replaced + " threw an exception when parsing for player " + target.getName(), e, true);
			return;
		}
		IChatBaseComponent colored = IChatBaseComponent.optimizedComponent("With colors: " + replaced);
		if (sender != null) {
			sender.sendMessage(colored);
		} else {
			sendRawMessage(null, colored.toLegacyText());
		}
		sendRawMessage(sender, "Without colors: " + EnumChatFormat.decolor(replaced));
	}
	
	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		if (arguments.length == 1) {
			List<String> suggestions = getOnlinePlayers(arguments[0]);
			if ("me".startsWith(arguments[0].toLowerCase())) suggestions.add("me");
			return suggestions;
		}
		if (arguments.length == 2) {
			return TAB.getInstance().getPlaceholderManager().getAllPlaceholders().stream().map(Placeholder::getIdentifier)
					.filter(placeholder -> placeholder.toLowerCase().startsWith(arguments[1].toLowerCase())).collect(Collectors.toList());
		}
		return new ArrayList<>();
	}
}