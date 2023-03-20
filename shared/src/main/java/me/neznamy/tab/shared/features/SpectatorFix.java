package me.neznamy.tab.shared.features;

import lombok.Getter;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.feature.*;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

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
            map.put(target.getUniqueId(), realGameMode ? target.getGamemode() : 1);
        }
        if (!map.isEmpty()) viewer.getTabList().updateGameModes(map);
    }

    @Override
    public int onGameModeChange(TabPlayer packetReceiver, UUID id, int gameMode) {
        if (gameMode != 3 || packetReceiver.hasPermission(TabConstants.Permission.SPECTATOR_BYPASS)) return gameMode;
        TabPlayer changed = TAB.getInstance().getPlayerByTabListUUID(id);
        if (changed != packetReceiver && changed.getServer().equals(packetReceiver.getServer())) return 0;
        return gameMode;
    }

    @Override
    public void onJoin(TabPlayer p) {
        updatePlayer(p, false);
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