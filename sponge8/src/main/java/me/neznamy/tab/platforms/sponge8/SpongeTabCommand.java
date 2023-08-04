package me.neznamy.tab.platforms.sponge8;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventContextKeys;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpongeTabCommand implements Command.Raw {

    @Override
    @NotNull
    public CommandResult process(@NotNull CommandCause cause, @NotNull ArgumentReader.Mutable arguments) {
        String[] args = arguments.input().split(" ");

        if (TAB.getInstance().isPluginDisabled()) {
            boolean hasReloadPermission = cause.hasPermission(TabConstants.Permission.COMMAND_RELOAD);
            boolean hasAdminPermission = cause.hasPermission(TabConstants.Permission.COMMAND_ALL);
            List<String> messages = TAB.getInstance().getDisabledCommand().execute(args, hasReloadPermission, hasAdminPermission);
            for (String message : messages) {
                cause.sendMessage(Identity.nil(), LegacyComponentSerializer.legacySection().deserialize(message));
            }
            return CommandResult.success();
        }
        TabPlayer player = null;
        if (cause.audience() instanceof Player) {
            player = TAB.getInstance().getPlayer(((Player) cause.audience()).uniqueId());
            if (player == null) return CommandResult.success(); // Player not loaded correctly
        }
        TAB.getInstance().getCommand().execute(player, args);
        return CommandResult.success();
    }

    @Override
    @NotNull
    public List<CommandCompletion> complete(@NotNull CommandCause cause, @NotNull ArgumentReader.Mutable arguments) {
        TabPlayer player = null;
        Player source = cause.context().get(EventContextKeys.PLAYER).orElse(null);
        if (source != null) {
            player = TAB.getInstance().getPlayer(source.uniqueId());
            if (player == null) return Collections.emptyList(); // Player not loaded correctly
        }
        String[] args = arguments.input().split(" ");
        if (arguments.input().endsWith(" ")) {
            args = Arrays.copyOf(args, args.length+1);
            args[args.length-1] = "";
        }
        return TAB.getInstance().getCommand().complete(player, args).stream().map(CommandCompletion::of).collect(Collectors.toList());
    }

    @Override
    public boolean canExecute(@NotNull CommandCause cause) {
        return true;
    }

    @Override
    @NotNull
    public Optional<Component> shortDescription(@NotNull CommandCause cause) {
        return Optional.empty();
    }

    @Override
    @NotNull
    public Optional<Component> extendedDescription(@NotNull CommandCause cause) {
        return Optional.empty();
    }

    @Override
    @NotNull
    public Component usage(@NotNull CommandCause cause) {
        return Component.empty();
    }
}
