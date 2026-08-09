package me.neznamy.tab.shared.command.scoreboard;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handler for "/tab scoreboard on [player] [options]" subcommand
 */
public class ScoreboardOnCommand extends SubCommand {

    /**
     * Constructs new instance
     */
    public ScoreboardOnCommand() {
        super("on", TabConstants.Permission.COMMAND_SCOREBOARD_TOGGLE);
    }

    @Override
    public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
        ScoreboardManagerImpl scoreboard = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SCOREBOARD);
        if (scoreboard == null) {
            sendMessage(sender, getMessages().getScoreboardFeatureNotEnabled());
            return;
        }
        TabPlayer target = sender;
        boolean silent = args.length > 0 && args[args.length-1].equals("-s");
        int providedArgs = args.length - (silent ? 1 : 0);
        if (providedArgs > 0) {
            if (hasPermission(sender, TabConstants.Permission.COMMAND_SCOREBOARD_TOGGLE_OTHER)) {
                String playerName = args[0];
                target = TAB.getInstance().getPlayer(playerName);
                if (target == null) {
                    sendMessage(sender, getMessages().getPlayerNotFound(playerName));
                    return;
                }
            } else {
                sendMessage(sender, getMessages().getNoPermission());
                return;
            }
        } else if (target == null) {
            sendMessage(null, getMessages().getCommandOnlyFromGame());
            return;
        }
        scoreboard.setScoreboardVisible(target, true, !silent);
    }

    @Override
    public @NotNull List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
        if (arguments.length == 1) {
            List<String> suggestions = new ArrayList<>();
            suggestions.addAll(getOnlinePlayers(arguments[0]));
            suggestions.addAll(getStartingArgument(Collections.singletonList("-s"), arguments[0]));
            return suggestions;
        }
        if (arguments.length == 2) return getStartingArgument(Collections.singletonList("-s"), arguments[1]);
        return Collections.emptyList();
    }
}