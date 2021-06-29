package me.neznamy.tab.shared.command.level1;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.ITabPlayer;
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
			sendMessage(sender, "&cScoreboard feature is not enabled, therefore toggle command cannot be used.");
			return;
		}
		if (scoreboard.isPermToToggle() && !hasPermission(sender, "tab.togglescoreboard")) {
			sendMessage(sender, getTranslation("no_permission"));
			return;
		}
		if (args.length == 0) {
			if (sender == null) {
				sendMessage(sender, "Toggle command must be ran from the game");
			} else {
				sender.toggleScoreboard(true);
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
		if (((ITabPlayer)p).getOtherPluginScoreboard() != null) return; //not overriding other plugins
		boolean silent = args.length >= 3 && args[2].equals("-s");
		if (args.length >= 1) {
			switch(args[0]) {
			case "on":
				p.setScoreboardVisible(true, !silent);
				break;
			case "off":
				p.setScoreboardVisible(false, !silent);
				break;
			case "toggle":
				p.toggleScoreboard(!silent);
				break;
			default:
				break;
			}
		}
	}
}