package me.neznamy.tab.shared.command.level1;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManager;

/**
 * Handler for "/tab scoreboard [on/off/toggle] [player] [options]" subcommand
 */
public class ScoreboardCommand extends SubCommand {

	/**
	 * Constructs new instance
	 */
	public ScoreboardCommand() {
		super("scoreboard", null);
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		ScoreboardManager scoreboard = (ScoreboardManager) TAB.getInstance().getFeatureManager().getFeature("scoreboard");
		if (scoreboard == null) {
			sendMessage(sender, TAB.getInstance().getPlaceholderManager().color("&cScoreboard feature is not enabled, therefore toggle command cannot be used."));
			return;
		}
		if (!scoreboard.permToToggle || sender.hasPermission("tab.togglescoreboard")) {
			if (args.length == 0) {
				sender.toggleScoreboard(true);
			}
			TabPlayer p = sender;
			if (args.length >= 2 && TAB.getInstance().getPlayer(args[1]) != null)
				p = TAB.getInstance().getPlayer(args[1]);
			boolean silent = args.length >= 3 && args[2].equals("-s");
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("on"))
					p.setScoreboardVisible(true, !silent);
			
				if (args[0].equalsIgnoreCase("off"))
					p.setScoreboardVisible(false, !silent);
			
				if (args[0].equalsIgnoreCase("toggle"))
					p.toggleScoreboard(!silent);
			}
		} else {
			sender.sendMessage(getTranslation("no_permission"), true);
		}
	}
}