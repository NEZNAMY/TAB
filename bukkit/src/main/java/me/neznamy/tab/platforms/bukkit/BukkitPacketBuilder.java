package me.neznamy.tab.platforms.bukkit;

import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutEntityDestroyStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutEntityMetadataStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutEntityTeleportStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutSpawnEntityLivingStorage;
import me.neznamy.tab.shared.backend.protocol.PacketPlayOutEntityDestroy;
import me.neznamy.tab.shared.backend.protocol.PacketPlayOutEntityMetadata;
import me.neznamy.tab.shared.backend.protocol.PacketPlayOutEntityTeleport;
import me.neznamy.tab.shared.backend.protocol.PacketPlayOutSpawnEntityLiving;

public class BukkitPacketBuilder extends PacketBuilder {

    {
        buildMap.put(PacketPlayOutEntityMetadata.class, (packet, version) -> PacketPlayOutEntityMetadataStorage.build((PacketPlayOutEntityMetadata) packet));
        buildMap.put(PacketPlayOutEntityTeleport.class, (packet, version) -> PacketPlayOutEntityTeleportStorage.build((PacketPlayOutEntityTeleport) packet));
        buildMap.put(PacketPlayOutEntityDestroy.class, (packet, version) -> PacketPlayOutEntityDestroyStorage.build((PacketPlayOutEntityDestroy) packet));
        buildMap.put(PacketPlayOutSpawnEntityLiving.class, (packet, version) -> PacketPlayOutSpawnEntityLivingStorage.build((PacketPlayOutSpawnEntityLiving) packet));
    }
}