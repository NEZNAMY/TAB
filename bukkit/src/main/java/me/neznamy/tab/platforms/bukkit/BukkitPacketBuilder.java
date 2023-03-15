package me.neznamy.tab.platforms.bukkit;

import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.protocol.*;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.*;
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
        buildMap.put(PacketPlayOutScoreboardObjective.class, (packet, version) -> PacketPlayOutScoreboardObjectiveStorage.build((PacketPlayOutScoreboardObjective) packet, version));
        buildMap.put(PacketPlayOutScoreboardTeam.class, (packet, version) -> PacketPlayOutScoreboardTeamStorage.build((PacketPlayOutScoreboardTeam) packet, version));
    }

    @Override
    public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        return PacketPlayOutPlayerInfoStorage.build(packet, clientVersion);
    }

    @Override
    public PacketPlayOutPlayerInfo readPlayerInfo(Object nmsPacket, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        return PacketPlayOutPlayerInfoStorage.read(nmsPacket);
    }

    @Override
    public PacketPlayOutScoreboardObjective readObjective(Object nmsPacket) throws ReflectiveOperationException {
        return PacketPlayOutScoreboardObjectiveStorage.read(nmsPacket);
    }
}