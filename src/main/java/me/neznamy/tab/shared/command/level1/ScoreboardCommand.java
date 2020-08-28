package me.neznamy.tab.shared.command.level1;

import me.neznamy.tab.premium.scoreboard.ScoreboardManager;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class ScoreboardCommand extends SubCommand{

	public ScoreboardCommand() {
		super("scoreboard", null);
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		ScoreboardManager scoreboard = (ScoreboardManager) Shared.features.get("scoreboard");
		if (scoreboard == null) {
			sendMessage(sender, Placeholders.color("&cScoreboard feature is not enabled, therefore toggle command cannot be used."));
			return;
		}
		if (!scoreboard.permToToggle || sender.hasPermission("tab.togglescoreboard")) {
			if (args.length == 0) {
				sender.hiddenScoreboard = !sender.hiddenScoreboard;
				if (sender.hiddenScoreboard) {
					scoreboard.unregisterScoreboard(sender, true);
					sender.sendMessage(scoreboard.scoreboard_off);
					if (scoreboard.remember_toggle_choice && !scoreboard.sb_off_players.contains(sender.getName())) {
						scoreboard.sb_off_players.add(sender.getName());
						Configs.playerdata.set("scoreboard-off", scoreboard.sb_off_players);
					}
				} else {
					scoreboard.send(sender);
					sender.sendMessage(scoreboard.scoreboard_on);
					if (scoreboard.remember_toggle_choice) {
						scoreboard.sb_off_players.remove(sender.getName());
						Configs.playerdata.set("scoreboard-off", scoreboard.sb_off_players);
					}
				}
			}
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("on") && sender.hiddenScoreboard) {
					scoreboard.send(sender);
					sender.sendMessage(scoreboard.scoreboard_on);
					sender.hiddenScoreboard = false;
					if (scoreboard.remember_toggle_choice) {
						scoreboard.sb_off_players.remove(sender.getName());
						Configs.playerdata.set("scoreboard-off", scoreboard.sb_off_players);
					}
				}
				if (args[0].equalsIgnoreCase("off") && !sender.hiddenScoreboard){
					scoreboard.onQuit(sender);
					sender.sendMessage(scoreboard.scoreboard_off);
					sender.hiddenScoreboard = true;
					if (scoreboard.remember_toggle_choice) {
						scoreboard.sb_off_players.add(sender.getName());
						Configs.playerdata.set("scoreboard-off", scoreboard.sb_off_players);
					}
				}
			}
		} else {
			sender.sendMessage(Configs.no_perm);
		}
	}
}