package me.neznamy.tab.platforms.krypton;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.kryptonmc.api.command.Sender;
import org.kryptonmc.api.command.SimpleCommand;
import org.kryptonmc.api.entity.player.Player;

import java.util.Collections;
import java.util.List;

public class KryptonTabCommand implements SimpleCommand {

    @Override
    public void execute(@NotNull Sender sender, String[] args) {
        if (TAB.getInstance().isPluginDisabled()) {
            boolean canReload = sender.hasPermission(TabConstants.Permission.COMMAND_RELOAD);
            boolean isAdmin = sender.hasPermission(TabConstants.Permission.COMMAND_ALL);
            for (String message : TAB.getInstance().getDisabledCommand().execute(args, canReload, isAdmin)) {
                sender.sendMessage(Component.text(message));
            }
            return;
        }
        TabPlayer player = null;
        if (sender instanceof Player) {
            player = TAB.getInstance().getPlayer(((Player) sender).getUuid());
            if (player == null) return;
        }
        TAB.getInstance().getCommand().execute(player, args);
    }

    @Override
    public @NotNull List<String> suggest(@NotNull Sender sender, String[] args) {
        TabPlayer player = null;
        if (sender instanceof Player) {
            player = TAB.getInstance().getPlayer(((Player) sender).getUuid());
            if (player == null) return Collections.emptyList();
        }
        return TAB.getInstance().getCommand().complete(player, args);
    }
}
