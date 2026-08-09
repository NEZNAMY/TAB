package me.neznamy.tab.shared.command.scoreboard;

import me.neznamy.tab.shared.command.SubCommand;
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
        if (args.length == 0) {
            args = new String[]{"toggle"};
        }
        SubCommand command = getSubcommands().get(args[0].toLowerCase());
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
}