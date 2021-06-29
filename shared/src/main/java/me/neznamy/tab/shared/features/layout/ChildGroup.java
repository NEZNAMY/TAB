package me.neznamy.tab.shared.features.layout;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

/**
 * Child group for layout
 */
public class ChildGroup {

	//title of group
	private String title;
	
	//condition required for player to be in group
	private Condition condition;
	
	/**
	 * Creates new instance of the class
	 * @param title - title of group
	 * @param condition - condition required for player to be in group
	 */
	public ChildGroup(String title, Condition condition) {
		this.title = title;
		this.condition = condition;
	}
	
	/**
	 * Returns title of group or null if not set
	 * @return title of group or null if not set
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Returns list of players who meet condition to be members of this group
	 * @param players - player pool to choose from
	 * @return list of players meeting condition
	 */
	public List<TabPlayer> selectPlayers(List<TabPlayer> players){
		if (condition == null) return players;
		List<TabPlayer> selected = new ArrayList<>();
		for (TabPlayer player : players) {
			if (condition.isMet(player)) {
				selected.add(player);
			}
		}
		return selected;
	}
}