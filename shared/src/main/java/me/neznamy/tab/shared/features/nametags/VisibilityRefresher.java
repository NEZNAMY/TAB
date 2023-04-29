package me.neznamy.tab.shared.features.nametags;

import java.util.Collections;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;

public class VisibilityRefresher extends TabFeature implements Refreshable {

    @Getter private final String featureName = "NameTags";
    @Getter private final String refreshDisplayName = "Updating NameTag visibility";
    @NonNull private final NameTag nameTags;

    public VisibilityRefresher(@NonNull NameTag nameTags) {
        this.nameTags = nameTags;
        TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(TabConstants.Placeholder.INVISIBLE, 500,
                p -> ((TabPlayer)p).hasInvisibilityPotion());
        addUsedPlaceholders(Collections.singletonList(TabConstants.Placeholder.INVISIBLE));
    }

    @Override
    public void refresh(@NonNull TabPlayer p, boolean force) {
        if (nameTags.isDisabledPlayer(p)) return;
        nameTags.updateTeamData(p);
    }
}