package me.neznamy.tab.platforms.krypton

import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.protocol.PacketBuilder
import me.neznamy.tab.platforms.krypton.features.PerWorldPlayerList
import me.neznamy.tab.platforms.krypton.features.unlimitedtags.NameTagX
import me.neznamy.tab.shared.Platform
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.TabConstants
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl
import me.neznamy.tab.shared.features.nametags.NameTag
import me.neznamy.tab.shared.permission.LuckPerms
import me.neznamy.tab.shared.permission.None
import me.neznamy.tab.shared.permission.PermissionPlugin
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.kryptonmc.api.auth.ProfileProperty
import java.io.File

class KryptonPlatform(
    private val plugin: Main,
    private val folder: File
) : Platform {

    private val server = plugin.server

    override fun detectPermissionPlugin(): PermissionPlugin {
        if (isPluginEnabled("luckperms")) return LuckPerms(server.pluginManager.plugin("luckperms")!!.description.version)
        return None()
    }

    override fun loadFeatures() {
        if (TAB.getInstance().configuration.isPipelineInjection) {
            TAB.getInstance().featureManager.registerFeature(TabConstants.Feature.PIPELINE_INJECTION, KryptonPipelineInjector())
        }

        // Placeholders
        KryptonPlaceholderRegistry(plugin).registerPlaceholders(TAB.getInstance().placeholderManager)
        UniversalPlaceholderRegistry().registerPlaceholders(TAB.getInstance().placeholderManager)

        // Load features
        loadNametagFeature()
        TAB.getInstance().loadUniversalFeatures()
        if (TAB.getInstance().config.getBoolean("bossbar.enabled", false)) {
            TAB.getInstance().featureManager.registerFeature(TabConstants.Feature.BOSS_BAR, BossBarManagerImpl())
        }
        if (TAB.getInstance().config.getBoolean("per-world-playerlist.enabled", false)) {
            TAB.getInstance().featureManager.registerFeature(TabConstants.Feature.PER_WORLD_PLAYER_LIST, PerWorldPlayerList(plugin))
        }
        server.players.forEach { TAB.getInstance().addPlayer(KryptonTabPlayer(it, plugin.protocolVersion(it))) }
    }

    override fun sendConsoleMessage(message: String, translateColors: Boolean) {
        val component = if (translateColors) LegacyComponentSerializer.legacyAmpersand().deserialize(message) else Component.text(message)
        server.console.sendMessage(component)
    }

    override fun registerUnknownPlaceholder(identifier: String) {
        val manager = TAB.getInstance().placeholderManager
        if (identifier.startsWith("%rel_")) {
            // One day, when PlaceholderAPI v3 is a thing, this will work. One day...
            manager.registerRelationalPlaceholder(identifier, manager.getRelationalRefresh(identifier)) { _, _ -> "" }
            return
        }
        val serverIntervals = manager.serverPlaceholderRefreshIntervals
        val playerIntervals = manager.playerPlaceholderRefreshIntervals
        if (serverIntervals.containsKey(identifier)) {
            manager.registerServerPlaceholder(identifier, serverIntervals[identifier]!!) { identifier }
            return
        }
        if (playerIntervals.containsKey(identifier)) {
            manager.registerPlayerPlaceholder(identifier, playerIntervals[identifier]!!) { identifier }
            return
        }
        manager.registerPlayerPlaceholder(identifier, manager.defaultRefresh) { identifier }
    }

    override fun getServerVersion(): String = "${server.platform.name} ${server.platform.version}"

    override fun getDataFolder(): File = folder

    override fun callLoadEvent() {
        // do nothing here, this will be removed
    }

    override fun callLoadEvent(player: TabPlayer) {
        // do nothing here, this will be removed
    }

    override fun getMaxPlayers(): Int = server.maxPlayers

    override fun getPacketBuilder(): PacketBuilder = KryptonPacketBuilder

    override fun getSkin(properties: List<String>): Any = listOf(ProfileProperty.of("textures", properties[0], properties[1]))

    override fun isProxy(): Boolean = false

    override fun isPluginEnabled(plugin: String): Boolean = server.pluginManager.isLoaded(plugin)

    override fun getConfigName(): String = "kryptonconfig.yml"

    private fun loadNametagFeature() {
        if (!TAB.getInstance().config.getBoolean("scoreboard-teams.enabled", true)) return
        if (TAB.getInstance().config.getBoolean("scoreboard-teams.unlimited-nametag-mode.enabled", false)) {
            TAB.getInstance().featureManager.registerFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS, NameTagX(plugin))
        } else {
            TAB.getInstance().featureManager.registerFeature(TabConstants.Feature.NAME_TAGS, NameTag())
        }
    }
}
