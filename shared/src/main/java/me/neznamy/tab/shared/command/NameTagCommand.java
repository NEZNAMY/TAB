package me.neznamy.tab.shared.command;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.nametags.NameTagInvisibilityReason;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Handler for "/tab nametag" subcommand.
 * Options:
 *   /tab nametag show/hide/toggle [player] [viewer] [-s]
 *   /tab nametag showview/hideview/toggleview [viewer] [-s]
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
        if (args.length == 1 && sender == null) {
            sendMessage(null, getMessages().getNameTagNoArgFromConsole());
            return;
        }

        if (args.length == 0 || args.length > 4) {
            sendMessages(sender, getMessages().getNameTagHelpMenu());
            return;
        }

        NameTag teams = TAB.getInstance().getNameTagManager();
        if (teams == null) {
            sendMessage(sender, getMessages().getNameTagFeatureNotEnabled());
            return;
        }

        String action = args[0].toLowerCase(Locale.US);
        if (action.equals("show") || action.equals("hide") || action.equals("toggle")) {
            processTarget(teams, sender, action, Arrays.copyOfRange(args, 1, args.length));
        } else if (action.equals("showview") || action.equals("hideview") || action.equals("toggleview")) {
            processView(teams, sender, action,  Arrays.copyOfRange(args, 1, args.length));
        } else {
            sendMessages(sender, getMessages().getNameTagHelpMenu());
        }
    }

    private void processTarget(@NotNull NameTag teams, @Nullable TabPlayer sender, @NotNull String action, @NotNull String[] args) {
        boolean silent = args.length > 0 && args[args.length - 1].equals("-s");

        TabPlayer player = args.length >= 1 ? TAB.getInstance().getPlayer(args[0]) : sender;
        if (player == null) {
            sendMessage(sender, getMessages().getPlayerNotFound(args[0]));
            return;
        }

        TabPlayer viewer = args.length >= 2 ? TAB.getInstance().getPlayer(args[1]) : null;

        String permission = sender == viewer ? TabConstants.Permission.COMMAND_NAMETAG_VISIBILITY : TabConstants.Permission.COMMAND_NAMETAG_VISIBILITY_OTHER;
        if (!hasPermission(sender, permission)) {
            sendMessage(sender, getMessages().getNoPermission());
            return;
        }

        if (action.equals("show")) {
            if (viewer != null) {
                teams.showNameTag(player, viewer, NameTagInvisibilityReason.HIDE_COMMAND, "Processing command (show)", !silent);
            } else {
                teams.showNameTag(player, NameTagInvisibilityReason.HIDE_COMMAND, "Processing command (show)", !silent);
            }
        } else if (action.equals("hide")) {
            if (viewer != null) {
                teams.hideNameTag(player, viewer, NameTagInvisibilityReason.HIDE_COMMAND, "Processing command (hide)", !silent);
            } else {
                teams.hideNameTag(player, NameTagInvisibilityReason.HIDE_COMMAND, "Processing command (hide)", !silent);
            }
        } else if (action.equals("toggle")) {
            if (viewer != null) {
                teams.toggleNameTag(player, viewer, NameTagInvisibilityReason.HIDE_COMMAND, "Processing command (toggle)", !silent);
            } else {
                teams.toggleNameTag(player, NameTagInvisibilityReason.HIDE_COMMAND, "Processing command (toggle)", !silent);
            }
        }
    }

    private void processView(@NotNull NameTag teams, @Nullable TabPlayer sender, @NotNull String action, @NotNull String[] args) {
        boolean silent = args.length > 0 && args[args.length - 1].equals("-s");
        TabPlayer viewer = args.length >= 1 ? TAB.getInstance().getPlayer(args[0]) : sender;

        if (viewer == null) {
            sendMessage(sender, getMessages().getPlayerNotFound(args[0]));
            return;
        }

        String permission = sender == viewer ? TabConstants.Permission.COMMAND_NAMETAG_VIEW : TabConstants.Permission.COMMAND_NAMETAG_VIEW_OTHER;
        if (!hasPermission(sender, permission)) {
            sendMessage(sender, getMessages().getNoPermission());
            return;
        }

        if (action.equals("showview")) {
            teams.showNameTagVisibilityView(viewer, !silent);
        } else if (action.equals("hideview")) {
            teams.hideNameTagVisibilityView(viewer, !silent);
        } else {
            teams.toggleNameTagVisibilityView(viewer, !silent);
        }
    }

    @Override
    @NotNull
    public List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
        if (arguments.length == 1) return getStartingArgument(Arrays.asList("show", "hide", "toggle", "showview", "hideview", "toggleview"), arguments[0]);
        if (arguments.length == 2) return getOnlinePlayers(arguments[1]);
        String action = arguments[0].toLowerCase(Locale.US);
        boolean targeting = action.equals("show") || action.equals("hide") || action.equals("toggle");
        if (arguments.length == 3) {
            if (targeting) {
                return getOnlinePlayers(arguments[2]);
            } else if (action.equals("showview") || action.equals("hideview") || action.equals("toggleview")) {
                return getStartingArgument(Collections.singletonList("-s"), arguments[2]);
            } else {
                return Collections.emptyList();
            }
        }
        if (arguments.length == 4) {
            if (targeting) {
                return getStartingArgument(Collections.singletonList("-s"), arguments[3]);
            } else {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }
}