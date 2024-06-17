package me.neznamy.tab.shared.placeholders;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.types.RelationalPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.types.ServerPlaceholderImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that refreshes all requested placeholders using given refresh
 * function and returns the results.
 */
@RequiredArgsConstructor
@Getter
public class PlaceholderRefreshTask implements Runnable {

    /** Placeholders that should be refreshed in this loop */
    private final Collection<Placeholder> placeholdersToRefresh;

    /** Map of server placeholder results */
    private final Map<ServerPlaceholderImpl, String> serverPlaceholderResults = new HashMap<>();

    /** Map of player placeholder results */
    private final Map<PlayerPlaceholderImpl, Map<TabPlayer, String>> playerPlaceholderResults = new HashMap<>();

    /** Map of relational placeholder results */
    @Nullable
    private Map<RelationalPlaceholderImpl, Map<TabPlayer, Map<TabPlayer, String>>> relationalPlaceholderResults;

    /** Time it took placeholders to retrieve value (in nanoseconds) */
    private final Map<String, Long> usedTime = new HashMap<>();

    @Override
    public void run() {
        boolean trackUsage = TAB.getInstance().getCpu().isTrackUsage();
        TabPlayer[] players = TAB.getInstance().getOnlinePlayers();
        for (Placeholder placeholder : placeholdersToRefresh) {
            long nanoTime = 0;
            if (placeholder instanceof ServerPlaceholderImpl) {
                ServerPlaceholderImpl serverPlaceholder = (ServerPlaceholderImpl) placeholder;
                long startTime = System.nanoTime();
                String result = serverPlaceholder.request();
                nanoTime += System.nanoTime()-startTime;
                serverPlaceholderResults.put(serverPlaceholder, result);
            }
            if (placeholder instanceof PlayerPlaceholderImpl) {
                PlayerPlaceholderImpl playerPlaceholder = (PlayerPlaceholderImpl) placeholder;
                Map<TabPlayer, String> playerResults = new HashMap<>();
                for (TabPlayer player : players) {
                    long startTime = System.nanoTime();
                    String result = playerPlaceholder.request(player);
                    nanoTime += System.nanoTime()-startTime;
                    playerResults.put(player, result);
                }
                playerPlaceholderResults.put(playerPlaceholder, playerResults);
            }
            if (placeholder instanceof RelationalPlaceholderImpl) {
                RelationalPlaceholderImpl relationalPlaceholder = (RelationalPlaceholderImpl) placeholder;
                Map<TabPlayer, Map<TabPlayer, String>> viewerMap = new HashMap<>();
                for (TabPlayer viewer : players) {
                    Map<TabPlayer, String> targetMap = new HashMap<>();
                    for (TabPlayer target : players) {
                        long startTime = System.nanoTime();
                        String result = relationalPlaceholder.request(viewer, target);
                        nanoTime += System.nanoTime()-startTime;
                        targetMap.put(target, result);
                    }
                    viewerMap.put(viewer, targetMap);
                }
                if (relationalPlaceholderResults == null) relationalPlaceholderResults = new HashMap<>();
                relationalPlaceholderResults.put(relationalPlaceholder, viewerMap);
            }
            if (trackUsage) usedTime.put(placeholder.getIdentifier(), nanoTime);
        }
    }
}
