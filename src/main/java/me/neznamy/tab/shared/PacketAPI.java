package me.neznamy.tab.shared;

import java.util.Collection;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.packets.EnumChatFormat;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;

/**
 * Soon to be removed assistant for easier packet creating
 */
public class PacketAPI {
	
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
	public static void registerScoreboardTeam(TabPlayer to, String teamName, String prefix, String suffix, boolean enumNameTagVisibility, boolean enumTeamPush, Collection<String> players, EnumChatFormat color) {
		if (to.getVersion().getMinorVersion() >= 8 && Configs.getSecretOption("unregister-before-register", true) && Shared.platform.getSeparatorType().equals("world")) {
			to.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName).setTeamOptions(69));
		}
		to.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName, prefix, suffix, enumNameTagVisibility?"always":"never", enumTeamPush?"always":"never", players, 69).setColor(color));
	}

	/**
	 * Registers scoreboard objective with given properties but sends unregister packet first unless disabled to avoid bungeecord kick
	 * @param to - player to send the packet to
	 * @param objectiveName - name of the objective
	 * @param title - title
	 * @param position - objective position (0 = Playerlist, 1 = Sidebar, 2 = Belowname)
	 * @param displayType - display type of the value (only supported in Playerlist)
	 */
	public static void registerScoreboardObjective(TabPlayer to, String objectiveName, String title, int position, EnumScoreboardHealthDisplay displayType) {
		if (to.getVersion().getMinorVersion() >= 8 && Configs.getSecretOption("unregister-before-register", true) && Shared.platform.getSeparatorType().equals("world")) {
			to.sendCustomPacket(new PacketPlayOutScoreboardObjective(objectiveName));
		}
		to.sendCustomPacket(new PacketPlayOutScoreboardObjective(0, objectiveName, title, displayType));
		to.sendCustomPacket(new PacketPlayOutScoreboardDisplayObjective(position, objectiveName));
	}
}