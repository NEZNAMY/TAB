package me.neznamy.tab.shared.features;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import org.jetbrains.annotations.NotNull;

/**
 * Cancelling GameMode change packet to spectator GameMode to avoid players being moved on
 * the bottom of TabList with transparent name. Does not work on self as that would result
 * in players not being able to clip through walls.
 */
@Getter
public class SpectatorFix extends TabFeature implements JoinListener, Loadable, UnLoadable, CustomThreaded {

    private final ThreadExecutor customThread = new ThreadExecutor("TAB Spectator Fix Thread");

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
            if (!viewer.hasPermission(TabConstants.Permission.SPECTATOR_BYPASS)) {
                viewer.getTabList().updateGameMode(target, realGameMode ? target.getGamemode() : 0);
            }
            if (mutually && !target.hasPermission(TabConstants.Permission.SPECTATOR_BYPASS)) {
                target.getTabList().updateGameMode(viewer, realGameMode ? viewer.getGamemode() : 0);
            }
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer p) {
        updatePlayer(p, false, true);
    }

    @Override
    public void load() {
        TAB.getInstance().getCpu().getTablistEntryCheckThread().repeatTask(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
                ((TrackedTabList<?>)p.getTabList()).checkGameModes();
            }
        }, getFeatureName(), TabConstants.CpuUsageCategory.PERIODIC_TASK), 500);
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

    @NotNull
    @Override
    public String getFeatureName() {
        return "Spectator fix";
    }
}