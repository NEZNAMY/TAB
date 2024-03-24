package me.neznamy.tab.shared.command;

import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handler for "/tab groups" subcommand
 */
public class GroupsCommand extends SubCommand {

    /**
     * Constructs new instance
     */
    public GroupsCommand() {
        super("groups", TabConstants.Permission.COMMAND_GROUP_LIST);
    }

    @Override
    public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
        sendMessage(sender, "&3Configured groups:");
        sendMessage(sender, "&9" + String.join(", &9", TAB.getInstance().getConfiguration().getGroups().getAllEntries()));
    }
}