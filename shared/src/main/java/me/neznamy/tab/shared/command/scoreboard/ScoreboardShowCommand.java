package me.neznamy.tab.shared.command.scoreboard;

import me.neznamy.tab.api.scoreboard.Scoreboard;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Handler for "/tab scoreboard show &lt;name&gt; [player]" subcommand
 */
public class ScoreboardShowCommand extends SubCommand {

    /**
     * Constructs new instance
     */
    public ScoreboardShowCommand() {
        super("show", TabConstants.Permission.COMMAND_SCOREBOARD_SHOW);
    }

    @Override
    public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
        ScoreboardManager scoreboard = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SCOREBOARD);
        if (scoreboard == null) {
            sendMessage(sender, getMessages().getScoreboardFeatureNotEnabled());
            return;
        }
        if (args.length == 0 || args.length > 2) {
            sendMessage(sender, getMessages().getScoreboardShowUsage());
            return;
        }
        Scoreboard sb = scoreboard.getRegisteredScoreboards().get(args[0]);
        if (sb == null) {
            sendMessage(sender, getMessages().getScoreboardNotFound(args[0]));
            return;
        }
        TabPlayer target;
        if (args.length == 1) {
            if (!hasPermission(sender,TabConstants.Permission.COMMAND_SCOREBOARD_SHOW)) {
                sendMessage(sender, getMessages().getNoPermission());
                return;
            }
            if (sender == null) {
                sendMessage(null, getMessages().getCommandOnlyFromGame());
                return;
            }
            target = sender;
        } else {
            if (!hasPermission(sender,TabConstants.Permission.COMMAND_SCOREBOARD_SHOW_OTHER)) {
                sendMessage(sender, getMessages().getNoPermission());
                return;
            }
            target = TAB.getInstance().getPlayer(args[1]);
            if (target == null) {
                sendMessage(sender, getMessages().getPlayerNotFound(args[1]));
                return;
            }
        }
        scoreboard.showScoreboard(target, sb);
    }

    @Override
    public @NotNull List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
        ScoreboardManager scoreboard = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SCOREBOARD);
        if (scoreboard == null) return Collections.emptyList();
        if (arguments.length == 1) return getStartingArgument(scoreboard.getRegisteredScoreboards().keySet(), arguments[0]);
        if (arguments.length == 2) return getOnlinePlayers(arguments[1]);
        return Collections.emptyList();
    }
}