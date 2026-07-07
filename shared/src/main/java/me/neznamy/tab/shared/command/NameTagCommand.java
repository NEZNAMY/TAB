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
 *   /tab nametag show/opaque/hide/toggle [player] [viewer] [-s]
 *   /tab nametag showview/opaqueview/hideview/toggleview [viewer] [-s]
 */
public class NameTagCommand extends SubCommand {

    private static final List<String> TARGET_ACTIONS = Arrays.asList("show", "opaque", "hide", "toggle");
    private static final List<String> VIEW_ACTIONS = Arrays.asList("showview", "opaqueview", "hideview", "toggleview");
    private static final List<String> ALL_ACTIONS = Arrays.asList("show", "opaque", "hide", "toggle", "showview", "opaqueview", "hideview", "toggleview");

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
        if (TARGET_ACTIONS.contains(action)) {
            processTarget(teams, sender, action, Arrays.copyOfRange(args, 1, args.length));
        } else if (VIEW_ACTIONS.contains(action)) {
            processView(teams, sender, action,  Arrays.copyOfRange(args, 1, args.length));
        } else {
            sendMessages(sender, getMessages().getNameTagHelpMenu());
        }
    }

    private void processTarget(@NotNull NameTag teams, @Nullable TabPlayer sender, @NotNull String action, @NotNull String[] args) {
        boolean silent = args.length > 0 && args[args.length - 1].equals("-s");
        String[] effectiveArgs = silent ? Arrays.copyOf(args, args.length - 1) : args;

        TabPlayer player = effectiveArgs.length >= 1 ? TAB.getInstance().getPlayer(effectiveArgs[0]) : sender;
        if (player == null) {
            sendMessage(sender, effectiveArgs.length == 0 ? getMessages().getNameTagNoArgFromConsole() : getMessages().getPlayerNotFound(effectiveArgs[0]));
            return;
        }

        TabPlayer viewer = effectiveArgs.length >= 2 ? TAB.getInstance().getPlayer(effectiveArgs[1]) : null;
        if (effectiveArgs.length >= 2 && viewer == null) {
            sendMessage(sender, getMessages().getPlayerNotFound(effectiveArgs[1]));
            return;
        }

        String permission = sender == viewer ? TabConstants.Permission.COMMAND_NAMETAG_VISIBILITY : TabConstants.Permission.COMMAND_NAMETAG_VISIBILITY_OTHER;
        if (!hasPermission(sender, permission)) {
            sendMessage(sender, getMessages().getNoPermission());
            return;
        }

        if (action.equals("opaque")) {
            if (viewer != null) {
                teams.getVisibilityManager().setOpaqueNameTag(player, viewer, "Processing command (opaque)", !silent);
            } else {
                teams.getVisibilityManager().setOpaqueNameTag(player, null, "Processing command (opaque)", !silent);
            }
        } else if (action.equals("show")) {
            if (viewer != null) {
                teams.getVisibilityManager().showNameTag(player, viewer, NameTagInvisibilityReason.HIDE_COMMAND, "Processing command (show)", !silent);
            } else {
                teams.getVisibilityManager().showNameTag(player, NameTagInvisibilityReason.HIDE_COMMAND, "Processing command (show)", !silent);
            }
        } else if (action.equals("hide")) {
            if (viewer != null) {
                teams.getVisibilityManager().hideNameTag(player, viewer, NameTagInvisibilityReason.HIDE_COMMAND, "Processing command (hide)", !silent);
            } else {
                teams.getVisibilityManager().hideNameTag(player, NameTagInvisibilityReason.HIDE_COMMAND, "Processing command (hide)", !silent);
            }
        } else if (action.equals("toggle")) {
            if (viewer != null) {
                teams.getVisibilityManager().toggleNameTag(player, viewer, NameTagInvisibilityReason.HIDE_COMMAND, "Processing command (toggle)", !silent);
            } else {
                teams.getVisibilityManager().toggleNameTag(player, NameTagInvisibilityReason.HIDE_COMMAND, "Processing command (toggle)", !silent);
            }
        }
    }

    private void processView(@NotNull NameTag teams, @Nullable TabPlayer sender, @NotNull String action, @NotNull String[] args) {
        boolean silent = args.length > 0 && args[args.length - 1].equals("-s");
        String[] effectiveArgs = silent ? Arrays.copyOf(args, args.length - 1) : args;
        TabPlayer viewer = effectiveArgs.length >= 1 ? TAB.getInstance().getPlayer(effectiveArgs[0]) : sender;

        if (viewer == null) {
            sendMessage(sender, effectiveArgs.length == 0 ? getMessages().getNameTagNoArgFromConsole() : getMessages().getPlayerNotFound(effectiveArgs[0]));
            return;
        }

        String permission = sender == viewer ? TabConstants.Permission.COMMAND_NAMETAG_VIEW : TabConstants.Permission.COMMAND_NAMETAG_VIEW_OTHER;
        if (!hasPermission(sender, permission)) {
            sendMessage(sender, getMessages().getNoPermission());
            return;
        }

        if (action.equals("showview")) {
            teams.showNameTagVisibilityView(viewer, !silent);
            teams.getVisibilityManager().clearOpaqueNameTagView(viewer, "Processing command (showview)");
        } else if (action.equals("opaqueview")) {
            teams.getVisibilityManager().setOpaqueNameTagView(viewer, "Processing command (opaqueview)", !silent);
        } else if (action.equals("hideview")) {
            teams.hideNameTagVisibilityView(viewer, !silent);
            teams.getVisibilityManager().clearOpaqueNameTagView(viewer, "Processing command (hideview)");
        } else {
            teams.toggleNameTagVisibilityView(viewer, !silent);
            teams.getVisibilityManager().clearOpaqueNameTagView(viewer, "Processing command (toggleview)");
        }
    }

    @Override
    @NotNull
    public List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
        if (arguments.length == 1) return getStartingArgument(ALL_ACTIONS, arguments[0]);
        if (arguments.length == 2) return getOnlinePlayers(arguments[1]);
        String action = arguments[0].toLowerCase(Locale.US);
        boolean targeting = TARGET_ACTIONS.contains(action);
        if (arguments.length == 3) {
            if (targeting) {
                return getOnlinePlayers(arguments[2]);
            } else if (VIEW_ACTIONS.contains(action)) {
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
