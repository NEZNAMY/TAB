package me.neznamy.tab.platforms.krypton

import me.neznamy.tab.shared.TAB
import org.kryptonmc.api.event.Listener
import org.kryptonmc.api.event.ListenerPriority
import org.kryptonmc.api.event.command.CommandExecuteEvent
import org.kryptonmc.api.event.command.CommandResult
import org.kryptonmc.api.event.player.JoinEvent
import org.kryptonmc.api.event.player.QuitEvent

class KryptonEventListener(private val plugin: Main) {

    @Listener(ListenerPriority.NONE)
    fun onJoin(event: JoinEvent) {
        if (TAB.getInstance().isDisabled) return
        TAB.getInstance().cpuManager.runTask("processing JoinEvent") {
            TAB.getInstance().featureManager.onJoin(KryptonTabPlayer(event.player, plugin.protocolVersion(event.player)))
        }
    }

    @Listener(ListenerPriority.NONE)
    fun onQuit(event: QuitEvent) {
        if (TAB.getInstance().isDisabled) return
        TAB.getInstance().cpuManager.runTask("processing QuitEvent") {
            TAB.getInstance().featureManager.onQuit(TAB.getInstance().getPlayer(event.player.uuid))
        }
    }

    @Listener(ListenerPriority.NONE)
    fun onCommand(event: CommandExecuteEvent) {
        if (TAB.getInstance().isDisabled) return
        if (TAB.getInstance().featureManager.onCommand(TAB.getInstance().getPlayer(event.sender.uuid), event.command)) {
            event.result = CommandResult.denied()
        }
    }
}
