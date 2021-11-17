package me.neznamy.tab.platforms.krypton.features.unlimitedtags

import me.neznamy.tab.api.ArmorStandManager
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.platforms.krypton.Main
import me.neznamy.tab.shared.TabConstants
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.features.nametags.NameTag
import org.kryptonmc.api.entity.Entity
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.krypton.entity.player.KryptonPlayer
import org.spongepowered.math.vector.Vector3d
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow
import kotlin.math.sqrt

class NameTagX : NameTag() {

    // Config options
    val markerFor18x = TAB.getInstance().configuration.config.getBoolean(
        "unlimited-nametag-prefix-suffix-mode.use-marker-tag-for-1-8-x-clients",
        false
    )
    private val disableOnBoats = TAB.getInstance().configuration.config.getBoolean(
        "unlimited-nametag-prefix-suffix-mode.disable-on-boats",
        true
    )
    private val spaceBetweenLines = TAB.getInstance().configuration.config.getDouble(
        "unlimited-nametag-prefix-suffix-mode.space-between-lines",
        0.22
    )
    val disabledUnlimitedWorlds: List<String> = TAB.getInstance().configuration.config.getStringList(
        "disable-features-in-worlds.unlimited-nametags",
        listOf("disabledworld")
    )

    private var dynamicLines = mutableListOf(TabConstants.Property.BELOWNAME, TabConstants.Property.NAMETAG, TabConstants.Property.ABOVENAME)
    private val staticLines = ConcurrentHashMap<String, Any>()
    val entityIdMap = ConcurrentHashMap<Int, TabPlayer>()
    // TODO: Add this stuff back when we support entities
    /*
    val vehicles = ConcurrentHashMap<Int, List<Entity>>()
    private val playersOnBoats = mutableListOf<TabPlayer>()
    private val playersInVehicle = ConcurrentHashMap<TabPlayer, Entity>()
    */
    private val playerLocations = ConcurrentHashMap<TabPlayer, Vector3d>()
    private val playersInDisabledUnlimitedWorlds = mutableSetOf<TabPlayer>()
    private var disableUnlimitedWorldsArray = emptyArray<String>()
    private var unlimitedWorldWhitelistMode = false

    init {
        disableUnlimitedWorldsArray = disabledUnlimitedWorlds.toTypedArray()
        unlimitedWorldWhitelistMode = disabledUnlimitedWorlds.contains("WHITELIST")

        val realList = TAB.getInstance().configuration.config.getStringList(
            "scoreboard-teams.unlimited-nametag-mode.dynamic-lines",
            listOf(TabConstants.Property.ABOVENAME, TabConstants.Property.NAMETAG, TabConstants.Property.BELOWNAME, "another")
        )
        dynamicLines = mutableListOf()
        dynamicLines.addAll(realList)
        dynamicLines.reverse()
        staticLines.putAll(TAB.getInstance().configuration.config.getConfigurationSection("scoreboard-teams.unlimited-nametag-mode.static-lines"))

        TAB.getInstance().featureManager.registerFeature("nametagx-packet", PacketListener(this))
        TAB.getInstance().debug("Loaded Unlimited nametag featured with parameters markerFor18x=$markerFor18x, disableOnBoats=$disableOnBoats, " +
            "spaceBetweenLines=$spaceBetweenLines, disabledUnlimitedWorlds=$disabledUnlimitedWorlds")
    }

    fun isPlayerDisabled(player: TabPlayer) = isDisabledPlayer(player) || playersInDisabledUnlimitedWorlds.contains(player)

    override fun load() {
        TAB.getInstance().onlinePlayers.forEach { all ->
            entityIdMap[(all.player as KryptonPlayer).id] = all
            updateProperties(all)
            loadArmorStands(all)
            if (isDisabled(all.world)) playersInDisabledUnlimitedWorlds.add(all)
            if (isPlayerDisabled(all)) return@forEach
//            loadPassengers(all)
            TAB.getInstance().onlinePlayers.forEach { spawnArmorStands(all, it, false) }
        }
        super.load()
        startVisibilityRefreshTask()
        startVehicleTickingTask()
    }

    override fun unload() {
        super.unload()
        TAB.getInstance().onlinePlayers.forEach { it.armorStandManager?.destroy() }
        entityIdMap.clear()
    }

