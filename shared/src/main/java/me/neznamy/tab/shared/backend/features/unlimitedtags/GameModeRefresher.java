package me.neznamy.tab.shared.backend.features.unlimitedtags;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.feature.Refreshable;
import me.neznamy.tab.api.feature.TabFeature;
import me.neznamy.tab.shared.backend.BackendTabPlayer;

import java.util.Collections;

@RequiredArgsConstructor
public class GameModeRefresher extends TabFeature implements Refreshable {

    @Getter private final String featureName = "Unlimited NameTags";
    @Getter private final String refreshDisplayName = "Gamemode listener";
    private final BackendNameTagX nameTagX;

    {
        addUsedPlaceholders(Collections.singletonList("%gamemode%"));
    }

    @Override
    public void refresh(TabPlayer viewer, boolean force) {
        for (TabPlayer target : TabAPI.getInstance().getOnlinePlayers()) {
            nameTagX.getArmorStandManager(target).updateMetadata((BackendTabPlayer) viewer);
        }
    }
}
