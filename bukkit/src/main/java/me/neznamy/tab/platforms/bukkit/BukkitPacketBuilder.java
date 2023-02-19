package me.neznamy.tab.platforms.bukkit;

import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.protocol.*;
import me.neznamy.tab.api.protocol.PacketPlayOutBoss.Action;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityDestroy;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityMetadata;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityTeleport;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.*;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.UUID;

public class BukkitPacketBuilder extends PacketBuilder {

    {
        buildMap.put(PacketPlayOutEntityMetadata.class, (packet, version) -> ((PacketPlayOutEntityMetadata)packet).build());
        buildMap.put(PacketPlayOutEntityTeleport.class, (packet, version) -> ((PacketPlayOutEntityTeleport)packet).build());
        buildMap.put(PacketPlayOutEntityDestroy.class, (packet, version) -> ((PacketPlayOutEntityDestroy)packet).build());
        buildMap.put(PacketPlayOutSpawnEntityLiving.class, (packet, version) -> ((PacketPlayOutSpawnEntityLiving)packet).build());
        buildMap.put(PacketPlayOutPlayerListHeaderFooter.class, (packet, version) -> PacketPlayOutPlayerListHeaderFooterStorage.build((PacketPlayOutPlayerListHeaderFooter) packet, version));
        buildMap.put(PacketPlayOutChat.class, (packet, version) -> PacketPlayOutChatStorage.build((PacketPlayOutChat) packet, version));
        buildMap.put(PacketPlayOutScoreboardObjective.class, (packet, version) -> PacketPlayOutScoreboardObjectiveStorage.build((PacketPlayOutScoreboardObjective) packet, version));
        buildMap.put(PacketPlayOutScoreboardDisplayObjective.class, (packet, version) -> PacketPlayOutScoreboardDisplayObjectiveStorage.build((PacketPlayOutScoreboardDisplayObjective) packet, version));
        buildMap.put(PacketPlayOutScoreboardTeam.class, (packet, version) -> PacketPlayOutScoreboardTeamStorage.build((PacketPlayOutScoreboardTeam) packet, version));
        buildMap.put(PacketPlayOutScoreboardScore.class, (packet, version) -> PacketPlayOutScoreboardScoreStorage.build((PacketPlayOutScoreboardScore) packet, version));
    }

    @Override
    public Object build(PacketPlayOutBoss packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        if (NMSStorage.getInstance().getMinorVersion() >= 9 || clientVersion.getMinorVersion() >= 9) {
            //1.9+ server or client, handled by bukkit api or ViaVersion
            return packet;
        }
        //<1.9 client and server
        return buildBossPacketEntity(packet, clientVersion);
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

    @Override
    public PacketPlayOutScoreboardDisplayObjective readDisplayObjective(Object nmsPacket) throws ReflectiveOperationException {
        return PacketPlayOutScoreboardDisplayObjectiveStorage.read(nmsPacket);
    }

    /**
     * Builds entity packet representing requested BossBar packet using Wither on 1.8- clients.
     *
     * @param   packet
     *          packet to build
     * @param   clientVersion
     *          client version
     * @return  entity BossBar packet
     * @throws  ReflectiveOperationException
     *          if thrown by reflective operation
     */
    private Object buildBossPacketEntity(PacketPlayOutBoss packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        if (packet.getAction() == Action.UPDATE_STYLE) return null; //nothing to do here

        int entityId = packet.getId().hashCode();
        if (packet.getAction() == Action.REMOVE) {
            return new PacketPlayOutEntityDestroy(entityId).build();
        }
        DataWatcher w = new DataWatcher();
        if (packet.getAction() == Action.UPDATE_PCT || packet.getAction() == Action.ADD) {
            float health = 300*packet.getPct();
            if (health == 0) health = 1;
            w.getHelper().setHealth(health);
        }
        if (packet.getAction() == Action.UPDATE_NAME || packet.getAction() == Action.ADD) {
            w.getHelper().setCustomName(packet.getName(), clientVersion);
        }
        if (packet.getAction() == Action.ADD) {
            w.getHelper().setEntityFlags((byte) 32);
            w.getHelper().setWitherInvulnerableTime(880); // Magic number
            return new PacketPlayOutSpawnEntityLiving(entityId, new UUID(0, 0), EntityType.WITHER, new Location(null, 0,0,0), w).build();
        } else {
            return new PacketPlayOutEntityMetadata(entityId, w).build();
        }
    }
}