package me.neznamy.tab.shared.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PropertyCommand extends SubCommand {

    protected PropertyCommand(String name) {
        super(name, null);
    }

    @Override
    public @NotNull List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
        if (arguments.length != 2) return Collections.emptyList();
        return getStartingArgument(getAllProperties(), arguments[1]);
    }

    protected void help(@Nullable TabPlayer sender) {
        sendMessage(sender, "&cSyntax&8: &3&l/tab &9group&3/&9player &3<name> &9<property> &3<value...>");
        sendMessage(sender, "&7Valid Properties are:");
        sendMessage(sender, " - &9tabprefix&3/&9tabsuffix&3/&9customtabname");
        sendMessage(sender, " - &9tagprefix&3/&9tagsuffix&3/&9customtagname");
        sendMessage(sender, " - &9belowname&3/&9abovename");
    }

    protected void trySaveEntity(@Nullable TabPlayer sender, @NotNull String[] args) {
        String value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        String world = null;
        String server = null;
        if (args[args.length-2].equals("-w")) {
            world = args[args.length-1];
            value = value.startsWith("-w") ? "" : value.substring(0, value.length()-world.length()-4);
        }
        if (args[args.length-2].equals("-s")) {
            server = args[args.length-1];
            value = value.startsWith("-s") ? "" : value.substring(0, value.length()-server.length()-4);
        }
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length()-1);
        }
        String property = args[1].toLowerCase();
        if (getAllProperties().contains(property)) {
            if (hasPermission(sender, TabConstants.Permission.COMMAND_PROPERTY_CHANGE_PREFIX + property)) {
                saveEntity(sender, args[0], property, value, server, world);
                if (extraProperties.contains(property) && !TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.UNLIMITED_NAME_TAGS)) {
                    sendMessage(sender, getMessages().getUnlimitedNametagModeNotEnabled());
                }
            } else {
                sendMessage(sender, getMessages().getNoPermission());
            }
            return;
        }
        help(sender);
    }

    public abstract void saveEntity(@Nullable TabPlayer sender, @NotNull String name, @NotNull String property,
                                    @NotNull String value, @Nullable String server, @Nullable String world);
}