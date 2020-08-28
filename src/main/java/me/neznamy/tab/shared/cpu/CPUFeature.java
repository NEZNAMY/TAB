package me.neznamy.tab.shared.cpu;

/**
 * Enum class for all possible CPU usage categories
 * Getting pretty messy, looking to split into two types in the future
 */
public enum CPUFeature {

	//tablist names
	ALIGNED_TABSUFFIX("Aligned tabsuffix"),
	TABLIST_NAMES_1("Tablist names 1"),
	TABLIST_NAMES_2("Tablist names 2"),
	TABLIST_NAMES_3("Tablist names 3"),
	
	//bossbar
	BOSSBAR_PERMISSION_CHECK("Bossbar permission update checks"),
	BOSSBAR_LEGACY("Bossbar <1.9"),
	BOSSBAR_ANNOUNCEMENT("Bossbar Announcement"),
	BOSSBAR_TEXT_REFRESH("Bossbar text refresh"),
	BOSSBAR_PROGRESS_REFRESH("Bossbar progress refresh"),
	BOSSBAR_COLOR_STYLE_REFRESH("Bossbar color and style refresh"),
	
	//header/footer
	HEADER_FOOTER("Header/Footer"),
	
	//nametags
	NAMETAG("NameTags"),
	NAMETAG_INVISFIX("NameTags - invisfix"),
	NAMETAG_ANTIOVERRIDE("NameTags - anti override"),
	NAMETAG_COLLISION("NameTags - collision rule refresh"),
	NAMETAGX_INVISCHECK("NameTagX - visibility update"),
	NAMETAGX_PACKET_ENTITY_MOVE("NameTagX - EntityMove"),
	NAMETAGX_PACKET_LISTENING("NameTagX - Packet Listening"),
	NAMETAGX_PACKET_MOUNT("NameTagX - Mount"),
	NAMETAGX_PACKET_NAMED_ENTITY_SPAWN("NameTagX - NamedEntitySpawn"),
	NAMETAGX_PACKET_ENTITY_DESTROY("NameTagX - EntityDestroy"),
	NAMETAGX_EVENT_SNEAK("NameTagX - PlayerToggleSneakEvent"),
	NAMETAGX_EVENT_MOVE("NameTagX - PlayerMoveEvent"),
	NAMETAGX_EVENT_TELEPORT("NameTagX - PlayerTeleportEvent"),
	NAMETAGX_EVENT_RESPAWN("NameTagX - PlayerRespawnEvent"),
	NAMETAGX_EVENT_QUIT("NameTagX - PlayerQuitEvent"),
	
	//all 3 scoreboard display objectives
	BELOWNAME_NUMBER("Belowname number"),
	BELOWNAME_TEXT("Belowname text"),
	SCOREBOARD_CONDITIONS("Scoreboard condition update checks"),
	SCOREBOARD_TITLE("Scoreboard title"),
	SCOREBOARD_LINES("Scoreboard lines"),
	YELLOW_NUMBER("Yellow number"),
	
	SPECTATOR_FIX("Spectator fix"),
	
	PET_NAME_FIX("Pet name fix"),
	
	GHOST_PLAYER_FIX("Ghost player fix"),
	
	GLOBAL_PLAYERLIST("Global playerlist"),
	
	PER_WORLD_PLAYERLIST("Per world playerlist"),
	
	WORLD_SWITCH("World switch processing"),
	
	GROUP_REFRESHING("Permission group refreshing"),
	
	PLACEHOLDER_REFRESHING("Refreshing placeholders"),
	
	WATERFALLFIX("Waterfall fix"),
	
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
