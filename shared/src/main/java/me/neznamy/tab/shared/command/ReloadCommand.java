package me.neznamy.tab.shared.command;

import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handler for "/tab reload" subcommand
 */
public class ReloadCommand extends SubCommand {

    /**
     * Constructs new instance
     */
    public ReloadCommand() {
        super("reload", TabConstants.Permission.COMMAND_RELOAD);
    }

    @Override
    public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
        TAB.getInstance().unload();
        sendMessage(sender, TAB.getInstance().load());
    }
}