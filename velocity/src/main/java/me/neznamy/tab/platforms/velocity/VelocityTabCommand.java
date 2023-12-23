package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Command handler for plugin's command for Velocity.
 */
public class VelocityTabCommand implements SimpleCommand {

    @Override
    public void execute(@NotNull Invocation invocation) {
        CommandSource sender = invocation.source();
        if (TAB.getInstance().isPluginDisabled()) {
            for (String message : TAB.getInstance().getDisabledCommand().execute(invocation.arguments(), sender.hasPermission(TabConstants.Permission.COMMAND_RELOAD), sender.hasPermission(TabConstants.Permission.COMMAND_ALL))) {
                sender.sendMessage(Component.text(EnumChatFormat.color(message)));
            }
        } else {
            TabPlayer p = null;
            if (sender instanceof Player) {
                p = TAB.getInstance().getPlayer(((Player)sender).getUniqueId());
                if (p == null) return; //player not loaded correctly
            }
            TAB.getInstance().getCommand().execute(p, invocation.arguments());
        }
    }

    @Override
    @NotNull
    public List<String> suggest(@NotNull Invocation invocation) {
        TabPlayer p = null;
        if (invocation.source() instanceof Player) {
            p = TAB.getInstance().getPlayer(((Player)invocation.source()).getUniqueId());
            if (p == null) return Collections.emptyList(); //player not loaded correctly
        }
        return TAB.getInstance().getCommand().complete(p, invocation.arguments());
    }
}
