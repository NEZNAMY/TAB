package me.neznamy.tab.shared.command;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.team.ScoreboardTeamManager;
import me.neznamy.tab.shared.TAB;

public class SetCollisionCommand extends SubCommand {

	/**
	 * Constructs new instance
	 */
	public SetCollisionCommand() {
		super("setcollision", "tab.setcollision");
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		ScoreboardTeamManager feature = TAB.getInstance().getScoreboardTeamManager();
		if (feature == null) {
			sendMessage(sender, "This command requires nametag feature enabled");
			return;
		}
		if (args.length == 2) {
			TabPlayer target = TAB.getInstance().getPlayer(args[0]);
			if (target == null) {
				sendMessage(sender, getTranslation("player_not_found"));
				return;
			}
			try {
				feature.setCollisionRule(target, Boolean.parseBoolean(args[1]));
				feature.updateTeamData(target);
			} catch (Exception e) {
				sendMessage(sender, "&c\"" + args[1] + "\" is not a valid true/false value");
			}
		} else {
			sendMessage(sender, "&cUsage: /tab setcollision <player> <true/false>");
		}
	}
}