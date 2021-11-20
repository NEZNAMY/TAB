package me.neznamy.tab.platforms.krypton.features

import me.neznamy.tab.api.TabFeature
import me.neznamy.tab.platforms.krypton.Main
import me.neznamy.tab.shared.TabConstants
import me.neznamy.tab.shared.TAB
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.api.event.Listener
import org.kryptonmc.api.event.player.JoinEvent

// TODO: Also check player on world change when Krypton supports world changing
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
        plugin.server.players.forEach { plugin.server.players.forEach(it::show) }
        plugin.server.eventManager.unregisterListener(plugin, this)
    }

    private fun checkPlayer(player: Player) {
        plugin.server.players.forEach {
            if (it === player) return@forEach
            if (!shouldSee(player, it) && player.canSee(it)) player.hide(it)
            if (shouldSee(player, it) && !player.canSee(it)) player.show(it)
            if (!shouldSee(it, player) && it.canSee(player)) it.hide(player)
            if (shouldSee(it, player) && !it.canSee(player)) it.show(player)
        }
    }

    private fun shouldSee(viewer: Player, target: Player): Boolean {
        if (target === viewer) return true
        if ((allowBypass && viewer.hasPermission(TabConstants.Permission.PER_WORLD_PLAYERLIST_BYPASS)) || ignoredWorlds.contains(viewer.world.name)) {
            return true
        }
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
