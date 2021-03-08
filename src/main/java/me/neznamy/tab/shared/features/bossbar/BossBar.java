package me.neznamy.tab.shared.features.bossbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.event.CommandListener;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.features.types.event.WorldChangeListener;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;

/**
 * Class for handling bossbar feature
 */
public class BossBar implements Loadable, JoinEventListener, WorldChangeListener, CommandListener {

	//tab instance
	private TAB tab;
	
	//default bossbars
	public List<String> defaultBars;
	
	//per-world / per-server bossbars
	public Map<String, List<String>> perWorld;
	
	//registered bossbars
	public Map<String, BossBarLine> lines = new HashMap<String, BossBarLine>();
	
	//toggle command
	private String toggleCommand;
	
	//list of currently running bossbar announcements
	public List<String> announcements = new ArrayList<String>();
	
	//saving toggle choice into file
	public boolean remember_toggle_choice;
	
	//players with toggled bossbar
	public List<String> bossbar_off_players = new ArrayList<String>();
	
	//if permission is required to toggle
	public boolean permToToggle;
	
	//list of worlds / servers where bossbar feature is disabled entirely
	private List<String> disabledWorlds;
	
	//time when bossbar announce ends, used for placeholder
	public long announceEndTime;
	
	//if bossbar is hidden by default until toggle command is used
	private boolean hiddenByDefault;

	/**
	 * Constructs new instance and loads configuration
	 * @param tab - tab instance
	 */
	public BossBar(TAB tab) {
		this.tab = tab;
		disabledWorlds = tab.getConfiguration().config.getStringList("disable-features-in-"+tab.getPlatform().getSeparatorType()+"s.bossbar", Arrays.asList("disabled" + tab.getPlatform().getSeparatorType()));
		toggleCommand = tab.getConfiguration().bossbar.getString("bossbar-toggle-command", "/bossbar");
		defaultBars = tab.getConfiguration().bossbar.getStringList("default-bars", new ArrayList<String>());
		permToToggle = tab.getConfiguration().bossbar.getBoolean("permission-required-to-toggle", false);
		hiddenByDefault = tab.getConfiguration().bossbar.getBoolean("hidden-by-default", false);
		perWorld = tab.getConfiguration().bossbar.getConfigurationSection("per-world");
		for (Object bar : tab.getConfiguration().bossbar.getConfigurationSection("bars").keySet()){
			lines.put(bar+"", BossBarLine.fromConfig(bar+""));
		}
		for (String bar : new ArrayList<String>(defaultBars)) {
			if (lines.get(bar) == null) {
				tab.getErrorManager().startupWarn("BossBar \"&e" + bar + "&c\" is defined as default bar, but does not exist! &bIgnoring.");
				defaultBars.remove(bar);
			}
		}
		for (Entry<String, List<String>> entry : perWorld.entrySet()) {
			List<String> bars = entry.getValue();
			for (String bar : new ArrayList<String>(bars)) {
				if (lines.get(bar) == null) {
					tab.getErrorManager().startupWarn("BossBar \"&e" + bar + "&c\" is defined as per-world bar in world &e" + entry.getKey() + "&c, but does not exist! &bIgnoring.");
					bars.remove(bar);
				}
			}
		}
		remember_toggle_choice = tab.getConfiguration().bossbar.getBoolean("remember-toggle-choice", false);
		if (remember_toggle_choice) {
			bossbar_off_players = tab.getConfiguration().getPlayerData("bossbar-off");
		}
		TAB.getInstance().getPlaceholderManager().allUsedPlaceholderIdentifiers.add("%countdown%");
		TAB.getInstance().getPlaceholderManager().registerPlaceholder(new ServerPlaceholder("%countdown%", 100) {

			@Override
			public String get() {
				return "" + (announceEndTime - System.currentTimeMillis()) / 1000;
			}
		});
	}
	
	@Override
	public void load() {
		for (TabPlayer p : tab.getPlayers()) {
			onJoin(p);
		}
		tab.getCPUManager().startRepeatingMeasuredTask(1000, "refreshing bossbar permissions", getFeatureType(), UsageType.REPEATING_TASK, new Runnable() {
			public void run() {
				for (TabPlayer p : tab.getPlayers()) {
					if (!p.hasBossbarVisible() || isDisabledWorld(disabledWorlds, p.getWorldName())) continue;
					for (BossBarLine bar : p.getActiveBossBars().toArray(new BossBarLine[0])) {
						if (!bar.isConditionMet(p)) {
							bar.remove(p);
							p.getActiveBossBars().remove(bar);
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
		for (TabPlayer p : tab.getPlayers()) {
			for (me.neznamy.tab.api.bossbar.BossBar line : p.getActiveBossBars().toArray(new me.neznamy.tab.api.bossbar.BossBar[0])) {
				p.removeBossBar(line);
			}
		}
		lines.clear();
	}
	
	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		connectedPlayer.setBossbarVisible(!bossbar_off_players.contains(connectedPlayer.getName()) && !hiddenByDefault);
		detectBossBarsAndSend(connectedPlayer);
	}
	
	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		for (me.neznamy.tab.api.bossbar.BossBar line : p.getActiveBossBars().toArray(new me.neznamy.tab.api.bossbar.BossBar[0])) {
			p.removeBossBar(line);
		}
		detectBossBarsAndSend(p);
	}
	
	@Override
	public boolean onCommand(TabPlayer sender, String message) {
		if (message.equalsIgnoreCase(toggleCommand)) {
			tab.command.execute(sender, new String[] {"bossbar"});
			return true;
		}
		return false;
	}
	
	/**
	 * Clears and resends all bossbars to specified player
	 * @param p - player to process
	 */
	public void detectBossBarsAndSend(TabPlayer p) {
		p.getActiveBossBars().clear();
		if (isDisabledWorld(disabledWorlds, p.getWorldName()) || !p.hasBossbarVisible()) return;
		showBossBars(p, defaultBars);
		showBossBars(p, announcements);
		showBossBars(p, perWorld.get(p.getWorldName()));
	}
	
	/**
	 * Shows bossbars to player if display condition is met
	 * @param p - player to show bossbars to
	 * @param bars - list of bossbars to check
	 */
	private void showBossBars(TabPlayer p, List<String> bars) {
		if (bars == null) return;
		for (String defaultBar : bars) {
			BossBarLine bar = lines.get(defaultBar);
			if (bar.isConditionMet(p) && !p.getActiveBossBars().contains(bar)) {
				bar.create(p);
				p.getActiveBossBars().add(bar);
			}
		}
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.BOSSBAR;
	}
	
	/**
	 * Returns line from specified uuid
	 * @param id - uuid of bossbar
	 * @return bossbar with specified uuid
	 */
	public BossBarLine getLine(UUID id) {
		for (BossBarLine line : lines.values()) {
			if (line.uuid == id) return line;
		}
		return null;
	}
}