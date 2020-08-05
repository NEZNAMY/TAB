package me.neznamy.tab.platforms.bukkit.packets;

import java.lang.reflect.Constructor;

import me.neznamy.tab.shared.ProtocolVersion;

public class PacketPlayOutEntityMetadata extends PacketPlayOut {

	private static Class<?> PacketPlayOutEntityMetadata = PacketPlayOut.getNMSClass("PacketPlayOutEntityMetadata");
	private static Constructor<?> newPacketPlayOutEntityMetadata = PacketPlayOut.getConstructor(PacketPlayOutEntityMetadata, 3);
	
	private int entityId;
	private DataWatcher dataWatcher;

	public PacketPlayOutEntityMetadata(int entityId, DataWatcher dataWatcher) {
		this.entityId = entityId;
		this.dataWatcher = dataWatcher;
	}
	
	@Override
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		return newPacketPlayOutEntityMetadata.newInstance(entityId, dataWatcher.toNMS(), true);
	}
}