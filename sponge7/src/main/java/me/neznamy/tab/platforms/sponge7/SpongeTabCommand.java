package me.neznamy.tab.platforms.sponge7;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;

public class SpongeTabCommand {

    public @NotNull CommandResult executeCommand(CommandSource source, CommandContext context) {
        String[] args = context.<String>getOne(Text.of("arguments")).orElse("").split(" ");

        if (TAB.getInstance().isPluginDisabled()) {
            boolean hasReloadPermission = source.hasPermission(TabConstants.Permission.COMMAND_RELOAD);
            boolean hasAdminPermission = source.hasPermission(TabConstants.Permission.COMMAND_ALL);
            List<String> messages = TAB.getInstance().getDisabledCommand().execute(args, hasReloadPermission, hasAdminPermission);

            for (String message : messages) {
                source.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(message));
            }
            return CommandResult.success();
        }

        TabPlayer player = null;
        if (source instanceof Player) {
            player = TAB.getInstance().getPlayer(((Player) source).getUniqueId());
            if (player == null) return CommandResult.success(); // Player not loaded correctly
        }
        TAB.getInstance().getCommand().execute(player, args);
        return CommandResult.success();
    }
}
