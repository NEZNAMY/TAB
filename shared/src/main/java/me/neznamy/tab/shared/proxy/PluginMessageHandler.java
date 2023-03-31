package me.neznamy.tab.shared.proxy;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.api.placeholder.RelationalPlaceholder;
import me.neznamy.tab.api.placeholder.ServerPlaceholder;
import me.neznamy.tab.shared.TAB;
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
     * @param   uuid
     *          plugin message receiver
     * @param   bytes
     *          incoming message
     */
    @SuppressWarnings("UnstableApiUsage")
    public void onPluginMessage(UUID uuid, String name, byte[] bytes) {
        TAB.getInstance().getCPUManager().runMeasuredTask("Plugin message handling",
                TabConstants.CpuUsageCategory.PLUGIN_MESSAGE, () -> {
                    ProxyTabPlayer player = (ProxyTabPlayer) TAB.getInstance().getPlayer(uuid);
                    if (player == null) {
                        TAB.getInstance().getErrorManager().printError("Ignoring plugin message (" + new String(bytes) + ") for player " +
                                name + ", because player was not found");
                        return;
                    }
                    ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
                    String subChannel = in.readUTF();
                    if ("Placeholder".equals(subChannel)) {
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
                        ((PlayerPlaceholderImpl) TAB.getInstance().getPlaceholderManager().getPlaceholder(TabConstants.Placeholder.VANISHED)).updateValue(player, player.isVanished());
                    }
                    if ("Disguised".equals(subChannel)) {
                        player.setDisguised(in.readBoolean());
                    }
                    if ("Invisible".equals(subChannel)) {
                        player.setInvisibilityPotion(in.readBoolean());
                    }
                    if ("World".equals(subChannel)) {
                        TAB.getInstance().getFeatureManager().onWorldChange(player.getUniqueId(), in.readUTF());
                    }
                    if ("Group".equals(subChannel)) {
                        ((PlayerPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder(TabConstants.Placeholder.GROUP)).updateValue(player, in.readUTF());
                    }
                    if ("Boat".equals(subChannel)) {
                        player.setOnBoat(in.readBoolean());
                    }
                    if ("Permission".equals(subChannel)) {
                        player.setHasPermission(in.readUTF(), in.readBoolean());
                    }
                    if ("PlayerJoinResponse".equals(subChannel)) {
                        TAB.getInstance().getFeatureManager().onWorldChange(player.getUniqueId(), in.readUTF());
                        if (TAB.getInstance().getGroupManager().getPlugin() instanceof VaultBridge &&
                            !TAB.getInstance().getGroupManager().isGroupsByPermissions()) player.setGroup(in.readUTF());
                        // reset attributes from previous server to default false values, new server will send separate update packets if needed
                        player.setVanished(false);
                        player.setDisguised(false);
                        player.setInvisibilityPotion(false);
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
                                Placeholder pl = TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier);
                                if (pl instanceof PlayerPlaceholder) {
                                    ((PlayerPlaceholder) pl).updateValue(player, in.readUTF());
                                } else {
                                    ((ServerPlaceholder) pl).updateValue(in.readUTF());
                                }
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
}
