package me.neznamy.tab.shared.command;

import java.util.Arrays;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.command.level2.AnnounceBarCommand;
import me.neznamy.tab.shared.command.level2.AnnounceScoreboardCommand;

/**
 * Handler for "/tab announce" subcommand
 */
public class AnnounceCommand extends SubCommand {

	/**
	 * Constructs new instance and registers subcommands
	 */
	public AnnounceCommand() {
		super("announce", null);
		getSubcommands().put("bar", new AnnounceBarCommand());
		getSubcommands().put("scoreboard", new AnnounceScoreboardCommand());
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		if (args.length == 0) {
			sendMessage(sender, getMessages().getAnnounceCommandUsage());
			return;
		}
		String arg0 = args[0].toLowerCase();
		SubCommand command = getSubcommands().get(arg0);
		if (command != null) {
			if (command.hasPermission(sender)) {
				command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
			} else {
				sendMessage(sender, getMessages().getNoPermission());
			}
		} else {
			sendMessage(sender, getMessages().getAnnounceCommandUsage());
		}
	}
}