package me.neznamy.tab.platforms.krypton.features.unlimitedtags

import me.neznamy.tab.api.TabFeature
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.shared.TAB
import org.kryptonmc.api.entity.Entity
import org.kryptonmc.api.entity.EntityTypes
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.krypton.entity.KryptonEntity
import java.util.concurrent.ConcurrentHashMap

class VehicleRefresher(private val feature: NameTagX) : TabFeature(feature.featureName, "Refreshing vehicles") {

    private val playersInVehicle = ConcurrentHashMap<TabPlayer, KryptonEntity>()
    private val vehicles = ConcurrentHashMap<Int, List<Entity>>()
    private val playersOnBoats = mutableListOf<TabPlayer>()

    init {
        addUsedPlaceholders(listOf("%vehicle%"))
        TAB.getInstance().placeholderManager.registerPlayerPlaceholder("%vehicle%", 100) { (it.player as Player).vehicle.toString() }
    }

    fun isInVehicle(player: TabPlayer): Boolean = playersInVehicle.containsKey(player)

    fun isOnBoat(player: TabPlayer): Boolean = playersOnBoats.contains(player)

    fun vehicles(id: Int): List<Entity> = vehicles[id] ?: emptyList()

    fun loadPassengers(player: TabPlayer) {
        if ((player.player as Player).vehicle == null) return
        val vehicle = (player.player as Player).vehicle
        vehicles[(vehicle as KryptonEntity).id] = vehicle.passengers
    }

    fun processPassengers(vehicle: Entity) {
        vehicle.passengers.forEach {
            if (it is Player) TAB.getInstance().getPlayer(it.uuid).armorStandManager.teleport()
            processPassengers(it)
        }
    }

    override fun refresh(refreshed: TabPlayer, force: Boolean) {
        if (feature.isPlayerDisabled(refreshed)) return
        val vehicle = (refreshed.player as Player).vehicle
        if (playersInVehicle.containsKey(refreshed) && vehicle == null) {
            // vehicle exit
            vehicles.remove((playersInVehicle[refreshed] as KryptonEntity).id)
            refreshed.armorStandManager.teleport()
            playersInVehicle.remove(refreshed)
            if (feature.disableOnBoats && playersOnBoats.contains(refreshed)) {
                playersOnBoats.remove(refreshed)
                feature.updateTeamData(refreshed)
            }
        }
        if (!playersInVehicle.containsKey(refreshed) && vehicle != null) {
            // vehicle enter
            vehicles[(vehicle as KryptonEntity).id] = vehicle.passengers
            refreshed.armorStandManager.respawn() // making teleport instant instead of showing teleport animation
            playersInVehicle[refreshed] = vehicle
            if (feature.disableOnBoats && vehicle.type === EntityTypes.BOAT) {
                playersOnBoats.add(refreshed)
                feature.updateTeamData(refreshed)
            }
        }
    }

    override fun onQuit(disconnectedPlayer: TabPlayer) {
        if (playersInVehicle.containsKey(disconnectedPlayer)) vehicles.remove(playersInVehicle[disconnectedPlayer]!!.id)
        playersInVehicle.remove(disconnectedPlayer)
        playersOnBoats.remove(disconnectedPlayer)
    }
}
