package me.neznamy.tab.platforms.forge;

import me.neznamy.tab.platforms.forge.hook.LuckPermsAPIHook;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Command handler for plugin's command for Fabric.
 */
public class ForgeTabCommand extends ForgeCommand {

    /**
     * Constructs new instance with given command name.
     *
     * @param   commandName
     *         Command name
     */
    public ForgeTabCommand(@NotNull String commandName) {
        super(commandName);
    }

    @Override
    public int execute(@NotNull CommandSourceStack source, @NotNull String[] args) {
        if (TAB.getInstance().isPluginDisabled()) {
            boolean hasReloadPermission = LuckPermsAPIHook.hasPermission(source, TabConstants.Permission.COMMAND_RELOAD);
            boolean hasAdminPermission = LuckPermsAPIHook.hasPermission(source, TabConstants.Permission.COMMAND_ALL);
            for (String message : TAB.getInstance().getDisabledCommand().execute(args, hasReloadPermission, hasAdminPermission)) {
                source.sendSystemMessage(TabComponent.fromColoredText(message).convert());
            }
        } else {
            if (source.getEntity() == null) {
                TAB.getInstance().getCommand().execute(null, args);
            } else {
                TabPlayer player = TAB.getInstance().getPlayer(source.getEntity().getUUID());
                if (player != null) TAB.getInstance().getCommand().execute(player, args);
            }
        }
        return 0;
    }

    @NotNull
    @Override
    public List<String> complete(@NotNull CommandSourceStack sender, @NotNull String[] args) {
        TabPlayer player = null;
        if (sender.getEntity() != null) {
            player = TAB.getInstance().getPlayer(sender.getEntity().getUUID());
            if (player == null) return Collections.emptyList();
        }
        return TAB.getInstance().getCommand().complete(player, args);
    }
}
