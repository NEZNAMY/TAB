package me.neznamy.tab.shared.command.level1;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class ParseCommand extends SubCommand{

	public ParseCommand() {
		super("parse", null);
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		if (args.length > 0) {
			String replaced = Placeholders.replaceAllPlaceholders(args[0], sender);
			sendMessage(sender, "&6Replacing placeholder &e" + args[0] + "&6 for player &e" + sender.getName());
			sendMessage(sender, "&6Result: &r" + replaced);
		} else {
			sendMessage(sender, "Usage: /tab parse <placeholder>");
		}
	}
	@Override
	public Object complete(ITabPlayer sender, String currentArgument) {
		return null;
	}
}