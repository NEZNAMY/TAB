package me.neznamy.tab.shared.features;

import lombok.Getter;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.*;
import org.jetbrains.annotations.NotNull;

/**
 * Cancelling GameMode change packet to spectator GameMode to avoid players being moved on
 * the bottom of TabList with transparent name. Does not work on self as that would result
 * in players not being able to clip through walls.
 */
@Getter
public class SpectatorFix extends TabFeature implements JoinListener, GameModeListener, Loadable, UnLoadable,
        ServerSwitchListener, WorldSwitchListener, VanishListener {

    private final String featureName = "Spectator fix";

    /**
     * Sends GameMode update of all players to either their real GameMode if
     * {@code realGameMode} is {@code true} or fake value if it's {@code false}.
     *
     * @param   viewer
     *          Player to send gamemode updates to
     * @param   realGameMode
     *          Whether real GameMode should be shown or fake one
     * @param   mutually
     *          If target's view should be updated as well
     */
    private void updatePlayer(@NotNull TabPlayer viewer, boolean realGameMode, boolean mutually) {
        for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
            if (viewer == target) continue;
            if (target.getGamemode() == 3 && !viewer.hasPermission(TabConstants.Permission.SPECTATOR_BYPASS)) {
                viewer.getTabList().updateGameMode(target.getTablistId(), realGameMode ? target.getGamemode() : 0);
            }
            if (mutually && viewer.getGamemode() == 3 && !target.hasPermission(TabConstants.Permission.SPECTATOR_BYPASS)) {
                target.getTabList().updateGameMode(viewer.getTablistId(), realGameMode ? viewer.getGamemode() : 0);
            }
        }
    }

    @Override
    public void onGameModeChange(@NotNull TabPlayer player) {
        if (player.getGamemode() != 3) return;
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.hasPermission(TabConstants.Permission.SPECTATOR_BYPASS)) continue;
            if (player != viewer && player.getServer().equals(viewer.getServer())) {
                viewer.getTabList().updateGameMode(player.getTablistId(), 0);
            }
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer p) {
        TAB.getInstance().getCPUManager().runTaskLater(100, featureName, TabConstants.CpuUsageCategory.PLAYER_JOIN,
                () -> updatePlayer(p, false, true));
    }

    @Override
    public void load() {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            updatePlayer(viewer, false, false);
        }
    }

    @Override
    public void unload() {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            updatePlayer(viewer, true, false);
        }
    }

    @Override
    public void onServerChange(@NotNull TabPlayer changed, @NotNull String from, @NotNull String to) {
        // 200ms delay for global playerlist, taking extra time
        TAB.getInstance().getCPUManager().runTaskLater(300, featureName, TabConstants.CpuUsageCategory.SERVER_SWITCH, () -> {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                updatePlayer(all, false, true);
            }
        });
    }

    @Override
    public void onWorldChange(@NotNull TabPlayer changed, @NotNull String from, @NotNull String to) {
        // Some server versions may resend gamemode on world switch, resend false value again
        if (changed.getGamemode() != 3) return;
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer == changed || viewer.hasPermission(TabConstants.Permission.SPECTATOR_BYPASS)) continue;
            viewer.getTabList().updateGameMode(changed.getTablistId(), 0);
        }
    }

    @Override
    public void onVanishStatusChange(@NotNull TabPlayer player) {
        if (player.isVanished() || player.getGamemode() != 3) return;
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer == player || viewer.hasPermission(TabConstants.Permission.SPECTATOR_BYPASS)) continue;
            viewer.getTabList().updateGameMode(player.getTablistId(), 0);
        }
    }
}