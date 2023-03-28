package me.neznamy.tab.platforms.krypton.features.unlimitedtags;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import org.kryptonmc.api.entity.player.Player;
import org.kryptonmc.api.event.Listener;
import org.kryptonmc.api.event.player.action.PlayerStartSneakingEvent;
import org.kryptonmc.api.event.player.action.PlayerStopSneakingEvent;

@RequiredArgsConstructor
public class EventListener {

    private final KryptonNameTagX feature;

    @Listener
    public void onStartSneak( PlayerStartSneakingEvent event) {
        onSneak(event.getPlayer(), true);
    }

    @Listener
    public void onStopSneak(PlayerStopSneakingEvent event) {
        onSneak(event.getPlayer(), false);
    }

    private void onSneak(Player eventPlayer, boolean sneaking) {
        TabPlayer player = TAB.getInstance().getPlayer(eventPlayer.getUuid());
        if (player == null || feature.isPlayerDisabled(player)) return;
        TAB.getInstance().getCPUManager().runMeasuredTask(feature, TabConstants.CpuUsageCategory.PLAYER_SNEAK,
                () -> feature.getArmorStandManager(player).sneak(sneaking));
    }
}
