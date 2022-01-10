package me.neznamy.tab.platforms.krypton.features.unlimitedtags

import me.neznamy.tab.api.ArmorStandManager
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.team.UnlimitedNametagManager
import me.neznamy.tab.platforms.krypton.Main
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.TabConstants
import me.neznamy.tab.shared.features.nametags.NameTag
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.krypton.entity.player.KryptonPlayer
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow
import kotlin.math.sqrt

class NameTagX(plugin: Main) : NameTag(), UnlimitedNametagManager {

    // config options
    val markerFor18x = TAB.getInstance().configuration.config.getBoolean(
        "scoreboard-teams.unlimited-nametag-mode.use-marker-tag-for-1-8-x-clients",
        false
    )
    val disableOnBoats = TAB.getInstance().configuration.config.getBoolean(
        "scoreboard-teams.unlimited-nametag-mode.disable-on-boats",
        true
    )
    private val spaceBetweenLines = TAB.getInstance().configuration.config.getDouble(
        "scoreboard-teams.unlimited-nametag-mode.space-between-lines",
        0.22
    )
    private val disabledUnlimitedWorlds: List<String> = TAB.getInstance().configuration.config.getStringList(
        "scoreboard-teams.unlimited-nametag-mode.disable-in-worlds",
        emptyList()
    )
    private val dynamicLines = TAB.getInstance().configuration.config.getStringList(
        "scoreboard-teams.unlimited-nametag-mode.dynamic-lines",
        listOf(TabConstants.Property.ABOVENAME, TabConstants.Property.NAMETAG, TabConstants.Property.BELOWNAME, "another")
    )
    private val staticLines = TAB.getInstance().configuration.config
        .getConfigurationSection<String, Any>("scoreboard-teams.unlimited-nametag-mode.static-lines")

    // player data by entity ID, used for better performance
    val entityIdMap = ConcurrentHashMap<Int, TabPlayer>()

    private val playersInDisabledUnlimitedWorlds = mutableSetOf<TabPlayer>()
    private val disableUnlimitedWorldsArray = disabledUnlimitedWorlds.toTypedArray()
    private val unlimitedWorldWhitelistMode = disabledUnlimitedWorlds.contains("WHITELIST")

    private val playersDisabledWithAPI = mutableListOf<TabPlayer>()

    val vehicleManager = VehicleRefresher(this)
    private val eventListener = EventListener(this)

