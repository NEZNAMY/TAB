package me.neznamy.tab.shared.command;

import java.util.Arrays;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.command.level2.SendBarCommand;

/**
 * Handler for "/tab send" subcommand
 */
public class SendCommand extends SubCommand {

	/**
	 * Constructs new instance
	 */
	public SendCommand() {
		super("send", null);
		getSubcommands().put("bar", new SendBarCommand());
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		if (args.length > 0) {
			String arg0 = args[0].toLowerCase();
			SubCommand command = getSubcommands().get(arg0);
			if (command != null) {
				if (command.hasPermission(sender)) {
					command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
				} else {
					sendMessage(sender, getMessages().getNoPermission());
				}
			} else {
				sendMessage(sender, getMessages().getSendCommandUsage());
			}
		} else {
			sendMessage(sender, getMessages().getSendCommandUsage());
		}
	}
}