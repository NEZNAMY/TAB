package me.neznamy.tab.shared.command;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.command.bossbar.BossBarCommand;
import me.neznamy.tab.shared.command.scoreboard.ScoreboardCommand;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The core command handler
 */
public class TabCommand extends SubCommand {

    /**
     * Constructs new instance with given parameter and registers all subcommands
     */
    public TabCommand() {
        super(null, null);
        registerSubCommand(new BossBarCommand());
        registerSubCommand(new CpuCommand());
        registerSubCommand(new DebugCommand());
        registerSubCommand(new GroupCommand());
        registerSubCommand(new GroupsCommand());
        registerSubCommand(new MySQLCommand());
        registerSubCommand(new NameTagCommand());
        registerSubCommand(new ParseCommand());
        registerSubCommand(new PlayerCommand());
        registerSubCommand(new PlayerUUIDCommand());
        registerSubCommand(new ReloadCommand());
        registerSubCommand(new SetCollisionCommand());
        registerSubCommand(new ScoreboardCommand());
    }

    @Override
    public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
        if (args.length > 0) {
            String arg0 = args[0];
            SubCommand command = getSubcommands().get(arg0.toLowerCase());
            if (command != null) {
                if (command.hasPermission(sender)) {
                    command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
                } else {
                    sendMessage(sender, getMessages().getNoPermission());
                }
            } else {
                help(sender);
            }
        } else {
            help(sender);
        }
    }

    /**
     * Sends help menu to the sender
     *
     * @param   sender
     *          player who ran command or null if from console
     */
    private void help(@Nullable TabPlayer sender) {
        if (hasPermission(sender, TabConstants.Permission.COMMAND_ALL)) {
            sendMessage(sender, "&3TAB v" + TabConstants.PLUGIN_VERSION);
            for (String message : getMessages().getHelpMenu()) {
                sendMessage(sender, message.replace("/tab", "/" + TAB.getInstance().getPlatform().getCommand()));
            }
        }
    }

    @Override
    public @NotNull List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
        if (!hasPermission(sender, TabConstants.Permission.COMMAND_AUTOCOMPLETE)) return Collections.emptyList();
        return super.complete(sender, arguments);
    }
}