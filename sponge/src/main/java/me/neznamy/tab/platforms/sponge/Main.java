package me.neznamy.tab.platforms.sponge;

import com.google.inject.Inject;
import java.io.File;
import java.util.List;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

@Plugin(id = "tab", name = "TAB", version = TabConstants.PLUGIN_VERSION, description = "An all-in-one solution that works", authors = {"NEZNAMY"})
public final class Main {

    @Inject
    private Game game;
    @Inject
    @ConfigDir(sharedRoot = false)
    private File configDir;
    @Inject
    private Logger logger;

    @Listener
    public void onServerStart(final GameStartedServerEvent event) {
        final String version = game.getPlatform().getMinecraftVersion().getName();
        final ConsoleSource console = game.getServer().getConsole();
        console.sendMessage(Text.of("[TAB] Server version: " + version));

        final SpongePlatform platform = new SpongePlatform(this);
        TAB.setInstance(new TAB(platform, ProtocolVersion.fromFriendlyName(version), version, configDir, logger));
        if (TAB.getInstance().getServerVersion() == ProtocolVersion.UNKNOWN_SERVER_VERSION) {
            console.sendMessage(Text.builder("[TAB] Unknown server version: " + version + "! Plugin may not work correctly.").color(TextColors.RED).build());
        }

        game.getEventManager().registerListeners(this, new SpongeEventListener());
        TAB.getInstance().load();

        final CommandSpec command = CommandSpec.builder()
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("arguments")))
                .executor(this::executeCommand)
                .build();
        game.getCommandManager().register(this, command, "tab");
    }

    @Listener
    public void onServerStop(final GameStoppedServerEvent event) {
        if (TAB.getInstance() != null) TAB.getInstance().unload();
    }

    private CommandResult executeCommand(final CommandSource source, final CommandContext context) {
        final String[] args = context.<String>getOne(Text.of("arguments")).orElse("").split(" ");

        if (TabAPI.getInstance().isPluginDisabled()) {
            final boolean hasReloadPermission = source.hasPermission(TabConstants.Permission.COMMAND_RELOAD);
            final boolean hasAdminPermission = source.hasPermission(TabConstants.Permission.COMMAND_ALL);
            final List<String> messages = TAB.getInstance().getDisabledCommand().execute(args, hasReloadPermission, hasAdminPermission);

            for (final String message : messages) {
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
