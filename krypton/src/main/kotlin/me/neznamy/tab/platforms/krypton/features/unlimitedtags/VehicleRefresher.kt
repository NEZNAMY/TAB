package me.neznamy.tab.platforms.krypton.features.unlimitedtags

import me.neznamy.tab.api.TabFeature
import me.neznamy.tab.api.TabPlayer
import org.kryptonmc.api.entity.Entity
import org.kryptonmc.krypton.entity.KryptonEntity
import java.util.concurrent.ConcurrentHashMap

class VehicleRefresher(private val feature: NameTagX) : TabFeature(feature.featureName, "Refreshing vehicles") {

    // list of players currently in a vehicle
    private val playersInVehicle = ConcurrentHashMap<TabPlayer, KryptonEntity>()

    // map of vehicles carrying players
    private val vehicles = ConcurrentHashMap<Int, List<Entity>>()

    // list of players currently on boats
    private val playersOnBoats = mutableListOf<TabPlayer>()

    init {
        addUsedPlaceholders(listOf("%vehicle%"))
        // TODO: Add vehicle placeholder
    }

    override fun refresh(refreshed: TabPlayer, force: Boolean) {
        if (feature.isPlayerDisabled(refreshed)) return
        // TODO: Vehicle handling
    }

    fun isOnBoat(player: TabPlayer): Boolean = playersOnBoats.contains(player)

    override fun onQuit(disconnectedPlayer: TabPlayer) {
        if (playersInVehicle.containsKey(disconnectedPlayer)) vehicles.remove(playersInVehicle[disconnectedPlayer]!!.id)
        playersInVehicle.remove(disconnectedPlayer)
        playersOnBoats.remove(disconnectedPlayer)
    }
}
