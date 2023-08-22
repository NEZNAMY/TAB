package me.neznamy.tab.platforms.bukkit;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Command handler for /tab command
 */
public class BukkitTabCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (TAB.getInstance().isPluginDisabled()) {
            for (String message : TAB.getInstance().getDisabledCommand().execute(args, sender.hasPermission(TabConstants.Permission.COMMAND_RELOAD), sender.hasPermission(TabConstants.Permission.COMMAND_ALL))) {
                sender.sendMessage(EnumChatFormat.color(message));
            }
        } else {
            TabPlayer p = null;
            if (sender instanceof Player) {
                p = TAB.getInstance().getPlayer(((Player)sender).getUniqueId());
                if (p == null) return true; //player not loaded correctly
            }
            TAB.getInstance().getCommand().execute(p, args);
        }
        return false;
    }

    @Override
    @NotNull
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        TabPlayer p = null;
        if (sender instanceof Player) {
            p = TAB.getInstance().getPlayer(((Player)sender).getUniqueId());
            if (p == null) return Collections.emptyList(); //player not loaded correctly
        }
        return TAB.getInstance().getCommand().complete(p, args);
    }
}