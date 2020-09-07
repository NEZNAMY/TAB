package me.neznamy.tab.shared.features.bossbar;

import java.util.UUID;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss.BarColor;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss.BarStyle;

/**
 * Class representing a bossbar from configuration
 */
public class BossBarLine {

	private static int idCounter = 1000000000;
	
	public String name;
	public boolean permissionRequired;
	public UUID uuid; //1.9+
	public int entityId = idCounter++; // <1.9
	public String style;
	public String color;
	public String text;
	public String progress;

	public BossBarLine(String name, boolean permissionRequired, String color, String style, String text, String progress) {
		this.name = name;
		this.permissionRequired = permissionRequired;
		this.uuid = UUID.randomUUID();
		this.color = color;
		this.style = style;
		this.text = text;
		this.progress = progress;
		Shared.featureManager.registerFeature("bossbar-text-" + name, new TextRefresher(this));
		Shared.featureManager.registerFeature("bossbar-progress-" + name, new ProgressRefresher(this));
		Shared.featureManager.registerFeature("bossbar-color-style-" + name, new ColorAndStyleRefresher(this));
	}
	public boolean hasPermission(ITabPlayer p) {
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
	public void create(ITabPlayer to){
		to.setProperty("bossbar-text-" + name, text, null);
		to.setProperty("bossbar-progress-" + name, progress, null);
		to.setProperty("bossbar-color-" + name, color, null);
		to.setProperty("bossbar-style-" + name, style, null);
		to.sendCustomPacket(PacketPlayOutBoss.ADD(
				uuid, 
				to.getProperty("bossbar-text-" + name).get(), 
				(float)parseProgress(to.getProperty("bossbar-progress-" + name).get())/100, 
				parseColor(to.getProperty("bossbar-color-" + name).get()), 
				parseStyle(to.getProperty("bossbar-style-" + name).get())
			)
		);
	}
	public void remove(ITabPlayer to) {
		to.sendCustomPacket(PacketPlayOutBoss.REMOVE(uuid));
	}
}