package me.neznamy.tab.shared.command.scoreboard;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Handler for /tab scoreboard
 */
public class ScoreboardCommand extends SubCommand {

    /**
     * Constructs new instance
     */
    public ScoreboardCommand() {
        super("scoreboard", null);
        registerSubCommand(new ScoreboardAnnounceCommand());
        registerSubCommand(new ScoreboardShowCommand());
        registerSubCommand(new ScoreboardToggleCommand());
        registerSubCommand(new ScoreboardOnCommand());
        registerSubCommand(new ScoreboardOffCommand());
    }

    @Override
    public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
        SubCommand command;
        if (args.length > 0) {
            command = getSubcommands().get(args[0].toLowerCase());
        } else {
            command = getSubcommands().get("toggle");
            args = new String[]{""};
        }
        if (command != null) {
            if (command.hasPermission(sender)) {
                command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
            } else {
                sendMessage(sender, getMessages().getNoPermission());
            }
        } else {
            sendMessages(sender, getMessages().getScoreboardHelpMenu());
        }
    }

    private void toggle(@Nullable TabPlayer sender, @NotNull ScoreboardManagerImpl scoreboard) {
        if (sender == null) {
            sendMessage(null, getMessages().getCommandOnlyFromGame());
            return;
        }
        if (sender.hasPermission(TabConstants.Permission.COMMAND_SCOREBOARD_TOGGLE)) {
            if (scoreboard.getOtherPluginScoreboards().containsKey(sender)) return; //not overriding other plugins
            scoreboard.toggleScoreboard(sender, true);
        } else {
            sender.sendMessage(getMessages().getNoPermission(), true);
        }
    }

    private TabPlayer getTarget(@Nullable TabPlayer sender, @NotNull String[] args) {
        if (args.length >= 2 && TAB.getInstance().getPlayer(args[1]) != null) {
            if (hasPermission(sender, TabConstants.Permission.COMMAND_SCOREBOARD_TOGGLE_OTHER)) {
                return TAB.getInstance().getPlayer(args[1]);
            } else {
                sendMessage(sender, getMessages().getNoPermission());
            }
        } else {
            if (hasPermission(sender, TabConstants.Permission.COMMAND_SCOREBOARD_TOGGLE)) {
                return sender;
            } else {
                sendMessage(sender, getMessages().getNoPermission());
            }
        }
        return null;
    }

    private @Nullable ScoreboardManagerImpl getScoreboardManager() {
        return TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SCOREBOARD);
    }
}