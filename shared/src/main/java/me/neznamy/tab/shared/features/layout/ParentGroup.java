package me.neznamy.tab.shared.features.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

public class ParentGroup {

	private Condition condition;
	private int[] slots;
	private Map<Integer, PlayerSlot> playerSlots = new HashMap<>();

	public ParentGroup(Layout layout, Condition condition, int[] slots) {
		this.condition = condition;
		this.slots = slots;
		for (int slot : slots) {
			playerSlots.put(slot, new PlayerSlot(layout, layout.uuids.get(slot), slot));
		}
	}

	public void tick(TabPlayer viewer, List<TabPlayer> remainingPlayers){
		int index = 0;
		for (TabPlayer p : new ArrayList<>(remainingPlayers)) {
			if (index == slots.length || (condition != null && !condition.isMet(p))) continue;
			int slot = slots[index++];
			playerSlots.get(slot).setPlayer(p);
			remainingPlayers.remove(p);
		}
		while (index < slots.length) {
			playerSlots.get(slots[index++]).setPlayer(null);
		}
	}
	
	public void onJoin(TabPlayer p) {
		playerSlots.values().forEach(s -> s.onJoin(p));
	}
}