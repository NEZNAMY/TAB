package me.neznamy.tab.shared.command.level1;

import java.util.Arrays;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.command.level2.AnnounceBarCommand;
import me.neznamy.tab.shared.config.Configs;

/**
 * Handler for "/tab announce" subcommand
 */
public class AnnounceCommand extends SubCommand {

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
}