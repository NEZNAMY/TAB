package me.neznamy.tab.shared.features;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;

/**
 * Universal interface for both proxies to manage plugin messages
 */
public interface PluginMessageHandler {

	/**
	 * Requests placeholder from bukkit server
	 * @param player - player to request placeholder for
	 * @param placeholder - placeholder identifier
	 */
	public default void requestPlaceholder(TabPlayer player, String placeholder) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Placeholder");
		out.writeUTF(placeholder);
		sendPluginMessage(player, out.toByteArray());
	}

	/**
	 * Requests attribute from bukkit server
	 * @param player - player to request attribute for
	 * @param attribute - attribute
	 */
	public default void requestAttribute(TabPlayer player, String attribute) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Attribute");
		out.writeUTF(attribute);
		sendPluginMessage(player, out.toByteArray());
	}
	
	/**
	 * Handles incoming plugin message
	 * @param player - plugin message receiver
	 * @param in - incoming message
	 * @return true if message was for TAB and event should be cancelled, false if not
	 */
	public default boolean onPluginMessage(TabPlayer player, ByteArrayDataInput in) {
		String subChannel = in.readUTF();
		if (subChannel.equalsIgnoreCase("Placeholder")){
			String placeholder = in.readUTF();
			String output = in.readUTF();
			long cpu = in.readLong();
			PlayerPlaceholder pl = (PlayerPlaceholder) Placeholders.getPlaceholder(placeholder); //all bridge placeholders are marked as player
			if (pl != null) {
				pl.lastValue.put(player.getName(), output);
				pl.forceUpdate.add(player.getName());
				Shared.cpu.addBridgePlaceholderTime(pl.getIdentifier(), cpu);
			} else {
				Shared.debug("Received output for unknown placeholder " + placeholder);
			}
			return true;
		}
		if (subChannel.equals("Attribute")) {
			String attribute = in.readUTF();
			String value = in.readUTF();
			player.setAttribute(attribute, value);
			return true;
		}
		return false;
	}
	
	/**
	 * Sends plugin message
	 * @param player - player to go through
	 * @param message - message
	 */
	public void sendPluginMessage(TabPlayer player, byte[] message);
}
