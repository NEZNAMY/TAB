package me.neznamy.tab.shared.features.nametags;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class VisibilityRefresher extends TabFeature implements Refreshable {

    @NotNull
    private final NameTag nameTags;

    public VisibilityRefresher(@NotNull NameTag nameTags) {
        this.nameTags = nameTags;
        TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(TabConstants.Placeholder.INVISIBLE, 500,
                p -> ((TabPlayer)p).hasInvisibilityPotion());
        addUsedPlaceholder(TabConstants.Placeholder.INVISIBLE);
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (p.disabledNametags.get()) return;
        nameTags.updateTeamData(p);
    }

    @Override
    @NotNull
    public String getRefreshDisplayName() {
        return "Updating NameTag visibility";
    }

    @Override
    @NotNull
    public String getFeatureName() {
        return nameTags.getFeatureName();
    }
}