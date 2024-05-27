package me.neznamy.tab.shared.task;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Task for refreshing permission groups of players.
 */
@RequiredArgsConstructor
public class GroupRefreshTask implements Runnable {

    /** Function for getting group of a player */
    @NotNull
    private final Function<TabPlayer, String> detectGroup;

    @Override
    public void run() {
        long time = System.nanoTime();
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            String oldGroup = all.getPermissionGroup();
            String newGroup = detectGroup.apply(all);
            if (!oldGroup.equals(newGroup)) {
                // Back to main thread to avoid concurrency issues
                TAB.getInstance().getCpu().runMeasuredTask("Permission group refreshing", "Applying changes", () -> all.setGroup(newGroup));
            }
        }
        TAB.getInstance().getCpu().addTime("Permission group refreshing", "Periodic task", System.nanoTime() - time);
    }
}
