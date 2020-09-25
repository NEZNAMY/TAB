package me.neznamy.tab.shared.command.level1;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.premium.scoreboard.ScoreboardManager;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * Handler for "/tab scoreboard" subcommand
 */
public class ScoreboardCommand extends SubCommand {

	public ScoreboardCommand() {
		super("scoreboard", null);
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		ScoreboardManager scoreboard = (ScoreboardManager) Shared.featureManager.getFeature("scoreboard");
		if (scoreboard == null) {
			sendMessage(sender, Placeholders.color("&cScoreboard feature is not enabled, therefore toggle command cannot be used."));
			return;
		}
		if (!scoreboard.permToToggle || sender.hasPermission("tab.togglescoreboard")) {
			if (args.length == 0) {
				sender.toggleScoreboard();
			}
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("on")) {
					sender.setScoreboardVisible(true);
				}
				if (args[0].equalsIgnoreCase("off")){
					sender.setScoreboardVisible(false);
				}
			}
		} else {
			sender.sendMessage(Configs.no_perm, true);
		}
	}
}