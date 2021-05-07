package me.neznamy.tab.shared.cpu;

/**
 * Enum class containing names of all features
 */
public enum TabFeature {

	ALIGNED_TABSUFFIX("Aligned tabsuffix"),
	TABLIST_NAMES("Tablist prefix/suffix"),
	BOSSBAR("Bossbar"),
	HEADER_FOOTER("Header/Footer"),
	NAMETAGS("Nametags"),
	NAMETAGX("Unlimited Nametags"),
	BELOWNAME_NUMBER("Belowname number"),
	BELOWNAME_TEXT("Belowname text"),
	SCOREBOARD("Scoreboard"),
	YELLOW_NUMBER("Yellow number"),
	SPECTATOR_FIX("Spectator fix"),
	PET_NAME_FIX("Pet name fix"),
	GHOST_PLAYER_FIX("Ghost player fix"),
	GLOBAL_PLAYERLIST("Global playerlist"),
	PER_WORLD_PLAYERLIST("Per world playerlist"),
	GROUP_REFRESHING("Permission group refreshing"),
	PLACEHOLDER_REFRESHING("Refreshing placeholders"),
	SORTING("Sorting"),
	PIPELINE_INJECTION("Pipeline injection"),
	PACKET_DESERIALIZING("Packet deserializing"),
	PACKET_SERIALIZING("Packet serializing"),
	TABLIST_LAYOUT("Tablist layout"),
	PLUGIN_MESSAGE_HANDLING("Plugin message handling"),
	PING_SPOOF("Ping spoof"),
	OTHER("Other");

	//user-friendly name to be used in /tab cpu
	private String displayName;

	/**
	 * Constructs new instance with given display name
	 * @param displayName - name to display in /tab cpu
	 */
	private TabFeature(String displayName){
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}