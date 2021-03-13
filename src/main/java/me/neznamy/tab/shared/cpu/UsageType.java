package me.neznamy.tab.shared.cpu;

/**
 * Enum containing all reasons features require CPU time
 * This includes events, packets, repeating tasks and other
 */
public enum UsageType {

	//events
	PLAYER_JOIN_EVENT("PlayerJoinEvent"),
	PLAYER_QUIT_EVENT("PlayerQuitEvent"),
	WORLD_SWITCH_EVENT("PlayerChangedWorldEvent"),
	PLAYER_TOGGLE_SNEAK_EVENT("PlayerToggleSneakEvent"),
	PLAYER_MOVE_EVENT("PlayerMoveEvent"),
	PLAYER_TELEPORT_EVENT("PlayerTeleportEvent"),
	PLAYER_RESPAWN_EVENT("PlayerRespawnEvent"),
	COMMAND_PREPROCESS("PlayerCommandPreprocessEvent"),
	PLUGIN_MESSAGE_EVENT("PluginMessageEvent"),
	PLAYER_CHAT_EVENT("AsyncPlayerChatEvent"),
	PLAYER_DEATH_EVENT("PlayerDeathEvent"),
	
	//packets
	ANTI_OVERRIDE("Anti override"),
	PACKET_READING("Packet reading"),
	PACKET_ENTITY_MOVE("PacketPlayOutEntity"),
	PACKET_PLAYER_INFO("PacketPlayOutPlayerInfo"),
	PACKET_LOGIN("PacketLogin"),
	PACKET_DISPLAY_OBJECTIVE("PacketPlayOutScoreboardDisplayObjective"),
	PACKET_OBJECTIVE("PacketPlayOutScoreboardObjective"),
	PACKET_HEADER_FOOTER("PacketPlayOutPlayerListHeaderFooter"),
	
	//nametags
	REFRESHING_COLLISION("Refreshing collision rule"),
	REFRESHING_NAMETAG_VISIBILITY("Refreshing nametag visibility"),
	REFRESHING_TEAM_NAME("Refreshing team name"),
	
	//bossbar
	TELEPORTING_ENTITY("Teleporting entity"),
	
	//other
	REFRESHING("Refreshing"),
	REPEATING_TASK("Repeating task"),
	TRACKING_VEHICLES("Tracking vehicles"),
	v1_8_0_BUG_COMPENSATION("Compensating for 1.8.0 bugs");
	
	//user-friendly name to be used in /tab cpu
	private String friendlyName;

	private UsageType(String friendlyName){
		this.friendlyName = friendlyName;
	}

	@Override
	public String toString() {
		return friendlyName;
	}
}