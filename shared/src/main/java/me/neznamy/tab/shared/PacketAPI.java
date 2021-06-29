package me.neznamy.tab.shared;

import java.util.Collection;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.packets.EnumChatFormat;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;

/**
 * Soon to be removed assistant for easier packet creating
 */
public class PacketAPI {
	
	private PacketAPI() {
	}
	
	/**
	 * Registers scoreboard team with given properties but sends unregister packet first unless disabled to avoid bungeecord kick
	 * @param to - player to send the packet to
	 * @param teamName - team name
	 * @param prefix - team prefix
	 * @param suffix - team suffix
	 * @param enumNameTagVisibility - visibility
	 * @param enumTeamPush - collision
	 * @param players - player list
	 * @param color - color field (1.13+)
	 */
	public static synchronized void registerScoreboardTeam(TabPlayer to, String teamName, String prefix, String suffix, boolean enumNameTagVisibility, boolean enumTeamPush, Collection<String> players, EnumChatFormat color, TabFeature feature) {
		if (to.getVersion().getMinorVersion() >= 8 && TAB.getInstance().getConfiguration().isUnregisterBeforeRegister() && TAB.getInstance().getPlatform().getSeparatorType().equals("world")) {
			to.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName), feature);
		}
		to.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName, prefix, suffix, enumNameTagVisibility?"always":"never", enumTeamPush?"always":"never", players, 0).setColor(color), feature);
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
		if (to.getVersion().getMinorVersion() >= 8 && TAB.getInstance().getConfiguration().isUnregisterBeforeRegister() && TAB.getInstance().getPlatform().getSeparatorType().equals("world")) {
			to.sendCustomPacket(new PacketPlayOutScoreboardObjective(objectiveName), feature);
		}
		to.sendCustomPacket(new PacketPlayOutScoreboardObjective(0, objectiveName, title, displayType), feature);
		to.sendCustomPacket(new PacketPlayOutScoreboardDisplayObjective(position, objectiveName), feature);
	}
}