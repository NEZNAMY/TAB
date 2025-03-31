package me.neznamy.tab.platforms.fabric;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Event listener for Fabric.
 */
public class FabricEventListener implements EventListener<ServerPlayer> {

    /**
     * Registers all event listeners.
     */
    public void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((connection, server) -> quit(connection.player.getUUID()));
        ServerPlayConnectionEvents.JOIN.register((connection, sender, server) -> join(connection.player));
        //TODO command preprocess
        ServerPlayerEvents.AFTER_RESPAWN.register(
                (oldPlayer, newPlayer, alive) -> {
                    replacePlayer(newPlayer.getUUID(), newPlayer);
                    // respawning from death & taking end portal in the end does not call world change event
                    worldChange(newPlayer.getUUID(), FabricTAB.getLevelName(newPlayer.level()));
                });
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(
                (player, origin, destination) -> worldChange(player.getUUID(), FabricTAB.getLevelName(destination)));
    }

    @Override
    @NotNull
    public TabPlayer createPlayer(@NotNull ServerPlayer player) {
        return new FabricTabPlayer((FabricPlatform) TAB.getInstance().getPlatform(), player);
    }
}
