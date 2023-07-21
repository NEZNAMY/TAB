package me.neznamy.tab.platforms.fabric;

import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

public class FabricEventListener extends EventListener<ServerPlayer> {

    public void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((connection, $) -> quit(connection.player.getUUID()));
        ServerPlayConnectionEvents.JOIN.register((connection, $, $$) -> join(connection.player));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) ->
                worldChange(player.getUUID(), destination.dimension().location().toString()));
        //TODO command
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> replacePlayer(oldPlayer.getUUID(), newPlayer));
    }

    @Override
    public TabPlayer createPlayer(ServerPlayer player) {
        return new FabricTabPlayer(player);
    }
}
