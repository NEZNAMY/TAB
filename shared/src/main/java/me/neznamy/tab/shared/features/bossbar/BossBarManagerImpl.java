package me.neznamy.tab.shared.features.bossbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.shared.TAB;

/**
 * Class for handling bossbar feature
 */
public class BossBarManagerImpl extends TabFeature implements BossBarManager {

	//default bossbars
	private List<String> defaultBars = new ArrayList<>();

	//registered bossbars
	private Map<String, BossBar> lines = new HashMap<>();
	private BossBar[] lineValues;

	//toggle command
	private String toggleCommand;
	
	private String toggleOnMessage;
	private String toggleOffMessage;

	//list of currently running bossbar announcements
	private List<BossBar> announcements = new ArrayList<>();

	//saving toggle choice into file
	private boolean rememberToggleChoice;

	//players with toggled bossbar
	private List<String> bossbarOffPlayers = new ArrayList<>();

	//time when bossbar announce ends, used for placeholder
	private long announceEndTime;

	//if bossbar is hidden by default until toggle command is used
	private boolean hiddenByDefault;

	private List<TabPlayer> visiblePlayers = new ArrayList<>();

	/**
	 * Constructs new instance and loads configuration
	 * @param tab - tab instance
	 */
	public BossBarManagerImpl() {
		super("BossBar", TAB.getInstance().getConfiguration().getConfig().getStringList("bossbar.disable-in-servers"),
				TAB.getInstance().getConfiguration().getConfig().getStringList("bossbar.disable-in-worlds"));
		toggleCommand = TAB.getInstance().getConfiguration().getConfig().getString("bossbar.toggle-command", "/bossbar");
		hiddenByDefault = TAB.getInstance().getConfiguration().getConfig().getBoolean("bossbar.hidden-by-default", false);
		toggleOnMessage = TAB.getInstance().getConfiguration().getMessages().getBossBarOn();
		toggleOffMessage = TAB.getInstance().getConfiguration().getMessages().getBossBarOff();
		for (Object bar : TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("bossbar.bars").keySet()){
			BossBarLine line = loadFromConfig(bar.toString());
			lines.put(bar.toString(), line);
			if (!line.isAnnouncementOnly()) defaultBars.add(bar.toString());
		}
		lineValues = lines.values().toArray(new BossBar[0]);
		rememberToggleChoice = TAB.getInstance().getConfiguration().getConfig().getBoolean("bossbar.remember-toggle-choice", false);
		if (rememberToggleChoice) {
			bossbarOffPlayers = TAB.getInstance().getConfiguration().getPlayerDataFile().getStringList("bossbar-off", new ArrayList<>());
		}
		TAB.getInstance().getPlaceholderManager().registerServerPlaceholder("%countdown%", 100, () -> (announceEndTime - System.currentTimeMillis()) / 1000);
		TAB.getInstance().debug(String.format("Loaded Bossbar feature with parameters disabledWorlds=%s, disabledServers=%s, toggleCommand=%s, defaultBars=%s, hiddenByDefault=%s, remember_toggle_choice=%s",
				Arrays.toString(disabledWorlds), Arrays.toString(disabledServers), toggleCommand, defaultBars, hiddenByDefault, rememberToggleChoice));
	}
	
	/**
	 * Loads bossbar from config by it's name
	 * @param bar - name of bossbar in config
	 * @return loaded bossbar
	 */
	private BossBarLine loadFromConfig(String bar) {
		Map<String, Object> bossbar = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("bossbar.bars." + bar);
		String condition = (String) bossbar.get("display-condition");
		String style = (String) bossbar.get("style");
		String color = (String) bossbar.get("color");
		String progress = String.valueOf(bossbar.get("progress"));
		String text = (String) bossbar.get("text");
		if (style == null) {
			TAB.getInstance().getErrorManager().missingAttribute(getFeatureName(), bar, "style");
			style = "PROGRESS";
		}
		if (color == null) {
			TAB.getInstance().getErrorManager().missingAttribute(getFeatureName(), bar, "color");
			color = "WHITE";
		}
		if (progress == null) {
			progress = "100";
			TAB.getInstance().getErrorManager().missingAttribute(getFeatureName(), bar, "progress");
		}
		if (text == null) {
			text = "";
			TAB.getInstance().getErrorManager().missingAttribute(getFeatureName(), bar, "text");
		}
		return new BossBarLine(this, bar, condition, color, style, text, progress, (boolean) bossbar.getOrDefault("announcement-bar", false));
	}

