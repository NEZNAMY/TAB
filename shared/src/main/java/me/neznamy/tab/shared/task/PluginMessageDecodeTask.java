package me.neznamy.tab.shared.task;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants.CpuUsageCategory;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import me.neznamy.tab.shared.proxy.message.incoming.*;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Task for decoding incoming plugin messages from backend server.
 */
@SuppressWarnings("unchecked") // Generic array
@RequiredArgsConstructor
public class PluginMessageDecodeTask implements Runnable {

    /** Registered plugin messages the plugin can receive from Bridge */
    private static final Supplier<IncomingMessage>[] registeredMessages = new Supplier[] {
            PlaceholderError::new,
            UpdateGameMode::new,
            HasPermission::new,
            Invisible::new,
            Disguised::new,
            SetWorld::new,
            SetGroup::new,
            Vanished::new,
            UpdatePlaceholder::new,
            PlayerJoinResponse::new,
            RegisterPlaceholder::new
    };

    /** UUID of player who received this plugin message */
    private final UUID playerId;

    /** Received bytes */
    private final byte[] bytes;

    @Override
    public void run() {
        ProxyTabPlayer player = (ProxyTabPlayer) TAB.getInstance().getPlayer(playerId);
        if (player == null) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        Supplier<IncomingMessage> supplier = registeredMessages[in.readByte()];
        IncomingMessage msg = supplier.get();
        msg.read(in);
        TAB.getInstance().getCpu().runMeasuredTask("Plugin message handling", CpuUsageCategory.PLUGIN_MESSAGE_PROCESS, new PluginMessageProcessTask(msg, player));
    }
}
