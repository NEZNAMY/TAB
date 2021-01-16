package me.neznamy.tab.shared.command.level1;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.features.bossbar.BossBar;

/**
 * Handler for "/tab bossbar" subcommand
 */
public class BossBarCommand extends SubCommand{

	public BossBarCommand() {
		super("bossbar", null);
	}

	@Override
	public void execute(TabPlayer sender, String[] args) {
		BossBar bossbar = (BossBar) TAB.getInstance().getFeatureManager().getFeature("bossbar");
		if (bossbar == null) {
			sender.sendMessage("&cBossbar feature is not enabled, therefore toggle command cannot be used.", true);
			return;
		}
		if (!bossbar.permToToggle || sender.hasPermission("tab.togglebar")) {
			sender.setBossbarVisible(!sender.hasBossbarVisible());
			if (sender.hasBossbarVisible()) {
				if (sender != null) bossbar.detectBossBarsAndSend(sender);
				sender.sendMessage(getTranslation("bossbar-toggle-on"), true);
				if (bossbar.remember_toggle_choice) {
					bossbar.bossbar_off_players.remove(sender.getName());
					TAB.getInstance().getConfiguration().playerdata.set("bossbar-off", bossbar.bossbar_off_players);
				}
			} else {
				for (me.neznamy.tab.api.bossbar.BossBar line : sender.getActiveBossBars().toArray(new me.neznamy.tab.api.bossbar.BossBar[0])) {
					sender.removeBossBar(line);
				}
				sender.getActiveBossBars().clear();
				sender.sendMessage(getTranslation("bossbar-toggle-off"), true);
				if (bossbar.remember_toggle_choice && !bossbar.bossbar_off_players.contains(sender.getName())) {
					bossbar.bossbar_off_players.add(sender.getName());
					TAB.getInstance().getConfiguration().playerdata.set("bossbar-off", bossbar.bossbar_off_players);
				}
			}
		} else {
			sender.sendMessage(getTranslation("no_permission"), true);
		}
	}
}