	@Override
	public void load() {
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
			onJoin(p);
		}
	}
	
	@Override
	public void refresh(TabPlayer p, boolean force) {
		if (!p.isLoaded() || !hasBossBarVisible(p) || isDisabledPlayer(p)) return;
		for (BossBar line : lineValues) {
			if (line.getPlayers().contains(p) && !((BossBarLine) line).isConditionMet(p)) {
				line.removePlayer(p);
			}
		}
		showBossBars(p, defaultBars);
	}

	@Override
	public void unload() {
		for (BossBar line : lineValues) {
			for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
				line.removePlayer(p);
			}
		}
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		if (isDisabled(connectedPlayer.getServer(), connectedPlayer.getWorld())) {
			addDisabledPlayer(connectedPlayer);
			return;
		}
		setBossBarVisible(connectedPlayer, !bossbarOffPlayers.contains(connectedPlayer.getName()) && !hiddenByDefault, false);
	}

	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		if (isDisabled(p.getServer(), p.getWorld())) {
			addDisabledPlayer(p);
		} else {
			removeDisabledPlayer(p);
		}
		for (BossBar line : lineValues) {
			line.removePlayer(p);
		}
		detectBossBarsAndSend(p);
	}

	@Override
	public boolean onCommand(TabPlayer sender, String message) {
		if (message.equalsIgnoreCase(toggleCommand)) {
			TAB.getInstance().getCommand().execute(sender, new String[] {"bossbar"});
			return true;
		}
		return false;
	}

	/**
	 * Clears and resends all bossbars to specified player
	 * @param p - player to process
	 */
	protected void detectBossBarsAndSend(TabPlayer p) {
		if (isDisabledPlayer(p) || !hasBossBarVisible(p)) return;
		showBossBars(p, defaultBars);
		showBossBars(p, announcements.stream().map(BossBar::getName).collect(Collectors.toList()));
	}

	/**
	 * Shows bossbars to player if display condition is met
	 * @param p - player to show bossbars to
	 * @param bars - list of bossbars to check
	 */
	private void showBossBars(TabPlayer p, List<String> bars) {
		if (bars == null) return;
		for (String defaultBar : bars) {
			BossBarLine bar = (BossBarLine) lines.get(defaultBar);
			if (bar.isConditionMet(p) && !bar.getPlayers().contains(p)) {
				bar.addPlayer(p);
			}
		}
	}

	@Override
	public void onQuit(TabPlayer disconnectedPlayer) {
		super.onQuit(disconnectedPlayer);
		visiblePlayers.remove(disconnectedPlayer);
		for (BossBar line : lineValues) {
			line.removePlayer(disconnectedPlayer);
		}
	}

	@Override
	public BossBar createBossBar(String title, float progress, BarColor color, BarStyle style) {
		return createBossBar(title, String.valueOf(progress), color.toString(), style.toString());
	}

	@Override
	public BossBar createBossBar(String title, String progress, String color, String style) {
		UUID id = UUID.randomUUID();
		BossBar bar = new BossBarLine(this, id.toString(), null, color, style, title, progress, true);
		lines.put(id.toString(), bar);
		lineValues = lines.values().toArray(new BossBar[0]);
		return bar;
	}

	@Override
	public BossBar getBossBar(String name) {
		return lines.get(name);
	}

	@Override
	public BossBar getBossBar(UUID id) {
		for (BossBar line : lineValues) {
			if (line.getUniqueId() == id) return line;
		}
		return null;
	}

	@Override
	public void toggleBossBar(TabPlayer player, boolean sendToggleMessage) {
		setBossBarVisible(player, !hasBossBarVisible(player), sendToggleMessage);
	}

	@Override
	public Map<String, BossBar> getRegisteredBossBars() {
		return lines;
	}

	@Override
	public boolean hasBossBarVisible(TabPlayer player) {
		return visiblePlayers.contains(player);
	}

	@Override
	public void setBossBarVisible(TabPlayer player, boolean visible, boolean sendToggleMessage) {
		if (visiblePlayers.contains(player) == visible) return;
		if (visible) {
			visiblePlayers.add(player);
			detectBossBarsAndSend(player);
			if (sendToggleMessage) player.sendMessage(toggleOnMessage, true);
			if (rememberToggleChoice) {
				bossbarOffPlayers.remove(player.getName());
				TAB.getInstance().getConfiguration().getPlayerDataFile().set("bossbar-off", bossbarOffPlayers);
			}
		} else {
			visiblePlayers.remove(player);
			for (BossBar l : lineValues) {
				l.removePlayer(player);
			}
			if (sendToggleMessage) player.sendMessage(toggleOffMessage, true);
			if (rememberToggleChoice && !bossbarOffPlayers.contains(player.getName())) {
				bossbarOffPlayers.add(player.getName());
				TAB.getInstance().getConfiguration().getPlayerDataFile().set("bossbar-off", bossbarOffPlayers);
			}
		}
	}

	@Override
	public void sendBossBarTemporarily(TabPlayer player, String bossbar, int duration) {
		if (!hasBossBarVisible(player)) return;
		BossBar line = lines.get(bossbar);
		if (line == null) throw new IllegalArgumentException("No registered bossbar found with name " + bossbar);
		new Thread(() -> {
			try {
				line.addPlayer(player);
				Thread.sleep(duration*1000L);
				line.removePlayer(player);
			} catch (InterruptedException pluginDisabled) {
				Thread.currentThread().interrupt();
			}
		}).start();
	}

	@Override
	public void announceBossBar(String bossbar, int duration) {
		BossBar line = lines.get(bossbar);
		if (line == null) throw new IllegalArgumentException("No registered bossbar found with name " + bossbar);
		new Thread(() -> {
			try {
				announcements.add(line);
				announceEndTime = (System.currentTimeMillis() + duration*1000);
				for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
					if (!hasBossBarVisible(all)) continue;
					if (((BossBarLine)line).isConditionMet(all)) line.addPlayer(all);
				}
				Thread.sleep(duration*1000L);
				for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
					if (!hasBossBarVisible(all)) continue;
					line.removePlayer(all);
				}
				announcements.remove(line);
			} catch (InterruptedException pluginDisabled) {
				Thread.currentThread().interrupt();
			}
		}).start();
	}

	@Override
	public List<BossBar> getAnnouncedBossBars() {
		return announcements;
	}
}