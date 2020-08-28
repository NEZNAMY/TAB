package me.neznamy.tab.shared.command.level1;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.config.Configs;

/**
 * Handler for "/tab ntpreview" subcommand
 */
public class NTPreviewCommand extends SubCommand{

	public NTPreviewCommand() {
		super("ntpreview", "tab.ntpreview");
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		if ((Shared.features.get("nametagx")) != null) {
			if (sender != null) {
				if (sender.previewingNametag) {
					sender.getArmorStandManager().destroy(sender);
					sendMessage(sender, Configs.preview_off);
				} else {
					sender.getArmorStandManager().spawn(sender);
					sendMessage(sender, Configs.preview_on);
				}
				sender.previewingNametag = !sender.previewingNametag;
			}
		} else {
			sendMessage(sender, Configs.unlimited_nametag_mode_not_enabled);
		}
	}
}