package me.neznamy.tab.shared.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.platform.TabPlayer;

public abstract class PropertyCommand extends SubCommand {

    protected PropertyCommand(String name) {
        super(name, null);
    }

    @Override
    public List<String> complete(TabPlayer sender, String[] arguments) {
        if (arguments.length != 2) return new ArrayList<>();
        return getStartingArgument(getAllProperties(), arguments[1]);
    }

    protected void help(TabPlayer sender) {
        sendMessage(sender, "&cSyntax&8: &3&l/tab &9group&3/&9player &3<name> &9<property> &3<value...>");
        sendMessage(sender, "&7Valid Properties are:");
        sendMessage(sender, " - &9tabprefix&3/&9tabsuffix&3/&9customtabname");
        sendMessage(sender, " - &9tagprefix&3/&9tagsuffix&3/&9customtagname");
        sendMessage(sender, " - &9belowname&3/&9abovename");
    }

    protected void trySaveEntity(TabPlayer sender, String[] args) {
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

    public abstract void saveEntity(TabPlayer sender, String name, String property, String value, String server, String world);
}