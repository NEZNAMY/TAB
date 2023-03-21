package me.neznamy.tab.platforms.sponge7;

import com.google.inject.Inject;
import lombok.Getter;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.util.ComponentCache;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.File;
import java.util.List;

@Plugin(
        id = TabConstants.PLUGIN_ID,
        name = TabConstants.PLUGIN_NAME,
        version = TabConstants.PLUGIN_VERSION,
        description = TabConstants.PLUGIN_DESCRIPTION,
        url = TabConstants.PLUGIN_WEBSITE,
        authors = {TabConstants.PLUGIN_AUTHOR}
)
public class Sponge7TAB {

    @Getter private static final ComponentCache<IChatBaseComponent, Text> textCache = new ComponentCache<>(10000,
            (component, version) -> TextSerializers.JSON.deserialize(component.toString(version)));

    @Inject private Game game;
    @Inject @ConfigDir(sharedRoot = false) private File configDir;
    @Inject private Logger logger;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        String version = game.getPlatform().getMinecraftVersion().getName();
        TAB.setInstance(new TAB(new SpongePlatform(), ProtocolVersion.fromFriendlyName(version), version, configDir, logger));
        game.getEventManager().registerListeners(this, new SpongeEventListener());
        TAB.getInstance().load();
        game.getCommandManager().register(this, CommandSpec.builder()
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("arguments")))
                .executor(this::executeCommand)
                .build(), "tab");
    }

    @Listener
    public void onServerStop(GameStoppedServerEvent event) {
        TAB.getInstance().unload();
    }

    private @NotNull CommandResult executeCommand(CommandSource source, CommandContext context) {
        String[] args = context.<String>getOne(Text.of("arguments")).orElse("").split(" ");

        if (TabAPI.getInstance().isPluginDisabled()) {
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
