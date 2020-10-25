package me.neznamy.tab.platforms.proxy.velocity;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

/**
 * Velocity API calls which cannot be in the main class because unsupported velocity version would throw an exception
 * when loading class without any chance to check for compatibility
 */
public class VelocityUtils {

	/**
	 * Converts provided json string into component
	 * @param json - string to convert
	 * @return component from given string or null if string is null
	 */
	public static Component stringToComponent(String json) {
		if (json == null) return null;
		return GsonComponentSerializer.gson().deserialize(json);
	}
	
	/**
	 * Converts provided component into json string
	 * @param component - component to convert
	 * @return json string or null if component is null
	 */
	public static String componentToString(Component component) {
		if (component == null) return null;
		return GsonComponentSerializer.gson().serialize(component);
	}
}