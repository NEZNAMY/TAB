package me.neznamy.tab.shared;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;

/**
 * Soon to be removed assistant for easier packet creating
 */
public class PacketAPI {
	
	private PacketAPI() {
	}

	/**
	 * Registers scoreboard objective with given properties but sends unregister packet first unless disabled to avoid bungeecord kick
	 * @param to - player to send the packet to
	 * @param objectiveName - name of the objective
	 * @param title - title
	 * @param position - objective position (0 = Playerlist, 1 = Sidebar, 2 = Belowname)
	 * @param displayType - display type of the value (only supported in Playerlist)
	 */
	public static synchronized void registerScoreboardObjective(TabPlayer to, String objectiveName, String title, int position, EnumScoreboardHealthDisplay displayType, TabFeature feature) {
		registerScoreboardObjective(to, objectiveName, title, position, displayType, feature.getFeatureName());
	}
	
	public static synchronized void registerScoreboardObjective(TabPlayer to, String objectiveName, String title, int position, EnumScoreboardHealthDisplay displayType, String feature) {
		if (to.getVersion().getMinorVersion() >= 8 && TAB.getInstance().getConfiguration().isUnregisterBeforeRegister()) {
			to.sendCustomPacket(new PacketPlayOutScoreboardObjective(objectiveName), feature);
		}
		to.sendCustomPacket(new PacketPlayOutScoreboardObjective(0, objectiveName, title, displayType), feature);
		to.sendCustomPacket(new PacketPlayOutScoreboardDisplayObjective(position, objectiveName), feature);
	}
}