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

@RequiredArgsConstructor
public class PlaceholderRefreshTask implements Runnable {

    private final Collection<Placeholder> placeholdersToRefresh;

    @Getter
    private final Map<ServerPlaceholderImpl, Object> serverPlaceholderResults = new HashMap<>();

    @Getter
    private final Map<PlayerPlaceholderImpl, Map<TabPlayer, Object>> playerPlaceholderResults = new HashMap<>();

    @Getter
    private final Map<RelationalPlaceholderImpl, Map<TabPlayer, Map<TabPlayer, Object>>> relationalPlaceholderResults = new HashMap<>();

    @Override
    public void run() {
        TabPlayer[] players = TAB.getInstance().getOnlinePlayers();
        for (Placeholder placeholder : placeholdersToRefresh) {
            long nanoTime = 0;
            if (placeholder instanceof ServerPlaceholderImpl) {
                long startTime = System.nanoTime();
                Object result = ((ServerPlaceholderImpl)placeholder).request();
                nanoTime += System.nanoTime()-startTime;
                serverPlaceholderResults.put((ServerPlaceholderImpl) placeholder, result);
            }
            if (placeholder instanceof PlayerPlaceholderImpl) {
                for (TabPlayer player : players) {
                    long startTime = System.nanoTime();
                    Object result = ((PlayerPlaceholderImpl)placeholder).request(player);
                    nanoTime += System.nanoTime()-startTime;
                    playerPlaceholderResults.computeIfAbsent((PlayerPlaceholderImpl) placeholder, p -> new HashMap<>(players.length + 1, 1)).put(player, result);
                }
            }
            if (placeholder instanceof RelationalPlaceholderImpl) {
                for (TabPlayer viewer : players) {
                    for (TabPlayer target : players) {
                        long startTime = System.nanoTime();
                        Object result = ((RelationalPlaceholderImpl)placeholder).request(viewer, target);
                        nanoTime += System.nanoTime()-startTime;
                        relationalPlaceholderResults.computeIfAbsent((RelationalPlaceholderImpl) placeholder, p -> new HashMap<>(players.length + 1, 1))
                                .computeIfAbsent(viewer, v -> new HashMap<>(players.length + 1, 1))
                                .put(target, result);
                    }
                }
            }
            TAB.getInstance().getCPUManager().addPlaceholderTime(placeholder.getIdentifier(), nanoTime);
        }
    }
}
