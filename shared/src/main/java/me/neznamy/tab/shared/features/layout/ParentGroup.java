package me.neznamy.tab.shared.features.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.Playerlist;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

public class ParentGroup {

	private Condition condition;
	private List<Integer> slots;
	private List<ChildGroup> childGroups;
	
	public ParentGroup(Condition condition, List<Integer> slots, List<ChildGroup> childGroups) {
		this.condition = condition;
		this.slots = slots;
		this.childGroups = childGroups;
	}
	
	public Map<Integer, IChatBaseComponent> createLayout(TabPlayer viewer, List<TabPlayer> playersMeetingCondition){
		List<IChatBaseComponent> texts = new ArrayList<>();
		for (ChildGroup child : childGroups) {
			List<TabPlayer> selected = child.selectPlayers(playersMeetingCondition);
			if (child.getTitle() != null) {
				String[] title = child.getTitle().split("\\n");
				for (String line : title) {
					texts.add(IChatBaseComponent.optimizedComponent(new Property(viewer, line).get()));
				}
			}
			for (TabPlayer selectedPlayer : selected) {
				texts.add(((Playerlist)TAB.getInstance().getFeatureManager().getFeature("playerlist")).getTabFormat(selectedPlayer, viewer));
			}
			playersMeetingCondition.removeAll(selected);
		}
		
		Map<Integer, IChatBaseComponent> map = new HashMap<>();
		int index = 0;
		for (Integer slot : slots) {
			if (texts.size() > index) {
				map.put(slot, texts.get(index));
			} else {
				map.put(slot, new IChatBaseComponent(""));
			}
			index++;
		}
		return map;
	}
	
	public Condition getCondition() {
		return condition;
	}
}