package me.neznamy.tab.shared.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.team.TeamManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;

public class SetCollisionCommand extends SubCommand {

	/**
	 * Constructs new instance
	 */
	public SetCollisionCommand() {
		super("setcollision", TabConstants.Permission.COMMAND_SETCOLLISION);
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		TeamManager feature = TAB.getInstance().getTeamManager();
		if (feature == null) {
			sendMessage(sender, getMessages().getTeamFeatureRequired());
			return;
		}
		if (args.length == 2) {
			TabPlayer target = TAB.getInstance().getPlayer(args[0]);
			if (target == null) {
				sendMessage(sender, getMessages().getPlayerNotFound(args[0]));
				return;
			}
			feature.setCollisionRule(target, Boolean.parseBoolean(args[1]));
			feature.updateTeamData(target);
		} else {
			sendMessage(sender, getMessages().getCollisionCommandUsage());
		}
	}
	
	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		if (arguments.length == 1) return getOnlinePlayers(arguments[0]);
		if (arguments.length == 2) return getStartingArgument(Arrays.asList("true", "false"), arguments[1]);
		return new ArrayList<>();
	}
}