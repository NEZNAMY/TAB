package me.neznamy.tab.platforms.krypton.features.unlimitedtags

import me.neznamy.tab.api.TabFeature
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.shared.TAB
import org.kryptonmc.api.entity.player.Player

class LocationRefresher(feature: NameTagX) : TabFeature(feature.featureName) {

    init {
        TAB.getInstance().placeholderManager.registerPlayerPlaceholder("%location0%", 50) {
            // TODO: Check if the player is in a vehicle
            val location = (it.player as Player).location
            location.x() + location.y() + location.z()
        }
    }

    override fun refresh(refreshed: TabPlayer, force: Boolean) {
        // TODO: Handle vehicles
        if (refreshed.isPreviewingNametag) refreshed.armorStandManager.teleport(refreshed)
    }
}
