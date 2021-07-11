package me.neznamy.tab.shared.command.level2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.features.bossbar.BossBar;
import me.neznamy.tab.shared.features.bossbar.BossBarLine;

/**
 * Handler for "/tab announce bar" subcommand
 */
public class AnnounceBarCommand extends SubCommand{

	/**
	 * Constructs new instance
	 */
	public AnnounceBarCommand() {
		super("bar", "tab.announce.bar");
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		BossBar feature = (BossBar) TAB.getInstance().getFeatureManager().getFeature("bossbar");
		if (feature == null) {
			sendMessage(sender, "&4This command requires the bossbar feature to be enabled.");
			return;
		}
		if (args.length != 2) {
			sendMessage(sender, "Usage: /tab announce bar <bar name> <length>");
			return;
		}
		String barname = args[0];
		int duration;
		try {
			duration = Integer.parseInt(args[1]);
		} catch (Exception e) {
			sendMessage(sender, args[1] + " is not a number!");
			return;
		}
		BossBarLine bar = feature.getLines().get(barname);
		if (bar == null) {
			sendMessage(sender, "Bar not found");
			return;
		}
		if (feature.getAnnouncements().contains(barname)) {
			sendMessage(sender, "This bossbar is already being announced");
			return;
		}
		new Thread(() -> {
			try {
				feature.getAnnouncements().add(barname);
				feature.setAnnounceEndTime(System.currentTimeMillis() + duration*1000);
				for (TabPlayer all : TAB.getInstance().getPlayers()) {
					if (!all.hasBossbarVisible()) continue;
					bar.create(all);
					all.getActiveBossBars().add(bar);
				}
				Thread.sleep(duration*1000L);
				for (TabPlayer all : TAB.getInstance().getPlayers()) {
					if (!all.hasBossbarVisible()) continue;
					bar.remove(all);
					all.getActiveBossBars().remove(bar);
				}
				feature.getAnnouncements().remove(barname);
			} catch (InterruptedException pluginDisabled) {
				Thread.currentThread().interrupt();
			}
		}).start();
	}

	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		BossBar b = (BossBar) TAB.getInstance().getFeatureManager().getFeature("bossbar");
		if (b == null) return new ArrayList<>();
		List<String> suggestions = new ArrayList<>();
		if (arguments.length == 1) {
			for (String bar : b.getLines().keySet()) {
				if (bar.toLowerCase().startsWith(arguments[0].toLowerCase())) suggestions.add(bar);
			}
		} else if (arguments.length == 2 && b.getLines().get(arguments[0]) != null){
			for (String time : Arrays.asList("5", "10", "30", "60", "120")) {
				if (time.startsWith(arguments[1])) suggestions.add(time);
			}
		}
		return suggestions;
	}
}