package me.neznamy.tab.shared.features.scoreboard;

import java.util.*;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore.Action;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.api.scoreboard.Line;
import me.neznamy.tab.api.scoreboard.Scoreboard;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.scoreboard.lines.*;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

/**
 * A class representing a scoreboard configured in config
 */
public class ScoreboardImpl extends TabFeature implements Scoreboard {

	//scoreboard manager
	private final ScoreboardManagerImpl manager;

	//name of this scoreboard
	private final String name;

	//scoreboard title
	private String title;

	//display condition
	private Condition displayCondition;

	//lines of scoreboard
	private final List<Line> lines = new ArrayList<>();

	//players currently seeing this scoreboard
	private final Set<TabPlayer> players = Collections.newSetFromMap(new WeakHashMap<>());

	/**
	 * Constructs new instance with given parameters and registers lines to feature manager
	 * @param manager - scoreboard manager
	 * @param name - name of this scoreboard
	 * @param title - scoreboard title
	 * @param lines - lines of scoreboard
	 * @param displayCondition - display condition
	 */
	public ScoreboardImpl(ScoreboardManagerImpl manager, String name, String title, List<String> lines, String displayCondition) {
		this(manager, name, title, lines, false);
		this.displayCondition = Condition.getCondition(displayCondition);
		if (this.displayCondition != null) {
			manager.addUsedPlaceholders(Collections.singletonList("%condition:" + this.displayCondition.getName() + "%"));
		}
	}

	/**
	 * Constructs new instance with given parameters and registers lines to feature manager
	 * @param manager - scoreboard manager
	 * @param name - name of this scoreboard
	 * @param title - scoreboard title
	 * @param lines - lines of scoreboard
	 */
	public ScoreboardImpl(ScoreboardManagerImpl manager, String name, String title, List<String> lines, boolean dynamicLinesOnly) {
		super(manager.getFeatureName(), "Updating scoreboard title");
		this.manager = manager;
		this.name = name;
		this.title = title;
		for (int i=0; i<lines.size(); i++) {
			ScoreboardLine score;
			if (dynamicLinesOnly) {
				score = new StableDynamicLine(this, i+1, lines.get(i));
			} else {
				score = registerLine(i+1, lines.get(i));
			}
			this.lines.add(score);
			TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.scoreboardLine(name, i), score);
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
		if (text.startsWith("Long|")) {
			return new LongLine(this, lineNumber, text.substring(5));
		}
		if (text.contains("%") || (manager.isUsingNumbers() && text.length() <= 26)) {
			return new StableDynamicLine(this, lineNumber, text);
		}
		return new StaticLine(this, lineNumber, text);
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
		return displayCondition == null || displayCondition.isMet(p);
	}

	public void addPlayer(TabPlayer p) {
		if (players.contains(p)) return; //already registered
		players.add(p);
		p.setProperty(this, TabConstants.Property.SCOREBOARD_TITLE, title);
		p.sendCustomPacket(new PacketPlayOutScoreboardObjective(0, ScoreboardManagerImpl.OBJECTIVE_NAME, p.getProperty(TabConstants.Property.SCOREBOARD_TITLE).get(),
				EnumScoreboardHealthDisplay.INTEGER), TabConstants.PacketCategory.SCOREBOARD_TITLE);
		p.sendCustomPacket(new PacketPlayOutScoreboardDisplayObjective(ScoreboardManagerImpl.DISPLAY_SLOT, ScoreboardManagerImpl.OBJECTIVE_NAME), TabConstants.PacketCategory.SCOREBOARD_TITLE);
		for (Line s : lines) {
			((ScoreboardLine)s).register(p);
		}
		manager.getActiveScoreboards().put(p, this);
		recalculateScores(p);
	}

	@Override
	public void unregister() {
		for (TabPlayer all : getPlayers().toArray(new TabPlayer[0])) {
			removePlayer(all);
		}
		players.clear();
	}

	public void removePlayer(TabPlayer p) {
		if (!players.contains(p)) return; //not registered
		p.sendCustomPacket(new PacketPlayOutScoreboardObjective(ScoreboardManagerImpl.OBJECTIVE_NAME), this);
		for (Line line : lines) {
			p.sendCustomPacket(new PacketPlayOutScoreboardTeam(((ScoreboardLine)line).getTeamName()), TabConstants.PacketCategory.SCOREBOARD_LINES);
		}
		players.remove(p);
		manager.getActiveScoreboards().remove(p);
	}

	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (refreshed.getProperty(TabConstants.Property.SCOREBOARD_TITLE) == null) return;
		refreshed.sendCustomPacket(new PacketPlayOutScoreboardObjective(2, ScoreboardManagerImpl.OBJECTIVE_NAME, 
				refreshed.getProperty(TabConstants.Property.SCOREBOARD_TITLE).updateAndGet(), EnumScoreboardHealthDisplay.INTEGER), TabConstants.PacketCategory.SCOREBOARD_TITLE);
	}

	@Override
	public List<Line> getLines() {
		return lines;
	}

	public Set<TabPlayer> getPlayers() {
		return players;
	}

	public ScoreboardManagerImpl getManager() {
		return manager;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
		for (TabPlayer p : players) {
			p.setProperty(this, TabConstants.Property.SCOREBOARD_TITLE, title);
			refresh(p, false);
		}
	}

	@Override
	public void addLine(String text) {
		StableDynamicLine line = new StableDynamicLine(this, lines.size()+1, text);
		TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.scoreboardLine(name, lines.size()), line);
		lines.add(line);
		for (TabPlayer p : players) {
			line.register(p);
			recalculateScores(p);
		}
	}

	@Override
	public void removeLine(int index) {
		if (index < 0 || index >= lines.size()) throw new IndexOutOfBoundsException("Index " + index + " is out of range (0 - " + (lines.size()-1) + ")");
		ScoreboardLine line = (ScoreboardLine) lines.get(index);
		lines.remove(line);
		for (TabPlayer p : players) {
			line.unregister(p);
			recalculateScores(p);
		}
		TAB.getInstance().getFeatureManager().unregisterFeature(TabConstants.Feature.scoreboardLine(name, index));
	}

	public void recalculateScores(TabPlayer p) {
		if (!manager.isUsingNumbers()) return;
		List<Line> linesReversed = new ArrayList<>(lines);
		Collections.reverse(linesReversed);
		int score = 1;
		for (Line line : linesReversed) {
			if (line instanceof CustomLine) {
				score++;
				continue;
			}
			if (line instanceof StaticLine || p.getProperty(getName() + "-" + ((ScoreboardLine)line).getTeamName()).get().length() > 0){
				p.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, ScoreboardManagerImpl.OBJECTIVE_NAME, ((ScoreboardLine)line).getPlayerName(), score++), this);
			}
		}
	}
}