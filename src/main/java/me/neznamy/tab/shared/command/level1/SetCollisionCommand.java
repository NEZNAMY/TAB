package me.neznamy.tab.shared.command.level1;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.SubCommand;

public class SetCollisionCommand extends SubCommand {

	/**
	 * Constructs new instance
	 */
	public SetCollisionCommand() {
		super("setcollision", "tab.setcollision");
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		if (args.length == 2) {
			TabPlayer target = TAB.getInstance().getPlayer(args[0]);
			if (target == null) {
				sendMessage(sender, getTranslation("player_not_found"));
				return;
			}
			try {
				boolean value = Boolean.parseBoolean(args[1]);
				target.setCollisionRule(value);
				TAB.getInstance().getFeatureManager().getNameTagFeature().updateTeamData(target);
			} catch (Exception e) {
				sendMessage(sender, "&c\"" + args[1] + "\" is not a valid true/false value");
			}
		} else {
			sendMessage(sender, "&cUsage: /tab setcollision <player> <true/false>");
		}
	}
}