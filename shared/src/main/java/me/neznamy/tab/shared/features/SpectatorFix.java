package me.neznamy.tab.shared.features;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.*;

/**
 * Cancelling GameMode change packet to spectator GameMode to avoid players being moved on
 * the bottom of TabList with transparent name. Does not work on self as that would result
 * in players not being able to clip through walls.
 */
public class SpectatorFix extends TabFeature implements JoinListener, GameModeListener, Loadable, UnLoadable, ServerSwitchListener {

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
    private void updatePlayer(@NonNull TabPlayer viewer, boolean realGameMode) {
        if (viewer.hasPermission(TabConstants.Permission.SPECTATOR_BYPASS)) return;
        for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
            if (viewer == target || target.getGamemode() != 3) continue;
            viewer.getTabList().updateGameMode(target.getTablistId(), realGameMode ? target.getGamemode() : 1);
        }
    }

    @Override
    public void onGameModeChange(@NonNull TabPlayer player) {
        if (player.getGamemode() != 3) return;
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.hasPermission(TabConstants.Permission.SPECTATOR_BYPASS)) continue;
            if (player != viewer && player.getServer().equals(viewer.getServer())) {
                viewer.getTabList().updateGameMode(player.getTablistId(), 0);
            }
        }
    }

    @Override
    public void onJoin(@NonNull TabPlayer p) {
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

    @Override
    public void onServerChange(@NonNull TabPlayer changed, @NonNull String from, @NonNull String to) {
        // 200ms delay for global playerlist, taking extra time
        TAB.getInstance().getCPUManager().runTaskLater(300, this, TabConstants.CpuUsageCategory.SERVER_SWITCH, () -> {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (changed == all) continue;
                if (changed.getGamemode() == 3 && !all.hasPermission(TabConstants.Permission.SPECTATOR_BYPASS)) {
                    all.getTabList().updateGameMode(changed.getTablistId(), 0);
                }
                if (all.getGamemode() == 3 && !changed.hasPermission(TabConstants.Permission.SPECTATOR_BYPASS)) {
                    changed.getTabList().updateGameMode(all.getTablistId(), 0);
                }
            }
        });
    }
}