package me.neznamy.tab.shared.features.bossbar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.api.protocol.PacketPlayOutBoss;
import me.neznamy.tab.shared.PropertyUtils;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

/**
 * Class representing a bossbar from configuration
 */
public class BossBarLine implements BossBar {

	private BossBarManagerImpl manager;
	
	//bossbar name
	private String name;
	
	//display condition
	private Condition displayCondition;
	
	//uuid
	private UUID uuid;
	
	//bossbar style
	private String style;
	
	//bossbar color
	private String color;
	
	//bossbar title
	private String title;
	
	//bossabr progress
	private String progress;
	
	//set of players seeing this bossbar
	private Set<TabPlayer> players = new HashSet<>();
	
	//refreshers
	private TextRefresher textRefresher;
	private ProgressRefresher progressRefresher;
	private ColorAndStyleRefresher colorAndStyleRefresher;

	/**
	 * Constructs new instance with given parameters
	 * @param manager - bossbar manager to count sent packets for
	 * @param name - name of bossbar
	 * @param displayCondition - display condition
	 * @param color - bossbar color
	 * @param style - bossbar style
	 * @param title - bossbar title
	 * @param progress - bossbar progress
	 */
	public BossBarLine(BossBarManagerImpl manager, String name, String displayCondition, String color, String style, String title, String progress) {
		this.manager = manager;
		this.name = name;
		this.displayCondition = Condition.getCondition(displayCondition);
		if (this.displayCondition != null) {
			manager.addUsedPlaceholders(Arrays.asList("%condition:" + this.displayCondition.getName() + "%"));
		}
		this.uuid = UUID.randomUUID();
		this.color = color;
		this.style = style;
		this.title = title;
		this.progress = progress;
		textRefresher = new TextRefresher(this);
		progressRefresher = new ProgressRefresher(this);
		colorAndStyleRefresher = new ColorAndStyleRefresher(this);
		TAB.getInstance().getFeatureManager().registerFeature("bossbar-title-" + name, textRefresher);
		TAB.getInstance().getFeatureManager().registerFeature("bossbar-progress-" + name, progressRefresher);
		TAB.getInstance().getFeatureManager().registerFeature("bossbar-color-style-" + name, colorAndStyleRefresher);
	}
	
	/**
	 * Returns true if condition is null or is met, false otherwise
	 * @param p - player to check condition for
	 * @return true if met, false if not
	 */
	public boolean isConditionMet(TabPlayer p) {
		if (displayCondition == null) return true;
		return displayCondition.isMet(p);
	}
	
	/**
	 * Parses string into color and returns it. If parsing failed, PURPLE is returned instead and
	 * error message is printed into error log
	 * @param color - string to parse
	 * @return parsed color
	 */
	public BarColor parseColor(String color) {
		return TAB.getInstance().getErrorManager().parseColor(color, BarColor.PURPLE, "bossbar color");
	}
	
	/**
	 * Parses string into style and returns it. If parsing failed, PROGRESS is returned instead and
	 * error message is printed into error log
	 * @param style - string to parse
	 * @return parsed style
	 */
	public BarStyle parseStyle(String style) {
		return TAB.getInstance().getErrorManager().parseStyle(style, BarStyle.PROGRESS, "bossbar style");
	}
	
	/**
	 * Parses string into progress and returns it. If parsing failed, 100 is returned instead and
	 * error message is printed into error log
	 * @param progress - string to parse
	 * @return parsed progress
	 */
	public float parseProgress(String progress) {
		return TAB.getInstance().getErrorManager().parseFloat(progress, 100, "bossbar progress");
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public UUID getUniqueId() {
		return uuid;
	}
	
	@Override
	public void setTitle(String title) {
		if (this.title == title) return;
		this.title = title;
		for (TabPlayer p : players) {
			p.setProperty(textRefresher, PropertyUtils.bossbarTitle(name), title);
			p.sendCustomPacket(new PacketPlayOutBoss(uuid, p.getProperty(PropertyUtils.bossbarTitle(name)).get()), manager);
		}
	}

	@Override
	public void setProgress(String progress) {
		if (this.progress == progress) return;
		this.progress = progress;
		for (TabPlayer p : players) {
			p.setProperty(progressRefresher, PropertyUtils.bossbarProgress(name), progress);
			p.sendCustomPacket(new PacketPlayOutBoss(uuid, parseProgress(p.getProperty(PropertyUtils.bossbarProgress(name)).get())/100), manager);
		}
	}

	@Override
	public void setProgress(float progress) {
		setProgress(String.valueOf(progress));
	}

	@Override
	public void setColor(String color) {
		if (this.color == color) return;
		this.color = color;
		for (TabPlayer p : players) {
			p.setProperty(colorAndStyleRefresher, PropertyUtils.bossbarColor(name), color);
			p.sendCustomPacket(new PacketPlayOutBoss(uuid, 
				parseColor(p.getProperty(PropertyUtils.bossbarColor(name)).get()),
				parseStyle(p.getProperty(PropertyUtils.bossbarStyle(name)).get())
			), manager);
		}
	}

	@Override
	public void setColor(BarColor color) {
		setColor(color.toString());
	}

	@Override
	public void setStyle(String style) {
		if (this.style == style) return;
		this.style = style;
		for (TabPlayer p : players) {
			p.setProperty(colorAndStyleRefresher, PropertyUtils.bossbarStyle(name), style);
			p.sendCustomPacket(new PacketPlayOutBoss(uuid, 
				parseColor(p.getProperty(PropertyUtils.bossbarColor(name)).get()),
				parseStyle(p.getProperty(PropertyUtils.bossbarStyle(name)).get())
			), manager);
		}
	}

	@Override
	public void setStyle(BarStyle style) {
		setStyle(style.toString());
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getProgress() {
		return progress;
	}

	@Override
	public String getColor() {
		return color;
	}

	@Override
	public String getStyle() {
		return style;
	}

	@Override
	public void addPlayer(TabPlayer player) {
		if (players.contains(player)) return;
		players.add(player);
		player.setProperty(textRefresher, PropertyUtils.bossbarTitle(name), title);
		player.setProperty(progressRefresher, PropertyUtils.bossbarProgress(name), progress);
		player.setProperty(colorAndStyleRefresher, PropertyUtils.bossbarColor(name), color);
		player.setProperty(colorAndStyleRefresher, PropertyUtils.bossbarStyle(name), style);
		player.sendCustomPacket(new PacketPlayOutBoss(
				uuid, 
				player.getProperty(PropertyUtils.bossbarTitle(name)).get(), 
				parseProgress(player.getProperty(PropertyUtils.bossbarProgress(name)).get())/100, 
				parseColor(player.getProperty(PropertyUtils.bossbarColor(name)).get()), 
				parseStyle(player.getProperty(PropertyUtils.bossbarStyle(name)).get())
			), manager
		);
	}

	@Override
	public void removePlayer(TabPlayer player) {
		if (!players.contains(player)) return;
		players.remove(player);
		player.sendCustomPacket(new PacketPlayOutBoss(uuid), manager);
	}

	@Override
	public Set<TabPlayer> getPlayers() {
		return new HashSet<>(players);
	}
}