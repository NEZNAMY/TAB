package me.neznamy.tab.platforms.sponge8.features.unlimitedtags;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;

@RequiredArgsConstructor
public class EventListener {

    private final SpongeNameTagX feature;

    @Listener
    public void onRespawn(final RespawnPlayerEvent.Post event) {
        TAB.getInstance().getCPUManager().runMeasuredTask(feature, TabConstants.CpuUsageCategory.PLAYER_RESPAWN, () -> {
            TabPlayer respawned = TabAPI.getInstance().getPlayer(event.entity().uniqueId());
            if (feature.isPlayerDisabled(respawned)) return;
            feature.getArmorStandManager(respawned).teleport();
        });
    }

    @Listener
    @SuppressWarnings("unchecked")
    public void onSneak(final ChangeDataHolderEvent.ValueChange event) {
        if (!(event.targetHolder() instanceof Player)) return;
        final Player player = (Player) event.targetHolder();

        event.originalChanges().ifSuccessful(changes -> {
            for (final Value.Immutable<?> change : changes) {
                if (change.key() != Keys.IS_SNEAKING) continue;
                final boolean value = ((Value.Immutable<Boolean>) change).get();

                TabPlayer p = TabAPI.getInstance().getPlayer(player.uniqueId());
                if (p == null || feature.isPlayerDisabled(p)) return;
                TAB.getInstance().getCPUManager().runMeasuredTask(feature, TabConstants.CpuUsageCategory.PLAYER_SNEAK, () -> feature.getArmorStandManager(p).sneak(value));
            }
        });
    }
}
