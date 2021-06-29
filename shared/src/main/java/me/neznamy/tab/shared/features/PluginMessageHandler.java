package me.neznamy.tab.shared.features;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;

/**
 * Universal interface for both proxies to manage plugin messages
 */
public interface PluginMessageHandler {

	//name of plugin messaging channel
	public String CHANNEL_NAME = "tab:placeholders";
	
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
	 * Handles incoming plugin message with tab's channel name
	 * @param player - plugin message receiver
	 * @param in - incoming message
	 */
	public default void onPluginMessage(TabPlayer player, ByteArrayDataInput in) {
		if (TAB.getInstance().isDisabled()) return; //reload in progress
		String subChannel = in.readUTF();
		if (subChannel.equalsIgnoreCase("Placeholder")){
			String placeholder = in.readUTF();
			String output = in.readUTF();
			long cpu = in.readLong();
			PlayerPlaceholder pl = (PlayerPlaceholder) TAB.getInstance().getPlaceholderManager().getPlaceholder(placeholder); //all bridge placeholders are marked as player
			if (pl != null) {
				pl.getLastValues().put(player.getName(), output);
				pl.getForceUpdate().add(player.getName());
				TAB.getInstance().getCPUManager().addBridgePlaceholderTime(pl.getIdentifier(), cpu);
			} else {
				TAB.getInstance().debug("Received output for unknown placeholder " + placeholder);
			}
		}
		if (subChannel.equals("Attribute")) {
			String attribute = in.readUTF();
			String value = in.readUTF();
			player.setAttribute(attribute, value);
		}
		if (subChannel.equals("Group")) {
			String group = in.readUTF();
			((ITabPlayer) player).setGroup(group, true);
		}
	}
	
	/**
	 * Sends plugin message
	 * @param player - player to go through
	 * @param message - message
	 */
	public void sendPluginMessage(TabPlayer player, byte[] message);
}
