package me.neznamy.tab.platforms.krypton.features

import me.neznamy.tab.api.TabFeature
import me.neznamy.tab.platforms.krypton.Main
import me.neznamy.tab.shared.TabConstants
import me.neznamy.tab.shared.TAB
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.api.event.Listener
import org.kryptonmc.api.event.player.JoinEvent

class PerWorldPlayerList(private val plugin: Main) : TabFeature("Per world playerlist", null) {

    private val allowBypass = TAB.getInstance().configuration.config.getBoolean(
        "per-world-playerlist.allow-bypass-permission",
        false
    )
    private val ignoredWorlds = TAB.getInstance().configuration.config.getStringList(
        "per-world-playerlist.ignore-effect-in-worlds",
        listOf("ignoredworld", "build")
    )
    private val sharedWorlds = TAB.getInstance().configuration.config
        .getConfigurationSection<String, List<String>>("per-world-playerlist.shared-playerlist-world-groups")

    init {
        sharedWorlds.forEach {
            if (it.value == null) {
                TAB.getInstance().errorManager.startupWarn("World group \"$it\" in per-world-playerlist does not contain any worlds. You can " +
                    "just remove the group.")
            } else if (it.value.size == 1) {
                TAB.getInstance().errorManager.startupWarn("World group \"$it\" in per-world-playerlist only contains a single world " +
                    "(\"${it.value.first()}\"), which has no effect and only makes config less readable. Delete the group entirely for" +
                    "a cleaner config.")
            }
        }
        TAB.getInstance().debug("Loaded PerWorldPlayerList feature with parameters allowBypass=$allowBypass, ignoredWorlds=$ignoredWorlds, " +
            "sharedWorlds=$sharedWorlds")
        plugin.server.eventManager.register(plugin, this)
    }

    @Listener
    fun onJoin(event: JoinEvent) {
        val time = System.nanoTime()
        checkPlayer(event.player)
        TAB.getInstance().cpuManager.addTime(featureName, TabConstants.CpuUsageCategory.PLAYER_JOIN, System.nanoTime() - time)
    }

    override fun load() {
        plugin.server.scheduler.run(plugin) { plugin.server.players.forEach(::checkPlayer) }
    }

    override fun unload() {
        // TODO: Show players back to each other on unload
        plugin.server.eventManager.unregisterListener(plugin, this)
    }

    private fun checkPlayer(player: Player) {
        plugin.server.players.forEach {
            if (it === player) return@forEach
            // TODO: Handle showing and hiding when it exists
        }
    }

    private fun shouldSee(viewer: Player, target: Player): Boolean {
        if (target === viewer) return true
        if ((allowBypass && viewer.hasPermission("tab.bypass")) || ignoredWorlds.contains(viewer.world.name)) return true
        var viewerWorldGroup = "${viewer.world.name}-default"
        var targetWorldGroup = "${target.world.name}-default"
        sharedWorlds.forEach { (key, value) ->
            if (value == null) return@forEach
            if (value.contains(viewer.world.name)) viewerWorldGroup = key
            if (value.contains(target.world.name)) targetWorldGroup = key
        }
        return viewerWorldGroup == targetWorldGroup
    }
}