    init {
        dynamicLines.reverse()
        plugin.server.eventManager.register(plugin, eventListener)
        TAB.getInstance().featureManager.registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS_PACKET_LISTENER, PacketListener(this))
        TAB.getInstance().featureManager.registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS_VEHICLE_REFRESHER, vehicleManager)
        TAB.getInstance().featureManager.registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS_LOCATION_REFRESHER, LocationRefresher(this))
        TAB.getInstance().debug("Loaded Unlimited nametag featured with parameters markerFor18x=$markerFor18x, disableOnBoats=$disableOnBoats, " +
            "spaceBetweenLines=$spaceBetweenLines, disabledUnlimitedWorlds=$disabledUnlimitedWorlds")
    }

    fun isPlayerDisabled(player: TabPlayer): Boolean = isDisabledPlayer(player) || playersInDisabledUnlimitedWorlds.contains(player)

    override fun load() {
        TAB.getInstance().onlinePlayers.forEach { all ->
            entityIdMap[(all.player as KryptonPlayer).id] = all
            updateProperties(all)
            loadArmorStands(all)
            if (isDisabled(all.world)) playersInDisabledUnlimitedWorlds.add(all)
            if (isPlayerDisabled(all)) return@forEach
            vehicleManager.loadPassengers(all)
            TAB.getInstance().onlinePlayers.forEach { spawnArmorStands(all, it, false) }
        }
        super.load()
        startVisibilityRefreshTask()
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
        vehicleManager.loadPassengers(connectedPlayer)
        TAB.getInstance().onlinePlayers.forEach { spawnArmorStands(connectedPlayer, it, true) }
    }

    override fun onQuit(disconnectedPlayer: TabPlayer) {
        super.onQuit(disconnectedPlayer)
        TAB.getInstance().onlinePlayers.forEach { it.armorStandManager?.unregisterPlayer(disconnectedPlayer) }
        entityIdMap.remove((disconnectedPlayer.player as KryptonPlayer).id)
        playersInDisabledUnlimitedWorlds.remove(disconnectedPlayer)
        playersDisabledWithAPI.remove(disconnectedPlayer)
        if (disconnectedPlayer.armorStandManager != null) { // player was not loaded yet
            disconnectedPlayer.armorStandManager.destroy()
            TAB.getInstance().cpuManager.runTaskLater(
                500,
                "processing onQuit",
                this,
                TabConstants.CpuUsageCategory.PLAYER_QUIT,
                disconnectedPlayer.armorStandManager::destroy
            )
        }
    }

    override fun refresh(refreshed: TabPlayer, force: Boolean) {
        super.refresh(refreshed, force)
        if (isPlayerDisabled(refreshed)) return
        if (force) {
            refreshed.armorStandManager.destroy()
            loadArmorStands(refreshed)
            vehicleManager.loadPassengers(refreshed)
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

    override fun updateProperties(player: TabPlayer): Boolean {
        super.updateProperties(player)
        player.loadPropertyFromConfig(this, TabConstants.Property.CUSTOMTAGNAME, player.name)
        rebuildNametagLine(player)
        dynamicLines.forEach { if (it != TabConstants.Property.NAMETAG) player.loadPropertyFromConfig(this, it) }
        staticLines.keys.forEach { if (it != TabConstants.Property.NAMETAG) player.loadPropertyFromConfig(this, it) }
        return true
    }

    override fun getFeatureName(): String = "Unlimited Nametags"

    override fun getTeamVisibility(player: TabPlayer, viewer: TabPlayer): Boolean {
        if (player.hasInvisibilityPotion()) return false // 1.8.x client sided bug
        return vehicleManager.isOnBoat(player) || isPlayerDisabled(player)
    }

    override fun pauseTeamHandling(player: TabPlayer) {
        if (teamHandlingPaused.contains(player)) return
        if (!isDisabledPlayer(player)) unregisterTeam(player)
        teamHandlingPaused.add(player) // adding after, so unregisterTeam method runs
        player.armorStandManager.destroy()
    }

    override fun resumeTeamHandling(player: TabPlayer) {
        if (!teamHandlingPaused.contains(player)) return
        teamHandlingPaused.remove(player) // removing before, so registerTeam method runs
        if (!isDisabledPlayer(player)) registerTeam(player)
        if (!isPlayerDisabled(player)) TAB.getInstance().onlinePlayers.forEach { spawnArmorStands(player, it, false) }
    }

    override fun disableArmorStands(player: TabPlayer) {
        if (playersDisabledWithAPI.contains(player)) return
        playersDisabledWithAPI.add(player)
        player.armorStandManager.destroy()
        updateTeamData(player)
    }

    override fun enableArmorStands(player: TabPlayer) {
        if (!playersDisabledWithAPI.contains(player)) return
        playersDisabledWithAPI.remove(player)
        if (!isPlayerDisabled(player)) TAB.getInstance().onlinePlayers.forEach { spawnArmorStands(player, it, false) }
        updateTeamData(player)
    }

    override fun hasDisabledArmorStands(player: TabPlayer): Boolean = playersDisabledWithAPI.contains(player)

    override fun setPrefix(player: TabPlayer, prefix: String) {
        player.getProperty(TabConstants.Property.TAGPREFIX).temporaryValue = prefix
        rebuildNametagLine(player)
        player.forceRefresh()
    }

    override fun setSuffix(player: TabPlayer, suffix: String) {
        player.getProperty(TabConstants.Property.TAGSUFFIX).temporaryValue = suffix
        rebuildNametagLine(player)
        player.forceRefresh()
    }

    override fun resetPrefix(player: TabPlayer) {
        player.getProperty(TabConstants.Property.TAGPREFIX).temporaryValue = null
        rebuildNametagLine(player)
        player.forceRefresh()
    }

    override fun resetSuffix(player: TabPlayer) {
        player.getProperty(TabConstants.Property.TAGSUFFIX).temporaryValue = null
        rebuildNametagLine(player)
        player.forceRefresh()
    }

    override fun setName(player: TabPlayer, customname: String) {
        player.getProperty(TabConstants.Property.CUSTOMTAGNAME).temporaryValue = customname
        rebuildNametagLine(player)
        player.forceRefresh()
    }

    override fun setLine(player: TabPlayer, line: String, value: String) {
        player.getProperty(line).temporaryValue = value
        player.forceRefresh()
    }

    override fun resetName(player: TabPlayer) {
        player.getProperty(TabConstants.Property.CUSTOMTAGNAME).temporaryValue = null
        rebuildNametagLine(player)
        player.forceRefresh()
    }

    override fun resetLine(player: TabPlayer, line: String) {
        player.getProperty(line).temporaryValue = null
        player.forceRefresh()
    }

    override fun getCustomName(player: TabPlayer): String = player.getProperty(TabConstants.Property.CUSTOMTAGNAME).temporaryValue

    override fun getCustomLineValue(player: TabPlayer, line: String): String = player.getProperty(line).temporaryValue

    override fun getOriginalName(player: TabPlayer): String = player.getProperty(TabConstants.Property.CUSTOMTAGNAME).originalRawValue

    override fun getOriginalLineValue(player: TabPlayer, line: String): String = player.getProperty(line).originalRawValue

    override fun getDefinedLines(): List<String> = dynamicLines.plus(staticLines.keys)

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
            TabConstants.CpuUsageCategory.REFRESHING_NAME_TAG_VISIBILITY
        ) {
            TAB.getInstance().onlinePlayers.forEach {
                if (!it.isLoaded || isPlayerDisabled(it)) return@forEach
                it.armorStandManager.updateVisibility(false)
            }
        }
    }

    private fun spawnArmorStands(owner: TabPlayer, viewer: TabPlayer, sendMutually: Boolean) {
        if (owner === viewer) return // not displaying own armor stands
        val ownerPlayer = owner.player as Player
        val viewerPlayer = viewer.player as Player
        if (viewerPlayer.world !== ownerPlayer.world) return // in different worlds
        if (isPlayerDisabled(owner)) return
        if (owner.distanceTo(viewer) <= 48) {
            if (viewerPlayer.canSee(ownerPlayer) && !owner.isVanished) {
                owner.armorStandManager.spawn(viewer)
            }
            if (sendMutually && viewer.armorStandManager != null && ownerPlayer.canSee(viewerPlayer) && !viewer.isVanished) {
                viewer.armorStandManager.spawn(owner)
            }
        }
    }

    private fun loadArmorStands(player: TabPlayer) {
        player.armorStandManager = ArmorStandManager()
        rebuildNametagLine(player)
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

    private fun rebuildNametagLine(player: TabPlayer) {
        player.setProperty(
            this,
            TabConstants.Property.NAMETAG,
            player.getProperty(TabConstants.Property.TAGPREFIX).currentRawValue +
                player.getProperty(TabConstants.Property.CUSTOMTAGNAME).currentRawValue +
                player.getProperty(TabConstants.Property.TAGSUFFIX).currentRawValue
        )
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
