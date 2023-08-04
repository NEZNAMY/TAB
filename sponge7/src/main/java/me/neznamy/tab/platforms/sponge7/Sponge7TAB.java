package me.neznamy.tab.platforms.sponge7;

import com.google.inject.Inject;
import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;

import java.io.File;

@Plugin(
        id = TabConstants.PLUGIN_ID,
        name = TabConstants.PLUGIN_NAME,
        version = TabConstants.PLUGIN_VERSION,
        description = TabConstants.PLUGIN_DESCRIPTION,
        url = TabConstants.PLUGIN_WEBSITE,
        authors = {TabConstants.PLUGIN_AUTHOR}
)
@Getter
public class Sponge7TAB {

    @Inject @ConfigDir(sharedRoot = false) private File configDir;
    @Inject private Logger logger;

    @Listener
    public void onServerStart(@Nullable GameStartedServerEvent event) {
        TAB.create(new SpongePlatform(this));
    }

    @Listener
    public void onServerStop(@Nullable GameStoppedServerEvent event) {
        TAB.getInstance().unload();
    }
}
