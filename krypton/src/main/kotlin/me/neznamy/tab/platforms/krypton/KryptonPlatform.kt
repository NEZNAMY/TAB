package me.neznamy.tab.platforms.krypton

import me.neznamy.tab.api.TabFeature
import me.neznamy.tab.api.protocol.PacketBuilder
import me.neznamy.tab.platforms.krypton.features.unlimitedtags.KryptonNameTagX
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.backend.BackendPlatform
import me.neznamy.tab.shared.features.injection.PipelineInjector
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion
import me.neznamy.tab.shared.features.nametags.NameTag
import me.neznamy.tab.shared.features.sorting.Sorting
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

class KryptonPlatform(private val plugin: Main) : BackendPlatform() {

    private val server = plugin.server
    private val packetBuilder = KryptonPacketBuilder

    override fun sendConsoleMessage(message: String, translateColors: Boolean) {
        val component = if (translateColors) LegacyComponentSerializer.legacyAmpersand().deserialize(message) else Component.text(message)
        val actualMessage = Component.text().append(Component.text("[TAB] ")).append(component).build()
        server.console.sendMessage(actualMessage)
    }

    override fun registerUnknownPlaceholder(identifier: String) {
        TAB.getInstance().placeholderManager.registerServerPlaceholder(identifier, -1) { identifier }
    }

    override fun getPluginVersion(plugin: String): String? = server.pluginManager.getPlugin(plugin.lowercase())?.description?.version

    override fun loadPlayers() {
        server.players.forEach { TAB.getInstance().addPlayer(KryptonTabPlayer(it, plugin.getProtocolVersion(it))) }
    }

    override fun registerPlaceholders() {
        KryptonPlaceholderRegistry(plugin).registerPlaceholders(TAB.getInstance().placeholderManager)
    }

    override fun getPipelineInjector(): PipelineInjector {
        return KryptonPipelineInjector()
    }

    override fun getUnlimitedNametags(sorting: Sorting): NameTag {
        return KryptonNameTagX(plugin, sorting)
    }

    override fun getTabExpansion(): TabExpansion {
        return EmptyTabExpansion()
    }

    override fun getPetFix(): TabFeature? {
        return null
    }

    override fun getPerWorldPlayerlist(): TabFeature? {
        return null
    }

    override fun getPacketBuilder(): PacketBuilder {
        return packetBuilder
    }
}
