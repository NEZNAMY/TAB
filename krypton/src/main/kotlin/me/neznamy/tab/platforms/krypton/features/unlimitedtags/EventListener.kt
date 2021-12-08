package me.neznamy.tab.platforms.krypton.features.unlimitedtags

import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.TabConstants.CpuUsageCategory
import org.kryptonmc.api.event.Listener
import org.kryptonmc.api.event.ListenerPriority
import org.kryptonmc.api.event.player.PerformActionEvent

class EventListener(private val feature: NameTagX) {

    @Listener(ListenerPriority.MAXIMUM)
    fun onAction(event: PerformActionEvent) {
        if (event.action != PerformActionEvent.Action.START_SNEAKING && event.action != PerformActionEvent.Action.STOP_SNEAKING) return
        val player = TAB.getInstance().getPlayer(event.player.uuid)
        if (player == null || !player.isLoaded || feature.isPlayerDisabled(player)) return
        TAB.getInstance().cpuManager.runMeasuredTask("processing PerformActionEvent", feature, CpuUsageCategory.PLAYER_SNEAK) {
            player.armorStandManager?.sneak(event.action == PerformActionEvent.Action.START_SNEAKING)
        }
    }
}
