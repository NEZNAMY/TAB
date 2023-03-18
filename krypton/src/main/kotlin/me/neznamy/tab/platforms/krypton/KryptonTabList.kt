package me.neznamy.tab.platforms.krypton

import me.neznamy.tab.api.chat.IChatBaseComponent
import me.neznamy.tab.api.tablist.TabListEntry
import me.neznamy.tab.shared.tablist.BulkUpdateTabList
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.kryptonmc.api.auth.GameProfile
import org.kryptonmc.api.world.GameMode
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerInfoRemove
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerInfoUpdate
import java.util.*

class KryptonTabList(private val player: KryptonTabPlayer) : BulkUpdateTabList() {

    override fun removeEntries(entries: MutableCollection<UUID>) {
        player.sendPacket(PacketOutPlayerInfoRemove(ArrayList(entries)))
    }

    override fun updateDisplayNames(entries: MutableMap<UUID, IChatBaseComponent?>) {
        player.sendPacket(PacketOutPlayerInfoUpdate(EnumSet.of(PacketOutPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME), entries.map { entry ->
            PacketOutPlayerInfoUpdate.Entry(
                entry.key,
                GameProfile.of("", entry.key),
                false,
                0,
                GameMode.SURVIVAL,
                entry.value?.toString(player.version)?.let { GsonComponentSerializer.gson().deserialize(it) },
                null
            )
        }))
    }

    override fun updateLatencies(entries: MutableMap<UUID, Int>) {
        player.sendPacket(PacketOutPlayerInfoUpdate(EnumSet.of(PacketOutPlayerInfoUpdate.Action.UPDATE_LATENCY), entries.map { entry ->
            PacketOutPlayerInfoUpdate.Entry(
                entry.key,
                GameProfile.of("", entry.key),
                false,
                entry.value,
                GameMode.SURVIVAL,
                null,
                null
            )
        }))
    }

    override fun updateGameModes(entries: MutableMap<UUID, Int>) {
        player.sendPacket(PacketOutPlayerInfoUpdate(EnumSet.of(PacketOutPlayerInfoUpdate.Action.UPDATE_GAME_MODE), entries.map { entry ->
            PacketOutPlayerInfoUpdate.Entry(
                entry.key,
                GameProfile.of("", entry.key),
                false,
                0,
                GameMode.values()[entry.value],
                null,
                null
            )
        }))
    }

    override fun addEntries(entries: MutableCollection<TabListEntry>) {
        player.sendPacket(PacketOutPlayerInfoUpdate(EnumSet.of(PacketOutPlayerInfoUpdate.Action.ADD_PLAYER), entries.map { entry ->
            PacketOutPlayerInfoUpdate.Entry(
                entry.uniqueId,
                GameProfile.of(entry.name ?: "", entry.uniqueId),
                entry.isListed,
                entry.latency,
                GameMode.values()[entry.gameMode],
                entry.displayName?.toString(player.version)?.let { GsonComponentSerializer.gson().deserialize(it) },
                null //TODO chat session?
            )
        }))
    }
}