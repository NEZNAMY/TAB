package me.neznamy.tab.shared.command.level1;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;

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
		ScoreboardManagerImpl scoreboard = (ScoreboardManagerImpl) TAB.getInstance().getFeatureManager().getFeature("scoreboard");
		if (scoreboard == null) {
			sendMessage(sender, "&cScoreboard feature is not enabled, therefore toggle command cannot be used.");
			return;
		}
		if (scoreboard.requiresPermissionToToggle() && !hasPermission(sender, "tab.togglescoreboard")) {
			sendMessage(sender, getTranslation("no_permission"));
			return;
		}
		if (args.length == 0) {
			if (sender == null) {
				sendMessage(sender, "Toggle command must be ran from the game");
			} else {
				scoreboard.toggleScoreboard(sender, true);
			}
			return;
		}
		TabPlayer p = sender;
		if (args.length >= 2 && TAB.getInstance().getPlayer(args[1]) != null) {
			if (hasPermission(sender, "tab.togglescoreboard.other")) {
				p = TAB.getInstance().getPlayer(args[1]);
			} else {
				sendMessage(sender, getTranslation("no_permission"));
				return;
			}
		}
		if (scoreboard.getOtherPluginScoreboards().containsKey(p)) return; //not overriding other plugins
		boolean silent = args.length >= 3 && args[2].equals("-s");
		if (args.length >= 1) {
			switch(args[0]) {
			case "on":
				scoreboard.setScoreboardVisible(p, true, !silent);
				break;
			case "off":
				scoreboard.setScoreboardVisible(p, false, !silent);
				break;
			case "toggle":
				scoreboard.toggleScoreboard(p, !silent);
				break;
			default:
				break;
			}
		}
	}
}