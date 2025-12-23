package me.neznamy.tab.platforms.fabric;

import me.neznamy.tab.platforms.fabric.hook.PermissionsAPIHook;
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
public class FabricTabCommand extends FabricCommand {

    /**
     * Constructs new instance with given command name.
     *
     * @param   commandName
     *         Command name
     */
    public FabricTabCommand(@NotNull String commandName) {
        super(commandName);
    }

    @Override
    public int execute(@NotNull CommandSourceStack source, @NotNull String[] args) {
        if (TAB.getInstance().isPluginDisabled()) {
            boolean hasReloadPermission = PermissionsAPIHook.hasPermission(source, TabConstants.Permission.COMMAND_RELOAD);
            boolean hasAdminPermission = PermissionsAPIHook.hasPermission(source, TabConstants.Permission.COMMAND_ALL);
            for (TabComponent message : TAB.getInstance().getDisabledCommand().execute(args, hasReloadPermission, hasAdminPermission)) {
                source.sendSystemMessage(message.convert());
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
