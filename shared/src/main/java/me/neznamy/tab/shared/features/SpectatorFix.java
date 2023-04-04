package me.neznamy.tab.shared.features;

import lombok.Getter;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.player.TabPlayer;
import me.neznamy.tab.shared.features.types.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Cancelling GameMode change packet to spectator GameMode to avoid players being moved on
 * the bottom of TabList with transparent name. Does not work on self as that would result
 * in players not being able to clip through walls.
 */
public class SpectatorFix extends TabFeature implements JoinListener, GameModeListener, Loadable, UnLoadable {

    @Getter private final String featureName = "Spectator fix";

    /**
     * Sends GameMode update of all players to either their real GameMode if
     * {@code realGameMode} is {@code true} or fake value if it's {@code false}.
     *
     * @param   viewer
     *          Player to send gamemode updates to
     * @param   realGameMode
     *          Whether real GameMode should be shown or fake one
     */
    private void updatePlayer(TabPlayer viewer, boolean realGameMode) {
        if (viewer.hasPermission(TabConstants.Permission.SPECTATOR_BYPASS)) return;
        Map<UUID, Integer> map = new HashMap<>();
        for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
            if (viewer == target || target.getGamemode() != 3) continue;
            map.put(target.getTablistId(), realGameMode ? target.getGamemode() : 1);
        }
        if (!map.isEmpty()) viewer.getTabList().updateGameModes(map);
    }

    @Override
    public void onGameModeChange(TabPlayer player) {
        if (player.getGamemode() != 3) return;
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.hasPermission(TabConstants.Permission.SPECTATOR_BYPASS)) continue;
            if (player != viewer && player.getServer().equals(viewer.getServer())) {
                viewer.getTabList().updateGameMode(player.getTablistId(), 0);
            }
        }
    }

    @Override
    public void onJoin(TabPlayer p) {
        TAB.getInstance().getCPUManager().runTaskLater(100, this, TabConstants.CpuUsageCategory.PLAYER_JOIN,
                () -> updatePlayer(p, false));
    }

    @Override
    public void load() {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            updatePlayer(viewer, false);
        }
    }

    @Override
    public void unload() {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            updatePlayer(viewer, true);
        }
    }
}