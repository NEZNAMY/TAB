package me.neznamy.tab.platforms.fabric;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

public class FabricEventListener extends EventListener<ServerPlayer> {

    public void register() {
        ServerPlayConnectionEvents.JOIN.register((connection, $, $$) -> join(connection.player));
        ServerPlayConnectionEvents.DISCONNECT.register((connection, $) -> quit(connection.player.getUUID()));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            TabPlayer p = TAB.getInstance().getPlayer(newPlayer.getUUID());
            if (p != null) p.setPlayer(newPlayer);
        });
    }

    @Override
    public TabPlayer createPlayer(ServerPlayer player) {
        return new FabricTabPlayer(player);
    }

    //TODO world switch
}
