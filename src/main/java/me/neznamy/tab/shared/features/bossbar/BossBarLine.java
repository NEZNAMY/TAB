package me.neznamy.tab.shared.features.bossbar;

import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

/**
 * Class representing a bossbar from configuration
 */
public class BossBarLine implements me.neznamy.tab.api.bossbar.BossBar {

	private static int idCounter = 1000000000;
	
	private TAB tab;
	public String name;
	public Condition displayCondition;
	public UUID uuid; //1.9+
	public int entityId = idCounter++; // <1.9
	public String style;
	public String color;
	public String title;
	public String progress;

	public BossBarLine(TAB tab, String name, String displayCondition, String color, String style, String title, String progress) {
		this.tab = tab;
		this.name = name;
		this.displayCondition = Condition.getCondition(displayCondition);
		this.uuid = UUID.randomUUID();
		this.color = color;
		this.style = style;
		this.title = title;
		this.progress = progress;
		TAB.getInstance().getPlaceholderManager().checkForRegistration(title, progress, color, style);
		tab.getFeatureManager().registerFeature("bossbar-title-" + name, new TextRefresher(this));
		tab.getFeatureManager().registerFeature("bossbar-progress-" + name, new ProgressRefresher(this));
		tab.getFeatureManager().registerFeature("bossbar-color-style-" + name, new ColorAndStyleRefresher(this));
	}
	
	public boolean isConditionMet(TabPlayer p) {
		if (displayCondition == null) return true;
		return displayCondition.isMet(p);
	}
	
	public BarColor parseColor(String color) {
		return tab.getErrorManager().parseColor(color, BarColor.PURPLE, "bossbar color");
	}
	
	public BarStyle parseStyle(String style) {
		return tab.getErrorManager().parseStyle(style, BarStyle.PROGRESS, "bossbar style");
	}
	
	public float parseProgress(String progress) {
		return tab.getErrorManager().parseFloat(progress, 100, "bossbar progress");
	}
	
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
			)
		);
	}
	
	public void remove(TabPlayer to) {
		to.sendCustomPacket(new PacketPlayOutBoss(uuid));
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public int getEntityId() {
		return entityId;
	}
	
	@Override
	public void setTitle(String title) {
		this.title = title;
		TAB.getInstance().getPlaceholderManager().checkForRegistration(title);
		for (TabPlayer p : tab.getPlayers()) {
			if (p.getActiveBossBars().contains(this)) {
				p.setProperty("bossbar-title-" + name, title);
				p.sendCustomPacket(new PacketPlayOutBoss(uuid, p.getProperty("bossbar-title-" + name).get()));
			}
		}
	}

	@Override
	public void setProgress(String progress) {
		this.progress = progress;
		TAB.getInstance().getPlaceholderManager().checkForRegistration(progress);
		for (TabPlayer p : tab.getPlayers()) {
			if (p.getActiveBossBars().contains(this)) {
				p.setProperty("bossbar-progress-" + name, progress);
				p.sendCustomPacket(new PacketPlayOutBoss(uuid, (float) parseProgress(p.getProperty("bossbar-progress-" + name).get())/100));
			}
		}
	}

	@Override
	public void setProgress(float progress) {
		setProgress(progress+"");
	}

	@Override
	public void setColor(String color) {
		this.color = color;
		TAB.getInstance().getPlaceholderManager().checkForRegistration(color);
		for (TabPlayer p : tab.getPlayers()) {
			if (p.getActiveBossBars().contains(this)) {
				p.setProperty("bossbar-color-" + name, color);
				p.sendCustomPacket(new PacketPlayOutBoss(uuid, 
					parseColor(p.getProperty("bossbar-color-" + name).get()),
					parseStyle(p.getProperty("bossbar-style-" + name).get())
				));
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
		for (TabPlayer p : tab.getPlayers()) {
			if (p.getActiveBossBars().contains(this)) {
				p.setProperty("bossbar-style-" + name, style);
				p.sendCustomPacket(new PacketPlayOutBoss(uuid, 
					parseColor(p.getProperty("bossbar-color-" + name).get()),
					parseStyle(p.getProperty("bossbar-style-" + name).get())
				));
			}
		}
	}

	@Override
	public void setStyle(BarStyle style) {
		setStyle(style.toString());
	}
	
	public static BossBarLine fromConfig(String bar) {
		TAB tab = TAB.getInstance();
		String condition = null;
		Object obj = tab.getConfiguration().bossbar.getBoolean("bars." + bar + ".permission-required");
		if (obj != null) {
			if ((boolean) obj) {
				condition = "permission:tab.bossbar." + bar;
			}
		} else {
			condition = tab.getConfiguration().bossbar.getString("bars." + bar + ".display-condition", null);
		}
		
		String style = tab.getConfiguration().bossbar.getString("bars." + bar + ".style");
		String color = tab.getConfiguration().bossbar.getString("bars." + bar + ".color");
		String progress = tab.getConfiguration().bossbar.getString("bars." + bar + ".progress");
		String text = tab.getConfiguration().bossbar.getString("bars." + bar + ".text");
		if (style == null) {
			tab.getErrorManager().missingAttribute("BossBar", bar, "style");
			style = "PROGRESS";
		}
		if (color == null) {
			tab.getErrorManager().missingAttribute("BossBar", bar, "color");
			color = "WHITE";
		}
		if (progress == null) {
			progress = "100";
			tab.getErrorManager().missingAttribute("BossBar", bar, "progress");
		}
		if (text == null) {
			text = "";
			tab.getErrorManager().missingAttribute("BossBar", bar, "text");
		}
		return new BossBarLine(tab, bar, condition, color, style, text, progress);
	}
}