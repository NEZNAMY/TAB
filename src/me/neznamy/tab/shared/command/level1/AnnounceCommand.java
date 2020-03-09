package me.neznamy.tab.shared.command.level1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.command.level2.AnnounceBarCommand;

public class AnnounceCommand extends SubCommand{

	public AnnounceCommand() {
		super("announce", null);
		subcommands.put("bar", new AnnounceBarCommand());
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		if (args.length > 0) {
			String arg0 = args[0].toLowerCase();
			SubCommand command = subcommands.get(arg0);
			if (command != null) {
				if (command.hasPermission(sender)) {
					command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
				} else {
					sendMessage(sender, Configs.no_perm);
				}
			} else {
				sendMessage(sender, "Usage: /tab announce <type> <bar name> <length>");
				sendMessage(sender, "Currently supported types: &lbar");
			}
		} else {
			sendMessage(sender, "Usage: /tab announce <type> <bar name> <length>");
			sendMessage(sender, "Currently supported types: &lbar");
		}
	}
	@Override
	public List<String> complete(ITabPlayer sender, String[] arguments) {
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
		return null;
	}
}