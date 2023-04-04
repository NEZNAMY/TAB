package me.neznamy.tab.platforms.fabric;

import me.neznamy.tab.shared.TAB;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class FabricEventListener {

    public void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, $, $$) -> onJoin(handler));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, $) -> onQuit(handler));
    }

    private void onJoin(ServerGamePacketListenerImpl connection) {
        if (TAB.getInstance().isPluginDisabled()) return;
        TAB.getInstance().getCPUManager().runTask(() -> TAB.getInstance().getFeatureManager().onJoin(new FabricTabPlayer(connection.player)));
    }

    private void onQuit(ServerGamePacketListenerImpl connection) {
        if (TAB.getInstance().isPluginDisabled()) return;
        TAB.getInstance().getCPUManager().runTask(() ->
                TAB.getInstance().getFeatureManager().onQuit(TAB.getInstance().getPlayer(connection.getPlayer().getUUID())));
    }
}
