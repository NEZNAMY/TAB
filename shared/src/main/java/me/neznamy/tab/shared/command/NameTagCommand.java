package me.neznamy.tab.shared.command;

import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Handler for "/tab nametag" subcommand
 */
public class NameTagCommand extends SubCommand {

    /**
     * Constructs new instance
     */
    public NameTagCommand() {
        super("nametag", null);
    }

    @Override
    public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
        if (args.length == 0 || args.length > 3) {
            sendMessages(sender, getMessages().getNameTagHelpMenu());
            return;
        }
        boolean silent = args.length == 3 && args[2].equals("-s");

        if (args[0].toLowerCase(Locale.US).equals("toggle")) {
            toggle(sender, getTarget(sender, args, TabConstants.Permission.COMMAND_NAMETAG_TOGGLE_OTHER, TabConstants.Permission.COMMAND_NAMETAG_TOGGLE), silent);
        } else {
            sendMessages(sender, getMessages().getNameTagHelpMenu());
        }
    }

    private void toggle(@Nullable TabPlayer sender, @Nullable TabPlayer target, boolean silent) {
        if (target == null) return;

        NameTagManager teams = TAB.getInstance().getNameTagManager();
        if (teams == null) {
            sendMessage(sender, getMessages().getNameTagFeatureNotEnabled());
            return;
        }
        teams.toggleNameTagVisibilityView(target, !silent);
    }

    private @Nullable TabPlayer getTarget(@Nullable TabPlayer sender, @NotNull String[] args, @NotNull String permissionOther, @NotNull String permission) {
        if (args.length >= 2 && TAB.getInstance().getPlayer(args[1]) != null) {
            if (hasPermission(sender, permissionOther)) {
                return TAB.getInstance().getPlayer(args[1]);
            } else {
                sendMessage(sender, getMessages().getNoPermission());
            }
        } else {
            if (hasPermission(sender, permission)) {
                return sender;
            } else {
                sendMessage(sender, getMessages().getNoPermission());
            }
        }
        return null;
    }

    @Override
    public @NotNull List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
        if (arguments.length == 1) return getStartingArgument(Collections.singletonList("toggle"), arguments[0]);
        if (arguments.length == 2) return getOnlinePlayers(arguments[1]);
        if (arguments.length == 3) return getStartingArgument(Collections.singletonList("-s"), arguments[2]);
        return Collections.emptyList();
    }
}