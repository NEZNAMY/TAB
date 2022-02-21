package me.neznamy.tab.shared.proxy;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.api.placeholder.RelationalPlaceholder;
import me.neznamy.tab.api.util.Preconditions;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.permission.VaultBridge;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholderImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Universal interface for proxy to manage plugin messages
 */
public class PluginMessageHandler {

	/**
	 * Handles incoming plugin message with tab's channel name
	 *
	 * @param	uuid
	 * 			plugin message receiver
	 * @param	bytes
	 * 			incoming message
	 */
	public void onPluginMessage(UUID uuid, byte[] bytes) {
		if (TAB.getInstance().isDisabled()) return; //reload in progress
		ProxyTabPlayer player = (ProxyTabPlayer) TAB.getInstance().getPlayer(uuid);
		if (player == null) return;
		TAB.getInstance().getCPUManager().runMeasuredTask("Plugin message handling",
				TabConstants.CpuUsageCategory.PLUGIN_MESSAGE, () -> {
					ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
					String subChannel = in.readUTF();
					if ("Placeholder".equals(subChannel)){
						Placeholder placeholder = TAB.getInstance().getPlaceholderManager().getPlaceholder(in.readUTF());
						if (placeholder instanceof RelationalPlaceholder) {
							((RelationalPlaceholder)placeholder).updateValue(player, TAB.getInstance().getPlayer(in.readUTF()), in.readUTF());
						} else {
							((PlayerPlaceholder)placeholder).updateValue(player, in.readUTF());
						}
					}
					if ("Vanished".equals(subChannel)) {
						player.setVanished(in.readBoolean());
						TAB.getInstance().getFeatureManager().onVanishStatusChange(player);
						((PlayerPlaceholderImpl) TAB.getInstance().getPlaceholderManager().getPlaceholder("%vanished%")).updateValue(player, player.isVanished());
					}
					if ("Disguised".equals(subChannel)) {
						player.setDisguised(in.readBoolean());
					}
					if ("Invisible".equals(subChannel)) {
						player.setInvisible(in.readBoolean());
					}
					if ("World".equals(subChannel)) {
						TAB.getInstance().getFeatureManager().onWorldChange(player.getUniqueId(), in.readUTF());
					}
					if ("Group".equals(subChannel)) {
						player.setGroup(in.readUTF());
					}
					if ("Boat".equals(subChannel)) {
						player.setOnBoat(in.readBoolean());
					}
					if ("Permission".equals(subChannel)) {
						player.setHasPermission(in.readUTF(), in.readBoolean());
					}
					if ("PlayerJoinResponse".equals(subChannel)) {
						TAB.getInstance().getFeatureManager().onWorldChange(player.getUniqueId(), in.readUTF());
						if (TAB.getInstance().getGroupManager().getPlugin() instanceof VaultBridge) player.setGroup(in.readUTF());
						int placeholderCount = in.readInt();
						for (int i=0; i<placeholderCount; i++) {
							String identifier = in.readUTF();
							if (identifier.startsWith("%rel_")) {
								int playerCount = in.readInt();
								for (int j=0; j<playerCount; j++) {
									((RelationalPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier))
											.updateValue(player, TAB.getInstance().getPlayer(in.readUTF()), in.readUTF());
								}
							} else {
								((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier)).updateValue(player, in.readUTF());
							}
						}
					}
					if ("PlaceholderError".equals(subChannel)) {
						String message = in.readUTF();
						int count = in.readInt();
						List<String> stack = new ArrayList<>();
						for (int i=0; i<count; i++) {
							stack.add(in.readUTF());
						}
						TAB.getInstance().getErrorManager().placeholderError(message, stack);
					}
					if ("RegisterPlaceholder".equals(subChannel)) {
						TAB.getInstance().getPlaceholderManager().addUsedPlaceholders(Collections.singletonList(in.readUTF()));
					}
				});
	}

	/**
	 * Sends plugin message to specified player
	 *
	 * @param	player
	 * 			Player to send plugin message to
	 * @param	args
	 * 			Messages to encode
	 */
	public void sendMessage(TabPlayer player, Object... args) {
//		System.out.println(player.getName() + ": " + Arrays.toString(args));
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		for (Object arg : args) {
			writeObject(out, arg);
		}
		((ProxyTabPlayer)player).sendPluginMessage(out.toByteArray());
	}

	/**
	 * Writes object to data input by calling proper write method
	 * based on data type of the object.
	 *
	 * @param	out
	 * 			Data output to write to
	 * @param	value
	 * 			Value to write
	 */
	private void writeObject(ByteArrayDataOutput out, Object value) {
		Preconditions.checkNotNull(value, "value to write");
		if (value instanceof String) {
			out.writeUTF((String) value);
		} else if (value instanceof Boolean) {
			out.writeBoolean((boolean) value);
		} else if (value instanceof Integer) {
			out.writeInt((int) value);
		} else if (value instanceof Double) {
			out.writeDouble((double) value);
		} else throw new IllegalArgumentException("Unhandled message data type " + value.getClass().getName());
	}
}
