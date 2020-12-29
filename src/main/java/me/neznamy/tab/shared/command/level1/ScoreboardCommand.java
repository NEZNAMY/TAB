package me.neznamy.tab.shared.command.level1;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManager;

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
			sendMessage(sender, PlaceholderManager.color("&cScoreboard feature is not enabled, therefore toggle command cannot be used."));
			return;
		}
		if (!scoreboard.permToToggle || sender.hasPermission("tab.togglescoreboard")) {
			if (args.length == 0) {
				sender.toggleScoreboard(true);
			}
			TabPlayer p = sender;
			if (args.length >= 2 && Shared.getPlayer(args[1]) != null)
				p = Shared.getPlayer(args[1]);
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("on"))
					p.setScoreboardVisible(true, true);
			
				if (args[0].equalsIgnoreCase("off"))
					p.setScoreboardVisible(false, true);
			
				if (args[0].equalsIgnoreCase("toggle"))
					p.toggleScoreboard(true);
			}
		} else {
			sender.sendMessage(Configs.no_perm, true);
		}
	}
}