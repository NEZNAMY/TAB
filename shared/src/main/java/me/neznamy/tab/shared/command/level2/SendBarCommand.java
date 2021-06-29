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
public class SendBarCommand extends SubCommand{

	/**
	 * Constructs new instance
	 */
	public SendBarCommand() {
		super("bar", "tab.send.bar");
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		BossBar feature = (BossBar) TAB.getInstance().getFeatureManager().getFeature("bossbar");
		if (feature == null) {
			sendMessage(sender, "&4This command requires the bossbar feature to be enabled.");
			return;
		}
		if (args.length != 3) {
			sendMessage(sender, "Usage: /tab send bar <player> <bar name> <length>");
			return;
		}
		TabPlayer target = TAB.getInstance().getPlayer(args[0]);
		if (target == null) {
			sendMessage(sender, getTranslation("player_not_found"));
			return;
		}
		String barname = args[1];
		int duration;
		try {
			duration = Integer.parseInt(args[2]);
		} catch (Exception e) {
			sendMessage(sender, args[1] + " is not a number!");
			return;
		}
		BossBarLine bar = feature.getLines().get(barname);
		if (bar == null) {
			sendMessage(sender, "No bar found with name \"" + bar + "\"");
			return;
		}
		new Thread(() -> {
			try {
				if (target.hasBossbarVisible()) {
					bar.create(target);
					target.getActiveBossBars().add(bar);
				}
				Thread.sleep(duration*1000L);
				if (target.hasBossbarVisible()) {
					bar.remove(target);
					target.getActiveBossBars().remove(bar);
				}
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
			return getPlayers(arguments[0]);
		} else if (arguments.length == 2) {
			for (String bar : b.getLines().keySet()) {
				if (bar.toLowerCase().startsWith(arguments[1].toLowerCase())) suggestions.add(bar);
			}
		} else if (arguments.length == 3 && b.getLines().get(arguments[1]) != null){
			for (String time : Arrays.asList("5", "10", "30", "60", "120")) {
				if (time.startsWith(arguments[2])) suggestions.add(time);
			}
		}
		return suggestions;
	}
}