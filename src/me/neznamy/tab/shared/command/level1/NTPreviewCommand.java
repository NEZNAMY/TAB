package me.neznamy.tab.shared.command.level1;

import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.NameTagX;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.command.SubCommand;
import me.neznamy.tab.shared.config.Configs;

public class NTPreviewCommand extends SubCommand{

	public NTPreviewCommand() {
		super("ntpreview", "tab.ntpreview");
	}

	@Override
	public void execute(ITabPlayer sender, String[] args) {
		NameTagX feature;
		if ((feature = (NameTagX) Shared.features.get("nametagx")) != null) {
			if (sender != null) {
				if (sender.previewingNametag) {
					sender.getArmorStands().forEach(a -> a.destroy(sender));
					sender.getArmorStands().forEach(a -> a.getNearbyPlayers().remove(sender));
					sendMessage(sender, Configs.preview_off);
				} else {
					feature.spawnArmorStand(sender, sender);
					sendMessage(sender, Configs.preview_on);
				}
				sender.previewingNametag = !sender.previewingNametag;
			}
		} else sendMessage(sender, Configs.unlimited_nametag_mode_not_enabled);
	}
}