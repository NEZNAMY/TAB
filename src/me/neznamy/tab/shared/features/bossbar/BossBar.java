package me.neznamy.tab.shared.features.bossbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.CommandListener;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;

public class BossBar implements Loadable, JoinEventListener, WorldChangeListener, CommandListener{

	public List<String> defaultBars;
	public Map<String, List<String>> perWorld;
	public Map<String, BossBarLine> lines = new HashMap<String, BossBarLine>();
	private String toggleCommand;
	public List<String> announcements = new ArrayList<String>();
	public boolean remember_toggle_choice;
	public List<String> bossbar_off_players;
	public boolean permToToggle;

	@SuppressWarnings("unchecked")
	public BossBar() {
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
			lines.put(bar+"", new BossBarLine(bar+"", permissionRequired, color, style, text, progress));
		}
		for (String bar : defaultBars.toArray(new String[0])) {
			if (lines.get(bar) == null) {
				Shared.errorManager.startupWarn("BossBar \"&e" + bar + "&c\" is defined as default bar, but does not exist! &bIgnoring.");
				defaultBars.remove(bar);
			}
		}
		for (Entry<String, List<String>> entry : perWorld.entrySet()) {
			List<String> bars = entry.getValue();
			for (String bar : bars.toArray(new String[0])) {
				if (lines.get(bar) == null) {
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
	}
	@Override
	public void load() {
		for (ITabPlayer p : Shared.getPlayers()) {
			p.bossbarVisible = !bossbar_off_players.contains(p.getName());
			detectBossBarsAndSend(p);
		}
		Shared.featureCpu.startRepeatingMeasuredTask(1000, "refreshing bossbar permissions", CPUFeature.BOSSBAR_PERMISSION_CHECK, new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) {
					if (!p.bossbarVisible || p.disabledBossbar) continue;
					for (BossBarLine bar : p.activeBossBars.toArray(new BossBarLine[0])) {
						if (!bar.hasPermission(p)) {
							bar.remove(p);
							p.activeBossBars.remove(bar);
						}
					}
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
				line.remove(p);
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
			line.remove(p);
		}
		detectBossBarsAndSend(p);
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
			BossBarLine bar = lines.get(defaultBar);
			if (bar.hasPermission(p) && !p.activeBossBars.contains(bar)) {
				bar.create(p);
				p.activeBossBars.add(bar);
			}
		}
	}
}