package me.neznamy.tab.shared.features.nametags;

import java.util.Collections;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.feature.Refreshable;
import me.neznamy.tab.api.feature.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.api.TabConstants;

@RequiredArgsConstructor
public class VisibilityRefresher extends TabFeature implements Refreshable {

    @Getter private final String featureName = "NameTags";
    @Getter private final String refreshDisplayName = "Updating NameTag visibility";
    private final NameTag nameTags;

    {
        TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(TabConstants.Placeholder.INVISIBLE, 500, TabPlayer::hasInvisibilityPotion);
        addUsedPlaceholders(Collections.singletonList(TabConstants.Placeholder.INVISIBLE));
    }

    @Override
    public void refresh(TabPlayer p, boolean force) {
        if (nameTags.isDisabledPlayer(p)) return;
        nameTags.updateTeamData(p);
    }
}