package me.neznamy.tab.shared.command.level1;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;

/**
 * Handler for "/tab bossbar" subcommand
 */
public class BossBarCommand extends SubCommand {

	/**
	 * Constructs new instance
	 */
	public BossBarCommand() {
		super("bossbar", null);
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		BossBarManagerImpl bossbar = (BossBarManagerImpl) TAB.getInstance().getFeatureManager().getFeature("bossbar");
		if (bossbar == null) {
			sendMessage(sender, "&cBossbar feature is not enabled, therefore toggle command cannot be used.");
			return;
		}
		if (sender == null) {
			sendMessage(sender, "&cThis command must be ran from the game");
			return;
		}
		if (!bossbar.requiresPermToToggle() || sender.hasPermission("tab.togglebar")) {
			bossbar.toggleBossBar(sender, true);
		} else {
			sender.sendMessage(getTranslation("no_permission"), true);
		}
	}
}