    override fun onJoin(connectedPlayer: TabPlayer) {
        if (isDisabled(connectedPlayer.world) && !playersInDisabledUnlimitedWorlds.contains(connectedPlayer)) {
            playersInDisabledUnlimitedWorlds.add(connectedPlayer)
        }
        super.onJoin(connectedPlayer)
        entityIdMap[(connectedPlayer.player as KryptonPlayer).id] = connectedPlayer
        loadArmorStands(connectedPlayer)
        if (isPlayerDisabled(connectedPlayer)) return
//        loadPassengers(connectedPlayer)
        TAB.getInstance().onlinePlayers.forEach { spawnArmorStands(connectedPlayer, it, true) }
    }

    override fun onQuit(disconnectedPlayer: TabPlayer) {
        super.onQuit(disconnectedPlayer)
        TAB.getInstance().onlinePlayers.forEach { it.armorStandManager?.unregisterPlayer(disconnectedPlayer) }
        entityIdMap.remove((disconnectedPlayer.player as KryptonPlayer).id)
//        playersInVehicle.remove(disconnectedPlayer)
        playerLocations.remove(disconnectedPlayer)
        playersInDisabledUnlimitedWorlds.remove(disconnectedPlayer)
        TAB.getInstance().cpuManager.runTaskLater(100, "processing onQuit", this, TabConstants.CpuUsageCategory.PLAYER_QUIT) {
            disconnectedPlayer.armorStandManager.destroy()
        }
    }

    override fun refresh(refreshed: TabPlayer, force: Boolean) {
        super.refresh(refreshed, force)
        if (isPlayerDisabled(refreshed)) return
        if (force) {
            refreshed.armorStandManager.destroy()
            loadArmorStands(refreshed)
//            loadPassengers(refreshed)
            TAB.getInstance().onlinePlayers.forEach {
                if (it === refreshed) return@forEach
                if (it.world == refreshed.world) refreshed.armorStandManager.spawn(it)
            }
            return
        }
        var fix = false
        refreshed.armorStandManager.armorStands.forEach {
            if (it.property.update()) {
                it.refresh()
                fix = true
            }
            if (fix) fixArmorStandHeights(refreshed)
        }
    }

    override fun updateProperties(player: TabPlayer) {
        super.updateProperties(player)
        player.loadPropertyFromConfig(this, TabConstants.Property.CUSTOMTAGNAME, player.name)
        player.setProperty(this, TabConstants.Property.NAMETAG, player.getProperty(TabConstants.Property.TAGPREFIX).currentRawValue +
            player.getProperty(TabConstants.Property.CUSTOMTAGNAME).currentRawValue +
            player.getProperty(TabConstants.Property.TAGSUFFIX).currentRawValue)
        dynamicLines.forEach { if (it != TabConstants.Property.NAMETAG) player.loadPropertyFromConfig(this, it) }
        staticLines.keys.forEach { if (it != TabConstants.Property.NAMETAG) player.loadPropertyFromConfig(this, it) }
    }

    override fun getFeatureName(): String = "Unlimited Nametags"

    override fun getTeamVisibility(player: TabPlayer, viewer: TabPlayer): Boolean {
        // only visible if player is on boat & config option is enabled and player is not invisible (1.8 bug) or feature is disabled
        return true
        // TODO
//        return playersOnBoats.contains(player) && !player.hasInvisibilityPotion() || isPlayerDisabled(player)
    }

    private fun isDisabled(world: String): Boolean {
        var contains = contains(disableUnlimitedWorldsArray, world)
        if (unlimitedWorldWhitelistMode) contains = !contains
        return contains
    }

    private fun startVisibilityRefreshTask() {
        TAB.getInstance().cpuManager.startRepeatingMeasuredTask(
            500,
            "refreshing nametag visibility",
            this,
            TabConstants.CpuUsageCategory.REFRESHING_NAMETAG_VISIBILITY
        ) {
            TAB.getInstance().onlinePlayers.forEach {
                if (!it.isLoaded || isPlayerDisabled(it)) return@forEach
                it.armorStandManager.updateVisibility(false)
//                if (disableOnBoats) processBoats(it)
            }
        }
    }

    /*
    private fun processBoats(player: TabPlayer) {
        val vehicle = (player.player as Player).vehicle
        val onBoat = vehicle != null && vehicle.type == EntityTypes.BOAT
        if (onBoat) {
            if (!playersOnBoats.contains(player)) {
                playersOnBoats.add(player)
                updateTeamData(player)
            }
            return
        }
        if (playersOnBoats.contains(player)) {
            playersOnBoats.remove(player)
            updateTeamData(player)
        }
    }
    */

