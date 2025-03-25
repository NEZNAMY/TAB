package me.neznamy.tab.platforms.neoforge;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event listener for NeoForge.
 */
public class NeoForgeEventListener implements EventListener<ServerPlayer> {

    /**
     * Registers all event listeners.
     */
    public void register() {
        IEventBus eventBus = NeoForge.EVENT_BUS;
        eventBus.addListener((PlayerEvent.PlayerLoggedInEvent event) -> join((ServerPlayer) event.getEntity()));
        eventBus.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> quit(event.getEntity().getUUID()));
        eventBus.addListener((PlayerEvent.PlayerRespawnEvent event) -> {
            ServerPlayer player = (ServerPlayer) event.getEntity();
          replacePlayer(player.getUUID(), player);
          worldChange(player.getUUID(), NeoForgeTAB.getLevelName(player.level()));
        });
        eventBus.addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> worldChange(event.getEntity().getUUID(), NeoForgeTAB.getLevelName(event.getEntity().level())));
    }

    @Override
    @NotNull
    public TabPlayer createPlayer(@NotNull ServerPlayer player) {
        return new NeoForgeTabPlayer((NeoForgePlatform) TAB.getInstance().getPlatform(), player);
    }
}
