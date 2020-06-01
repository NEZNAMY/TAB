package me.neznamy.tab.shared.cpu;

public enum CPUFeature {

	//tablist names
	ALIGNED_TABSUFFIX("Aligned Tabsuffix"),
	TABLIST_NAMES_1("Tablist names 1"),
	TABLIST_NAMES_2("Tablist names 2"),
	TABLIST_NAMES_3("Tablist names 3"),
	
	//bossbar
	BOSSBAR("BossBar"),
	BOSSBAR_LEGACY("BossBar <1.9"),
	BOSSBAR_ANNOUNCEMENT("BossBar Announcement"),
	
	//header/footer
	HEADER_FOOTER("Header/Footer"),
	
	//nametags
	NAMETAG("NameTags"),
	NAMETAG_INVISFIX("NameTags - invisfix"),
	NAMETAG_ANTIOVERRIDE("NameTags - Anti override"),
	NAMETAGX_INVISCHECK("NameTagX - visibility update"),
	NAMETAGX_PACKET_LISTENING("NameTagX - Packet Listening"),
	NAMETAGX_PACKET_NAMED_ENTITY_SPAWN("NameTagX - NamedEntitySpawn"),
	NAMETAGX_PACKET_ENTITY_DESTROY("NameTagX - EntityDestroy"),
	NAMETAGX_EVENT_SNEAK("NameTagX - PlayerToggleSneakEvent"),
	NAMETAGX_EVENT_MOVE("NameTagX - PlayerMoveEvent"),
	NAMETAGX_EVENT_TELEPORT("NameTagX - PlayerTeleportEvent"),
	NAMETAGX_EVENT_RESPAWN("NameTagX - PlayerRespawnEvent"),
	NAMETAGX_EVENT_QUIT("NameTagX - PlayerQuitEvent"),
	
	//all 3 scoreboard display objectives
	BELOWNAME("BelowName"),
	SCOREBOARD("ScoreBoard"),
	YELLOW_NUMBER("Yellow Number"),
	
	SPECTATOR_FIX("Spectator fix"),
	
	PET_NAME_FIX("Pet name fix"),
	
	GHOST_PLAYER_FIX("Ghost player fix"),
	
	GLOBAL_PLAYERLIST("Global Playerlist"),
	
	PER_WORLD_PLAYERLIST("Per World Playerlist"),
	
	WORLD_SWITCH("World Switch Processing"),
	
	//onjoin, 
	OTHER("Other");
	
	
	private String friendlyName;
	
	private CPUFeature(String friendlyName){
		this.friendlyName = friendlyName;
	}
	
	@Override
	public String toString() {
		return friendlyName;
	}
}
