package me.neznamy.tab.shared.features.layout;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore.Action;
import me.neznamy.tab.shared.PropertyUtils;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.YellowNumber;

public class YellowNumberFix extends TabFeature {
	
	private Layout layout;
	private YellowNumber objective;
	
	public YellowNumberFix(Layout layout, YellowNumber objective) {
		super(layout.getFeatureName());
		this.layout = layout;
		this.objective = objective;
	}
	
	@Override
	public void load() {
		TAB.getInstance().getOnlinePlayers().forEach(this::onJoin);
	}
	
	@Override
	public void onJoin(TabPlayer p) {
		p.getProperty(PropertyUtils.YELLOW_NUMBER).addListener(this);
		refresh(p, false);
	}
	
	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		refresh(p, false);
	}
	
	@Override
	public void refresh(TabPlayer p, boolean force) {
		if (objective.getDisabledPlayers().contains(p)) return;
		int value = getLastValue(p);
		String fakeplayer = null;
		for (ParentGroup group : layout.getGroups()) {
			if (group.getPlayers().containsKey(p)) {
				fakeplayer = group.getPlayers().get(p).getFakePlayer();
			}
		}
		if (fakeplayer == null) return; //player not displayed in layout
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, YellowNumber.OBJECTIVE_NAME, fakeplayer, value), this);
		}
	}
	
	public int getLastValue(TabPlayer p) {
		return TAB.getInstance().getErrorManager().parseInteger(p.getProperty(PropertyUtils.YELLOW_NUMBER).get(), 0, "yellow number");
	}
}