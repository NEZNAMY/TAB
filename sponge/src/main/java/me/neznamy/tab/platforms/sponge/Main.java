package me.neznamy.tab.platforms.sponge;

import com.google.inject.Inject;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Plugin("tab")
public final class Main {

    @Inject private Game game;
    @Inject @ConfigDir(sharedRoot = false) private File configDir;
    @Inject private Logger logger;
    @Inject private PluginContainer container;

    @Listener
    public void onServerStart(final StartingEngineEvent<Server> event) {
        final SystemSubject console = event.game().systemSubject();
        final String version = game.platform().minecraftVersion().name();
        console.sendMessage(Component.text("[TAB] Server version: " + version));
        final SpongePlatform platform = new SpongePlatform();
        TAB.setInstance(new TAB(platform, ProtocolVersion.fromFriendlyName(version), version, configDir, logger));
        game.eventManager().registerListeners(container, new SpongeEventListener());
        TAB.getInstance().load();
    }

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Command.Raw> event) {
        event.register(container, new TABCommand(), "tab");
    }

    @Listener
    public void onServerStop(final StoppingEngineEvent<Server> event) {
        if (TAB.getInstance() != null) TAB.getInstance().unload();
    }

    private static final class TABCommand implements Command.Raw {

        @Override
        public CommandResult process(CommandCause cause, ArgumentReader.Mutable arguments) {
            final String[] args = arguments.input().split(" ");

            if (TabAPI.getInstance().isPluginDisabled()) {
                final boolean hasReloadPermission = cause.hasPermission(TabConstants.Permission.COMMAND_RELOAD);
                final boolean hasAdminPermission = cause.hasPermission(TabConstants.Permission.COMMAND_ALL);

                final List<String> messages = TAB.getInstance().getDisabledCommand().execute(args, hasReloadPermission, hasAdminPermission);
                for (final String message : messages) {
                    cause.sendMessage(Identity.nil(), LegacyComponentSerializer.legacySection().deserialize(message));
                }
                return CommandResult.success();
            }

            TabPlayer player = null;
            final Player source = cause.context().get(EventContextKeys.PLAYER).orElse(null);
            if (source != null) {
                player = TAB.getInstance().getPlayer(source.uniqueId());
                if (player == null) return CommandResult.success(); // Player not loaded correctly
            }
            TAB.getInstance().getCommand().execute(player, args);
            return CommandResult.success();
        }

        @Override
        public List<CommandCompletion> complete(CommandCause cause, ArgumentReader.Mutable arguments) {
            TabPlayer player = null;
            final Player source = cause.context().get(EventContextKeys.PLAYER).orElse(null);
            if (source != null) {
                player = TAB.getInstance().getPlayer(source.uniqueId());
                if (player == null) return Collections.emptyList(); // Player not loaded correctly
            }
            return TAB.getInstance().getCommand().complete(player, arguments.input().split(" ")).stream().map(CommandCompletion::of).collect(Collectors.toList());
        }

        @Override
        public boolean canExecute(CommandCause cause) {
            return true;
        }

        @Override
        public Optional<Component> shortDescription(CommandCause cause) {
            return Optional.empty();
        }

        @Override
        public Optional<Component> extendedDescription(CommandCause cause) {
            return Optional.empty();
        }

        @Override
        public Component usage(CommandCause cause) {
            return Component.empty();
        }
    }
}
