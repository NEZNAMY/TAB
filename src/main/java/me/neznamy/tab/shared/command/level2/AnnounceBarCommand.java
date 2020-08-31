package me.neznamy.tab.shared.command.level2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.features.bossbar.BossBar;
import me.neznamy.tab.shared.features.bossbar.BossBarLine;

/**
 * Handler for "/tab announce bar" subcommand
 */
public class AnnounceBarCommand extends SubCommand{

	
	public AnnounceBarCommand() {
		super("bar", "tab.announce.bar");
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		if (Shared.featureManager.isFeatureEnabled("bossbar")) {
			BossBar feature = (BossBar) Shared.featureManager.getFeature("bossbar");
			if (args.length == 2) {
				String barname = args[0];
				int duration;
				try {
					duration = Integer.parseInt(args[1]);
					Shared.cpu.runTask("announcing bossbar", new Runnable() {

						public void run() {
							try {
								BossBarLine bar = feature.lines.get(barname);
								if (bar == null) {
									sender.sendMessage("Bar not found");
									return;
								}
								feature.announcements.add(barname);
								for (ITabPlayer all : Shared.getPlayers()) {
									bar.create(all);
									all.activeBossBars.add(bar);
								}
								Thread.sleep(duration*1000);
								for (ITabPlayer all : Shared.getPlayers()) {
									bar.remove(all);
									all.activeBossBars.remove(bar);
								}
								feature.announcements.remove(barname);
							} catch (Exception e) {

							}
						}
					});
				} catch (Exception e) {
					sender.sendMessage(args[3] + " is not a number!");
				}
			} else {
				sendMessage(sender, "Usage: /tab announce bar <bar name> <length>");
			}
		} else {
			sendMessage(sender, "&4This command requires the bossbar feature to be enabled.");
		}
	}

	@Override
	public List<String> complete(ITabPlayer sender, String[] arguments) {
		BossBar b = (BossBar) Shared.featureManager.getFeature("bossbar");
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