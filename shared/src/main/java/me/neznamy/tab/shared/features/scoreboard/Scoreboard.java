package me.neznamy.tab.shared.features.scoreboard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.scoreboard.lines.All0StableDynamicLine;
import me.neznamy.tab.shared.features.scoreboard.lines.All0StaticLine;
import me.neznamy.tab.shared.features.scoreboard.lines.CustomLine;
import me.neznamy.tab.shared.features.scoreboard.lines.NumberedStableDynamicLine;
import me.neznamy.tab.shared.features.scoreboard.lines.NumberedStaticLine;
import me.neznamy.tab.shared.features.scoreboard.lines.ScoreboardLine;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

/**
 * A class representing a scoreboard configured in premiumconfig
 */
public class Scoreboard implements me.neznamy.tab.api.Scoreboard, Refreshable {

	//scoreboard manager
	public ScoreboardManager manager;
	
	//name of this scoreboard
	private String name;
	
	//scoreboard title
	private String title;
	
	//display condition
	private Condition displayCondition;
	
	//scoreboard to display if condition is not met
	private String childBoard;
	
	//lines of scoreboard
	public List<ScoreboardLine> lines = new ArrayList<ScoreboardLine>();
	
	//players currently seeing this scoreboard
	public Set<TabPlayer> players = new HashSet<TabPlayer>();
	
	//placeholders used in title
	private List<String> usedPlaceholders;

	/**
	 * Constructs new instance with given parameters and registers lines to feature manager
	 * @param manager - scoreboard manager
	 * @param name - name of this scoreboard
	 * @param title - scoreboard title
	 * @param lines - lines of scoreboard
	 * @param displayCondition - display condition
	 * @param childBoard - scoreboard to display if condition is not met
	 */
	public Scoreboard(ScoreboardManager manager, String name, String title, List<String> lines, String displayCondition, String childBoard) {
		this(manager, name, title, lines);
		this.displayCondition = Condition.getCondition(displayCondition);
		this.childBoard = childBoard;
		refreshUsedPlaceholders();
	}

	/**
	 * Constructs new instance with given parameters and registers lines to feature manager
	 * @param manager - scoreboard manager
	 * @param name - name of this scoreboard
	 * @param title - scoreboard title
	 * @param lines - lines of scoreboard
	 */
	public Scoreboard(ScoreboardManager manager, String name, String title, List<String> lines) {
		this.manager = manager;
		this.name = name;
		this.title = title;
		for (int i=0; i<lines.size(); i++) {
			ScoreboardLine score = registerLine(i+1, lines.get(i));
			this.lines.add(score);
			manager.tab.getFeatureManager().registerFeature("scoreboard-score-" + name + "-" + i, score);
		}
	}

	/**
	 * Registers line with given text and line number
	 * @param lineNumber - ID of line
	 * @param text - text to display
	 * @return most optimal line from provided text
	 */
	private ScoreboardLine registerLine(int lineNumber, String text) {
		if (text.startsWith("Custom|")) {
			String[] elements = text.split("\\|");
			return new CustomLine(this, lineNumber, elements[1], elements[2], elements[3], Integer.parseInt(elements[4]));
		}
		if (text.contains("%")) {
			if (manager.useNumbers) {
				return new NumberedStableDynamicLine(this, lineNumber, text);
			} else {
				return new All0StableDynamicLine(this, lineNumber, text);
			}
		}
		//static text
		if (manager.useNumbers) {
			if (text.length() > 26) {
				return new NumberedStaticLine(this, lineNumber, text);
			} else {
				//trying to avoid same player name when multiple lines have the same short text (such as for empty lines)
				return new NumberedStableDynamicLine(this, lineNumber, text);
			}
		}
		return new All0StaticLine(this, lineNumber, text);
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Returns true if condition is null or is met, false otherwise
	 * @param p - player to check
	 * @return true if condition is null or is met, false otherwise
	 */
	public boolean isConditionMet(TabPlayer p) {
		if (displayCondition == null) return true;
		return displayCondition.isMet(p);
	}

	/**
	 * Returns scoreboard that should be displayed if display condition is not met
	 * @return scoreboard that should be displayed if display condition is not met
	 */
	public String getChildScoreboard() {
		return childBoard;
	}

	/**
	 * Returns list of users currently seeing this scoreboard
	 * @return list of users currently seeing this scoreboard
	 */
	public Set<TabPlayer> getRegisteredUsers(){
		return players;
	}

	@Override
	public void register(TabPlayer p) {
		if (players.contains(p)) return; //already registered
		p.setProperty("scoreboard-title", title);
		PacketAPI.registerScoreboardObjective(p, ScoreboardManager.ObjectiveName, p.getProperty("scoreboard-title").get(), ScoreboardManager.DisplaySlot, EnumScoreboardHealthDisplay.INTEGER, getFeatureType());
		for (ScoreboardLine s : lines) {
			s.register(p);
		}
		players.add(p);
		p.setActiveScoreboard(this);
	}

	/**
	 * Unregisters this scoreboard from all players
	 */
	public void unregister() {
		for (TabPlayer all : players.toArray(new TabPlayer[0])) {
			unregister(all);
		}
		players.clear();
		lines.clear();
	}

	@Override
	public void unregister(TabPlayer p) {
		if (!players.contains(p)) return; //not registered
		p.sendCustomPacket(new PacketPlayOutScoreboardObjective(ScoreboardManager.ObjectiveName), TabFeature.SCOREBOARD);
		for (ScoreboardLine s : lines) {
			s.unregister(p);
		}
		players.remove(p);
		p.setActiveScoreboard(null);
	}

	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (refreshed.getProperty("scoreboard-title") == null) return;
		refreshed.sendCustomPacket(new PacketPlayOutScoreboardObjective(2, ScoreboardManager.ObjectiveName, refreshed.getProperty("scoreboard-title").updateAndGet(), EnumScoreboardHealthDisplay.INTEGER), TabFeature.SCOREBOARD);
	}

	@Override
	public List<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}

	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = TAB.getInstance().getPlaceholderManager().getUsedPlaceholderIdentifiersRecursive(title);
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.SCOREBOARD;
	}
}