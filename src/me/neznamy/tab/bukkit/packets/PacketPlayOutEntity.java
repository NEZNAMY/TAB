package me.neznamy.tab.bukkit.packets;

import java.lang.reflect.Field;

import me.neznamy.tab.shared.Shared;

public abstract class PacketPlayOutEntity extends PacketPlayOut{

	protected int entityId;

	public PacketPlayOutEntity() {
	}

	public PacketPlayOutEntity(int entityId) {
		this.entityId = entityId;
	}

	public static class PacketPlayOutRelEntityMoveLook extends PacketPlayOutEntity{
		public PacketPlayOutRelEntityMoveLook(int entityId) {
			super(entityId);
		}
		public int getEntityId() {
			return entityId;
		}
		public Object toNMS() {
			throw new IllegalStateException();
		}
		public static PacketPlayOutRelEntityMoveLook read(Object nmsPacket) throws Exception{
			if (!PacketPlayOutRelEntityMoveLook.isInstance(nmsPacket)) return null;
			int entityId = PacketPlayOutEntity_ENTITYID.getInt(nmsPacket);
			return new PacketPlayOutRelEntityMoveLook(entityId);
		}
	}

	public static class PacketPlayOutRelEntityMove extends PacketPlayOutEntity{
		public PacketPlayOutRelEntityMove(int entityId) {
			super(entityId);
		}
		public int getEntityId() {
			return entityId;
		}
		public Object toNMS() {
			throw new IllegalStateException();
		}
		public static PacketPlayOutRelEntityMove read(Object nmsPacket) throws Exception{
			if (!PacketPlayOutRelEntityMove.isInstance(nmsPacket)) return null;
			int entityId = PacketPlayOutEntity_ENTITYID.getInt(nmsPacket);
			return new PacketPlayOutRelEntityMove(entityId);
		}
	}

	private static Class<?> PacketPlayOutEntity;
	private static Class<?> PacketPlayOutRelEntityMove;
	private static Class<?> PacketPlayOutRelEntityMoveLook;
	private static Field PacketPlayOutEntity_ENTITYID;

	static {
		try {
			PacketPlayOutEntity = getNMSClass("PacketPlayOutEntity");
			if (version.equals("v1_8_R1")) {
				PacketPlayOutRelEntityMove = getNMSClass("PacketPlayOutRelEntityMove");
				PacketPlayOutRelEntityMoveLook = getNMSClass("PacketPlayOutRelEntityMoveLook");
			} else {
				PacketPlayOutRelEntityMove = getNMSClass("PacketPlayOutEntity$PacketPlayOutRelEntityMove");
				PacketPlayOutRelEntityMoveLook = getNMSClass("PacketPlayOutEntity$PacketPlayOutRelEntityMoveLook");
			}
			(PacketPlayOutEntity_ENTITYID = PacketPlayOutEntity.getDeclaredField("a")).setAccessible(true);
		} catch (Exception e) {
			Shared.error("Failed to initialize PacketPlayOutEntity class", e);
		}
	}
}