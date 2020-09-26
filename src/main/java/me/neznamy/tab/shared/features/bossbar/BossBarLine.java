package me.neznamy.tab.shared.features.bossbar;

import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * Class representing a bossbar from configuration
 */
public class BossBarLine implements me.neznamy.tab.api.bossbar.BossBar {

	private static int idCounter = 1000000000;
	
	public String name;
	public boolean permissionRequired;
	public UUID uuid; //1.9+
	public int entityId = idCounter++; // <1.9
	public String style;
	public String color;
	public String title;
	public String progress;

	public BossBarLine(String name, boolean permissionRequired, String color, String style, String title, String progress) {
		this.name = name;
		this.permissionRequired = permissionRequired;
		this.uuid = UUID.randomUUID();
		this.color = color;
		this.style = style;
		this.title = title;
		this.progress = progress;
		Placeholders.checkForRegistration(title, progress, color, style);
		Shared.featureManager.registerFeature("bossbar-title-" + name, new TextRefresher(this));
		Shared.featureManager.registerFeature("bossbar-progress-" + name, new ProgressRefresher(this));
		Shared.featureManager.registerFeature("bossbar-color-style-" + name, new ColorAndStyleRefresher(this));
	}
	
	public boolean hasPermission(TabPlayer p) {
		return !permissionRequired || p.hasPermission("tab.bossbar." + name);
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
	public void setTitle(String title) {
		this.title = title;
		Placeholders.checkForRegistration(title);
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
		Placeholders.checkForRegistration(progress);
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
		Placeholders.checkForRegistration(color);
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
		Placeholders.checkForRegistration(style);
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
}