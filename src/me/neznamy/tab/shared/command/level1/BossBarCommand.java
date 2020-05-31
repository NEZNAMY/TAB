package me.neznamy.tab.shared.command.level1;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.features.BossBar;
import me.neznamy.tab.shared.features.BossBar.BossBarLine;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class BossBarCommand extends SubCommand{

	public BossBarCommand() {
		super("bossbar", null);
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		BossBar bossbar = (BossBar) Shared.features.get("bossbar");
		if (bossbar == null) {
			sender.sendMessage(Placeholders.color("&cBossbar feature is not enabled, therefore toggle command cannot be used."));
			return;
		}
		if (!bossbar.permToToggle || sender.hasPermission("tab.togglebar")) {
			sender.bossbarVisible = !sender.bossbarVisible;
			if (sender.bossbarVisible) {
				sender.detectBossBarsAndSend();
				sender.sendMessage(Configs.bossbar_on);
				if (bossbar.remember_toggle_choice) {
					bossbar.bossbar_off_players.remove(sender.getName());
					Configs.playerdata.set("bossbar-off", bossbar.bossbar_off_players);
				}
			} else {
				for (BossBarLine line : sender.getActiveBossBars()) {
					PacketAPI.removeBossBar(sender, line);
				}
				sender.getActiveBossBars().clear();
				sender.sendMessage(Configs.bossbar_off);
				if (bossbar.remember_toggle_choice && !bossbar.bossbar_off_players.contains(sender.getName())) {
					bossbar.bossbar_off_players.add(sender.getName());
					Configs.playerdata.set("bossbar-off", bossbar.bossbar_off_players);
				}
			}
		} else {
			sender.sendMessage(Configs.no_perm);
		}
	}
	@Override
	public List<String> complete(ITabPlayer sender, String[] arguments) {
		return new ArrayList<String>();
	}
}