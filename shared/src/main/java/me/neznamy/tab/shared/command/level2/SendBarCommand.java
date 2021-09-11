package me.neznamy.tab.shared.command.level2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.SubCommand;

/**
 * Handler for "/tab announce bar" subcommand
 */
public class SendBarCommand extends SubCommand {

	/**
	 * Constructs new instance
	 */
	public SendBarCommand() {
		super("bar", "tab.send.bar");
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		BossBarManager feature = (BossBarManager) TAB.getInstance().getFeatureManager().getFeature("bossbar");
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
		} catch (NumberFormatException e) {
			sendMessage(sender, args[1] + " is not a number!");
			return;
		}
		BossBar bar = feature.getBossBar(barname);
		if (bar == null) {
			sendMessage(sender, "No bar found with name \"" + bar + "\"");
			return;
		}
		feature.sendBossBarTemporarily(target, bar.getName(), duration);
	}

	@Override
	public List<String> complete(TabPlayer sender, String[] arguments) {
		BossBarManager b = (BossBarManager) TAB.getInstance().getFeatureManager().getFeature("bossbar");
		if (b == null) return new ArrayList<>();
		if (arguments.length == 1) return getOnlinePlayers(arguments[0]);
		if (arguments.length == 2) return getStartingArgument(b.getRegisteredBossBars().keySet(), arguments[1]);
		if (arguments.length == 3 && b.getBossBar(arguments[1]) != null) return getStartingArgument(Arrays.asList("5", "10", "30", "60", "120"), arguments[2]);
		return new ArrayList<>();
	}
}