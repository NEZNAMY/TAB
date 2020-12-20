package me.neznamy.tab.shared.features.bossbar;

import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

/**
 * Class representing a bossbar from configuration
 */
public class BossBarLine implements me.neznamy.tab.api.bossbar.BossBar {

	private static int idCounter = 1000000000;
	
	public String name;
	public Condition displayCondition;
	public UUID uuid; //1.9+
	public int entityId = idCounter++; // <1.9
	public String style;
	public String color;
	public String title;
	public String progress;

	public BossBarLine(String name, String displayCondition, String color, String style, String title, String progress) {
		this.name = name;
		this.displayCondition = Condition.getCondition(displayCondition);
		this.uuid = UUID.randomUUID();
		this.color = color;
		this.style = style;
		this.title = title;
		this.progress = progress;
		((PlaceholderManager) Shared.featureManager.getFeature("placeholders")).checkForRegistration(title, progress, color, style);
		Shared.featureManager.registerFeature("bossbar-title-" + name, new TextRefresher(this));
		Shared.featureManager.registerFeature("bossbar-progress-" + name, new ProgressRefresher(this));
		Shared.featureManager.registerFeature("bossbar-color-style-" + name, new ColorAndStyleRefresher(this));
	}
	
	public boolean isConditionMet(TabPlayer p) {
		if (displayCondition == null) return true;
		return displayCondition.isMet(p);
	}
	
	public BarColor parseColor(String color) {
		return Shared.errorManager.parseColor(color, BarColor.PURPLE, "bossbar color");
	}
	
	public BarStyle parseStyle(String style) {
		return Shared.errorManager.parseStyle(style, BarStyle.PROGRESS, "bossbar style");
	}
	
	public float parseProgress(String progress) {
		return Shared.errorManager.parseFloat(progress, 100, "bossbar progress");
	}
	
	public void create(TabPlayer to){
		to.setProperty("bossbar-title-" + name, title);
		to.setProperty("bossbar-progress-" + name, progress);
		to.setProperty("bossbar-color-" + name, color);
		to.setProperty("bossbar-style-" + name, style);
		to.sendCustomPacket(PacketPlayOutBoss.ADD(
				uuid, 
				to.getProperty("bossbar-title-" + name).get(), 
				(float)parseProgress(to.getProperty("bossbar-progress-" + name).get())/100, 
				parseColor(to.getProperty("bossbar-color-" + name).get()), 
				parseStyle(to.getProperty("bossbar-style-" + name).get())
			)
		);
	}
	
	public void remove(TabPlayer to) {
		to.sendCustomPacket(PacketPlayOutBoss.REMOVE(uuid));
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
		((PlaceholderManager) Shared.featureManager.getFeature("placeholders")).checkForRegistration(title);
		for (TabPlayer p : Shared.getPlayers()) {
			if (p.getActiveBossBars().contains(this)) {
				p.setProperty("bossbar-title-" + name, title);
				p.sendCustomPacket(PacketPlayOutBoss.UPDATE_NAME(uuid, p.getProperty("bossbar-title-" + name).get()));
			}
		}
	}

	@Override
	public void setProgress(String progress) {
		this.progress = progress;
		((PlaceholderManager) Shared.featureManager.getFeature("placeholders")).checkForRegistration(progress);
		for (TabPlayer p : Shared.getPlayers()) {
			if (p.getActiveBossBars().contains(this)) {
				p.setProperty("bossbar-progress-" + name, progress);
				p.sendCustomPacket(PacketPlayOutBoss.UPDATE_PCT(uuid, (float) parseProgress(p.getProperty("bossbar-progress-" + name).get())/100));
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
		((PlaceholderManager) Shared.featureManager.getFeature("placeholders")).checkForRegistration(color);
		for (TabPlayer p : Shared.getPlayers()) {
			if (p.getActiveBossBars().contains(this)) {
				p.setProperty("bossbar-color-" + name, color);
				p.sendCustomPacket(PacketPlayOutBoss.UPDATE_STYLE(uuid, 
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
		((PlaceholderManager) Shared.featureManager.getFeature("placeholders")).checkForRegistration(style);
		for (TabPlayer p : Shared.getPlayers()) {
			if (p.getActiveBossBars().contains(this)) {
				p.setProperty("bossbar-style-" + name, style);
				p.sendCustomPacket(PacketPlayOutBoss.UPDATE_STYLE(uuid, 
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
		String condition = null;
		Object obj = Configs.bossbar.getBoolean("bars." + bar + ".permission-required");
		if (obj != null) {
			if ((boolean) obj) {
				condition = "permission:tab.bossbar." + bar;
			}
		} else {
			condition = Configs.bossbar.getString("bars." + bar + ".display-condition", null);
		}
		
		String style = Configs.bossbar.getString("bars." + bar + ".style");
		String color = Configs.bossbar.getString("bars." + bar + ".color");
		String progress = Configs.bossbar.getString("bars." + bar + ".progress");
		String text = Configs.bossbar.getString("bars." + bar + ".text");
		if (style == null) {
			Shared.errorManager.missingAttribute("BossBar", bar, "style");
			style = "PROGRESS";
		}
		if (color == null) {
			Shared.errorManager.missingAttribute("BossBar", bar, "color");
			color = "WHITE";
		}
		if (progress == null) {
			progress = "100";
			Shared.errorManager.missingAttribute("BossBar", bar, "progress");
		}
		if (text == null) {
			text = "";
			Shared.errorManager.missingAttribute("BossBar", bar, "text");
		}
		return new BossBarLine(bar, condition, color, style, text, progress);
	}
}