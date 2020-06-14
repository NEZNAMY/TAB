package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.entity.EntityType;

import me.neznamy.tab.platforms.bukkit.features.BossBar_legacy;
import me.neznamy.tab.platforms.bukkit.packets.DataWatcher;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.CommandListener;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss.BarColor;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss.BarStyle;

public class BossBar implements Loadable, JoinEventListener, WorldChangeListener, CommandListener{

	public List<String> defaultBars;
	public Map<String, List<String>> perWorld;
	public List<BossBarLine> lines = new ArrayList<BossBarLine>();
	private String toggleCommand;
	public List<String> announcements = new ArrayList<String>();
	public boolean remember_toggle_choice;
	public List<String> bossbar_off_players;
	public boolean permToToggle;

	@SuppressWarnings("unchecked")
	@Override
	public void load() {
		int refresh = Configs.bossbar.getInt("refresh-interval-milliseconds", 1000);
		if (refresh < 50) Shared.errorManager.refreshTooLow("BossBar", refresh);
		toggleCommand = Configs.bossbar.getString("bossbar-toggle-command", "/bossbar");
		defaultBars = Configs.bossbar.getStringList("default-bars");
		permToToggle = Configs.bossbar.getBoolean("permission-required-to-toggle", false);
		if (defaultBars == null) defaultBars = new ArrayList<String>();
		perWorld = Configs.bossbar.getConfigurationSection("per-world");
		for (Object bar : Configs.bossbar.getConfigurationSection("bars").keySet()){
			boolean permissionRequired = Configs.bossbar.getBoolean("bars." + bar + ".permission-required", false);
			String style = Configs.bossbar.getString("bars." + bar + ".style");
			String color = Configs.bossbar.getString("bars." + bar + ".color");
			String progress = Configs.bossbar.getString("bars." + bar + ".progress");
			String text = Configs.bossbar.getString("bars." + bar + ".text");
			if (style == null) {
				if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) Shared.errorManager.missingAttribute("BossBar", bar, "style");
				style = "PROGRESS";
			}
			if (color == null) {
				if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) Shared.errorManager.missingAttribute("BossBar", bar, "color");
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
			lines.add(new BossBarLine(bar+"", permissionRequired, color, style, text, progress));
		}
		for (String bar : defaultBars.toArray(new String[0])) {
			if (getLine(bar) == null) {
				Shared.errorManager.startupWarn("BossBar \"&e" + bar + "&c\" is defined as default bar, but does not exist! &bIgnoring.");
				defaultBars.remove(bar);
			}
		}
		for (Entry<String, List<String>> entry : perWorld.entrySet()) {
			List<String> bars = entry.getValue();
			for (String bar : bars.toArray(new String[0])) {
				if (getLine(bar) == null) {
					Shared.errorManager.startupWarn("BossBar \"&e" + bar + "&c\" is defined as per-world bar in world &e" + entry.getKey() + "&c, but does not exist! &bIgnoring.");
					bars.remove(bar);
				}
			}
		}
		remember_toggle_choice = Configs.bossbar.getBoolean("remember-toggle-choice", false);
		if (remember_toggle_choice) {
			bossbar_off_players = Configs.getPlayerData("bossbar-off");
		}
		if (bossbar_off_players == null) bossbar_off_players = new ArrayList<String>();

