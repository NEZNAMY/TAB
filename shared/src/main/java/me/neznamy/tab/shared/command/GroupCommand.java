package me.neznamy.tab.shared.command;

import java.util.*;
import java.util.stream.Collectors;

import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handler for "/tab group" subcommand
 */
public class GroupCommand extends PropertyCommand {
    
    /**
     * Constructs new instance
     */
    public GroupCommand() {
        super("group");
    }

    @Override
    public void execute(@Nullable TabPlayer sender, @NotNull String[] args) {
        //<name> <property> [value...]
        if (args.length == 0) {
            help(sender);
            return;
        }
        if (args.length == 1) {
            if (hasPermission(sender, TabConstants.Permission.COMMAND_GROUP_INFO)) {
                sendGroupInfo(sender, args[0]);
            } else {
                sendMessage(sender, getMessages().getNoPermission());
            }
            return;
        }
        if ("remove".equalsIgnoreCase(args[1])) {
            remove(sender, args[0]);
            return;
        }
        trySaveEntity(sender, args);
    }

    private void remove(@Nullable TabPlayer sender, @NotNull String group) {
        if (hasPermission(sender, TabConstants.Permission.COMMAND_DATA_REMOVE)) {
            TAB.getInstance().getConfiguration().getGroups().remove(group);
            for (TabPlayer pl : TAB.getInstance().getOnlinePlayers()) {
                if (pl.getGroup().equals(group) || TabConstants.DEFAULT_GROUP.equals(group)) {
                    pl.forceRefresh();
                }
            }
            sendMessage(sender, getMessages().getGroupDataRemoved(group));
        } else {
            sendMessage(sender, getMessages().getNoPermission());
        }
    }

    private void sendGroupInfo(@Nullable TabPlayer sender, @NotNull String group) {
        sendMessage(sender, "&f=== Group &9" + group + "&f ===");
        for (Map.Entry<String, Object> entry : TAB.getInstance().getConfiguration().getGroups().getGlobalSettings(group).entrySet()) {
            sendRawMessage(sender, "  " + entry.getKey() + ": " + entry.getValue());
        }
        for (Map.Entry<String, Map<String, Object>> entry : TAB.getInstance().getConfiguration().getGroups().getPerWorldSettings(group).entrySet()) {
            if (entry.getValue() == null) continue;
            sendMessage(sender, "&6World " + entry.getKey() + ":&e");
            for (Map.Entry<String, Object> properties : entry.getValue().entrySet()) {
                sendRawMessage(sender, "  " + properties.getKey() + ": " + properties.getValue());
            }
        }
        for (Map.Entry<String, Map<String, Object>> entry : TAB.getInstance().getConfiguration().getGroups().getPerServerSettings(group).entrySet()) {
            if (entry.getValue() == null) continue;
            sendMessage(sender, "&3Server " + entry.getKey() + ":&b");
            for (Map.Entry<String, Object> properties : entry.getValue().entrySet()) {
                sendRawMessage(sender, "  " + properties.getKey() + ": " + properties.getValue());
            }
        }
    }

    @Override
    public void saveEntity(@Nullable TabPlayer sender, @NotNull String group, @NotNull String type, @NotNull String value, @Nullable String server, @Nullable String world) {
        if (!value.isEmpty()) {
            sendMessage(sender, getMessages().getGroupValueAssigned(type, value, group));
        } else {
            sendMessage(sender, getMessages().getGroupValueRemoved(type, group));
        }
        String[] property = TAB.getInstance().getConfiguration().getGroups().getProperty(group, type, server, world);
        if (property.length > 0 && String.valueOf(value.isEmpty() ? null : value).equals(String.valueOf(property[0]))) return;
        TAB.getInstance().getConfiguration().getGroups().setProperty(group, type, server, world, value.isEmpty() ? null : value);
        for (TabPlayer pl : TAB.getInstance().getOnlinePlayers()) {
            if (pl.getGroup().equals(group) || TabConstants.DEFAULT_GROUP.equals(group)) {
                pl.forceRefresh();
            }
        }
    }

    @Override
    public @NotNull List<String> complete(@Nullable TabPlayer sender, @NotNull String[] arguments) {
        if (arguments.length == 1) {
            Set<String> groups = new HashSet<>(TAB.getInstance().getConfiguration().getGroups().getAllEntries());
            groups.add(TabConstants.DEFAULT_GROUP);
            return groups.stream().filter(group -> group.toLowerCase().startsWith(arguments[0].toLowerCase())).collect(Collectors.toList());
        }
        return super.complete(sender, arguments);
    }
}