package me.neznamy.tab.shared.features.layout;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

public class VanishListener extends TabFeature {

    private final LayoutManager layoutManager;

    protected VanishListener(LayoutManager layoutManager) {
        super(layoutManager.getFeatureName(), "Refreshing vanished players");
        this.layoutManager = layoutManager;
        TAB.getInstance().getPlaceholderManager().addUsedPlaceholder("%vanished%", this);
    }

    @Override
    public void refresh(TabPlayer p, boolean force) {
        layoutManager.getLayouts().values().forEach(Layout::tick);
    }
}
