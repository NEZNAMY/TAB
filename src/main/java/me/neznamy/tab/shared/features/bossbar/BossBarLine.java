package me.neznamy.tab.shared.features.bossbar;

import java.util.UUID;

import org.bukkit.entity.EntityType;

import me.neznamy.tab.platforms.bukkit.features.BossBar_legacy;
import me.neznamy.tab.platforms.bukkit.packets.DataWatcher;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOutEntityDestroy;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
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
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) Shared.featureManager.registerFeature("bossbar-color-style-" + name, new ColorAndStyleRefresher(this));
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
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			to.sendCustomPacket(PacketPlayOutBoss.CREATE(
					uuid, 
					to.properties.get("bossbar-text-" + name).get(), 
					(float)parseProgress(to.properties.get("bossbar-progress-" + name).get())/100, 
					parseColor(to.properties.get("bossbar-color-" + name).get()), 
					parseStyle(to.properties.get("bossbar-style-" + name).get())
				)
			);
		} else {
			PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(entityId, null, EntityType.WITHER, ((BossBar_legacy)Shared.featureManager.getFeature("bossbar1.8")).getWitherLocation(to));
			DataWatcher w = new DataWatcher();
			w.helper().setEntityFlags((byte) 32);
			w.helper().setCustomName(to.properties.get("bossbar-text-" + name).get(), to.getVersion());
			float health = (float)3*parseProgress(to.properties.get("bossbar-progress-" + name).get());
			if (health == 0) health = 1;
			w.helper().setHealth(health);
			packet.setDataWatcher(w);
			to.sendCustomBukkitPacket(packet);
		}
	}
	public void remove(ITabPlayer to) {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			to.sendCustomPacket(PacketPlayOutBoss.REMOVE(uuid));
		} else {
			to.sendCustomBukkitPacket(new PacketPlayOutEntityDestroy(entityId));
		}
	}
}