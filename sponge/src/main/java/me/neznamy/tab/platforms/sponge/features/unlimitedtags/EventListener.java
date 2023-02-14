package me.neznamy.tab.platforms.sponge.features.unlimitedtags;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;

@RequiredArgsConstructor
public class EventListener {

    private final SpongeNameTagX feature;

    @Listener
    public void onRespawn(final RespawnPlayerEvent.Post event) {
        TabAPI.getInstance().getThreadManager().runMeasuredTask(feature, TabConstants.CpuUsageCategory.PLAYER_RESPAWN, () -> {
            TabPlayer respawned = TabAPI.getInstance().getPlayer(event.entity().uniqueId());
            if (feature.isPlayerDisabled(respawned)) return;
            feature.getArmorStandManager(respawned).teleport();
        });
    }
}
