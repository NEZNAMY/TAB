package me.neznamy.tab.shared.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.team.TeamManager;
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
		TeamManager feature = TAB.getInstance().getTeamManager();
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
			feature.setCollisionRule(target, Boolean.parseBoolean(args[1]));
			feature.updateTeamData(target);
		} else {
			sendMessage(sender, "&cUsage: /tab setcollision <player> <true/false>");
		}
	}
	
	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		if (arguments.length == 1) return getOnlinePlayers(arguments[0]);
		if (arguments.length == 2) return getStartingArgument(Arrays.asList("true", "false"), arguments[1]);
		return new ArrayList<>();
	}
}