package me.neznamy.tab.platforms.krypton;

import com.google.inject.Inject;
import lombok.Getter;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.kryptonmc.api.Server;
import org.kryptonmc.api.command.CommandMeta;
import org.kryptonmc.api.event.Event;
import org.kryptonmc.api.event.EventFilter;
import org.kryptonmc.api.event.EventNode;
import org.kryptonmc.api.event.Listener;
import org.kryptonmc.api.event.server.ServerStartEvent;
import org.kryptonmc.api.event.server.ServerStopEvent;
import org.kryptonmc.api.plugin.annotation.Dependency;
import org.kryptonmc.api.plugin.annotation.Plugin;

import java.nio.file.Path;

/**
 * Main class for Krypton platform
 */
@Plugin(
        id = TabConstants.PLUGIN_ID,
        name = TabConstants.PLUGIN_NAME,
        version = TabConstants.PLUGIN_VERSION,
        description = TabConstants.PLUGIN_DESCRIPTION,
        authors = {TabConstants.PLUGIN_AUTHOR, "BomBardyGamer"},
        dependencies = {
                @Dependency(id = "luckperms", optional = true),
                @Dependency(id = "viaversion", optional = true)
        }
)
public class KryptonTAB {

    @Inject @Getter private Server server;
    @Inject private EventNode<Event> pluginEventNode;
    @Inject @Getter private Path folder;
    @Getter private final EventNode<Event> eventNode = EventNode.Companion.filteredForEvent("tab_events", EventFilter.ALL,
            (it) -> !TAB.getInstance().isPluginDisabled());

    @Listener
    public final void onStart(ServerStartEvent event) {
        pluginEventNode.addChild(eventNode);
        eventNode.registerListeners(new KryptonEventListener());
        server.getCommandManager().register(new KryptonTabCommand(), CommandMeta.builder(TabConstants.COMMAND_BACKEND).build());
        TAB.setInstance(new TAB(new KryptonPlatform(this), ProtocolVersion.fromNetworkId(server.getPlatform().protocolVersion()),
                server.getPlatform().version(), folder.toFile()));
        TAB.getInstance().load();
    }

    @Listener
    public final void onStop(ServerStopEvent event) {
        TAB.getInstance().unload();
    }
}
