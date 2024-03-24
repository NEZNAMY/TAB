package me.neznamy.tab.shared.command.bossbar;

import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Handler for "/tab bossbar"
 */
public class BossBarCommand extends SubCommand {

    /**
     * Constructs new instance
     */
    public BossBarCommand() {
        super("bossbar", null);
        registerSubCommand(new BossBarAnnounceCommand());
        registerSubCommand(new BossBarSendCommand());
        registerSubCommand(new BossBarToggleCommand());
        registerSubCommand(new BossBarOnCommand());
        registerSubCommand(new BossBarOffCommand());
    }

    @Override
    public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
        SubCommand command;
        if (args.length == 0) {
            command = getSubcommands().get("toggle");
            if (command.hasPermission(sender)) {
                command.execute(sender, new String[0]);
            } else {
                sendMessage(sender, getMessages().getNoPermission());
            }
            return;
        }
        command = getSubcommands().get(args[0].toLowerCase());
        if (command != null) {
            if (command.hasPermission(sender)) {
                command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
            } else {
                sendMessage(sender, getMessages().getNoPermission());
            }
        } else {
            sendMessages(sender, getMessages().getBossbarHelpMenu());
        }
    }
}