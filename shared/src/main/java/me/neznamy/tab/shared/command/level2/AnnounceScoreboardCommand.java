package me.neznamy.tab.shared.command.level2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.api.Scoreboard;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManager;

/**
 * Handler for "/tab announce scoreboard" subcommand
 */
public class AnnounceScoreboardCommand extends SubCommand{

	/**
	 * Constructs new instance
	 */
	public AnnounceScoreboardCommand() {
		super("scoreboard", "tab.announce.scoreboard");
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		ScoreboardManager feature = (ScoreboardManager) TAB.getInstance().getFeatureManager().getFeature("scoreboard");
		if (feature == null) {
			sendMessage(sender, "&4This command requires the scoreboard feature to be enabled.");
			return;
		}
		if (args.length != 2) {
			sendMessage(sender, "Usage: /tab announce scoreboard <scoreboard name> <length>");
			return;
		}
		String scoreboard = args[0];
		int duration;
		try {
			duration = Integer.parseInt(args[1]);
		} catch (Exception e) {
			sendMessage(sender, args[1] + " is not a number!");
			return;
		}
		Scoreboard sb = feature.getScoreboards().get(scoreboard);
		if (sb == null) {
			sendMessage(sender, "Scoreboard not found");
			return;
		}
		announce(feature, sb, duration);
	}
	
	/**
	 * Performs accounce
	 * @param feature - scoreboard feature
	 * @param sb - scoreboard to announce
	 * @param duration - duration in seconds
	 */
	private void announce(ScoreboardManager feature, Scoreboard sb, int duration) {
		new Thread(() -> {
			try {
				feature.setAnnouncement(sb);
				Map<TabPlayer, Scoreboard> previous = new HashMap<>();
				for (TabPlayer all : TAB.getInstance().getPlayers()) {
					if (!all.isScoreboardVisible()) continue;
					previous.put(all, all.getActiveScoreboard());
					if (all.getActiveScoreboard() != null) all.getActiveScoreboard().unregister(all);
					sb.register(all);
				}
				Thread.sleep(duration*1000L);
				for (TabPlayer all : TAB.getInstance().getPlayers()) {
					if (!all.hasBossbarVisible()) continue;
					sb.unregister(all);
					if (previous.get(all) != null) previous.get(all).register(all);
				}
				feature.setAnnouncement(null);
			} catch (InterruptedException pluginDisabled) {
				Thread.currentThread().interrupt();
			}
		}).start();
	}

	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		ScoreboardManager s = (ScoreboardManager) TAB.getInstance().getFeatureManager().getFeature("scoreboard");
		if (s == null) return new ArrayList<>();
		List<String> suggestions = new ArrayList<>();
		if (arguments.length == 1) {
			for (String bar : s.getScoreboards().keySet()) {
				if (bar.toLowerCase().startsWith(arguments[0].toLowerCase())) suggestions.add(bar);
			}
		} else if (arguments.length == 2 && s.getScoreboards().get(arguments[0]) != null){
			for (String time : Arrays.asList("5", "10", "30", "60", "120")) {
				if (time.startsWith(arguments[1])) suggestions.add(time);
			}
		}
		return suggestions;
	}
}