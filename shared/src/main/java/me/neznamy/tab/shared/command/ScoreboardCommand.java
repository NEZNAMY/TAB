package me.neznamy.tab.shared.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.scoreboard.Scoreboard;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;

/**
 * Handler for "/tab scoreboard [on/off/toggle] [player] [options]" subcommand
 * and "/tab scoreboard show <name> [player]"
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
		ScoreboardManagerImpl scoreboard = getScoreboardManager();
		if (scoreboard == null) {
			sendMessage(sender, "&cScoreboard feature is not enabled, therefore toggle command cannot be used.");
			return;
		}
		if (args.length == 0) {
			toggle(sender);
			return;
		}
		if (args[0].equalsIgnoreCase("show")) {
			show(sender, args);
			return;
		}
		TabPlayer p = getTarget(sender, args);
		if (scoreboard.getOtherPluginScoreboards().containsKey(p)) return; //not overriding other plugins
		boolean silent = args.length >= 3 && args[2].equals("-s");
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

	private void toggle(TabPlayer sender) {
		if (sender == null) {
			sendMessage(null, getMessages().getCommandOnlyFromGame());
			return;
		}
		if (sender.hasPermission(TabConstants.Permission.COMMAND_SCOREBOARD_TOGGLE)) {
			ScoreboardManagerImpl scoreboard = getScoreboardManager();
			if (scoreboard.getOtherPluginScoreboards().containsKey(sender)) return; //not overriding other plugins
			scoreboard.toggleScoreboard(sender, true);
		} else {
			sender.sendMessage(getMessages().getNoPermission(), true);
		}
	}
	
	private void show(TabPlayer sender, String[] args) {
		if (args.length < 2) {
			sendMessage(sender, getMessages().getScoreboardShowUsage());
			return;
		}
		ScoreboardManagerImpl scoreboard = getScoreboardManager();
		Scoreboard sb = scoreboard.getRegisteredScoreboards().get(args[1]);
		if (sb == null) {
			sendMessage(sender, getMessages().getScoreboardNotFound(args[1]));
			return;
		}
		TabPlayer target;
		if (args.length == 2) {
			if (!hasPermission(sender,TabConstants.Permission.COMMAND_SCOREBOARD_SHOW)) {
				sendMessage(sender, getMessages().getNoPermission());
				return;
			}
			target = sender;
		} else {
			if (!hasPermission(sender,TabConstants.Permission.COMMAND_SCOREBOARD_SHOW_OTHER)) {
				sendMessage(sender, getMessages().getNoPermission());
				return;
			}
			target = TAB.getInstance().getPlayer(args[2]);
			if (target == null) {
				sendMessage(sender, getMessages().getPlayerNotFound(args[2]));
				return;
			}
		}
		scoreboard.showScoreboard(target, sb);
	}

	private TabPlayer getTarget(TabPlayer sender, String[] args) {
		TabPlayer target = sender;
		if (args.length >= 2 && TAB.getInstance().getPlayer(args[1]) != null) {
			if (hasPermission(sender, TabConstants.Permission.COMMAND_SCOREBOARD_TOGGLE_OTHER)) {
				target = TAB.getInstance().getPlayer(args[1]);
			} else {
				sendMessage(sender, getMessages().getNoPermission());
			}
		}
		return target;
	}
	
	private ScoreboardManagerImpl getScoreboardManager() {
		return (ScoreboardManagerImpl) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SCOREBOARD);
	}

	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		ScoreboardManagerImpl scoreboard = getScoreboardManager();
		if (scoreboard == null) return new ArrayList<>();
		if (arguments.length == 1) return getStartingArgument(Arrays.asList("on", "off", "toggle", "show"), arguments[0]);
		if (arguments.length == 2) return arguments[0].equalsIgnoreCase("show") ? getStartingArgument(scoreboard.getRegisteredScoreboards().keySet(), arguments[1]) : getOnlinePlayers(arguments[1]);
		if (arguments.length == 3) return arguments[0].equalsIgnoreCase("show") ? getOnlinePlayers(arguments[2]) : getStartingArgument(Collections.singletonList("-s"), arguments[2]);
		return new ArrayList<>();
	}
}