package me.neznamy.tab.platforms.krypton;

import com.google.inject.Inject;
import lombok.Getter;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.shared.TAB;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.kryptonmc.api.Server;
import org.kryptonmc.api.command.CommandMeta;
import org.kryptonmc.api.command.Sender;
import org.kryptonmc.api.command.SimpleCommand;
import org.kryptonmc.api.entity.player.Player;
import org.kryptonmc.api.event.Event;
import org.kryptonmc.api.event.EventFilter;
import org.kryptonmc.api.event.EventNode;
import org.kryptonmc.api.event.Listener;
import org.kryptonmc.api.event.server.ServerStartEvent;
import org.kryptonmc.api.event.server.ServerStopEvent;
import org.kryptonmc.api.plugin.annotation.DataFolder;
import org.kryptonmc.api.plugin.annotation.Dependency;
import org.kryptonmc.api.plugin.annotation.Plugin;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
public class Main {
    
    @Getter private final Server server;
    private final EventNode<Event> pluginEventNode;
    @Getter private final Path folder;
    @Getter private final EventNode<Event> eventNode;

    @Inject
    public Main(Server server, EventNode<Event> pluginEventNode, @DataFolder Path folder) {
        this.server = server;
        this.pluginEventNode = pluginEventNode;
        this.folder = folder;
        this.eventNode = EventNode.Companion.filteredForEvent("tab_events", EventFilter.ALL, (it) -> !TAB.getInstance().isPluginDisabled());
    }

    @Listener
    public final void onStart(ServerStartEvent event) {
        pluginEventNode.addChild(this.eventNode);
        TAB tab = new TAB(
                new KryptonPlatform(this),
                ProtocolVersion.fromNetworkId(this.server.getPlatform().protocolVersion()),
                server.getPlatform().version(),
                folder.toFile(),
                null);
        TAB.setInstance(tab);
        eventNode.registerListeners(new KryptonEventListener(this));
        server.getCommandManager().register(new KryptonTABCommand(), CommandMeta.Companion.builder("tab").build());
        TAB.getInstance().load();
    }

    @Listener
    public final void onStop(ServerStopEvent event) {
        TAB.getInstance().unload();
    }

    public final int getProtocolVersion(Player player) {
        if (server.getPluginManager().isLoaded("ViaVersion".toLowerCase(Locale.ROOT)))
            return ProtocolVersion.getPlayerVersionVia(player.getUuid(), player.getProfile().name());
        return TAB.getInstance().getServerVersion().getNetworkId();
    }

    public static Component toComponent(String text, ProtocolVersion clientVersion) {
        if (text == null || text.length() == 0) return Component.empty();
        return GsonComponentSerializer.gson().deserialize(IChatBaseComponent.optimizedComponent(text).toString(clientVersion));
    }

    public static class KryptonTABCommand implements SimpleCommand {

        @Override
        public void execute(@NotNull Sender sender, String[] args) {
            if (TAB.getInstance().isPluginDisabled()) {
                boolean canReload = sender.hasPermission("tab.reload");
                boolean isAdmin = sender.hasPermission("tab.admin");
                for (String message : TAB.getInstance().getDisabledCommand().execute(args, canReload, isAdmin)) {
                    sender.sendMessage(Component.text(message));
                }
                return;
            }
            TabPlayer player = null;
            if (sender instanceof Player) {
                player = TAB.getInstance().getPlayer(((Player) sender).getUuid());
                if (player == null) return;
            }
            TAB.getInstance().getCommand().execute(player, args);
        }

        @Override
        public @NotNull List<String> suggest(@NotNull Sender sender, String[] args) {
            TabPlayer player = null;
            if (sender instanceof Player) {
                player = TAB.getInstance().getPlayer(((Player) sender).getUuid());
                if (player == null) return Collections.emptyList();
            }
            return TAB.getInstance().getCommand().complete(player, args);
        }
    }
}
