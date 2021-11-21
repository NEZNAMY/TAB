package me.neznamy.tab.platforms.krypton.features.unlimitedtags

import me.neznamy.tab.api.TabFeature
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.shared.TAB
import org.kryptonmc.api.entity.player.Player

class LocationRefresher(private val feature: NameTagX) : TabFeature(feature.featureName, "Processing passengers / preview") {

    init {
        TAB.getInstance().placeholderManager.registerPlayerPlaceholder("%location0%", 50) {
            if (!feature.vehicleManager.isInVehicle(it) && !it.isPreviewingNametag) return@registerPlayerPlaceholder null
            val location = (it.player as Player).location
            location.x() + location.y() + location.z()
        }
        addUsedPlaceholders(listOf("%location0%"))
    }

    override fun refresh(refreshed: TabPlayer, force: Boolean) {
        if (feature.vehicleManager.isInVehicle(refreshed)) feature.vehicleManager.processPassengers(refreshed.player as Player)
        if (refreshed.isPreviewingNametag) refreshed.armorStandManager.teleport(refreshed)
    }
}
