package me.neznamy.tab.platforms.krypton

import com.google.inject.Inject
import me.neznamy.tab.api.ProtocolVersion
import me.neznamy.tab.api.TabConstants
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.shared.TAB
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.kryptonmc.api.Server
import org.kryptonmc.api.command.CommandMeta
import org.kryptonmc.api.command.Sender
import org.kryptonmc.api.command.SimpleCommand
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.api.event.Event
import org.kryptonmc.api.event.EventFilter
import org.kryptonmc.api.event.EventNode
import org.kryptonmc.api.event.Listener
import org.kryptonmc.api.event.server.ServerStartEvent
import org.kryptonmc.api.event.server.ServerStopEvent
import org.kryptonmc.api.plugin.annotation.DataFolder
import org.kryptonmc.api.plugin.annotation.Dependency
import org.kryptonmc.api.plugin.annotation.Plugin
import java.nio.file.Path
import java.util.*

/**
 * Main class for Krypton platform
 */
@Plugin(
    "tab",
    "TAB",
    TabConstants.PLUGIN_VERSION,
    "An all-in-one solution that works",
    ["NEZNAMY", "BomBardyGamer"],
    [
        Dependency("luckperms", true),
        Dependency("viaversion", true)
    ]
)
class Main @Inject constructor(
    val server: Server,
    private val pluginEventNode: EventNode<Event>,
    @DataFolder val folder: Path
) {

    val eventNode: EventNode<Event> = EventNode.filteredForEvent("tab_events", EventFilter.ALL) { !TAB.getInstance().isPluginDisabled }

    @Listener
    fun onStart(event: ServerStartEvent) {
        pluginEventNode.addChild(eventNode)

        val tab = TAB(
            KryptonPlatform(this),
            ProtocolVersion.fromNetworkId(server.platform.protocolVersion),
            server.platform.version,
            folder.toFile(),
            null
        )
        TAB.setInstance(tab)

        if (TAB.getInstance().serverVersion == ProtocolVersion.UNKNOWN_SERVER_VERSION) {
            server.console.sendMessage(Component.text(
                "[TAB] Unknown server version: ${server.platform.version}! Plugin may not work correctly",
                NamedTextColor.RED
            ))
        }

        eventNode.registerListeners(KryptonEventListener(this))
        server.commandManager.register(KryptonTABCommand(), CommandMeta.builder("tab").build())
        TAB.getInstance().load()
    }

    @Listener
    fun onStop(event: ServerStopEvent) {
        TAB.getInstance()?.unload()
    }

    fun getProtocolVersion(player: Player): Int {
        if (server.pluginManager.isLoaded(TabConstants.Plugin.VIAVERSION.lowercase(Locale.getDefault())))
            return TAB.getInstance().platform.getProtocolVersionVia(player.uuid, player.profile.name, 0)
        return TAB.getInstance().serverVersion.networkId
    }

    class KryptonTABCommand : SimpleCommand {

        override fun execute(sender: Sender, args: Array<String>) {
            if (TAB.getInstance().isPluginDisabled) {
                val canReload = sender.hasPermission("tab.reload")
                val isAdmin = sender.hasPermission("tab.admin")
                TAB.getInstance().disabledCommand.execute(args, canReload, isAdmin).forEach { sender.sendMessage(Component.text(it)) }
                return
            }
            var player: TabPlayer? = null
            if (sender is Player) {
                player = TAB.getInstance().getPlayer(sender.uuid)
                if (player == null) return
            }
            TAB.getInstance().command.execute(player, args)
        }

        override fun suggest(sender: Sender, args: Array<String>): List<String> {
            var player: TabPlayer? = null
            if (sender is Player) {
                player = TAB.getInstance().getPlayer(sender.uuid)
                if (player == null) return emptyList()
            }
            return TAB.getInstance().command.complete(player, args)
        }
    }
}
