package me.neznamy.tab.shared.features;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ConfigurationFile;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss.BarColor;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss.BarStyle;

public class BossBar implements SimpleFeature{

	public List<String> defaultBars;
	public Map<String, List<String>> perWorld;
	private List<BossBarLine> lines;
	private int refresh;
	private String toggleCommand;
	public List<String> announcements;
	private boolean remember_toggle_choice;
	public List<String> bossbar_off_players;

	@SuppressWarnings("unchecked")
	@Override
	public void load() {
		refresh = Configs.bossbar.getInt("refresh-interval-milliseconds", 1000);
		toggleCommand = Configs.bossbar.getString("bossbar-toggle-command", "/bossbar");
		defaultBars = Configs.bossbar.getStringList("default-bars");
		if (defaultBars == null) defaultBars = new ArrayList<String>();
		perWorld = (Map<String, List<String>>) Configs.bossbar.get("per-world");
		if (perWorld == null) perWorld = new HashMap<String, List<String>>();
		if (Configs.bossbar.getConfigurationSection("bars") != null) {
			for (String bar : Configs.bossbar.getConfigurationSection("bars").keySet()){
				boolean permissionRequired = Configs.bossbar.getBoolean("bars." + bar + ".permission-required", false);
				int refresh = Configs.bossbar.getInt("bars." + bar + ".refresh", 0);
				String style = Configs.bossbar.getString("bars." + bar + ".style");
				String color = Configs.bossbar.getString("bars." + bar + ".color");
				Object progress = Configs.bossbar.get("bars." + bar + ".progress");
				String text = Configs.bossbar.getString("bars." + bar + ".text");
				if (progress == null) {
					Shared.errorManager.startupWarn("BossBar \"&e" + bar + "&c\" is missing \"&eprogress&c\" attribute! &bUsing 100");
					progress = 100;
				}
				lines.add(new BossBarLine(bar, permissionRequired, refresh, color, style, text, progress+""));
			}
		}
		List<String> toRemove = new ArrayList<String>();
		for (String bar : defaultBars) {
			if (getLine(bar) == null) {
				Shared.errorManager.startupWarn("BossBar \"&e" + bar + "&c\" is defined as default bar, but does not exist! &bIgnoring.");
				toRemove.add(bar);
			}
		}
		defaultBars.removeAll(toRemove);
		remember_toggle_choice = Configs.bossbar.getBoolean("remember-toggle-choice", false);
		if (remember_toggle_choice) {
			File file = new File("plugins" + File.separatorChar + "TAB" + File.separatorChar + "playerdata.yml");
			try {
				if (!file.exists()) file.createNewFile();
				Configs.playerdata = new ConfigurationFile("playerdata.yml", null);
				bossbar_off_players = Configs.playerdata.getStringList("bossbar-off");
			} catch (Exception e) {
				Shared.errorManager.criticalError("Failed to load playerdata.yml", e);
			}
		}
		if (bossbar_off_players == null) bossbar_off_players = new ArrayList<String>();

		for (ITabPlayer p : Shared.getPlayers()) {
			p.detectBossBarsAndSend();
		}
		Shared.cpu.startRepeatingMeasuredTask(getRefresh(), "refreshing bossbar", "BossBar", new Runnable() {
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
					if (perWorld.get(p.getWorldName()) != null)
						for (String worldbar : perWorld.get(p.getWorldName())) {
							BossBarLine bar = getLine(worldbar);
							if (bar.hasPermission(p) && !p.getActiveBossBars().contains(bar)) {
								PacketAPI.createBossBar(p, bar);
								p.activeBossBars.add(bar);
							}
						}
				}
			}
		});
	}
	@Override
	public void unload() {
		for (ITabPlayer p : Shared.getPlayers()) {
			for (BossBarLine line : p.getActiveBossBars()) {
				PacketAPI.removeBossBar(p, line);
			}
			p.getActiveBossBars().clear();
		}
		lines.clear();
	}
	@Override
	public void onJoin(ITabPlayer p) {
		p.detectBossBarsAndSend();
	}
	@Override
	public void onQuit(ITabPlayer p) {
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		if (p.disabledBossbar) {
			for (BossBarLine line : lines)
				PacketAPI.removeBossBar(p, line);
		} else for (BossBarLine active : p.getActiveBossBars()) {
			if (!defaultBars.contains(active.getName())) { //per-world bar from previous world
				PacketAPI.removeBossBar(p, active);
			}
		}
		p.detectBossBarsAndSend();
	}

	public BossBarLine getLine(String name) {
		for (BossBarLine l : lines) {
			if (l.getName().equalsIgnoreCase(name)) return l;
		}
		return null;
	}
	public boolean onChat(ITabPlayer sender, String message) {
		if (message.equalsIgnoreCase(toggleCommand)) {
			sender.bossbarVisible = !sender.bossbarVisible;
			if (sender.bossbarVisible) {
				sender.detectBossBarsAndSend();
				sender.sendMessage(Configs.bossbar_on);
				if (remember_toggle_choice) {
					bossbar_off_players.remove(sender.getName());
					Configs.playerdata.set("bossbar-off", bossbar_off_players);
					Configs.playerdata.save();
				}
			} else {
				for (BossBarLine line : sender.getActiveBossBars()) {
					PacketAPI.removeBossBar(sender, line);
				}
				sender.getActiveBossBars().clear();
				sender.sendMessage(Configs.bossbar_off);
				if (remember_toggle_choice && !bossbar_off_players.contains(sender.getName())) {
					bossbar_off_players.add(sender.getName());
					Configs.playerdata.set("bossbar-off", bossbar_off_players);
					Configs.playerdata.save();
				}
			}
			return true;
		}
		return false;
	}
	public int getRefresh() {
		return refresh;
	}
	public class BossBarLine{

		private String name;
		private boolean permissionRequired;
		private UUID uuid; //1.9+
		private Object nmsEntity; // <1.9
		private int entityId; // <1.9
		private int refresh;
		public String style;
		public String color;
		public String text;
		public String progress;

		public BossBarLine(String name, boolean permissionRequired, int refresh, String color, String style, String text, String progress) {
			this.name = name;
			this.permissionRequired = permissionRequired;
			this.refresh = Shared.errorManager.fixBossBarRefresh(name, refresh);
			this.uuid = UUID.randomUUID();
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() < 9) {
				nmsEntity = MethodAPI.getInstance().newEntityWither();
				entityId = MethodAPI.getInstance().getEntityId(nmsEntity);
			}
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
			return Shared.errorManager.parseColor(color, BarColor.PURPLE, "bossbar color");
		}
		public BarStyle parseStyle(String style) {
			return Shared.errorManager.parseStyle(style, BarStyle.PROGRESS, "bossbar style");
		}
		public float parseProgress(String progress) {
			return Shared.errorManager.parseFloat(progress, 100, "bossbar progress");
		}
	}
}