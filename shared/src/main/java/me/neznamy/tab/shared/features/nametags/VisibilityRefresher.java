package me.neznamy.tab.shared.features.nametags;

import lombok.Getter;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.jetbrains.annotations.NotNull;

public class VisibilityRefresher extends TabFeature implements Refreshable {

    @Getter private final String featureName = "NameTags";
    @Getter private final String refreshDisplayName = "Updating NameTag visibility";
    @NotNull private final NameTag nameTags;

    public VisibilityRefresher(@NotNull NameTag nameTags) {
        this.nameTags = nameTags;
        TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(TabConstants.Placeholder.INVISIBLE, 500,
                p -> ((TabPlayer)p).hasInvisibilityPotion());
        addUsedPlaceholder(TabConstants.Placeholder.INVISIBLE);
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (nameTags.getDisableChecker().isDisabledPlayer(p)) return;
        nameTags.updateTeamData(p);
    }
}