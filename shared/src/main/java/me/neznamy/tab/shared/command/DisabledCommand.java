package me.neznamy.tab.shared.command;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Command processor when TAB is disabled due to broken configuration file
 */
public class DisabledCommand {

    /**
     * Performs command and return messages to be sent back
     *
     * @param   args
     *          command arguments
     * @param   hasReloadPermission
     *          if player has permission to reload or not
     * @param   hasAdminPermission
     *          if player has admin permission or not
     * @return  list of messages to send back
     */
    public List<String> execute(@NotNull String[] args, boolean hasReloadPermission, boolean hasAdminPermission) {
        List<String> messages = new ArrayList<>();
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (hasReloadPermission) {
                messages.add(TAB.getInstance().load());
            } else {
                //cannot take message from file when syntax is broken
                messages.add("&cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
            }
        } else {
            if (hasAdminPermission) {
                String command = "/" + (TAB.getInstance().getServerVersion() != ProtocolVersion.PROXY ? TabConstants.COMMAND_BACKEND : TabConstants.COMMAND_PROXY);
                messages.add("&m                                                                                ");
                messages.add(" &cPlugin is disabled due to an error. Check console for more details.");
                messages.add(" &8>> &3&l" + command + " reload");
                messages.add("      - &7Reloads plugin and config");
                messages.add("&m                                                                                ");
            }
        }
        return messages;
    }
}