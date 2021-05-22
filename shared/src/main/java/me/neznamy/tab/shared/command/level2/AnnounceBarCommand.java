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
		BossBarLine bar = feature.lines.get(barname);
		if (bar == null) {
			sendMessage(sender, "Bar not found");
			return;
		}
		new Thread(() -> {
			try {
				feature.announcements.add(barname);
				feature.announceEndTime = System.currentTimeMillis() + duration*1000;
				for (TabPlayer all : TAB.getInstance().getPlayers()) {
					if (!all.hasBossbarVisible()) continue;
					bar.create(all);
					all.getActiveBossBars().add(bar);
				}
				Thread.sleep(duration*1000);
				for (TabPlayer all : TAB.getInstance().getPlayers()) {
					if (!all.hasBossbarVisible()) continue;
					bar.remove(all);
					all.getActiveBossBars().remove(bar);
				}
				feature.announcements.remove(barname);
			} catch (Exception e) {

			}
		}).start();
	}

	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		BossBar b = (BossBar) TAB.getInstance().getFeatureManager().getFeature("bossbar");
		if (b == null) return new ArrayList<String>();
		List<String> suggestions = new ArrayList<String>();
		if (arguments.length == 1) {
			for (String bar : b.lines.keySet()) {
				if (bar.toLowerCase().startsWith(arguments[0].toLowerCase())) suggestions.add(bar);
			}
		} else if (arguments.length == 2 && b.lines.get(arguments[0]) != null){
			for (String time : Arrays.asList("5", "10", "30", "60", "120")) {
				if (time.startsWith(arguments[1])) suggestions.add(time);
			}
		}
		return suggestions;
	}
}