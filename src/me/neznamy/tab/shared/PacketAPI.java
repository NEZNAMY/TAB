package me.neznamy.tab.shared;

import java.util.Arrays;
import java.util.Collection;

import me.neznamy.tab.shared.packets.EnumChatFormat;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore.Action;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;

public class PacketAPI{
	
	//scoreboard team
	public static void registerScoreboardTeam(ITabPlayer to, String teamName, String prefix, String suffix, boolean enumNameTagVisibility, boolean enumTeamPush, Collection<String> players, EnumChatFormat color) {
		if (to.getVersion().getMinorVersion() >= 8 && Configs.SECRET_safe_register && Shared.separatorType.equals("world")) {
			Shared.debug("Unregistering team " + teamName + " for " + to.getName());
			to.sendCustomPacket(PacketPlayOutScoreboardTeam.REMOVE_TEAM(teamName).setTeamOptions(69));
		}
		Shared.debug("Registering team " + teamName + " for " + to.getName());
		to.sendCustomPacket(PacketPlayOutScoreboardTeam.CREATE_TEAM(teamName, prefix, suffix, enumNameTagVisibility?"always":"never", enumTeamPush?"always":"never", players, 69).setColor(color));
	}
	public static void updateScoreboardTeamPrefixSuffix(ITabPlayer to, String teamName, String prefix, String suffix, boolean enumNameTagVisibility, boolean enumTeamPush) {
		to.sendCustomPacket(PacketPlayOutScoreboardTeam.UPDATE_TEAM_INFO(teamName, prefix, suffix, enumNameTagVisibility?"always":"never", enumTeamPush?"always":"never", 69));
	}

	//scoreboard objective
	public static void registerScoreboardObjective(ITabPlayer to, String objectiveName, String title, int position, EnumScoreboardHealthDisplay displayType) {
		if (to.getVersion().getMinorVersion() >= 8 && Configs.SECRET_safe_register) {
			to.sendCustomPacket(PacketPlayOutScoreboardObjective.UNREGISTER(objectiveName));
		}
		to.sendCustomPacket(PacketPlayOutScoreboardObjective.REGISTER(objectiveName, title, displayType));
		to.sendCustomPacket(new PacketPlayOutScoreboardDisplayObjective(position, objectiveName));
	}

	//scoreboard score
	public static void registerScoreboardScore(ITabPlayer p, String team, String fakeplayer, String prefix, String suffix, String objective, int score) {
		registerScoreboardTeam(p, team, prefix, suffix, false, false, Arrays.asList(fakeplayer), EnumChatFormat.RESET);
		setScoreboardScore(p, fakeplayer, objective, score);
	}
	public static void removeScoreboardScore(ITabPlayer p, String fakeplayer, String objective) {
		p.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.REMOVE, objective, fakeplayer, 0));
		Shared.debug("Unregistering team " + objective + " for " + p.getName());
		p.sendCustomPacket(PacketPlayOutScoreboardTeam.REMOVE_TEAM(objective).setTeamOptions(69));
	}
	public static void setScoreboardScore(ITabPlayer to, String fakeplayer, String objective, int score) {
		to.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, objective, fakeplayer, score));
	}
}