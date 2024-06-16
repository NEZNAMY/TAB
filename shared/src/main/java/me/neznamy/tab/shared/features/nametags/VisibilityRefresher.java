package me.neznamy.tab.shared.features.nametags;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Sub-feature that makes nametags invisible for players who are invisible
 * for 1.8 players to compensate for a client-sided bug.
 */
public class VisibilityRefresher extends RefreshableFeature implements CustomThreaded {

    @NotNull
    private final NameTag nameTags;

    /**
     * Constructs new instance and registers {@link TabConstants.Placeholder#INVISIBLE} placeholders.
     *
     * @param   nameTags
     *          Parent feature
     */
    public VisibilityRefresher(@NotNull NameTag nameTags) {
        super(nameTags.getFeatureName(), "Updating NameTag visibility");
        this.nameTags = nameTags;
        TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder(TabConstants.Placeholder.INVISIBLE, 500,
                p -> Boolean.toString(((TabPlayer)p).hasInvisibilityPotion()));
        addUsedPlaceholder(TabConstants.Placeholder.INVISIBLE);
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (p.teamData.disabled.get()) return;
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.getVersion().getMinorVersion() == 8) nameTags.updateVisibility(p, viewer);
        }
    }

    @Override
    @NotNull
    public ThreadExecutor getCustomThread() {
        return nameTags.getCustomThread();
    }
}