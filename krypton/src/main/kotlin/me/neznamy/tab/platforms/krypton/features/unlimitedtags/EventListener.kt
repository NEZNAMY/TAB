package me.neznamy.tab.platforms.krypton.features.unlimitedtags

import me.neznamy.tab.api.TabConstants
import me.neznamy.tab.shared.TAB
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.api.event.Listener
import org.kryptonmc.api.event.player.action.PlayerStartSneakingEvent
import org.kryptonmc.api.event.player.action.PlayerStopSneakingEvent

class EventListener(private val feature: KryptonNameTagX) {

    @Listener
    fun onStartSneak(event: PlayerStartSneakingEvent) {
        onSneak(event.player, true)
    }

    @Listener
    fun onStopSneak(event: PlayerStopSneakingEvent) {
        onSneak(event.player, false)
    }

    private fun onSneak(eventPlayer: Player, sneaking: Boolean) {
        val player = TAB.getInstance().getPlayer(eventPlayer.uuid)
        if (player == null || feature.isPlayerDisabled(player)) return

        TAB.getInstance().threadManager.runMeasuredTask(feature, TabConstants.CpuUsageCategory.PLAYER_SNEAK) {
            feature.getArmorStandManager(player)!!.sneak(sneaking)
        }
    }
}
