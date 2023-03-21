package me.neznamy.tab.platforms.sponge8;

import com.google.inject.Inject;
import lombok.Getter;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.util.ComponentCache;
import me.neznamy.tab.shared.TAB;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.logging.log4j.Logger;
import org.bstats.charts.SimplePie;
import org.bstats.sponge.Metrics;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
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

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Sponge8TAB {

    @Getter private static final ComponentCache<IChatBaseComponent, net.minecraft.network.chat.Component> componentCache = new ComponentCache<>(10000,
            (component, clientVersion) -> net.minecraft.network.chat.Component.Serializer.fromJson(component.toString(clientVersion)));

    @Getter private static final ComponentCache<IChatBaseComponent, Component> adventureCache = new ComponentCache<>(10000,
            (component, clientVersion) -> GsonComponentSerializer.gson().deserialize(component.toString(clientVersion)));

    @Inject private Game game;
    @Inject @ConfigDir(sharedRoot = false) private Path configDir;
    @Inject private Logger logger;
    @Inject @Getter private PluginContainer container;
    private final Metrics metrics;

    @Inject
    public Sponge8TAB(Metrics.Factory metricsFactory) {
        this.metrics = metricsFactory.make(17732);
    }

    @Listener
    public void onServerStart(StartingEngineEvent<Server> event) {
        String version = game.platform().minecraftVersion().name();
        TAB.setInstance(new TAB(new SpongePlatform(this), ProtocolVersion.fromFriendlyName(version), version, configDir.toFile(), logger));
        game.eventManager().registerListeners(container, new SpongeEventListener());
        TAB.getInstance().load();
        metrics.addCustomChart(new SimplePie(TabConstants.MetricsChart.UNLIMITED_NAME_TAG_MODE_ENABLED, () -> TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.UNLIMITED_NAME_TAGS) ? "Yes" : "No"));
        metrics.addCustomChart(new SimplePie(TabConstants.MetricsChart.SERVER_VERSION, () -> TAB.getInstance().getServerVersion().getFriendlyName()));
    }

    @Listener
    public void onRegisterCommands(RegisterCommandEvent<Command.Raw> event) {
        event.register(container, new TABCommand(), "tab");
    }

    @Listener
    public void onServerStop(StoppingEngineEvent<Server> event) {
        TAB.getInstance().unload();
    }

    private static class TABCommand implements Command.Raw {

        @Override
        public CommandResult process(CommandCause cause, ArgumentReader.Mutable arguments) {
            String[] args = arguments.input().split(" ");

            if (TabAPI.getInstance().isPluginDisabled()) {
                boolean hasReloadPermission = cause.hasPermission(TabConstants.Permission.COMMAND_RELOAD);
                boolean hasAdminPermission = cause.hasPermission(TabConstants.Permission.COMMAND_ALL);

                List<String> messages = TAB.getInstance().getDisabledCommand().execute(args, hasReloadPermission, hasAdminPermission);
                for (String message : messages) {
                    cause.sendMessage(Identity.nil(), LegacyComponentSerializer.legacySection().deserialize(message));
                }
                return CommandResult.success();
            }

            TabPlayer player = null;
            Audience audience = cause.audience();
            if (audience instanceof Player) {
                player = TAB.getInstance().getPlayer(((Player) audience).uniqueId());
                if (player == null) return CommandResult.success(); // Player not loaded correctly
            }
            TAB.getInstance().getCommand().execute(player, args);
            return CommandResult.success();
        }

        @Override
        public List<CommandCompletion> complete(CommandCause cause, ArgumentReader.Mutable arguments) {
            TabPlayer player = null;
            Player source = cause.context().get(EventContextKeys.PLAYER).orElse(null);
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
