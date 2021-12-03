package me.neznamy.tab.shared.features.nametags;

import java.util.Collections;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

public class VisibilityRefresher extends TabFeature {

	private final NameTag nameTags;

	public VisibilityRefresher(NameTag nameTags) {
		super(nameTags.getFeatureName(), "Updating NameTag visibility");
		this.nameTags = nameTags;
		TAB.getInstance().getPlaceholderManager().registerPlayerPlaceholder("%invisible%", 500, TabPlayer::hasInvisibilityPotion);
		addUsedPlaceholders(Collections.singletonList("%invisible%"));
	}

	@Override
	public void refresh(TabPlayer p, boolean force) {
		nameTags.updateTeamData(p);
	}
}