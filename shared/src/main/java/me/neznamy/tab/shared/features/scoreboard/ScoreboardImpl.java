package me.neznamy.tab.shared.features.scoreboard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.scoreboard.Scoreboard;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.PropertyUtils;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.TabFeature;
import me.neznamy.tab.shared.features.scoreboard.lines.All0StableDynamicLine;
import me.neznamy.tab.shared.features.scoreboard.lines.All0StaticLine;
import me.neznamy.tab.shared.features.scoreboard.lines.CustomLine;
import me.neznamy.tab.shared.features.scoreboard.lines.NumberedStableDynamicLine;
import me.neznamy.tab.shared.features.scoreboard.lines.NumberedStaticLine;
import me.neznamy.tab.shared.features.scoreboard.lines.ScoreboardLine;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

/**
 * A class representing a scoreboard configured in premiumconfig
 */
public class ScoreboardImpl extends TabFeature implements Scoreboard {

	//scoreboard manager
	private ScoreboardManagerImpl manager;
	
	//name of this scoreboard
	private String name;
	
	//scoreboard title
	private String title;
	
	//display condition
	private Condition displayCondition;
	
	//scoreboard to display if condition is not met
	private String childBoard;
	
	//lines of scoreboard
	private List<ScoreboardLine> lines = new ArrayList<>();
	
	//players currently seeing this scoreboard
	private Set<TabPlayer> players = new HashSet<>();

	/**
	 * Constructs new instance with given parameters and registers lines to feature manager
	 * @param manager - scoreboard manager
	 * @param name - name of this scoreboard
	 * @param title - scoreboard title
	 * @param lines - lines of scoreboard
	 * @param displayCondition - display condition
	 * @param childBoard - scoreboard to display if condition is not met
	 */
	public ScoreboardImpl(ScoreboardManagerImpl manager, String name, String title, List<String> lines, String displayCondition, String childBoard) {
		this(manager, name, title, lines);
		this.displayCondition = Condition.getCondition(displayCondition);
		this.childBoard = childBoard;
	}

	/**
	 * Constructs new instance with given parameters and registers lines to feature manager
	 * @param manager - scoreboard manager
	 * @param name - name of this scoreboard
	 * @param title - scoreboard title
	 * @param lines - lines of scoreboard
	 */
	public ScoreboardImpl(ScoreboardManagerImpl manager, String name, String title, List<String> lines) {
		this.manager = manager;
		this.name = name;
		this.title = title;
		for (int i=0; i<lines.size(); i++) {
			ScoreboardLine score = registerLine(i+1, lines.get(i));
			getLines().add(score);
			TAB.getInstance().getFeatureManager().registerFeature("scoreboard-score-" + name + "-" + i, score);
		}
		refreshUsedPlaceholders();
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
			if (manager.isUsingNumbers()) {
				return new NumberedStableDynamicLine(this, lineNumber, text);
			} else {
				return new All0StableDynamicLine(this, lineNumber, text);
			}
		}
		//static text
		if (manager.isUsingNumbers()) {
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

	@Override
	public void addPlayer(TabPlayer p) {
		if (getPlayers().contains(p)) return; //already registered
		p.setProperty(PropertyUtils.SCOREBOARD_TITLE, title);
		PacketAPI.registerScoreboardObjective(p, ScoreboardManagerImpl.OBJECTIVE_NAME, p.getProperty(PropertyUtils.SCOREBOARD_TITLE).get(), ScoreboardManagerImpl.DISPLAY_SLOT, EnumScoreboardHealthDisplay.INTEGER, getFeatureType());
		for (ScoreboardLine s : getLines()) {
			s.register(p);
		}
		players.add(p);
		manager.getActiveScoreboards().remove(p);
	}

	/**
	 * Unregisters this scoreboard from all players
	 */
	public void unregister() {
		for (TabPlayer all : getPlayers().toArray(new TabPlayer[0])) {
			removePlayer(all);
		}
		players.clear();
	}

	@Override
	public void removePlayer(TabPlayer p) {
		if (!getPlayers().contains(p)) return; //not registered
		p.sendCustomPacket(new PacketPlayOutScoreboardObjective(ScoreboardManagerImpl.OBJECTIVE_NAME), getFeatureType());
		for (ScoreboardLine s : getLines()) {
			s.unregister(p);
		}
		players.remove(p);
		manager.getActiveScoreboards().remove(p);
	}

	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (refreshed.getProperty(PropertyUtils.SCOREBOARD_TITLE) == null) return;
		refreshed.sendCustomPacket(new PacketPlayOutScoreboardObjective(2, ScoreboardManagerImpl.OBJECTIVE_NAME, refreshed.getProperty(PropertyUtils.SCOREBOARD_TITLE).updateAndGet(), EnumScoreboardHealthDisplay.INTEGER), getFeatureType());
	}

	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = new HashSet<>(TAB.getInstance().getPlaceholderManager().detectPlaceholders(title));
	}

	@Override
	public String getFeatureType() {
		return manager.getFeatureType();
	}

	public List<ScoreboardLine> getLines() {
		return lines;
	}

	@Override
	public Set<TabPlayer> getPlayers() {
		return players;
	}

	public ScoreboardManagerImpl getManager() {
		return manager;
	}
}