package me.neznamy.tab.shared.command;

import java.util.List;
import java.util.UUID;

import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handler for "/tab player" subcommand
 */
public class PlayerCommand extends PropertyCommand {

    /**
     * Constructs new instance
     */
    public PlayerCommand() {
        super("player");
    }

    @Override
    public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
        //<name> <property> [value...]
        if (args.length <= 1) {
            help(sender);
            return;
        }
        if ("remove".equalsIgnoreCase(args[1])) {
            remove(sender, args[0]);
            return;
        }
        trySaveEntity(sender, args);
    }

    private void remove(@Nullable TabPlayer sender, @NotNull String player) {
        if (hasPermission(sender, TabConstants.Permission.COMMAND_DATA_REMOVE)) {
            TAB.getInstance().getConfiguration().getUsers().remove(player);
            TabPlayer pl = TAB.getInstance().getPlayer(player);
            if (pl != null) {
                pl.forceRefresh();
            }
            sendMessage(sender, getMessages().getPlayerDataRemoved(player));
        } else {
            sendMessage(sender, getMessages().getNoPermission());
        }
    }

    @Override
    public void saveEntity(@Nullable TabPlayer sender, @NotNull String player, @NotNull String type, @NotNull String value, @Nullable String server, @Nullable String world) {
        if (!value.isEmpty()) {
            sendMessage(sender, getMessages().getPlayerValueAssigned(type, value, player));
        } else {
            sendMessage(sender, getMessages().getPlayerValueRemoved(type, player));
        }
        String[] property = TAB.getInstance().getConfiguration().getUsers().getProperty(player, type, server, world);
        if (property.length > 0 && String.valueOf(value.isEmpty() ? null : value).equals(String.valueOf(property[0]))) return;
        TAB.getInstance().getConfiguration().getUsers().setProperty(player, type, server, world, value.isEmpty() ? null : value);
        TabPlayer pl = TAB.getInstance().getPlayer(player);
        try {
            if (pl == null) pl = TAB.getInstance().getPlayer(UUID.fromString(player));
        } catch (IllegalArgumentException ignored) {} // not an uuid string
        if (pl != null) {
            pl.forceRefresh();
        }
    }

    @Override
    public @NotNull List<String> complete(TabPlayer sender, String[] arguments) {
        if (arguments.length == 1) return getOnlinePlayers(arguments[0]);
        return super.complete(sender, arguments);
    }
}