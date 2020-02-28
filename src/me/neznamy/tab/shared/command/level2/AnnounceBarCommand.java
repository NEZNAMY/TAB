package me.neznamy.tab.shared.command.level2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.features.BossBar;
import me.neznamy.tab.shared.features.BossBar.BossBarLine;

public class AnnounceBarCommand extends SubCommand{

	public AnnounceBarCommand() {
		super("bar", "tab.announce.bar");
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		if (Shared.features.containsKey("bossbar")) {
			BossBar feature = (BossBar) Shared.features.get("bossbar");
			if (args.length == 2) {
				String barname = args[0];
				int duration;
				try {
					duration = Integer.parseInt(args[1]);
					int d2 = duration;
					Shared.cpu.runMeasuredTask("announcing bossbar", "BossBar", new Runnable() {

						public void run() {
							try {
								BossBarLine bar = feature.getLine(barname);
								if (bar == null) {
									sender.sendMessage("Bar not found");
									return;
								}
								feature.announcements.add(barname);
								for (ITabPlayer all : Shared.getPlayers()) {
									PacketAPI.createBossBar(all, bar);
								}
//								List<String> animationFrames = //maybe later
								for (int i=0; i<(float)d2*1000/feature.getRefresh(); i++) {
									Thread.sleep(feature.getRefresh());
								}
								for (ITabPlayer all : Shared.getPlayers()) {
									PacketAPI.removeBossBar(all, bar);
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
		BossBar b = (BossBar) Shared.features.get("bossbar");
		if (b == null) return new ArrayList<String>();
		List<String> suggestions = new ArrayList<String>();
		if (arguments.length == 1) {
			for (BossBarLine bar : b.lines) {
				if (bar.getName().startsWith(arguments[0])) suggestions.add(bar.getName());
			}
		} else if (arguments.length == 2 && b.getLine(arguments[0]) != null){
			for (String time : Arrays.asList("5", "10", "30", "60", "120")) {
				if (time.startsWith(arguments[1])) suggestions.add(time);
			}
		}
		return suggestions;
	}
}