		for (ITabPlayer p : Shared.getPlayers()) {
			p.bossbarVisible = !bossbar_off_players.contains(p.getName());
			detectBossBarsAndSend(p);
		}
		Shared.featureCpu.startRepeatingMeasuredTask(refresh, "refreshing bossbar", CPUFeature.BOSSBAR, new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) {
					if (!p.bossbarVisible || p.disabledBossbar) continue;
					for (BossBarLine bar : p.activeBossBars.toArray(new BossBarLine[0])) {
						if (bar.hasPermission(p)) {
							updateBossBar(p, bar);
						} else {
							removeBossBar(p, bar);
							p.activeBossBars.remove(bar);
						}
					}
					//permission update check
					showBossBars(p, defaultBars);
					showBossBars(p, perWorld.get(p.getWorldName()));
				}
			}
		});
	}
	@Override
	public void unload() {
		for (ITabPlayer p : Shared.getPlayers()) {
			for (BossBarLine line : p.activeBossBars) {
				removeBossBar(p, line);
			}
			p.activeBossBars.clear();
		}
		lines.clear();
	}
	@Override
	public void onJoin(ITabPlayer connectedPlayer) {
		connectedPlayer.bossbarVisible = !bossbar_off_players.contains(connectedPlayer.getName());
		detectBossBarsAndSend(connectedPlayer);
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		for (BossBarLine line : p.activeBossBars) {
			removeBossBar(p, line);
		}
		detectBossBarsAndSend(p);
	}

	public BossBarLine getLine(String name) {
		for (BossBarLine l : lines) {
			if (l.getName().equalsIgnoreCase(name)) return l;
		}
		return null;
	}
	@Override
	public boolean onCommand(ITabPlayer sender, String message) {
		if (message.equalsIgnoreCase(toggleCommand)) {
			Shared.command.execute(sender, new String[] {"bossbar"});
			return true;
		}
		return false;
	}
	public void detectBossBarsAndSend(ITabPlayer p) {
		p.activeBossBars.clear();
		if (p.disabledBossbar || !p.bossbarVisible) return;
		showBossBars(p, defaultBars);
		showBossBars(p, announcements);
		showBossBars(p, perWorld.get(p.getWorldName()));
	}
	private void showBossBars(ITabPlayer p, List<String> bars) {
		if (bars == null) return;
		for (String defaultBar : bars) {
			BossBarLine bar = getLine(defaultBar);
			if (bar.hasPermission(p) && !p.activeBossBars.contains(bar)) {
				createBossBar(p, bar);
				p.activeBossBars.add(bar);
			}
		}
	}
	
	public void createBossBar(ITabPlayer to, BossBarLine bar){
		to.setProperty("bossbar-text-"+bar.getName(), bar.text, null);
		to.setProperty("bossbar-progress-"+bar.getName(), bar.progress, null);
		to.setProperty("bossbar-color-"+bar.getName(), bar.color, null);
		to.setProperty("bossbar-style-"+bar.getName(), bar.style, null);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			to.sendCustomPacket(PacketPlayOutBoss.CREATE(bar.getUniqueId(), 
					to.properties.get("bossbar-text-"+bar.getName()).get(), 
					(float)bar.parseProgress(to.properties.get("bossbar-progress-"+bar.getName()).get())/100, 
					bar.parseColor(to.properties.get("bossbar-color-"+bar.getName()).get()), 
					bar.parseStyle(to.properties.get("bossbar-style-"+bar.getName()).get())));
		} else {
			PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(bar.getEntityId(), null, EntityType.WITHER, ((BossBar_legacy)Shared.features.get("bossbar1.8")).getWitherLocation(to));
			DataWatcher w = new DataWatcher(null);
			DataWatcher.Helper.setEntityFlags(w, (byte) 32);
			DataWatcher.Helper.setCustomName(w, to.properties.get("bossbar-text-"+bar.getName()).get(), to.getVersion());
			float health = (float)3*bar.parseProgress(to.properties.get("bossbar-progress-"+bar.getName()).get());
			if (health == 0) health = 1;
			DataWatcher.Helper.setHealth(w, health);
			packet.setDataWatcher(w);
			to.sendCustomBukkitPacket(packet);
		}
	}
	public void removeBossBar(ITabPlayer to, BossBarLine bar) {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			to.sendCustomPacket(PacketPlayOutBoss.REMOVE(bar.getUniqueId()));
		} else {
			to.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityDestroy(bar.getEntityId()));
		}
	}
	public void updateBossBar(ITabPlayer to, BossBarLine bar) {
		Property progress = to.properties.get("bossbar-progress-"+bar.getName());
		Property text = to.properties.get("bossbar-text-"+bar.getName());
		if (text == null) return; //not registered yet
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			Property color = to.properties.get("bossbar-color-"+bar.getName());
			Property style = to.properties.get("bossbar-style-"+bar.getName());
			boolean colorUpdate = color.isUpdateNeeded();
			boolean styleUpdate = style.isUpdateNeeded();
			if (colorUpdate || styleUpdate) {
				to.sendCustomPacket(PacketPlayOutBoss.UPDATE_STYLE(bar.getUniqueId(), bar.parseColor(color.get()), bar.parseStyle(style.get())));
			}
			if (progress.isUpdateNeeded()) {
				to.sendCustomPacket(PacketPlayOutBoss.UPDATE_PCT(bar.getUniqueId(), (float)bar.parseProgress(progress.get())/100));
			}
			if (text.isUpdateNeeded()) {
				to.sendCustomPacket(PacketPlayOutBoss.UPDATE_NAME(bar.getUniqueId(), text.get()));
			}
		} else {
			DataWatcher w = new DataWatcher(null);
			boolean update = false;
			if (text.isUpdateNeeded()) {
				DataWatcher.Helper.setCustomName(w, text.get(), to.getVersion());
				update = true;
			}
			if (progress.isUpdateNeeded()) {
				float health = (float)3*bar.parseProgress(progress.get());
				if (health == 0) health = 1;
				DataWatcher.Helper.setHealth(w, health);
				update = true;
			}
			if (update) to.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityMetadata(bar.getEntityId(), w.toNMS(), true));
		}
	}
	
	public class BossBarLine{

		private String name;
		private boolean permissionRequired;
		private UUID uuid; //1.9+
		private Object nmsEntity; // <1.9
		private int entityId; // <1.9
		public String style;
		public String color;
		public String text;
		public String progress;

		public BossBarLine(String name, boolean permissionRequired, String color, String style, String text, String progress) {
			this.name = name;
			this.permissionRequired = permissionRequired;
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