package me.neznamy.tab.shared;

public class CpuConstants {

	private CpuConstants() {}
	
	public static class UsageCategory {
		
		public static final String PLAYER_JOIN = "Player Join";
		public static final String PLAYER_QUIT = "Player Quit";
		public static final String WORLD_SWITCH = "World Switch";
		public static final String SERVER_SWITCH = "Server Switch";
		public static final String COMMAND_PREPROCESS = "Command Preprocess";
		public static final String PLAYER_SNEAK = "Player Sneak";
		public static final String PLAYER_RESPAWN = "Player Respawn";
		public static final String PLUGIN_MESSAGE = "PluginMessageEvent";
		public static final String LUCKPERMS_RECALCULATE_EVENT = "UserDataRecalculateEvent";
		
		public static final String ANTI_OVERRIDE = "Anti override";
		
		public static final String BYTEBUF = "ByteBuf";
		public static final String PACKET_PLAYER_INFO = "PacketPlayOutPlayerInfo";
		public static final String PACKET_DISPLAY_OBJECTIVE = "PacketPlayOutScoreboardDisplayObjective";
		public static final String PACKET_OBJECTIVE = "PacketPlayOutScoreboardObjective";
		public static final String PACKET_JOIN_GAME = "Login Packet";
		public static final String PACKET_ENTITY_MOVE = "PacketPlayOutEntity";
		public static final String PACKET_ENTITY_MOVE_PASSENGER = "PacketPlayOutEntity (passenger)";
		public static final String PACKET_ENTITY_SPAWN = "PacketPlayOutNamedEntitySpawn";
		public static final String PACKET_ENTITY_DESTROY = "PacketPlayOutEntityDestroy";
		public static final String RAW_PACKET_IN = "Packet reading (in)";
		public static final String RAW_PACKET_OUT = "Packet reading (out)";
		
		public static final String PLACEHOLDER_REFRESHING = "Refreshing placeholders";
		public static final String UPDATING = "Updating visuals";
		
		public static final String V1_8_0_BUG_COMPENSATION = "Compensating for 1.8.0 bugs";
		public static final String REFRESHING_NAMETAG_VISIBILITY = "Refreshing nametag visibility";
		public static final String TICKING_VEHICLES = "Ticking vehicles";
		public static final String TELEPORTING_WITHER = "Teleporting Wither entity";
		public static final String SCOREBOARD_PACKET_CHECK = "Checking for other plugins";
		public static final String REFRESHING_TEAM_NAMES = "Refreshing team names";
		public static final String REFRESHING_GROUPS = "Refreshing player groups";
		
		private UsageCategory() {}
	}
	
	public static class PacketCategory {
		
		public static final String BOSSBAR_COLOR_STYLE = "BossBar (Color and style)";
		public static final String BOSSBAR_PROGRESS = "BossBar (Progress)";
		public static final String BOSSBAR_TEXT = "BossBar (Text)";
		public static final String BOSSBAR_WITHER_TELEPORT = "BossBar (Teleporting Wither)";
		
		public static final String GLOBAL_PLAYERLIST_LATENCY = "Global Playerlist (Updating latency)";
		public static final String GLOBAL_PLAYERLIST_VANISH = "Global Playerlist (Vanishing)";
		
		public static final String LAYOUT_FIXED_SLOTS = "Layout (Fixed slots)";
		public static final String LAYOUT_LATENCY = "Layout (Updating latency)";
		public static final String LAYOUT_PLAYER_SLOTS = "Layout (Player slots)";
		
		public static final String NAMETAGS_TEAM_REGISTER = "Nametags (Team register)";
		public static final String NAMETAGS_TEAM_UNREGISTER = "Nametags (Team unregister)";
		public static final String NAMETAGS_TEAM_UPDATE = "Nametags (Team update)";
		
		public static final String SCOREBOARD_TITLE = "Scoreboard (Title)";
		public static final String SCOREBOARD_LINES = "Scoreboard (Lines)";
		
		public static final String UNLIMITED_NAMETAGS_OFFSET_CHANGE = "Unlimited Nametags (Changing offset)";
		public static final String UNLIMITED_NAMETAGS_SPAWN = "Unlimited Nametags (Spawning)";
		public static final String UNLIMITED_NAMETAGS_DESPAWN = "Unlimited Nametags (Despawning)";
		public static final String UNLIMITED_NAMETAGS_TELEPORT = "Unlimited Nametags (Teleporting)";
		public static final String UNLIMITED_NAMETAGS_SNEAK = "Unlimited Nametags (Sneaking)";
		public static final String UNLIMITED_NAMETAGS_METADATA = "Unlimited Nametags (Metadata)";
		
		private PacketCategory() {}
	}
}