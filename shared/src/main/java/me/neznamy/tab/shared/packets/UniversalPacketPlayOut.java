package me.neznamy.tab.shared.packets;

import java.lang.reflect.InvocationTargetException;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;

/**
 * Abstract class to be extended by packets which can be sent on all 3 supported platforms
 */
public abstract class UniversalPacketPlayOut {
	
	/**
	 * Converts the class into raw packet
	 * @param clientVersion - version of player to create packet for
	 * @return the raw packet
	 * @throws InvocationTargetException 

	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws NegativeArraySizeException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	protected abstract Object build(ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException, NegativeArraySizeException;
	
	/**
	 * Calls build(...) and wraps it into a try/catch
	 * @param clientVersion - player version
	 * @return built packet depending on platform
	 */
	public Object create(ProtocolVersion clientVersion) {
		try {
			return build(clientVersion);
		} catch (Exception e) {
			return TAB.getInstance().getErrorManager().printError(null, "An error occurred when creating " + getClass().getSimpleName(), e);
		}
	}
}