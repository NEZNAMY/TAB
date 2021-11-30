package me.neznamy.tab.platforms.krypton

import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.protocol.PacketBuilder
import me.neznamy.tab.platforms.krypton.event.TabLoadEvent
import me.neznamy.tab.platforms.krypton.event.TabPlayerLoadEvent
import me.neznamy.tab.platforms.krypton.features.PerWorldPlayerList
import me.neznamy.tab.platforms.krypton.features.unlimitedtags.NameTagX
import me.neznamy.tab.shared.Platform
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl
import me.neznamy.tab.shared.features.nametags.NameTag
import me.neznamy.tab.shared.permission.LuckPerms
import me.neznamy.tab.shared.permission.None
import me.neznamy.tab.shared.permission.PermissionPlugin
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.kryptonmc.api.auth.ProfileProperty
import java.io.File

class KryptonPlatform(
    private val plugin: Main,
    private val folder: File,
) : Platform {

    private val server = plugin.server

    override fun detectPermissionPlugin(): PermissionPlugin {
        if (isPluginEnabled("luckperms")) return LuckPerms(server.pluginManager.plugin("luckperms")!!.description.version)
        return None()
    }

    override fun loadFeatures() {
        val tab = TAB.getInstance()
        if (tab.configuration.isPipelineInjection) tab.featureManager.registerFeature("injection", KryptonPipelineInjector())

        // Placeholders
        KryptonPlaceholderRegistry(plugin).registerPlaceholders(tab.placeholderManager)
        UniversalPlaceholderRegistry().registerPlaceholders(tab.placeholderManager)

        // Load features
        loadNametagFeature(tab)
        tab.loadUniversalFeatures()
        if (tab.config.getBoolean("bossbar.enabled", false)) {
            tab.featureManager.registerFeature("bossbar", BossBarManagerImpl())
        }
        if (tab.config.getBoolean("per-world-playerlist.enabled", false)) {
            tab.featureManager.registerFeature("pwp", PerWorldPlayerList(plugin))
        }
        server.players.forEach { tab.addPlayer(KryptonTabPlayer(it, plugin.protocolVersion(it))) }
    }

    override fun sendConsoleMessage(message: String, translateColors: Boolean) {
        val component = if (translateColors) LegacyComponentSerializer.legacyAmpersand().deserialize(message) else Component.text(message)
        server.console.sendMessage(component)
    }

    override fun registerUnknownPlaceholder(identifier: String) {
        val manager = TAB.getInstance().placeholderManager
        if (identifier.startsWith("%rel_")) {
            manager.registerPlayerPlaceholder(identifier, manager.defaultRefresh) { identifier }
            return
        }
        val serverIntervals = manager.serverPlaceholderRefreshIntervals
        val playerIntervals = manager.playerPlaceholderRefreshIntervals
        if (identifier.startsWith("%sync:")) {
            val refresh = when {
                serverIntervals.containsKey(identifier) -> serverIntervals[identifier]!!
                playerIntervals.containsKey(identifier) -> playerIntervals[identifier]!!
                else -> manager.defaultRefresh
            }
            manager.registerPlaceholder(object : PlayerPlaceholder(identifier, refresh, null) {

                override fun get(player: TabPlayer): Any? {
                    server.scheduler.run(plugin) {
                        val time = System.nanoTime()
                        lastValues[player.name] = identifier
                        if (!forceUpdate.contains(player.name)) forceUpdate.add(player.name)
                        TAB.getInstance().cpuManager.addPlaceholderTime(identifier, System.nanoTime() - time)
                    }
                    return lastValues[player.name]
                }
            })
            return
        }
        if (serverIntervals.containsKey(identifier)) {
            manager.registerServerPlaceholder(identifier, serverIntervals[identifier]!!) { identifier }
            return
        }
        val refresh = if (serverIntervals.containsKey(identifier)) serverIntervals[identifier]!! else manager.defaultRefresh
        manager.registerPlayerPlaceholder(identifier, refresh) { identifier }
    }

    override fun getServerVersion(): String = "${server.platform.name}v${server.platform.version}"

    override fun getDataFolder(): File = folder

    override fun callLoadEvent() {
        server.eventManager.fireAndForget(TabLoadEvent)
    }

    override fun callLoadEvent(player: TabPlayer) {
        server.eventManager.fireAndForget(TabPlayerLoadEvent(player))
    }

    override fun getMaxPlayers(): Int = server.maxPlayers

    override fun getPacketBuilder(): PacketBuilder = KryptonPacketBuilder

    override fun getSkin(properties: List<String>): Any = listOf(ProfileProperty.of("textures", properties[0], properties[1]))

    override fun isProxy(): Boolean = false

    override fun isPluginEnabled(plugin: String): Boolean = server.pluginManager.isLoaded(plugin)

    override fun getConfigName(): String = "kryptonconfig.yml"

    private fun loadNametagFeature(tab: TAB) {
        if (!tab.config.getBoolean("scoreboard-teams.enabled", true)) return
        if (tab.config.getBoolean("scoreboard-teams.unlimited-nametag-mode.enabled", false)) {
            tab.featureManager.registerFeature("nametagx", NameTagX())
        } else {
            tab.featureManager.registerFeature("nametag16", NameTag())
        }
    }
}
