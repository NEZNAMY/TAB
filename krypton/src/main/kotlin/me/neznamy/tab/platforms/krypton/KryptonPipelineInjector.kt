package me.neznamy.tab.platforms.krypton

import me.neznamy.tab.api.ProtocolVersion
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.chat.IChatBaseComponent
import me.neznamy.tab.api.chat.WrappedChatComponent
import me.neznamy.tab.api.util.ComponentCache
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.features.injection.PipelineInjector
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.kryptonmc.krypton.network.NioConnection
import org.kryptonmc.krypton.network.handlers.PacketHandler
import org.kryptonmc.krypton.network.interceptor.PacketInterceptor
import org.kryptonmc.krypton.network.interceptor.PacketInterceptorRegistry
import org.kryptonmc.krypton.packet.GenericPacket
import org.kryptonmc.krypton.packet.InboundPacket
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerInfoUpdate
import org.kryptonmc.krypton.util.enumhelper.GameModes
import java.util.concurrent.ConcurrentHashMap

class KryptonPipelineInjector : PipelineInjector() {

    private val interceptor = TabPacketInterceptor()
    private val componentCache = ComponentCache<IChatBaseComponent, Component>(10000) { component, clientVersion ->
        GsonComponentSerializer.gson().deserialize(component.toString(clientVersion))
    }
    private val players = ConcurrentHashMap<NioConnection, TabPlayer>()

    override fun inject(player: TabPlayer) {
        if (player !is KryptonTabPlayer) return
        if (players.isEmpty()) {
            // If it's empty, no players have been registered yet, and we should add the interceptor
            PacketInterceptorRegistry.register(interceptor)
        }
        players.put(player.player.connection, player)
    }

    override fun uninject(player: TabPlayer) {
        if (player !is KryptonTabPlayer) return
        players.remove(player.player.connection)
        if (players.isEmpty()) {
            // If it's empty, we unregistered the last player, and we should remove the interceptor
            PacketInterceptorRegistry.unregister(interceptor)
        }
    }

    private inner class TabPacketInterceptor : PacketInterceptor {

        override fun onSend(connection: NioConnection, packet: GenericPacket): GenericPacket {
            val player = players.get(connection) ?: return packet // Packet needs to be returned unmodified to be sent
            if (packet is PacketOutPlayerInfoUpdate) return rewritePlayerInfo(player, packet)
            TAB.getInstance().featureManager.onPacketSend(player, packet)
            return packet
        }

        private fun rewritePlayerInfo(receiver: TabPlayer, packet: PacketOutPlayerInfoUpdate): PacketOutPlayerInfoUpdate {
            val actions = packet.actions
            var newEntries: MutableList<PacketOutPlayerInfoUpdate.Entry>? = null

            for (entry in packet.entries) {
                val profile = entry.profile
                var gameMode = entry.gameMode
                var latency = entry.latency
                var displayName = entry.displayName
                var updated = false

                if (actions.contains(PacketOutPlayerInfoUpdate.Action.UPDATE_GAME_MODE)) {
                    val newGameMode = TAB.getInstance().featureManager.onGameModeChange(receiver, profile.uuid, gameMode.ordinal)
                    if (newGameMode != gameMode.ordinal) {
                        updated = true
                        gameMode = GameModes.fromId(newGameMode)!!
                    }
                }
                if (actions.contains(PacketOutPlayerInfoUpdate.Action.UPDATE_LATENCY)) {
                    val newLatency = TAB.getInstance().featureManager.onLatencyChange(receiver, profile.uuid, latency)
                    if (newLatency != latency) {
                        updated = true
                        latency = newLatency
                    }
                }
                if (actions.contains(PacketOutPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME)) {
                    val input = if (displayName == null) null else WrappedChatComponent(displayName)
                    val output = TAB.getInstance().featureManager.onDisplayNameChange(receiver, profile.uuid, input)
                    if (output != input) {
                        updated = true
                        displayName = convertComponent(output, receiver.version)
                    }
                }
                if (actions.contains(PacketOutPlayerInfoUpdate.Action.ADD_PLAYER)) {
                    TAB.getInstance().featureManager.onEntryAdd(receiver, profile.uuid, profile.name)
                }
                if (!updated) {
                    // Don't bother rewriting the entry if nothing was changed
                    continue
                }

                val result = PacketOutPlayerInfoUpdate.Entry(profile.uuid, profile, entry.listed, latency, gameMode, displayName, entry.chatSession)
                if (newEntries == null) newEntries = ArrayList()
                newEntries.add(result)
            }

            if (newEntries.isNullOrEmpty()) {
                // Don't bother rewriting the packet if there were no updated entries
                return packet
            }
            return PacketOutPlayerInfoUpdate(packet.actions, newEntries)
        }

        private fun convertComponent(component: IChatBaseComponent, clientVersion: ProtocolVersion): Component? {
            if (component is WrappedChatComponent) return component.originalComponent as? Component
            return componentCache.get(component, clientVersion)
        }

        override fun <H : PacketHandler> onReceive(connection: NioConnection, packet: InboundPacket<H>): InboundPacket<H>? {
            val player = players.get(connection) ?: return packet // Packet needs to be returned unmodified to be received
            if (TAB.getInstance().featureManager.onPacketReceive(player, packet)) return null // Returning null stops the packet from being received
            return packet
        }
    }
}
