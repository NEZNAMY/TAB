package me.neznamy.tab.platforms.krypton

import me.neznamy.tab.api.TabConstants
import me.neznamy.tab.api.TabFeature
import me.neznamy.tab.api.protocol.PacketBuilder
import me.neznamy.tab.platforms.krypton.features.unlimitedtags.KryptonNameTagX
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.backend.BackendPlatform
import me.neznamy.tab.shared.features.PipelineInjector
import me.neznamy.tab.shared.features.TabExpansion
import me.neznamy.tab.shared.features.nametags.NameTag
import me.neznamy.tab.shared.permission.LuckPerms
import me.neznamy.tab.shared.permission.None
import me.neznamy.tab.shared.permission.PermissionPlugin
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import java.util.*

class KryptonPlatform(private val plugin: Main) : BackendPlatform() {

    private val server = plugin.server

    override fun detectPermissionPlugin(): PermissionPlugin {
        if (server.pluginManager.isLoaded(TabConstants.Plugin.LUCKPERMS.lowercase(Locale.getDefault())))
            return LuckPerms(getPluginVersion(TabConstants.Plugin.LUCKPERMS.lowercase(Locale.getDefault())))
        return None()
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
            manager.registerServerPlaceholder(identifier, serverIntervals.get(identifier)!!) { identifier }
            return
        }
        if (playerIntervals.containsKey(identifier)) {
            manager.registerPlayerPlaceholder(identifier, playerIntervals.get(identifier)!!) { identifier }
            return
        }
        manager.registerPlayerPlaceholder(identifier, manager.defaultRefresh) { identifier }
    }

    override fun getPluginVersion(plugin: String): String? = server.pluginManager.getPlugin(plugin)?.description?.version

    override fun loadPlayers() {
        server.players.forEach { TAB.getInstance().addPlayer(KryptonTabPlayer(it, plugin.getProtocolVersion(it))) }
    }

    override fun registerPlaceholders() {
        KryptonPlaceholderRegistry(plugin).registerPlaceholders(TAB.getInstance().placeholderManager)
        UniversalPlaceholderRegistry().registerPlaceholders(TAB.getInstance().placeholderManager)
    }

    override fun getPipelineInjector(): PipelineInjector {
        return KryptonPipelineInjector()
    }

    override fun getUnlimitedNametags(): NameTag {
        return KryptonNameTagX(plugin)
    }

    override fun getTabExpansion(): TabExpansion? {
        return null
    }

    override fun getPetFix(): TabFeature? {
        return null
    }

    override fun getPerWorldPlayerlist(): TabFeature? {
        return null
    }

    override fun createPacketBuilder(): PacketBuilder {
        return KryptonPacketBuilder
    }
}
