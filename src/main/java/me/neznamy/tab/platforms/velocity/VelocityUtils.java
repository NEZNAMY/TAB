package me.neznamy.tab.platforms.velocity;

import me.neznamy.tab.shared.placeholders.Placeholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

/**
 * Velocity API calls which cannot be in the main class because unsupported velocity version would throw an exception
 * when loading class without any chance to check for compatibility
 */
public class VelocityUtils {

	//java class loader throws NoClassDefFoundError in inactive code (PacketPlayOutPlayerInfo#toVelocity)
	//making it return Object and then casting fixes it
	public static Object componentFromString(String json) {
		if (json == null) return null;
		return GsonComponentSerializer.gson().deserialize(json);
	}
	
	public static String componentToString(Component component) {
		if (component == null) return null;
		return GsonComponentSerializer.gson().serialize(component);
	}
	
	public static TextComponent asColoredComponent(String text) {
		return TextComponent.of(Placeholders.color(text));
	}
}