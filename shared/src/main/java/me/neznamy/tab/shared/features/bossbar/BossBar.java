package me.neznamy.tab.shared.features.bossbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.event.CommandListener;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.features.types.event.QuitEventListener;
import me.neznamy.tab.shared.features.types.event.WorldChangeListener;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;

/**
 * Class for handling bossbar feature
 */
public class BossBar implements Loadable, JoinEventListener, QuitEventListener, WorldChangeListener, CommandListener {

	//tab instance
	private TAB tab;
	
	//default bossbars
	private List<String> defaultBars;
	
	//per-world / per-server bossbars
	private Map<String, List<String>> perWorld;
	
	//registered bossbars
	private Map<String, BossBarLine> lines = new HashMap<>();
	
	//toggle command
	private String toggleCommand;
	
	//list of currently running bossbar announcements
	private List<String> announcements = new ArrayList<>();
	
	//saving toggle choice into file
	private boolean rememberToggleChoice;
	
	//players with toggled bossbar
	private List<String> bossbarOffPlayers = new ArrayList<>();
	
	//if permission is required to toggle
	private boolean permToToggle;
	
	//list of worlds / servers where bossbar feature is disabled entirely
	private List<String> disabledWorlds;
	
	//time when bossbar announce ends, used for placeholder
	private long announceEndTime;
	
	//if bossbar is hidden by default until toggle command is used
	private boolean hiddenByDefault;
	
	private Set<TabPlayer> playersInDisabledWorlds = new HashSet<>();

	/**
	 * Constructs new instance and loads configuration
	 * @param tab - tab instance
	 */
	public BossBar(TAB tab) {
		this.tab = tab;
		disabledWorlds = tab.getConfiguration().getConfig().getStringList("disable-features-in-"+tab.getPlatform().getSeparatorType()+"s.bossbar", Arrays.asList("disabled" + tab.getPlatform().getSeparatorType()));
		toggleCommand = tab.getConfiguration().getBossbarConfig().getString("bossbar-toggle-command", "/bossbar");
		defaultBars = tab.getConfiguration().getBossbarConfig().getStringList("default-bars", new ArrayList<>());
		permToToggle = tab.getConfiguration().getBossbarConfig().getBoolean("permission-required-to-toggle", false);
		hiddenByDefault = tab.getConfiguration().getBossbarConfig().getBoolean("hidden-by-default", false);
		perWorld = tab.getConfiguration().getBossbarConfig().getConfigurationSection("per-world");
		for (Object bar : tab.getConfiguration().getBossbarConfig().getConfigurationSection("bars").keySet()){
			getLines().put(bar.toString(), BossBarLine.fromConfig(bar.toString()));
		}
		for (String bar : new ArrayList<>(defaultBars)) {
			if (getLines().get(bar) == null) {
				tab.getErrorManager().startupWarn("BossBar \"&e" + bar + "&c\" is defined as default bar, but does not exist! &bIgnoring.");
				defaultBars.remove(bar);
			}
		}
		for (Entry<String, List<String>> entry : perWorld.entrySet()) {
			List<String> bars = entry.getValue();
			for (String bar : new ArrayList<>(bars)) {
				if (getLines().get(bar) == null) {
					tab.getErrorManager().startupWarn("BossBar \"&e" + bar + "&c\" is defined as per-world bar in world &e" + entry.getKey() + "&c, but does not exist! &bIgnoring.");
					bars.remove(bar);
				}
			}
		}
		rememberToggleChoice = tab.getConfiguration().getBossbarConfig().getBoolean("remember-toggle-choice", false);
		if (isRememberToggleChoice()) {
			bossbarOffPlayers = tab.getConfiguration().getPlayerData("bossbar-off");
		}
		TAB.getInstance().getPlaceholderManager().getAllUsedPlaceholderIdentifiers().add("%countdown%");
		TAB.getInstance().getPlaceholderManager().registerPlaceholder(new ServerPlaceholder("%countdown%", 100) {

			@Override
			public String get() {
				return String.valueOf((getAnnounceEndTime() - System.currentTimeMillis()) / 1000);
			}
		});
		tab.debug(String.format("Loaded Bossbar feature with parameters disabledWorlds=%s, toggleCommand=%s, defaultBars=%s, permToToggle=%s, hiddenByDefault=%s, perWorld=%s, remember_toggle_choice=%s",
				disabledWorlds, toggleCommand, defaultBars, isPermToToggle(), hiddenByDefault, perWorld, isRememberToggleChoice()));
	}
	
