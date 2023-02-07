package me.neznamy.tab.platforms.krypton.features.unlimitedtags

import me.neznamy.tab.api.TabConstants
import me.neznamy.tab.shared.TAB
import org.kryptonmc.api.event.Listener
import org.kryptonmc.api.event.player.PerformActionEvent

class EventListener(private val feature: KryptonNameTagX) {

    @Listener
    fun onSneak(event: PerformActionEvent) {
        val player = TAB.getInstance().getPlayer(event.player.uuid)
        if (player == null || feature.isPlayerDisabled(player)) return

        TAB.getInstance().threadManager.runMeasuredTask(feature, TabConstants.CpuUsageCategory.PLAYER_SNEAK) {
            feature.getArmorStandManager(player)!!.sneak(event.action == PerformActionEvent.Action.START_SNEAKING)
        }
    }
}
