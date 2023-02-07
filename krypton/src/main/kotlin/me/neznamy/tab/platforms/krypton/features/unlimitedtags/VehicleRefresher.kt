package me.neznamy.tab.platforms.krypton.features.unlimitedtags

import me.neznamy.tab.api.TabAPI
import me.neznamy.tab.api.TabConstants
import me.neznamy.tab.api.TabFeature
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.shared.TAB
import org.kryptonmc.api.entity.Entity
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.krypton.entity.KryptonEntity
import java.util.Collections
import java.util.WeakHashMap

class VehicleRefresher(private val feature: KryptonNameTagX) : TabFeature(feature.featureName, "Refreshing vehicles") {

    private val playersInVehicle = WeakHashMap<TabPlayer, KryptonEntity>()
    private val vehicles = HashMap<Int, MutableList<Entity>>()
    private val playersOnBoats = Collections.newSetFromMap<TabPlayer>(WeakHashMap())

    init {
        TabAPI.getInstance().threadManager.startRepeatingMeasuredTask(50, this, TabConstants.CpuUsageCategory.PROCESSING_PLAYER_MOVEMENT) {
            playersInVehicle.keys.forEach { inVehicle ->
                if (!inVehicle.isOnline || feature.getArmorStandManager(inVehicle) == null) return@forEach
                feature.getArmorStandManager(inVehicle)!!.teleport()
            }
            TabAPI.getInstance().onlinePlayers.forEach { player ->
                if (feature.isPreviewingNametag(player)) feature.getArmorStandManager(player)!!.teleport(player)
            }
        }
        addUsedPlaceholders(listOf(TabConstants.Placeholder.VEHICLE))
        TAB.getInstance().placeholderManager.registerPlayerPlaceholder(TabConstants.Placeholder.VEHICLE, 100) {
            (it.player as Player).vehicle.toString()
        }
    }

    override fun load() {
        TabAPI.getInstance().onlinePlayers.forEach { player ->
            val vehicle = (player.player as Player).vehicle ?: return@forEach
            vehicles.put(vehicle.id, ArrayList(vehicle.passengers))
            playersInVehicle.put(player, vehicle as KryptonEntity)
            if (feature.isDisableOnBoats && vehicle.type.toString().contains("BOAT")) playersOnBoats.add(player)
        }
    }

    override fun onJoin(connectedPlayer: TabPlayer) {
        val vehicle = (connectedPlayer.player as Player).vehicle ?: return
        vehicles.put(vehicle.id, ArrayList(vehicle.passengers))
    }

    override fun onQuit(disconnectedPlayer: TabPlayer) {
        val vehicle = playersInVehicle.get(disconnectedPlayer)
        if (vehicle != null) vehicles.remove(vehicle.id)
        vehicles.values.forEach { it.remove(disconnectedPlayer.player as Player) }
    }

    override fun refresh(refreshed: TabPlayer, force: Boolean) {
        if (feature.isPlayerDisabled(refreshed)) return
        val vehicle = (refreshed.player as Player).vehicle

        if (playersInVehicle.containsKey(refreshed) && vehicle == null) {
            // Vehicle exit
            vehicles.remove(playersInVehicle.get(refreshed)!!.id)
            feature.getArmorStandManager(refreshed)!!.teleport()
            playersInVehicle.remove(refreshed)
            if (feature.isDisableOnBoats && playersOnBoats.contains(refreshed)) {
                playersOnBoats.remove(refreshed)
                feature.updateTeamData(refreshed)
            }
        }

        if (!playersInVehicle.containsKey(refreshed) && vehicle != null) {
            // Vehicle enter
            vehicles.put(vehicle.id, ArrayList(vehicle.passengers))
            feature.getArmorStandManager(refreshed)!!.respawn()
            playersInVehicle.put(refreshed, vehicle as KryptonEntity)
            if (feature.isDisableOnBoats && vehicle.type.toString().contains("BOAT")) {
                playersOnBoats.add(refreshed)
                feature.updateTeamData(refreshed)
            }
        }
    }

    fun isOnBoat(player: TabPlayer): Boolean = playersOnBoats.contains(player)

    fun vehicles(): Map<Int, List<Entity>> = vehicles
}
