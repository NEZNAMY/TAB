package me.neznamy.tab.shared.command;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handler for "/tab debug" subcommand
 */
public class DebugCommand extends SubCommand {

    /**
     * Constructs new instance
     */
    public DebugCommand() {
        super("debug", TabConstants.Permission.COMMAND_DEBUG);
    }

    @Override
    public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
        sendMessage(sender, "&cThe debug command was removed in favor of a new \"/" +
                TAB.getInstance().getPlatform().getCommand() + " dump <player>\" command.");
    }
}
