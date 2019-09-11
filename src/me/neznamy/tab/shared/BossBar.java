package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.Shared.Feature;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss.BarColor;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss.BarStyle;

public class BossBar{

	public static List<String> defaultBars = Lists.newArrayList();
	public static Map<String, List<String>> perWorld = Maps.newConcurrentMap();
	public static List<BossBarLine> lines = new ArrayList<BossBarLine>();
	public static int refresh;
	public static String toggleCommand;
	public static List<String> announcements = new ArrayList<String>();
	
	public static void load() {
		for (ITabPlayer p : Shared.getPlayers()) {
			p.detectBossBarsAndSend();
		}
		Shared.scheduleRepeatingTask(refresh, "refreshing bossbar", Feature.BOSSBAR, new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) {
					if (!p.bossbarVisible) continue;
					for (BossBarLine bar : p.getActiveBossBars().toArray(new BossBarLine[0])) {
						if (bar.hasPermission(p)) {
							PacketAPI.updateBossBar(p, bar);
						} else {
							PacketAPI.removeBossBar(p, bar);
							p.getActiveBossBars().remove(bar);
						}
					}
					for (String name : defaultBars) {
						BossBarLine bar = getLine(name);
						if (bar.hasPermission(p) && !p.getActiveBossBars().contains(bar)) {
							p.getActiveBossBars().add(bar);
							PacketAPI.createBossBar(p, bar);
						}
					}
					if (BossBar.perWorld.get(p.getWorldName()) != null)
						for (String worldbar : BossBar.perWorld.get(p.getWorldName())) {
							BossBarLine bar = BossBar.getLine(worldbar);
							if (bar.hasPermission(p) && !p.getActiveBossBars().contains(bar)) {
								PacketAPI.createBossBar(p, bar);
								p.activeBossBars.add(bar);
							}
						}
				}
			}
		});
	}
	public static BossBarLine getLine(String name) {
		for (BossBarLine l : lines) {
			if (l.getName().equalsIgnoreCase(name)) return l;
		}
		return null;
	}
	public static boolean onChat(final ITabPlayer sender, String message) {
		if (message.equalsIgnoreCase(toggleCommand)) {
			sender.bossbarVisible = !sender.bossbarVisible;
			if (sender.bossbarVisible) {
				sender.detectBossBarsAndSend();
				sender.sendMessage(Configs.bossbar_on);
			} else {
				for (BossBarLine line : sender.getActiveBossBars()) {
					PacketAPI.removeBossBar(sender, line);
				}
				sender.getActiveBossBars().clear();
				sender.sendMessage(Configs.bossbar_off);
			}
			return true;
		}
		return false;
	}
	public static void playerJoin(ITabPlayer p) {
		p.detectBossBarsAndSend();
	}
	public static void unload() {
		for (ITabPlayer p : Shared.getPlayers()) {
			for (BossBarLine line : p.getActiveBossBars()) {
				PacketAPI.removeBossBar(p, line);
			}
			p.getActiveBossBars().clear();
		}
		lines.clear();
	}
	public static class BossBarLine{
		
		private String name;
		private boolean permissionRequired;
		private UUID uuid; //1.9+
		private Object nmsEntity; //1.8.x
		private int entityId; //1.8.x
		private int refresh;
		public String style;
		public String color;
		public String text;
		public String progress;
		
		public BossBarLine(String name, boolean permissionRequired, int refresh, String color, String style, String text, String progress) {
			this.name = name;
			this.permissionRequired = permissionRequired;
			if (refresh == 0) {
				Shared.startupWarn("One of the BossBars has refresh interval of 0 milliseconds! Did you forget to configure it? Using 1000 to avoid issues.");
				refresh = 1000;
			}
			this.uuid = UUID.randomUUID();
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() == 7 || ProtocolVersion.SERVER_VERSION.getMinorVersion() == 8) {
				nmsEntity = MethodAPI.getInstance().newEntityWither();
				entityId = MethodAPI.getInstance().getEntityId(nmsEntity);
			}
			this.refresh = refresh;
			this.color = color;
			this.style = style;
			this.text = text;
			this.progress = progress;
		}
		public boolean hasPermission(ITabPlayer p) {
			return !permissionRequired || p.hasPermission("tab.bossbar." + name);
		}
		public String getName() {
			return name;
		}
		public int getRefresh() {
			return refresh;
		}
		public Object getEntity() {
			return nmsEntity;
		}
		public int getEntityId() {
			return entityId;
		}
		public UUID getUniqueId() {
			return uuid;
		}
		public BarColor parseColor(String color) {
			try {
				return BarColor.valueOf(color);
			} catch (Exception e) {
				Shared.error("\"" + color + "\" is not a valid boss bar color");
				return BarColor.WHITE;
			}
		}
		public BarStyle parseStyle(String style) {
			try {
				return BarStyle.valueOf(style);
			} catch (Exception e) {
				Shared.error("\"" + color + "\" is not a valid boss bar color");
				return BarStyle.PROGRESS;
			}
		}
		public float parseProgress(String progress) {
			try {
				return Integer.parseInt(progress);
			} catch (Exception e) {
				Shared.error("\"" + progress + "\" is not a valid number");
				return 100;
			}
		}
	}
}