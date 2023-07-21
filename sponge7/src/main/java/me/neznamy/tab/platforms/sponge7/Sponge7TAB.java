package me.neznamy.tab.platforms.sponge7;

import com.google.inject.Inject;
import lombok.Getter;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
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

    @Inject private Game game;
    @Inject @ConfigDir(sharedRoot = false) private File configDir;
    @Inject @Getter private Logger logger;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        game.getEventManager().registerListeners(this, new SpongeEventListener());
        TAB.setInstance(new TAB(new SpongePlatform(this), ProtocolVersion.fromFriendlyName(game.getPlatform().getMinecraftVersion().getName()), configDir));
        TAB.getInstance().load();
        SpongeTabCommand cmd = new SpongeTabCommand();
        game.getCommandManager().register(this, CommandSpec.builder()
                .arguments(cmd, GenericArguments.remainingJoinedStrings(Text.of("arguments"))) // GenericArguments.none() doesn't work, so rip no-arg
                .executor(cmd)
                .build(), TabConstants.COMMAND_BACKEND);
    }

    @Listener
    public void onServerStop(GameStoppedServerEvent event) {
        TAB.getInstance().unload();
    }
}
