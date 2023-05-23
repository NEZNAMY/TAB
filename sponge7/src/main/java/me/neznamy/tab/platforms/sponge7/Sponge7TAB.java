package me.neznamy.tab.platforms.sponge7;

import com.google.inject.Inject;
import lombok.Getter;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.util.ComponentCache;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.File;

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
    @Inject @Getter private Logger logger;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        SpongeTabCommand cmd = new SpongeTabCommand();
        game.getCommandManager().register(this, CommandSpec.builder()
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("arguments")))
                .executor(cmd::executeCommand)
                .build(), TabConstants.COMMAND_BACKEND);
        game.getEventManager().registerListeners(this, new SpongeEventListener());
        String version = game.getPlatform().getMinecraftVersion().getName();
        TAB.setInstance(new TAB(new SpongePlatform(this), ProtocolVersion.fromFriendlyName(version), version, configDir));
        TAB.getInstance().load();
    }

    @Listener
    public void onServerStop(GameStoppedServerEvent event) {
        TAB.getInstance().unload();
    }
}
