package me.neznamy.tab.shared.command.level1;

import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.NameTagX;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;

public class NTPreviewCommand extends SubCommand{

	public NTPreviewCommand() {
		super("ntpreview", "tab.ntpreview");
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		if (Shared.features.containsKey("nametagx")) {
			if (sender != null) {
				if (sender.previewingNametag) {
					sender.getArmorStands().forEach(a -> a.destroy(sender));
					sender.getArmorStands().forEach(a -> a.getNearbyPlayers().remove(sender));
					sendMessage(sender, Configs.preview_off);
				} else {
					NameTagX.spawnArmorStand(sender, sender);
					sendMessage(sender, Configs.preview_on);
				}
				sender.previewingNametag = !sender.previewingNametag;
			}
		} else sendMessage(sender, Configs.unlimited_nametag_mode_not_enabled);
	}
}