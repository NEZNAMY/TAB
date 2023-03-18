package me.neznamy.tab.platforms.krypton

import me.neznamy.tab.api.ProtocolVersion
import me.neznamy.tab.api.chat.IChatBaseComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

// All build functions that just return the packet parameter will be passed through to be handled in KryptonTabPlayer.
object KryptonPacketBuilder {

    @JvmStatic
    fun toComponent(text: String?, clientVersion: ProtocolVersion): Component {
        if (text.isNullOrEmpty()) return Component.empty()
        return GsonComponentSerializer.gson().deserialize(IChatBaseComponent.optimizedComponent(text).toString(clientVersion))
    }
}
