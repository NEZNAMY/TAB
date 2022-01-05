package me.neznamy.tab.shared.features;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;

/**
 * Universal interface for both proxies to manage plugin messages
 */
public abstract class PluginMessageHandler {

	//name of plugin messaging channel
	protected final String channelName = "tab:placeholders";
	
	/**
	 * Requests placeholder from bukkit server
	 * @param player - player to request placeholder for
	 * @param placeholder - placeholder identifier
	 */
	public void requestPlaceholder(TabPlayer player, String placeholder) {
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
	public void requestAttribute(TabPlayer player, String attribute) {
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
	public void onPluginMessage(ProxyTabPlayer player, ByteArrayDataInput in) {
		if (TAB.getInstance().isDisabled()) return; //reload in progress
		String subChannel = in.readUTF();
		if (subChannel.equalsIgnoreCase("Placeholder")){
			String placeholder = in.readUTF();
			String output = in.readUTF();
			long cpu = in.readLong();
			PlayerPlaceholderImpl pl = (PlayerPlaceholderImpl) TAB.getInstance().getPlaceholderManager().getPlaceholder(placeholder); //all bridge placeholders are marked as player
			pl.updateValue(player, output);
			TAB.getInstance().getCPUManager().addBridgePlaceholderTime(pl.getIdentifier(), cpu);
		}
		if ("Attribute".equals(subChannel)) {
			String attribute = in.readUTF();
			String value = in.readUTF();
			player.setAttribute(attribute, value);
			if ("world".equals(attribute)) {
				TAB.getInstance().getFeatureManager().onWorldChange(player.getUniqueId(), value);
			}
			if ("vanished".equals(attribute)) {
				((PlayerPlaceholderImpl) TAB.getInstance().getPlaceholderManager().getPlaceholder("%vanished%")).updateValue(player, value);
			}
		}
		if ("Group".equals(subChannel)) {
			String group = in.readUTF();
			player.setGroup(group, true);
		}
	}

	/**
	 * Sends plugin message
	 * @param player - player to go through
	 * @param message - message
	 */
	public abstract void sendPluginMessage(TabPlayer player, byte[] message);
}
