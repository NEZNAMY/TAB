package me.neznamy.tab.platforms.sponge7;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Command processor for plugin's command for Sponge 7.
 */
public class SpongeTabCommand extends CommandElement implements CommandExecutor {

    protected SpongeTabCommand() {
        super(null);
    }

    @NotNull
    public CommandResult execute(@NotNull CommandSource source, @NotNull CommandContext context) {
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

    @Override
    @Nullable
    protected Object parseValue(@NotNull CommandSource source, @NotNull CommandArgs args) {
        return null;
    }

    @Override
    @NotNull
    public List<String> complete(@NotNull CommandSource source, @NotNull CommandArgs commandArgs, @NotNull CommandContext context) {
        TabPlayer player = null;
        if (source instanceof Player) {
            player = TAB.getInstance().getPlayer(((Player)source).getUniqueId());
            if (player == null) return Collections.emptyList(); // Player not loaded correctly
        }
        String[] args = commandArgs.getRaw().split(" ");
        if (commandArgs.getRaw().endsWith(" ")) {
            args = Arrays.copyOf(args, args.length+1);
            args[args.length-1] = "";
        }
        return TAB.getInstance().getCommand().complete(player, args);
    }
}
