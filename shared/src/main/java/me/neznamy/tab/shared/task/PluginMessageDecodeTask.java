package me.neznamy.tab.shared.task;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants.CpuUsageCategory;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import me.neznamy.tab.shared.proxy.message.incoming.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Task for decoding incoming plugin messages from backend server.
 */
@RequiredArgsConstructor
public class PluginMessageDecodeTask implements Runnable {

    /** Registered plugin messages the plugin can receive from Bridge */
    private static final Map<String, Supplier<IncomingMessage>> registeredMessages = new HashMap<>();

    /** UUID of player who received this plugin message */
    private final UUID playerId;

    /** Received bytes */
    private final byte[] bytes;

    static {
        registeredMessages.put("PlaceholderError", PlaceholderError::new);
        registeredMessages.put("UpdateGameMode", UpdateGameMode::new);
        registeredMessages.put("Permission", HasPermission::new);
        registeredMessages.put("Invisible", Invisible::new);
        registeredMessages.put("Disguised", Disguised::new);
        registeredMessages.put("World", SetWorld::new);
        registeredMessages.put("Group", SetGroup::new);
        registeredMessages.put("Vanished", Vanished::new);
        registeredMessages.put("Placeholder", UpdatePlaceholder::new);
        registeredMessages.put("PlayerJoinResponse", PlayerJoinResponse::new);
        registeredMessages.put("RegisterPlaceholder", RegisterPlaceholder::new);
    }

    @Override
    public void run() {
        ProxyTabPlayer player = (ProxyTabPlayer) TAB.getInstance().getPlayer(playerId);
        if (player == null) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        Supplier<IncomingMessage> supplier = registeredMessages.get(in.readUTF());
        if (supplier == null) return;
        IncomingMessage msg = supplier.get();
        msg.read(in);
        TAB.getInstance().getCpu().runMeasuredTask("Plugin message handling", CpuUsageCategory.PLUGIN_MESSAGE_PROCESS, new PluginMessageProcessTask(msg, player));
    }
}
