package me.neznamy.tab.platforms.fabric;

import me.neznamy.tab.shared.platform.PlatformEventListener;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class FabricEventListener extends PlatformEventListener {

    public void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, $, $$) -> onJoin(handler));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, $) -> onQuit(handler));
    }

    private void onJoin(ServerGamePacketListenerImpl connection) {
        join(new FabricTabPlayer(connection.player));
    }

    private void onQuit(ServerGamePacketListenerImpl connection) {
        quit(connection.player.getUUID());
    }

    //TODO replace player on respawn

    //TODO world switch
}
