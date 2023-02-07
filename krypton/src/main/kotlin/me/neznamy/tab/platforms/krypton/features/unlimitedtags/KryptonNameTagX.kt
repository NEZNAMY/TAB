package me.neznamy.tab.platforms.krypton.features.unlimitedtags

import me.neznamy.tab.api.TabAPI
import me.neznamy.tab.api.TabConstants
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.platforms.krypton.Main
import me.neznamy.tab.shared.features.nametags.unlimited.NameTagX
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.api.event.EventFilter
import org.kryptonmc.api.event.EventNode
import org.kryptonmc.api.event.player.PerformActionEvent
import java.util.function.BiFunction
import kotlin.math.sqrt

class KryptonNameTagX(private val plugin: Main) : NameTagX(BiFunction(::KryptonArmorStandManager)) {

    private val eventListener = EventListener(this)
    private val vehicleManager = VehicleRefresher(this)

    init {
        val eventNode = EventNode.filteredForEvent("tab_nametagx", EventFilter.ALL) { event ->
            if (event !is PerformActionEvent) return@filteredForEvent false
            event.action == PerformActionEvent.Action.START_SNEAKING || event.action == PerformActionEvent.Action.STOP_SNEAKING
        }
        plugin.eventNode.addChild(eventNode)
        eventNode.registerListeners(eventListener)

        TabAPI.getInstance().featureManager.registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS_PACKET_LISTENER, PacketListener(this))
        TabAPI.getInstance().featureManager.registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS_VEHICLE_REFRESHER, vehicleManager)
    }

    override fun load() {
        super.load()
        TabAPI.getInstance().onlinePlayers.forEach { all ->
            if (isPlayerDisabled(all)) return@forEach
            TabAPI.getInstance().onlinePlayers.forEach { viewer ->
                spawnArmorStands(viewer, all)
            }
        }
        startVisibilityRefreshTask()
    }

    private fun startVisibilityRefreshTask() {
        TabAPI.getInstance().threadManager.startRepeatingMeasuredTask(500, this, TabConstants.CpuUsageCategory.REFRESHING_NAME_TAG_VISIBILITY) {
            TabAPI.getInstance().onlinePlayers.forEach { player ->
                if (isPlayerDisabled(player)) return@forEach
                getArmorStandManager(player)!!.updateVisibility(false)
            }
        }
    }

    override fun unload() {
        super.unload()
        plugin.eventNode.unregisterListeners(eventListener)
    }

    override fun onJoin(connectedPlayer: TabPlayer) {
        super.onJoin(connectedPlayer)
        if (isPlayerDisabled(connectedPlayer)) return
        TabAPI.getInstance().onlinePlayers.forEach { viewer ->
            spawnArmorStands(viewer, connectedPlayer)
            spawnArmorStands(connectedPlayer, viewer)
        }
    }

    override fun isOnBoat(player: TabPlayer): Boolean = vehicleManager.isOnBoat(player)

    private fun spawnArmorStands(viewer: TabPlayer, target: TabPlayer) {
        if (target === viewer || isPlayerDisabled(target)) return
        if ((viewer.player as Player).world != (target.player as Player).world) return
        if (getDistance(viewer, target) <= 48) {
            getArmorStandManager(viewer)!!.spawn(target)
        }
    }

    override fun setNameTagPreview(player: TabPlayer, status: Boolean) {
        if (status) {
            getArmorStandManager(player)!!.spawn(player)
        } else {
            getArmorStandManager(player)!!.destroy(player)
        }
    }

    override fun resumeArmorStands(player: TabPlayer) {
        if (isPlayerDisabled(player)) return
        TabAPI.getInstance().onlinePlayers.forEach { viewer -> spawnArmorStands(viewer, player) }
    }

    override fun pauseArmorStands(player: TabPlayer) {
        getArmorStandManager(player)!!.destroy()
    }

    override fun updateNameTagVisibilityView(player: TabPlayer) {
        TabAPI.getInstance().onlinePlayers.forEach { all -> getArmorStandManager(all)!!.updateVisibility(true) }
    }

    override fun onQuit(disconnectedPlayer: TabPlayer) {
        super.onQuit(disconnectedPlayer)
        TabAPI.getInstance().onlinePlayers.forEach { all -> getArmorStandManager(all)!!.unregisterPlayer(disconnectedPlayer) }
        armorStandManagerMap.get(disconnectedPlayer)!!.destroy()
        armorStandManagerMap.remove(disconnectedPlayer)
    }

    override fun onWorldChange(p: TabPlayer, from: String, to: String) {
        super.onWorldChange(p, from, to)
        if (isUnlimitedDisabled(p.server, to)) {
            disabledUnlimitedPlayers.add(p)
            updateTeamData(p)
        } else if (disabledUnlimitedPlayers.remove(p)) {
            updateTeamData(p)
        }
        if (isPreviewingNametag(p)) getArmorStandManager(p)!!.spawn(p)
        // For some reason this is needed for some users
        TabAPI.getInstance().onlinePlayers.forEach { viewer ->
            if (viewer.world == from) getArmorStandManager(p)!!.destroy(viewer)
        }
    }

    private fun getDistance(player1: TabPlayer, player2: TabPlayer): Double {
        val pos1 = (player1.player as Player).position
        val pos2 = (player2.player as Player).position
        return sqrt((pos1.x - pos2.x) * (pos1.x - pos2.x) + (pos1.z - pos2.z) * (pos1.z - pos2.z))
    }

    override fun getArmorStandManager(player: TabPlayer): KryptonArmorStandManager? {
        return armorStandManagerMap.get(player) as? KryptonArmorStandManager
    }

    fun vehicleManager(): VehicleRefresher = vehicleManager
}
