package me.neznamy.tab.shared.features.nametags;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Sub-feature that makes nametags invisible for players who are invisible
 * for 1.8 players to compensate for a client-sided bug,
 * as well as offering condition for managing visibility.
 */
public class VisibilityRefresher extends RefreshableFeature implements JoinListener, Loadable, CustomThreaded {

    @NotNull private final NameTag nameTags;
    @NotNull private final Condition invisibleCondition;

    /**
     * Constructs new instance.
     *
     * @param   nameTags
     *          Parent feature
     */
    public VisibilityRefresher(@NotNull NameTag nameTags) {
        this.nameTags = nameTags;
        invisibleCondition = TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(nameTags.getConfiguration().getInvisibleNameTags());
    }

    @Override
    public void load() {
        TAB.getInstance().getPlaceholderManager().registerInternalPlayerPlaceholder(TabConstants.Placeholder.INVISIBLE, 500, p -> {
            TabPlayer player = (TabPlayer) p;
            boolean newInvisibility = player.hasInvisibilityPotion() || invisibleCondition.isMet((TabPlayer) p);
            if (newInvisibility) {
                player.teamData.hideNametag(NameTagInvisibilityReason.MEETING_CONFIGURED_CONDITION);
            } else {
                player.teamData.showNametag(NameTagInvisibilityReason.MEETING_CONFIGURED_CONDITION);
            }
            return Boolean.toString(newInvisibility);
        });
        addUsedPlaceholder(TabConstants.Placeholder.INVISIBLE);
        for (TabPlayer all : nameTags.getOnlinePlayers().getPlayers()) {
            onJoin(all);
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        if (invisibleCondition.isMet(connectedPlayer)) {
            connectedPlayer.teamData.hideNametag(NameTagInvisibilityReason.MEETING_CONFIGURED_CONDITION);
            nameTags.updateVisibility(connectedPlayer);
        }
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Updating NameTag visibility";
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (p.teamData.isDisabled()) return;
        if (!nameTags.getOnlinePlayers().contains(p)) return; // player is not loaded by this feature yet
        nameTags.updateVisibility(p);
    }

    @Override
    @NotNull
    public ThreadExecutor getCustomThread() {
        return nameTags.getCustomThread();
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return nameTags.getFeatureName();
    }
}