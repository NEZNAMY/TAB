package me.neznamy.tab.shared.command;

import java.util.List;
import java.util.UUID;

import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;

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
    public void execute(TabPlayer sender, String[] args) {
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

    private void remove(TabPlayer sender, String player) {
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
    public void saveEntity(TabPlayer sender, String player, String type, String value, String server, String world) {
        if (value.length() > 0) {
            sendMessage(sender, getMessages().getPlayerValueAssigned(type, value, player));
        } else {
            sendMessage(sender, getMessages().getPlayerValueRemoved(type, player));
        }
        String[] property = TAB.getInstance().getConfiguration().getUsers().getProperty(player, type, server, world);
        if (property.length > 0 && String.valueOf(value.length() == 0 ? null : value).equals(String.valueOf(property[0]))) return;
        TAB.getInstance().getConfiguration().getUsers().setProperty(player, type, server, world, value.length() == 0 ? null : value);
        TabPlayer pl = TAB.getInstance().getPlayer(player);
        try {
            if (pl == null) pl = TAB.getInstance().getPlayer(UUID.fromString(player));
        } catch (IllegalArgumentException ignored) {} // not an uuid string
        if (pl != null) {
            pl.forceRefresh();
        }
    }

    @Override
    public List<String> complete(TabPlayer sender, String[] arguments) {
        if (arguments.length == 1) return getOnlinePlayers(arguments[0]);
        return super.complete(sender, arguments);
    }
}