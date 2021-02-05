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

	
	public AnnounceScoreboardCommand() {
		super("scoreboard", "tab.announce.scoreboard");
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		if (TAB.getInstance().getFeatureManager().isFeatureEnabled("scoreboard")) {
			ScoreboardManager feature = (ScoreboardManager) TAB.getInstance().getFeatureManager().getFeature("scoreboard");
			if (args.length == 2) {
				String scoreboard = args[0];
				int duration;
				try {
					duration = Integer.parseInt(args[1]);
					new Thread(new Runnable() {

						public void run() {
							try {
								Scoreboard sb = feature.getScoreboards().get(scoreboard);
								if (sb == null) {
									sender.sendMessage("Scoreboard not found", false);
									return;
								}
								feature.announcement = sb;
								Map<TabPlayer, Scoreboard> previous = new HashMap<TabPlayer, Scoreboard>();
								for (TabPlayer all : TAB.getInstance().getPlayers()) {
									if (all.isScoreboardVisible()) {
										previous.put(all, all.getActiveScoreboard());
										if (all.getActiveScoreboard() != null) all.getActiveScoreboard().unregister(all);
										sb.register(all);
									}
								}
								Thread.sleep(duration*1000);
								for (TabPlayer all : TAB.getInstance().getPlayers()) {
									if (all.hasBossbarVisible()) {
										sb.unregister(all);
										if (previous.get(all) != null) previous.get(all).register(all);
									}
								}
								feature.announcement = null;
							} catch (Exception e) {

							}
						}
					}).start();
				} catch (Exception e) {
					sender.sendMessage(args[1] + " is not a number!", false);
				}
			} else {
				sendMessage(sender, "Usage: /tab announce scoreboard <scoreboard name> <length>");
			}
		} else {
			sendMessage(sender, "&4This command requires the scoreboard feature to be enabled.");
		}
	}

	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		ScoreboardManager s = (ScoreboardManager) TAB.getInstance().getFeatureManager().getFeature("scoreboard");
		if (s == null) return new ArrayList<String>();
		List<String> suggestions = new ArrayList<String>();
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