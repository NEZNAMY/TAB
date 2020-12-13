package me.neznamy.tab.shared.command;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;

public class DisabledCommand {

	public List<String> execute(String[] args, boolean hasReloadPermission, boolean hasAdminPermission) {
		List<String> messages = new ArrayList<String>();
		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			if (hasReloadPermission) {
				Shared.unload();
				Shared.load();
				if (Shared.disabled) {
					messages.add(Configs.reloadFailed.replace("%file%", Shared.brokenFile));
				} else {
					messages.add(Configs.reloaded);
				}
			} else {
				messages.add(Configs.no_perm);
			}
		} else {
			if (hasAdminPermission) {
				String command = Shared.platform.getSeparatorType().equals("world") ? "/tab" : "/btab";
				messages.add("&m                                                                                ");
				messages.add(" &cPlugin is disabled due to a broken configuration file (" + Shared.brokenFile + "). Check console for more details.");
				messages.add(" &8>> &3&l" + command + " reload");
				messages.add("      - &7Reloads plugin and config");
				messages.add("&m                                                                                ");
			}
		}
		return messages;
	}
}