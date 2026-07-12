package me.neznamy.tab.platforms.fand;

import io.fand.api.command.CommandContext;
import io.fand.api.entity.Player;
import java.util.Collections;
import java.util.List;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/** Handler for TAB's main command on Fand. */
public final class FandTabCommand extends FandCommand {

    public FandTabCommand(@NotNull String commandName) {
        super(commandName);
    }

    @Override
    protected void execute(@NotNull CommandContext context, @NotNull String[] arguments) {
        if (TAB.getInstance().isPluginDisabled()) {
            boolean reload = context.sender().can(TabConstants.Permission.COMMAND_RELOAD);
            boolean admin = context.sender().can(TabConstants.Permission.COMMAND_ALL);
            for (TabComponent message : TAB.getInstance().getDisabledCommand().execute(arguments, reload, admin)) {
                context.sender().sendMessage(message.toAdventure());
            }
            return;
        }

        if (context.sender() instanceof Player player) {
            TabPlayer tabPlayer = TAB.getInstance().getPlayer(player.uniqueId());
            if (tabPlayer != null) {
                TAB.getInstance().getCommand().execute(tabPlayer, arguments);
            }
        } else {
            TAB.getInstance().getCommand().execute(null, arguments);
        }
    }

    @Override
    @NotNull
    protected List<String> complete(@NotNull CommandContext context, @NotNull String[] arguments) {
        TabPlayer tabPlayer = null;
        if (context.sender() instanceof Player player) {
            tabPlayer = TAB.getInstance().getPlayer(player.uniqueId());
            if (tabPlayer == null) {
                return Collections.emptyList();
            }
        }
        return TAB.getInstance().getCommand().complete(tabPlayer, arguments);
    }
}
