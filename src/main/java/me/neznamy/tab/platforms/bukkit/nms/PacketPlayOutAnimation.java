package me.neznamy.tab.platforms.bukkit.nms;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing the n.m.s.PacketPlayOutAnimation class to make work with it much easier
 */
public class PacketPlayOutAnimation extends PacketPlayOut {

	//used constructor and fields
	private static Constructor<?> newPacketPlayOutAnimation;
	private static Field PacketPlayOutAnimation_ENTITYID;
	private static Field PacketPlayOutAnimation_ANIMATIONTYPE;
	
	//entity id
	private int entityId;
	
	//animation type
	private int animationType;
	
	/**
	 * Initializes required NMS classes and fields
	 * @throws Exception - if something fails
	 */
	public static void initializeClass() throws Exception {
		Class<?> PacketPlayOutAnimation;
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
	
	/**
	 * Constructs new instance of this class
	 * @param entityId entity id
	 * @param animationType animation type
	 */
	public PacketPlayOutAnimation(int entityId, int animationType) {
		this.entityId = entityId;
		this.animationType = animationType;
	}
	
	/**
	 * Converts the custom class into an actual minecraft packet
	 * @param clientVersion client version to build the packet for
	 * @return NMS packet
	 * @throws Exception if something fails
	 */
	@Override
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		Object packet = newPacketPlayOutAnimation.newInstance();
		PacketPlayOutAnimation_ENTITYID.set(packet, entityId);
		PacketPlayOutAnimation_ANIMATIONTYPE.set(packet, animationType);
		return packet;
	}
}