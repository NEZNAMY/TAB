package me.neznamy.tab.platforms.krypton

import com.google.inject.Inject
import com.viaversion.viaversion.api.Via
import me.neznamy.tab.api.ProtocolVersion
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.shared.TAB
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.kryptonmc.api.Server
import org.kryptonmc.api.command.Sender
import org.kryptonmc.api.command.SimpleCommand
import org.kryptonmc.api.command.meta.SimpleCommandMeta
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.api.event.Listener
import org.kryptonmc.api.event.server.ServerStartEvent
import org.kryptonmc.api.event.server.ServerStopEvent
import org.kryptonmc.api.plugin.annotation.DataFolder
import org.kryptonmc.api.plugin.annotation.Dependency
import org.kryptonmc.api.plugin.annotation.Plugin
import java.nio.file.Path

/**
 * Main class for Krypton platform
 */
@Plugin(
    "tab",
    "TAB",
    TAB.PLUGIN_VERSION,
    "An all-in-one solution that works",
    ["NEZNAMY", "BomBardyGamer"],
    [Dependency("luckperms", true), Dependency("viaversion", true)]
)
class Main @Inject constructor(
    val server: Server,
    @DataFolder val folder: Path
) {

    @Listener
    fun onStart(event: ServerStartEvent) {
        TAB.setInstance(TAB(KryptonPlatform(this, folder.toFile()), ProtocolVersion.fromNetworkId(server.platform.protocolVersion)))
        if (TAB.getInstance().serverVersion == ProtocolVersion.UNKNOWN) {
            server.console.sendMessage(Component.text(
                "[TAB] Unknown server version: ${server.platform.version}! Plugin may not work correctly",
                NamedTextColor.RED
            ))
        }
        server.eventManager.register(this, KryptonEventListener(this))
        server.commandManager.register(KryptonTABCommand(), SimpleCommandMeta.builder("tab").build())
        TAB.getInstance().load()
    }

    @Listener
    fun onStop(event: ServerStopEvent) {
        TAB.getInstance()?.unload()
    }

    fun protocolVersion(player: Player): Int {
        if (server.pluginManager.isLoaded("viaversion")) return viaProtocolVersion(player)
        return TAB.getInstance().serverVersion.networkId
    }

    private fun viaProtocolVersion(player: Player, retryLevel: Int = 0): Int {
        try {
            if (retryLevel == 10) {
                TAB.getInstance().debug("Failed to get protocol version of ${player.profile.name} after 10 retries")
                return TAB.getInstance().serverVersion.networkId
            }
            val version = Via.getAPI().getPlayerVersion(player)
            if (version == -1) {
                Thread.sleep(5)
                return viaProtocolVersion(player, retryLevel + 1)
            }
            TAB.getInstance().debug("ViaVersion returned protocol version $version for ${player.profile.name}")
            return version
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            return -1
        } catch (exception: Throwable) {
            TAB.getInstance().errorManager.printError("Failed to get protocol version of ${player.profile.name} using ViaVersion " +
                "v${server.pluginManager.plugin("viaversion")?.description?.version}")
            return TAB.getInstance().serverVersion.networkId
        }
    }

    class KryptonTABCommand : SimpleCommand {

        override fun execute(sender: Sender, args: Array<String>) {
            if (TAB.getInstance().isDisabled) {
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
