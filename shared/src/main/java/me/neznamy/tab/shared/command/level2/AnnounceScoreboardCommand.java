package me.neznamy.tab.shared.command.level2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.NonNull;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.api.scoreboard.Scoreboard;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.command.SubCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handler for "/tab announce scoreboard" subcommand
 */
public class AnnounceScoreboardCommand extends SubCommand {

    /**
     * Constructs new instance
     */
    public AnnounceScoreboardCommand() {
        super("scoreboard", TabConstants.Permission.COMMAND_SCOREBOARD_ANNOUNCE);
    }

    @Override
    public void execute(@Nullable TabPlayer sender, @NonNull String[] args) {
        ScoreboardManager feature = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SCOREBOARD);
        if (feature == null) {
            sendMessage(sender, getMessages().getScoreboardFeatureNotEnabled());
            return;
        }
        if (args.length != 2) {
            sendMessage(sender, getMessages().getScoreboardAnnounceCommandUsage());
            return;
        }
        String scoreboard = args[0];
        int duration;
        try {
            duration = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sendMessage(sender, getMessages().getInvalidNumber(args[1]));
            return;
        }
        Scoreboard sb = feature.getRegisteredScoreboards().get(scoreboard);
        if (sb == null) {
            sendMessage(sender, getMessages().getScoreboardNotFound(scoreboard));
            return;
        }
        feature.announceScoreboard(sb.getName(), duration);
    }

    @Override
    public @NotNull List<String> complete(@Nullable TabPlayer sender, @NonNull String[] arguments) {
        ScoreboardManager s = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SCOREBOARD);
        if (s == null) return new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        if (arguments.length == 1) {
            for (String bar : s.getRegisteredScoreboards().keySet()) {
                if (bar.toLowerCase().startsWith(arguments[0].toLowerCase())) suggestions.add(bar);
            }
        } else if (arguments.length == 2 && s.getRegisteredScoreboards().get(arguments[0]) != null) {
            for (String time : Arrays.asList("5", "10", "30", "60", "120")) {
                if (time.startsWith(arguments[1])) suggestions.add(time);
            }
        }
        return suggestions;
    }
}