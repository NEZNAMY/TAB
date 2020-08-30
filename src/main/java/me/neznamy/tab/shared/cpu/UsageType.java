package me.neznamy.tab.shared.cpu;

public enum UsageType {

	PLAYER_JOIN_EVENT("PlayerJoinEvent"),
	PLAYER_QUIT_EVENT("PlayerQuitEvent"),
	WORLD_SWITCH_EVENT("WorldSwitchEvent"),
	PLAYER_TOGGLE_SNEAK_EVENT("PlayerToggleSneakEvent"),
	PLAYER_MOVE_EVENT("PlayerMoveEvent"),
	PLAYER_TELEPORT_EVENT("PlayerTeleportEvent"),
	PLAYER_RESPAWN_EVENT("PlayerRespawnEvent"),
	REFRESHING("Refreshing"),
	PACKET_READING("Packet reading"),
	PACKET_MOUNT("Mount packet"),
	PACKET_ENTITY_DESTROY("Entity destroy packet"),
	PACKET_ENTITY_MOVE("Entity move packet"),
	PACKET_NAMED_ENTITY_SPAWN("Named entity spawn"),
	REPEATING_TASK("Additional repeating task"),
	OTHER("Other"),
	TOTAL("Total");
	
	private String friendlyName;

	private UsageType(String friendlyName){
		this.friendlyName = friendlyName;
	}

	@Override
	public String toString() {
		return friendlyName;
	}
}