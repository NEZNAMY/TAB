package me.neznamy.tab.shared.packets;

import java.lang.reflect.Constructor;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;

public abstract class UniversalPacketPlayOut{

	public abstract Object toNMS(ProtocolVersion clientVersion) throws Exception;
	public abstract Object toBungee(ProtocolVersion clientVersion);
	public abstract Object toVelocity(ProtocolVersion clientVersion);
	
	public static Class<?> getNMSClass(String name) throws Exception{
		return Class.forName("net.minecraft.server." + ProtocolVersion.packageName + "." + name);
	}
	public static Constructor<?> getConstructor(Class<?> clazz, int parameterCount){
		for (Constructor<?> c : clazz.getDeclaredConstructors()) {
			if (c.getParameterCount() == parameterCount) return c;
		}
		return null;
	}
	public void send(ITabPlayer to) {
		try {
			Object packet = Shared.mainClass.toNMS(this, to.getVersion());
			if (packet != null) to.sendPacket(packet);
		} catch (Exception e) {
			Shared.error("An error occured when creating " + getClass().getSimpleName(), e);
		}
	}
}