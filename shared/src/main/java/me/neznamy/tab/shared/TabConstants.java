package me.neznamy.tab.shared;

public class TabConstants {

	private TabConstants() {}

	public static class CpuUsageCategory {

		public static final String PLAYER_JOIN = "Player Join";
		public static final String PLAYER_QUIT = "Player Quit";
		public static final String WORLD_SWITCH = "World Switch";
		public static final String SERVER_SWITCH = "Server Switch";
		public static final String COMMAND_PREPROCESS = "Command Preprocess";
		public static final String PLAYER_SNEAK = "Player Sneak";
		public static final String PLAYER_RESPAWN = "Player Respawn";
		public static final String PLUGIN_MESSAGE = "PluginMessageEvent";
		public static final String REDIS_BUNGEE_MESSAGE = "PubSubMessageEvent";
		public static final String LUCKPERMS_RECALCULATE_EVENT = "UserDataRecalculateEvent";

		public static final String ANTI_OVERRIDE = "Anti override";

		public static final String BYTE_BUF = "ByteBuf";
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

		public static final String V1_8_0_BUG_COMPENSATION = "Compensating for 1.8.0 bugs";
		public static final String REFRESHING_NAME_TAG_VISIBILITY = "Refreshing NameTag visibility";
		public static final String SCOREBOARD_PACKET_CHECK = "Checking for other plugins";
		public static final String PROCESSING_PLAYER_MOVEMENT = "Processing player movement";

		private CpuUsageCategory() {}
	}

	public static class PacketCategory {

		public static final String BOSSBAR_COLOR_STYLE = "BossBar (Color and style)";
		public static final String BOSSBAR_PROGRESS = "BossBar (Progress)";
		public static final String BOSSBAR_TEXT = "BossBar (Text)";
		public static final String BOSSBAR_WITHER_TELEPORT = "BossBar (Teleporting Wither)";

		public static final String GLOBAL_PLAYERLIST_LATENCY = "Global PlayerList (Updating latency)";
		public static final String GLOBAL_PLAYERLIST_VANISH = "Global PlayerList (Vanishing)";

		public static final String LAYOUT_FIXED_SLOTS = "Layout (Fixed slots)";
		public static final String LAYOUT_PLAYER_SLOTS = "Layout (Player slots)";

		public static final String NAMETAGS_TEAM_REGISTER = "NameTags (Team register)";
		public static final String NAMETAGS_TEAM_UNREGISTER = "NameTags (Team unregister)";
		public static final String NAMETAGS_TEAM_UPDATE = "NameTags (Team update)";

		public static final String SCOREBOARD_TITLE = "Scoreboard (Title)";
		public static final String SCOREBOARD_LINES = "Scoreboard (Lines)";

		public static final String UNLIMITED_NAMETAGS_OFFSET_CHANGE = "Unlimited NameTags (Changing offset)";
		public static final String UNLIMITED_NAMETAGS_SPAWN = "Unlimited NameTags (Spawning)";
		public static final String UNLIMITED_NAMETAGS_DESPAWN = "Unlimited NameTags (Despawning)";
		public static final String UNLIMITED_NAMETAGS_TELEPORT = "Unlimited NameTags (Teleporting)";
		public static final String UNLIMITED_NAMETAGS_SNEAK = "Unlimited NameTags (Sneaking)";
		public static final String UNLIMITED_NAMETAGS_METADATA = "Unlimited NameTags (Metadata)";

		private PacketCategory() {}
	}

	public static class Permission {

		public static final String COMMAND_ALL = "tab.admin";
		public static final String COMMAND_RELOAD = "tab.reload";
		public static final String COMMAND_BOSSBAR_ANNOUNCE = "tab.announce.bar";
		public static final String COMMAND_BOSSBAR_TOGGLE = "tab.bossbar.toggle";
		public static final String COMMAND_BOSSBAR_SEND = "tab.send.bar";
		public static final String COMMAND_CPU = "tab.cpu";
		public static final String COMMAND_DEBUG = "tab.debug";
		public static final String COMMAND_NTPREVIEW = "tab.ntpreview";
		public static final String COMMAND_PARSE = "tab.parse";
		public static final String COMMAND_SCOREBOARD_ANNOUNCE = "tab.announce.scoreboard";
		public static final String COMMAND_SCOREBOARD_TOGGLE = "tab.scoreboard.toggle";
		public static final String COMMAND_SCOREBOARD_TOGGLE_OTHER = "tab.scoreboard.toggle.other";
		public static final String COMMAND_SCOREBOARD_SHOW = "tab.scoreboard.show";
		public static final String COMMAND_SCOREBOARD_SHOW_OTHER = "tab.scoreboard.show.other";
		public static final String COMMAND_SETCOLLISION = "tab.setcollision";
		public static final String COMMAND_AUTOCOMPLETE = "tab.tabcomplete";
		public static final String COMMAND_DATA_REMOVE = "tab.remove";
		public static final String COMMAND_PROPERTY_CHANGE_PREFIX = "tab.change.";
		public static final String COMMAND_WIDTH = "tab.width";