	@Override
	public void load() {
		for (TabPlayer p : tab.getPlayers()) {
			onJoin(p);
		}
		tab.getCPUManager().startRepeatingMeasuredTask(1000, "refreshing bossbar permissions", getFeatureType(), UsageType.REPEATING_TASK, () -> {

			for (TabPlayer p : tab.getPlayers()) {
				if (!p.isLoaded() || !p.hasBossbarVisible() || playersInDisabledWorlds.contains(p)) continue;
				for (BossBarLine bar : p.getActiveBossBars().toArray(new BossBarLine[0])) {
					if (!bar.isConditionMet(p)) {
						bar.remove(p);
						p.getActiveBossBars().remove(bar);
					}
				}
				showBossBars(p, defaultBars);
				showBossBars(p, perWorld.get(tab.getConfiguration().getWorldGroupOf(perWorld.keySet(), p.getWorldName())));
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
		getLines().clear();
	}
	
	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		if (isDisabledWorld(disabledWorlds, connectedPlayer.getWorldName())) {
			playersInDisabledWorlds.add(connectedPlayer);
			return;
		}
		connectedPlayer.setBossbarVisible(!getBossbarOffPlayers().contains(connectedPlayer.getName()) && !hiddenByDefault, false);
	}
	
	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		if (isDisabledWorld(disabledWorlds, p.getWorldName())) {
			playersInDisabledWorlds.add(p);
		} else {
			playersInDisabledWorlds.remove(p);
		}
		for (me.neznamy.tab.api.bossbar.BossBar line : p.getActiveBossBars().toArray(new me.neznamy.tab.api.bossbar.BossBar[0])) {
			p.removeBossBar(line);
		}
		detectBossBarsAndSend(p);
	}
	
	@Override
	public boolean onCommand(TabPlayer sender, String message) {
		if (message.equalsIgnoreCase(toggleCommand)) {
			tab.getCommand().execute(sender, new String[] {"bossbar"});
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
		if (playersInDisabledWorlds.contains(p) || !p.hasBossbarVisible()) return;
		showBossBars(p, defaultBars);
		showBossBars(p, getAnnouncements());
		showBossBars(p, perWorld.get(tab.getConfiguration().getWorldGroupOf(perWorld.keySet(), p.getWorldName())));
	}
	
	/**
	 * Shows bossbars to player if display condition is met
	 * @param p - player to show bossbars to
	 * @param bars - list of bossbars to check
	 */
	private void showBossBars(TabPlayer p, List<String> bars) {
		if (bars == null) return;
		for (String defaultBar : bars) {
			BossBarLine bar = getLines().get(defaultBar);
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
		for (BossBarLine line : getLines().values()) {
			if (line.getUuid() == id) return line;
		}
		return null;
	}

	public List<String> getBossbarOffPlayers() {
		return bossbarOffPlayers;
	}

	public boolean isRememberToggleChoice() {
		return rememberToggleChoice;
	}

	public Map<String, BossBarLine> getLines() {
		return lines;
	}

	public List<String> getAnnouncements() {
		return announcements;
	}

	public long getAnnounceEndTime() {
		return announceEndTime;
	}

	public void setAnnounceEndTime(long announceEndTime) {
		this.announceEndTime = announceEndTime;
	}

	public boolean isPermToToggle() {
		return permToToggle;
	}

	@Override
	public void onQuit(TabPlayer disconnectedPlayer) {
		playersInDisabledWorlds.remove(disconnectedPlayer);
	}
}