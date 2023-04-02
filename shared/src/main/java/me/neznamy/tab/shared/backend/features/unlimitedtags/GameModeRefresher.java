package me.neznamy.tab.shared.backend.features.unlimitedtags;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.player.TabPlayer;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.backend.BackendTabPlayer;

import java.util.Collections;

public class GameModeRefresher extends TabFeature implements Refreshable {

    @Getter private final String featureName = "Unlimited NameTags";
    @Getter private final String refreshDisplayName = "Gamemode listener";
    private final BackendNameTagX nameTagX;

    public GameModeRefresher(BackendNameTagX nameTagX) {
        this.nameTagX = nameTagX;
        addUsedPlaceholders(Collections.singletonList("%gamemode%"));
    }

    @Override
    public void refresh(TabPlayer viewer, boolean force) {
        for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
            nameTagX.getArmorStandManager(target).updateMetadata((BackendTabPlayer) viewer);
        }
    }
}
