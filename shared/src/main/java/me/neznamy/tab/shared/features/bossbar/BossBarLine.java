package me.neznamy.tab.shared.features.bossbar;

import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

/**
 * Class representing a bossbar from configuration
 */
public class BossBarLine implements me.neznamy.tab.api.bossbar.BossBar {

	//bossbar name
	public String name;
	
	//display condition
	public Condition displayCondition;
	
	//uuid
	public UUID uuid;
	
	//bossbar style
	public String style;
	
	//bossbar color
	public String color;
	
	//bossbar title
	public String title;
	
	//bossabr progress
	public String progress;

	/**
	 * Constructs new instance with given parameters
	 * @param name - name of bossbar
	 * @param displayCondition - display condition
	 * @param color - bossbar color
	 * @param style - bossbar style
	 * @param title - bossbar title
	 * @param progress - bossbar progress
	 */
	public BossBarLine(String name, String displayCondition, String color, String style, String title, String progress) {
		this.name = name;
		this.displayCondition = Condition.getCondition(displayCondition);
		this.uuid = UUID.randomUUID();
		this.color = color;
		this.style = style;
		this.title = title;
		this.progress = progress;
		TAB.getInstance().getPlaceholderManager().checkForRegistration(title, progress, color, style);
		TAB.getInstance().getFeatureManager().registerFeature("bossbar-title-" + name, new TextRefresher(this));
		TAB.getInstance().getFeatureManager().registerFeature("bossbar-progress-" + name, new ProgressRefresher(this));
		TAB.getInstance().getFeatureManager().registerFeature("bossbar-color-style-" + name, new ColorAndStyleRefresher(this));
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
	
	/**
	 * Registers this bossbar to specified player
	 * @param to - player to send bossbar to
	 */
	public void create(TabPlayer to){
		to.setProperty("bossbar-title-" + name, title);
		to.setProperty("bossbar-progress-" + name, progress);
		to.setProperty("bossbar-color-" + name, color);
		to.setProperty("bossbar-style-" + name, style);
		to.sendCustomPacket(new PacketPlayOutBoss(
				uuid, 
				to.getProperty("bossbar-title-" + name).get(), 
				(float)parseProgress(to.getProperty("bossbar-progress-" + name).get())/100, 
				parseColor(to.getProperty("bossbar-color-" + name).get()), 
				parseStyle(to.getProperty("bossbar-style-" + name).get())
			), TabFeature.BOSSBAR
		);
	}
	
	/**
	 * Removes bossbar from specified player
	 * @param to - player to remove bossbar from
	 */
	public void remove(TabPlayer to) {
		to.sendCustomPacket(new PacketPlayOutBoss(uuid), TabFeature.BOSSBAR);
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
		this.title = title;
		TAB.getInstance().getPlaceholderManager().checkForRegistration(title);
		for (TabPlayer p : TAB.getInstance().getPlayers()) {
			if (p.getActiveBossBars().contains(this)) {
				p.setProperty("bossbar-title-" + name, title);
				p.sendCustomPacket(new PacketPlayOutBoss(uuid, p.getProperty("bossbar-title-" + name).get()), TabFeature.BOSSBAR);
			}
		}
	}

	@Override
	public void setProgress(String progress) {
		this.progress = progress;
		TAB.getInstance().getPlaceholderManager().checkForRegistration(progress);
		for (TabPlayer p : TAB.getInstance().getPlayers()) {
			if (p.getActiveBossBars().contains(this)) {
				p.setProperty("bossbar-progress-" + name, progress);
				p.sendCustomPacket(new PacketPlayOutBoss(uuid, (float) parseProgress(p.getProperty("bossbar-progress-" + name).get())/100), TabFeature.BOSSBAR);
			}
		}
	}

	@Override
	public void setProgress(float progress) {
		setProgress(String.valueOf(progress));
	}

	@Override
	public void setColor(String color) {
		this.color = color;
		TAB.getInstance().getPlaceholderManager().checkForRegistration(color);
		for (TabPlayer p : TAB.getInstance().getPlayers()) {
			if (p.getActiveBossBars().contains(this)) {
				p.setProperty("bossbar-color-" + name, color);
				p.sendCustomPacket(new PacketPlayOutBoss(uuid, 
					parseColor(p.getProperty("bossbar-color-" + name).get()),
					parseStyle(p.getProperty("bossbar-style-" + name).get())
				), TabFeature.BOSSBAR);
			}
		}
	}

	@Override
	public void setColor(BarColor color) {
		setColor(color.toString());
	}

	@Override
	public void setStyle(String style) {
		this.style = style;
		TAB.getInstance().getPlaceholderManager().checkForRegistration(style);
		for (TabPlayer p : TAB.getInstance().getPlayers()) {
			if (p.getActiveBossBars().contains(this)) {
				p.setProperty("bossbar-style-" + name, style);
				p.sendCustomPacket(new PacketPlayOutBoss(uuid, 
					parseColor(p.getProperty("bossbar-color-" + name).get()),
					parseStyle(p.getProperty("bossbar-style-" + name).get())
				), TabFeature.BOSSBAR);
			}
		}
	}

	@Override
	public void setStyle(BarStyle style) {
		setStyle(style.toString());
	}
	
	/**
	 * Loads bossbar from config by it's name
	 * @param bar - name of bossbar in config
	 * @return loaded bossbar
	 */
	public static BossBarLine fromConfig(String bar) {
		String condition = TAB.getInstance().getConfiguration().bossbar.getString("bars." + bar + ".display-condition", null);
		if (condition == null) {
			Object permRequired = TAB.getInstance().getConfiguration().bossbar.getBoolean("bars." + bar + ".permission-required");
			if (permRequired != null && (boolean) permRequired) {
				condition = "permission:tab.bossbar." + bar;
			}
		}
		
		String style = TAB.getInstance().getConfiguration().bossbar.getString("bars." + bar + ".style");
		String color = TAB.getInstance().getConfiguration().bossbar.getString("bars." + bar + ".color");
		String progress = TAB.getInstance().getConfiguration().bossbar.getString("bars." + bar + ".progress");
		String text = TAB.getInstance().getConfiguration().bossbar.getString("bars." + bar + ".text");
		if (style == null) {
			TAB.getInstance().getErrorManager().missingAttribute("BossBar", bar, "style");
			style = "PROGRESS";
		}
		if (color == null) {
			TAB.getInstance().getErrorManager().missingAttribute("BossBar", bar, "color");
			color = "WHITE";
		}
		if (progress == null) {
			progress = "100";
			TAB.getInstance().getErrorManager().missingAttribute("BossBar", bar, "progress");
		}
		if (text == null) {
			text = "";
			TAB.getInstance().getErrorManager().missingAttribute("BossBar", bar, "text");
		}
		return new BossBarLine(bar, condition, color, style, text, progress);
	}
}