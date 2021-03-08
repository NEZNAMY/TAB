package me.neznamy.tab.shared.permission;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.PluginMessageHandler;

/**
 * Class to take groups from Vault on bukkit side if no permission plugin on bungeecord is found
 */
public class VaultBridge implements PermissionPlugin {

	//plugin message handler to request groups through
	private PluginMessageHandler plm;
	
	/**
	 * Constructs new instance with given parameter
	 * @param plm - plugin message handler to request groups through
	 */
	public VaultBridge(PluginMessageHandler plm) {
		this.plm = plm;
	}
	
	@Override
	public String getPrimaryGroup(TabPlayer p) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Group");
		plm.sendPluginMessage(p, out.toByteArray());
		return p.getGroup();
	}

	@Override
	public String[] getAllGroups(TabPlayer p) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Group");
		plm.sendPluginMessage(p, out.toByteArray());
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