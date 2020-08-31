package me.neznamy.tab.shared;

import java.util.Arrays;
import java.util.Collection;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.packets.EnumChatFormat;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore.Action;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;

/**
 * Soon to be removed assistant for easier packet creating
 */
public class PacketAPI {
	
	//scoreboard team
	public static void registerScoreboardTeam(TabPlayer to, String teamName, String prefix, String suffix, boolean enumNameTagVisibility, boolean enumTeamPush, Collection<String> players, EnumChatFormat color) {
		if (to.getVersion().getMinorVersion() >= 8 && Configs.SECRET_unregister_before_register && Shared.platform.getSeparatorType().equals("world")) {
			to.sendCustomPacket(PacketPlayOutScoreboardTeam.REMOVE_TEAM(teamName).setTeamOptions(69));
		}
		to.sendCustomPacket(PacketPlayOutScoreboardTeam.CREATE_TEAM(teamName, prefix, suffix, enumNameTagVisibility?"always":"never", enumTeamPush?"always":"never", players, 69).setColor(color));
	}

	//scoreboard objective
	public static void registerScoreboardObjective(TabPlayer to, String objectiveName, String title, int position, EnumScoreboardHealthDisplay displayType) {
		if (to.getVersion().getMinorVersion() >= 8 && Configs.SECRET_unregister_before_register && Shared.platform.getSeparatorType().equals("world")) {
			to.sendCustomPacket(PacketPlayOutScoreboardObjective.UNREGISTER(objectiveName));
		}
		to.sendCustomPacket(PacketPlayOutScoreboardObjective.REGISTER(objectiveName, title, displayType));
		to.sendCustomPacket(new PacketPlayOutScoreboardDisplayObjective(position, objectiveName));
	}

	//scoreboard score
	public static void registerScoreboardScore(TabPlayer p, String team, String fakeplayer, String prefix, String suffix, String objective, int score) {
		registerScoreboardTeam(p, team, prefix, suffix, false, false, Arrays.asList(fakeplayer), null);
		setScoreboardScore(p, fakeplayer, objective, score);
	}
	public static void removeScoreboardScore(TabPlayer p, String fakeplayer, String objective) {
		p.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.REMOVE, objective, fakeplayer, 0));
		p.sendCustomPacket(PacketPlayOutScoreboardTeam.REMOVE_TEAM(objective).setTeamOptions(69));
	}
	public static void setScoreboardScore(TabPlayer to, String fakeplayer, String objective, int score) {
		to.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, objective, fakeplayer, score));
	}
}