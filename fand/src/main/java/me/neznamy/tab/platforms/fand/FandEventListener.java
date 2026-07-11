package me.neznamy.tab.platforms.fand;

import io.fand.api.entity.Player;
import io.fand.api.event.player.PlayerChangedWorldEvent;
import io.fand.api.event.player.PlayerJoinEvent;
import io.fand.api.event.player.PlayerQuitEvent;
import io.fand.api.event.player.PlayerRespawnEvent;
import io.fand.api.plugin.PluginContext;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/** Bridges Fand player lifecycle events into TAB. */
public final class FandEventListener implements EventListener<Player> {

    private final PluginContext context;

    public FandEventListener(@NotNull PluginContext context) {
        this.context = context;
    }

    public void register() {
        context.events().subscribe(PlayerJoinEvent.class, event -> join(event.player()));
        context.events().subscribe(PlayerQuitEvent.class, event -> quit(event.player().uniqueId()));
        context.events().subscribe(PlayerChangedWorldEvent.class,
                event -> worldChange(event.player().uniqueId(), event.toWorld().name()));
        context.events().subscribe(PlayerRespawnEvent.class,
                event -> replacePlayer(event.player().uniqueId(), event.player()));
    }

    @Override
    @NotNull
    public TabPlayer createPlayer(@NotNull Player player) {
        return new FandTabPlayer((FandPlatform) TAB.getInstance().getPlatform(), player);
    }
}
