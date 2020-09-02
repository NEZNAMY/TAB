package me.neznamy.tab.platforms.bukkit.nms;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing the n.m.s.PacketPlayOutAnimation class to make work with it much easier
 */
public class PacketPlayOutAnimation extends PacketPlayOut {

	private static Class<?> PacketPlayOutAnimation;
	private static Constructor<?> newPacketPlayOutAnimation;
	private static Field PacketPlayOutAnimation_ENTITYID;
	private static Field PacketPlayOutAnimation_ANIMATIONTYPE;
	
	private int entityId;
	private int animationType;
	
	public static void initializeClass() throws Exception {
		try {
			//1.7+
			PacketPlayOutAnimation = getNMSClass("PacketPlayOutAnimation");
		} catch (ClassNotFoundException e) {
			//1.6-
			PacketPlayOutAnimation = getNMSClass("Packet18ArmAnimation");
		}
		newPacketPlayOutAnimation = PacketPlayOutAnimation.getConstructor();
		(PacketPlayOutAnimation_ENTITYID = PacketPlayOutAnimation.getDeclaredField("a")).setAccessible(true);
		(PacketPlayOutAnimation_ANIMATIONTYPE = PacketPlayOutAnimation.getDeclaredField("b")).setAccessible(true);
	}
	
	public PacketPlayOutAnimation(int entityId, int animationType) {
		this.entityId = entityId;
		this.animationType = animationType;
	}
	
	@Override
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		Object packet = newPacketPlayOutAnimation.newInstance();
		PacketPlayOutAnimation_ENTITYID.set(packet, entityId);
		PacketPlayOutAnimation_ANIMATIONTYPE.set(packet, animationType);
		return packet;
	}
}