		public static final String STAFF = "tab.staff";

		public static final String PER_WORLD_PLAYERLIST_BYPASS = "tab.bypass";
		public static final String GLOBAL_PLAYERLIST_SEE_VANISHED = "tab.seevanished";
		public static final String SPECTATOR_BYPASS = "tab.spectatorbypass";

		public static final String TEST_PERMISSION = "tab.testpermission";

		public static final String GROUP_PREFIX = "tab.group.";
	}

	public static class Property {

		public static final String HEADER = "header";
		public static final String FOOTER = "footer";

		public static final String TABPREFIX = "tabprefix";
		public static final String CUSTOMTABNAME = "customtabname";
		public static final String TABSUFFIX = "tabsuffix";

		public static final String TAGPREFIX = "tagprefix";
		public static final String CUSTOMTAGNAME = "customtagname";
		public static final String TAGSUFFIX = "tagsuffix";

		public static final String ABOVENAME = "abovename";
		public static final String NAMETAG = "nametag";
		public static final String BELOWNAME = "belowname";

		public static final String SCOREBOARD_TITLE = "scoreboard-title";

		public static final String BELOWNAME_NUMBER = "belowname-number";
		public static final String BELOWNAME_TEXT = "belowname-text";

		public static final String YELLOW_NUMBER = "yellow-number";

		private Property() {
		}

		public static String bossbarTitle(String name) {
			return "bossbar-title-" + name;
		}

		public static String bossbarProgress(String name) {
			return "bossbar-progress-" + name;
		}

		public static String bossbarColor(String name) {
			return "bossbar-color-" + name;
		}

		public static String bossbarStyle(String name) {
			return "bossbar-style-" + name;
		}

		public static String scoreboardPrefix(String scoreboard, int lineNumber) {
			return scoreboard + "-" + lineNumber + "-prefix";
		}

		public static String scoreboardName(String scoreboard, int lineNumber) {
			return scoreboard + "-" + lineNumber + "-name";
		}

		public static String scoreboardSuffix(String scoreboard, int lineNumber) {
			return scoreboard + "-" + lineNumber + "-suffix";
		}
	}

	public static class Feature {

		//universal features
		public static final String HEADER_FOOTER = "HeaderFooter";
		public static final String GHOST_PLAYER_FIX = "GhostPlayerFix";
		public static final String PLAYER_LIST = "PlayerList";
		public static final String SPECTATOR_FIX = "SpectatorFix";
		public static final String YELLOW_NUMBER = "YellowNumber";
		public static final String BELOW_NAME = "BelowName";
		public static final String BELOW_NAME_TEXT = "BelowNameText";
		public static final String SCOREBOARD = "ScoreBoard";
		public static final String PING_SPOOF = "PingSpoof";
		public static final String SORTING = "sorting";
		public static final String LAYOUT = "layout";
		public static final String LAYOUT_VANISH = "layout-vanish";
		public static final String NICK_COMPATIBILITY = "nick";
		public static final String PIPELINE_INJECTION = "injection";
		public static final String BOSS_BAR = "BossBar";
		public static final String NAME_TAGS = "NameTag16";
		public static final String NAME_TAGS_COLLISION = "NameTagCollision";
		public static final String NAME_TAGS_VISIBILITY = "NameTagVisibility";
		public static final String GROUP_MANAGER = "GroupManager";
		public static final String PLACEHOLDER_MANAGER = "PlaceholderManager";

		//Bukkit only
		public static final String PER_WORLD_PLAYER_LIST = "PerWorldPlayerList";
		public static final String PET_FIX = "PetFix";
		public static final String UNLIMITED_NAME_TAGS = "NameTagX";
		public static final String UNLIMITED_NAME_TAGS_PACKET_LISTENER = "nametagx-packet";
		public static final String UNLIMITED_NAME_TAGS_VEHICLE_REFRESHER = "nametagx-vehicle";
		public static final String UNLIMITED_NAME_TAGS_LOCATION_REFRESHER = "nametagx-location";

		//BungeeCord only
		public static final String REDIS_BUNGEE = "RedisBungee";
		public static final String GLOBAL_PLAYER_LIST = "GlobalPlayerList";
		public static final String GLOBAL_PLAYER_LIST_LATENCY = "GlobalPlayerList-Latency";
		public static final String GLOBAL_PLAYER_LIST_VANISH = "GlobalPlayerList-Vanish";

        private Feature(){
		}

		public static String scoreboardLine(String line) {
			return "scoreboard-" + line;
		}

		public static String layout(String name) {
			return "layout-" + name;
		}

		public static String layoutSlot(String layout, int slot) {
			return "layout-" + layout + "-slot-" + slot;
		}

		public static String bossBarTitle(String name) {
			return "BossBar-title-" + name;
		}

		public static String bossBarProgress(String name) {
			return "BossBar-progress-" + name;
		}

		public static String bossBarColorStyle(String name) {
			return "BossBar-color-style-" + name;
		}

		public static String scoreboardLine(String scoreboard, int index) {
			return "scoreboard-score-" + scoreboard + "-" + index;
		}
	}
}