    private fun startVehicleTickingTask() {
        TAB.getInstance().cpuManager.startRepeatingMeasuredTask(
            100,
            "ticking vehicles",
            this,
            TabConstants.CpuUsageCategory.TICKING_VEHICLES
        ) {
            TAB.getInstance().onlinePlayers.forEach {
                if (!it.isLoaded) return@forEach
                if (isPlayerDisabled(it)) {
//                    playersInVehicles.remove(it)
                    playerLocations.remove(it)
                    return@forEach
                }
//                processVehicles(it)
                if (!playerLocations.containsKey(it) || playerLocations[it] != (it.player as Player).location) {
                    playerLocations[it] = (it.player as Player).location
//                    processPassengers(it)
                    // also updating position if player is previewing since we're here as the code would be same if we want to avoid listening to move event
                    if (it.isPreviewingNametag) it.armorStandManager?.teleport(it)
                }
            }
        }
    }

    /*
    private fun processVehicles(player: TabPlayer) {
        val vehicle = (player.player as Player).vehicle
        if (playersInVehicle.containsKey(player) && vehicle == null) {
            // vehicle exit
            vehicles.remove((playersInVehicle[player] as KryptonEntity).id)
            player.armorStandManager.teleport()
            playersInVehicle.remove(player)
        }
        if (!playersInVehicle.containsKey(player) && vehicle != null) {
            // vehicle enter
            vehicles[vehicle.id] = vehicle.passengers
            player.armorStandManager.teleport()
            playersInVehicle[player] = vehicle
        }
    }
    */

    private fun processPassengers(vehicle: Entity) {
        vehicle.passengers.forEach {
            if (it is Player) {
                val player = TAB.getInstance().getPlayer(it.uuid)
                player.armorStandManager.teleport()
            } else {
                processPassengers(it)
            }
        }
    }

    /*
    private fun loadPassengers(player: TabPlayer) {
        val vehicle = (player.player as KryptonEntity).vehicle ?: return
        vehicles[vehicle.id] = vehicle.passengers
    }
     */

    private fun spawnArmorStands(owner: TabPlayer, viewer: TabPlayer, sendMutually: Boolean) {
        if (owner === viewer) return // not displaying own armor stands
        if ((viewer.player as Player).world != (owner.player as Player).world) return // in different worlds
        if (isPlayerDisabled(owner)) return
        if (owner.distanceTo(viewer) <= 48) {
            // TODO: Handle hidden players if/when that becomes a thing in Krypton
            owner.armorStandManager.spawn(viewer)
            if (sendMutually) viewer.armorStandManager.spawn(owner)
        }
    }

    private fun loadArmorStands(player: TabPlayer) {
        player.armorStandManager = ArmorStandManager()
        player.setProperty(this, TabConstants.Property.NAMETAG, player.getProperty(TabConstants.Property.TAGPREFIX).currentRawValue +
            player.getProperty(TabConstants.Property.CUSTOMTAGNAME).currentRawValue +
            player.getProperty(TabConstants.Property.TAGSUFFIX).currentRawValue)
        var height = 0.0
        dynamicLines.forEach {
            val property = player.getProperty(it)
            if (property.currentRawValue.isEmpty()) return@forEach
            player.armorStandManager.addArmorStand(it, KryptonArmorStand(player, property, height, false))
            height += spaceBetweenLines
        }
        staticLines.forEach { (key, value) ->
            val property = player.getProperty(key)
            if (property.currentRawValue.isEmpty()) return@forEach
            player.armorStandManager.addArmorStand(key, KryptonArmorStand(player, property, value.toString().toDouble(), true))
        }
        fixArmorStandHeights(player)
    }

    private fun fixArmorStandHeights(player: TabPlayer) {
        var currentY = -spaceBetweenLines
        player.armorStandManager.armorStands.forEach {
            if (it.hasStaticOffset()) return@forEach
            if (it.property.get().isNotEmpty()) {
                currentY += spaceBetweenLines
                it.offset = currentY
            }
        }
    }

    companion object {

        @JvmStatic
        private fun TabPlayer.distanceTo(other: TabPlayer): Double {
            val first = (player as Player).location
            val second = (other.player as Player).location
            return sqrt((first.x() - second.x()).pow(2) + (first.z() - second.z()).pow(2))
        }
    }
}
