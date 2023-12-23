package me.neznamy.tab.platforms.bungeecord;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * Command handler for plugin's command on BungeeCord.
 */
public class BungeeTabCommand extends Command implements TabExecutor {

    /**
     * Constructs new instance
     */
    public BungeeTabCommand() {
        super(TabConstants.COMMAND_PROXY, null);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (TAB.getInstance().isPluginDisabled()) {
            for (String message : TAB.getInstance().getDisabledCommand().execute(args, sender.hasPermission(TabConstants.Permission.COMMAND_RELOAD), sender.hasPermission(TabConstants.Permission.COMMAND_ALL))) {
                sender.sendMessage(new TextComponent(EnumChatFormat.color(message)));
            }
        } else {
            TabPlayer p = null;
            if (sender instanceof ProxiedPlayer) {
                p = TAB.getInstance().getPlayer(((ProxiedPlayer)sender).getUniqueId());
                if (p == null) return; //player not loaded correctly
            }
            TAB.getInstance().getCommand().execute(p, args);
        }
    }

    @Override
    @NotNull
    public Iterable<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        TabPlayer p = null;
        if (sender instanceof ProxiedPlayer) {
            p = TAB.getInstance().getPlayer(((ProxiedPlayer)sender).getUniqueId());
            if (p == null) return Collections.emptyList(); //player not loaded correctly
        }
        return TAB.getInstance().getCommand().complete(p, args);
    }
}
