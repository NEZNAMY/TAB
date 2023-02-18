package me.neznamy.tab.platforms.krypton

import me.neznamy.tab.shared.TAB
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.api.event.Listener
import org.kryptonmc.api.event.command.CommandExecuteEvent
import org.kryptonmc.api.event.player.PlayerJoinEvent
import org.kryptonmc.api.event.player.PlayerQuitEvent

class KryptonEventListener(private val plugin: Main) {

    @Listener
    fun onJoin(event: PlayerJoinEvent) {
        TAB.getInstance().cpuManager.runTask {
            TAB.getInstance().featureManager.onJoin(KryptonTabPlayer(event.player, plugin.getProtocolVersion(event.player)))
        }
    }

    @Listener
    fun onQuit(event: PlayerQuitEvent) {
        TAB.getInstance().cpuManager.runTask {
            TAB.getInstance().featureManager.onQuit(TAB.getInstance().getPlayer(event.player.uuid))
        }
    }

    @Listener
    fun onCommand(event: CommandExecuteEvent) {
        val player = event.sender
        if (player !is Player) return

        if (TAB.getInstance().featureManager.onCommand(TAB.getInstance().getPlayer(player.uuid), event.command)) {
            event.deny()
        }
    }
}
