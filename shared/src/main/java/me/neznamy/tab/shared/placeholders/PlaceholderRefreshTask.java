package me.neznamy.tab.shared.placeholders;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.types.RelationalPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.types.ServerPlaceholderImpl;
import me.neznamy.tab.shared.platform.TabPlayer;

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
    private final Map<ServerPlaceholderImpl, Object> serverPlaceholderResults = new HashMap<>();

    /** Map of player placeholder results */
    private final Map<PlayerPlaceholderImpl, Map<TabPlayer, Object>> playerPlaceholderResults = new HashMap<>();

    /** Map of relational placeholder results */
    private final Map<RelationalPlaceholderImpl, Map<TabPlayer, Map<TabPlayer, Object>>> relationalPlaceholderResults = new HashMap<>();

    @Override
    public void run() {
        TabPlayer[] players = TAB.getInstance().getOnlinePlayers();
        for (Placeholder placeholder : placeholdersToRefresh) {
            long nanoTime = 0;
            if (placeholder instanceof ServerPlaceholderImpl) {
                ServerPlaceholderImpl serverPlaceholder = (ServerPlaceholderImpl) placeholder;
                long startTime = System.nanoTime();
                Object result = serverPlaceholder.request();
                nanoTime += System.nanoTime()-startTime;
                serverPlaceholderResults.put(serverPlaceholder, result);
            }
            if (placeholder instanceof PlayerPlaceholderImpl) {
                PlayerPlaceholderImpl playerPlaceholder = (PlayerPlaceholderImpl) placeholder;
                playerPlaceholderResults.put(playerPlaceholder, new HashMap<>());
                for (TabPlayer player : players) {
                    long startTime = System.nanoTime();
                    Object result = playerPlaceholder.request(player);
                    nanoTime += System.nanoTime()-startTime;
                    playerPlaceholderResults.get(playerPlaceholder).put(player, result);
                }
            }
            if (placeholder instanceof RelationalPlaceholderImpl) {
                RelationalPlaceholderImpl relationalPlaceholder = (RelationalPlaceholderImpl) placeholder;
                relationalPlaceholderResults.put(relationalPlaceholder, new HashMap<>());
                for (TabPlayer viewer : players) {
                    relationalPlaceholderResults.get(relationalPlaceholder).put(viewer, new HashMap<>());
                    for (TabPlayer target : players) {
                        long startTime = System.nanoTime();
                        Object result = relationalPlaceholder.request(viewer, target);
                        nanoTime += System.nanoTime()-startTime;
                        relationalPlaceholderResults.get(relationalPlaceholder).get(viewer).put(target, result);
                    }
                }
            }
            TAB.getInstance().getCPUManager().addPlaceholderTime(placeholder.getIdentifier(), nanoTime);
        }
    }
}
