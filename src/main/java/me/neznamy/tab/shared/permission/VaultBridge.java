package me.neznamy.tab.shared.permission;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bungee.Main;

/**
 * Class to take groups from bungeecord config.yml when no permission plugin is found
 */
public class VaultBridge implements PermissionPlugin {

	@Override
	public String getPrimaryGroup(TabPlayer p) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Group");
		Main.plm.sendPluginMessage(p, out.toByteArray());
		return p.getGroup();
	}

	@Override
	public String[] getAllGroups(TabPlayer p) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Group");
		Main.plm.sendPluginMessage(p, out.toByteArray());
		return new String[] {p.getGroup()};
	}
	
	@Override
	public String getName() {
		return "Vault through BukkitBridge";
	}

	@Override
	public String getVersion() {
		return "-";
	}
}