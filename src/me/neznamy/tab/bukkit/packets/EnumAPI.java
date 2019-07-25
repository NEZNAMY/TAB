package me.neznamy.tab.bukkit.packets;

import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;

public class EnumAPI {

	public static Object BarColor_since_1_9_R1_BLUE;
	public static Object BarColor_since_1_9_R1_GREEN;
	public static Object BarColor_since_1_9_R1_PINK;
	public static Object BarColor_since_1_9_R1_PURPLE;
	public static Object BarColor_since_1_9_R1_RED;
	public static Object BarColor_since_1_9_R1_WHITE;
	public static Object BarColor_since_1_9_R1_YELLOW;

	public static Object BarStyle_since_1_9_R1_NOTCHED_6;
	public static Object BarStyle_since_1_9_R1_NOTCHED_10;
	public static Object BarStyle_since_1_9_R1_NOTCHED_12;
	public static Object BarStyle_since_1_9_R1_NOTCHED_20;
	public static Object BarStyle_since_1_9_R1_PROGRESS;

	public static Object ChatMessageType_since_1_12_R1_CHAT;
	public static Object ChatMessageType_since_1_12_R1_SYSTEM;
	public static Object ChatMessageType_since_1_12_R1_GAME_INFO;

	public static Object EnumChatFormat_BLACK;
	public static Object EnumChatFormat_DARK_BLUE;
	public static Object EnumChatFormat_DARK_GREEN;
	public static Object EnumChatFormat_DARK_AQUA;
	public static Object EnumChatFormat_DARK_RED;
	public static Object EnumChatFormat_DARK_PURPLE;
	public static Object EnumChatFormat_GOLD;
	public static Object EnumChatFormat_GRAY;
	public static Object EnumChatFormat_DARK_GRAY; 
	public static Object EnumChatFormat_BLUE; 
	public static Object EnumChatFormat_GREEN;
	public static Object EnumChatFormat_AQUA;
	public static Object EnumChatFormat_RED;
	public static Object EnumChatFormat_LIGHT_PURPLE;
	public static Object EnumChatFormat_YELLOW; 
	public static Object EnumChatFormat_WHITE;
	public static Object EnumChatFormat_OBFUSCATED; 
	public static Object EnumChatFormat_BOLD; 
	public static Object EnumChatFormat_STRIKETHROUGH;
	public static Object EnumChatFormat_UNDERLINE; 
	public static Object EnumChatFormat_ITALIC; 
	public static Object EnumChatFormat_RESET;

	public static Object EnumEntityUseAction_ATTACK;
	public static Object EnumEntityUseAction_INTERACT;
	public static Object EnumEntityUseAction_INTERACT_AT;

	public static Object EnumPlayerInfoAction_ADD_PLAYER;
	public static Object EnumPlayerInfoAction_REMOVE_PLAYER;
	public static Object EnumPlayerInfoAction_UPDATE_DISPLAY_NAME;
	public static Object EnumPlayerInfoAction_UPDATE_GAME_MODE;
	public static Object EnumPlayerInfoAction_UPDATE_LATENCY;

	public static Object EnumScoreboardHealthDisplay_INTEGER;
	public static Object EnumScoreboardHealthDisplay_HEARTS;
	
	public static Object EntityPose_SLEEPING;

	public static Object PacketPlayOutBoss_Action_since_1_9_R1_ADD;
	public static Object PacketPlayOutBoss_Action_since_1_9_R1_REMOVE;
	public static Object PacketPlayOutBoss_Action_since_1_9_R1_UPDATE_NAME;
	public static Object PacketPlayOutBoss_Action_since_1_9_R1_UPDATE_PCT;
	public static Object PacketPlayOutBoss_Action_since_1_9_R1_UPDATE_PROPERTIES;
	public static Object PacketPlayOutBoss_Action_since_1_9_R1_UPDATE_STYLE;

	public static Object PacketPlayOutScoreboardScore_Action_CHANGE;
	public static Object PacketPlayOutScoreboardScore_Action_REMOVE;

	public static Object EnumGamemode_ADVENTURE;
	public static Object EnumGamemode_CREATIVE;
	public static Object EnumGamemode_NOT_SET;
	public static Object EnumGamemode_SPECTATOR;
	public static Object EnumGamemode_SURVIVAL;

	static {
		try {
			if (UniversalPacketPlayOut.versionNumber > 0) {
				Class<?> EnumChatFormat = NMSClass.get("EnumChatFormat");
				EnumChatFormat_BLACK = EnumChatFormat.getDeclaredField("BLACK").get(null);
				EnumChatFormat_DARK_BLUE = EnumChatFormat.getDeclaredField("DARK_BLUE").get(null);
				EnumChatFormat_DARK_GREEN = EnumChatFormat.getDeclaredField("DARK_GREEN").get(null);
				EnumChatFormat_DARK_AQUA = EnumChatFormat.getDeclaredField("DARK_AQUA").get(null);
				EnumChatFormat_DARK_RED = EnumChatFormat.getDeclaredField("DARK_RED").get(null);
				EnumChatFormat_DARK_PURPLE = EnumChatFormat.getDeclaredField("DARK_PURPLE").get(null);
				EnumChatFormat_GOLD = EnumChatFormat.getDeclaredField("GOLD").get(null);
				EnumChatFormat_GRAY = EnumChatFormat.getDeclaredField("GRAY").get(null);
				EnumChatFormat_DARK_GRAY = EnumChatFormat.getDeclaredField("DARK_GRAY").get(null); 
				EnumChatFormat_BLUE = EnumChatFormat.getDeclaredField("BLUE").get(null); 
				EnumChatFormat_GREEN = EnumChatFormat.getDeclaredField("GREEN").get(null);
				EnumChatFormat_AQUA = EnumChatFormat.getDeclaredField("AQUA").get(null);
				EnumChatFormat_RED = EnumChatFormat.getDeclaredField("RED").get(null);
				EnumChatFormat_LIGHT_PURPLE = EnumChatFormat.getDeclaredField("LIGHT_PURPLE").get(null);
				EnumChatFormat_YELLOW = EnumChatFormat.getDeclaredField("YELLOW").get(null); 
				EnumChatFormat_WHITE = EnumChatFormat.getDeclaredField("WHITE").get(null);
				EnumChatFormat_OBFUSCATED = EnumChatFormat.getDeclaredField("OBFUSCATED").get(null); 
				EnumChatFormat_BOLD = EnumChatFormat.getDeclaredField("BOLD").get(null); 
				EnumChatFormat_STRIKETHROUGH = EnumChatFormat.getDeclaredField("STRIKETHROUGH").get(null);
				EnumChatFormat_UNDERLINE = EnumChatFormat.getDeclaredField("UNDERLINE").get(null); 
				EnumChatFormat_ITALIC = EnumChatFormat.getDeclaredField("ITALIC").get(null); 
				EnumChatFormat_RESET = EnumChatFormat.getDeclaredField("RESET").get(null);

				Class<?> EnumGamemode;
				try{
					//1.8.R1, 1.10+
					EnumGamemode = NMSClass.get("EnumGamemode");
				} catch (Exception e) {
					//1.8.R2, 1.8.R3, 1.9.R1, 1.9.R2
					EnumGamemode = NMSClass.get("WorldSettings$EnumGamemode");
				}
				EnumGamemode_ADVENTURE = EnumGamemode.getDeclaredField("ADVENTURE").get(null);
				EnumGamemode_CREATIVE = EnumGamemode.getDeclaredField("CREATIVE").get(null);
				EnumGamemode_NOT_SET = EnumGamemode.getDeclaredField("NOT_SET").get(null);
				EnumGamemode_SPECTATOR = EnumGamemode.getDeclaredField("SPECTATOR").get(null);
				EnumGamemode_SURVIVAL = EnumGamemode.getDeclaredField("SURVIVAL").get(null);

				Class<?> EnumEntityUseAction;
				Class<?> EnumScoreboardHealthDisplay;
				Class<?> EnumPlayerInfoAction;
				try{
					//1.8.R1
					EnumEntityUseAction = NMSClass.get("EnumEntityUseAction");
					EnumScoreboardHealthDisplay = NMSClass.get("EnumScoreboardHealthDisplay");
					EnumPlayerInfoAction = NMSClass.get("EnumPlayerInfoAction");
				} catch (Exception e) {
					EnumEntityUseAction = NMSClass.get("PacketPlayInUseEntity$EnumEntityUseAction");
					EnumScoreboardHealthDisplay = NMSClass.get("IScoreboardCriteria$EnumScoreboardHealthDisplay");
					EnumPlayerInfoAction = NMSClass.get("PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
				}


				EnumEntityUseAction_ATTACK = EnumEntityUseAction.getDeclaredField("ATTACK").get(null);
				EnumEntityUseAction_INTERACT = EnumEntityUseAction.getDeclaredField("INTERACT").get(null);
				EnumEntityUseAction_INTERACT_AT = EnumEntityUseAction.getDeclaredField("INTERACT_AT").get(null);

				EnumScoreboardHealthDisplay_INTEGER = EnumScoreboardHealthDisplay.getDeclaredField("INTEGER").get(null);
				EnumScoreboardHealthDisplay_HEARTS = EnumScoreboardHealthDisplay.getDeclaredField("HEARTS").get(null);

				EnumPlayerInfoAction_ADD_PLAYER = EnumPlayerInfoAction.getDeclaredField("ADD_PLAYER").get(null);
				EnumPlayerInfoAction_REMOVE_PLAYER = EnumPlayerInfoAction.getDeclaredField("REMOVE_PLAYER").get(null);
				EnumPlayerInfoAction_UPDATE_DISPLAY_NAME = EnumPlayerInfoAction.getDeclaredField("UPDATE_DISPLAY_NAME").get(null);
				EnumPlayerInfoAction_UPDATE_GAME_MODE = EnumPlayerInfoAction.getDeclaredField("UPDATE_GAME_MODE").get(null);
				EnumPlayerInfoAction_UPDATE_LATENCY = EnumPlayerInfoAction.getDeclaredField("UPDATE_LATENCY").get(null);

				Class<?> PacketPlayOutScoreboardScore_Action;
				if (NMSClass.versionNumber >= 13) {
					PacketPlayOutScoreboardScore_Action = NMSClass.get("ScoreboardServer$Action");
				} else {
					try {
						PacketPlayOutScoreboardScore_Action = NMSClass.get("EnumScoreboardAction");
					} catch (Exception e) {
						PacketPlayOutScoreboardScore_Action = NMSClass.get("PacketPlayOutScoreboardScore$EnumScoreboardAction");
					}
				}
				PacketPlayOutScoreboardScore_Action_CHANGE = PacketPlayOutScoreboardScore_Action.getDeclaredField("CHANGE").get(null);
				PacketPlayOutScoreboardScore_Action_REMOVE = PacketPlayOutScoreboardScore_Action.getDeclaredField("REMOVE").get(null);

				if (NMSClass.versionNumber >= 9) {
					Class<?> BarColor = NMSClass.get("BossBattle$BarColor");
					BarColor_since_1_9_R1_BLUE = BarColor.getDeclaredField("BLUE").get(null);
					BarColor_since_1_9_R1_GREEN = BarColor.getDeclaredField("GREEN").get(null);
					BarColor_since_1_9_R1_PINK = BarColor.getDeclaredField("PINK").get(null);
					BarColor_since_1_9_R1_PURPLE = BarColor.getDeclaredField("PURPLE").get(null);
					BarColor_since_1_9_R1_RED = BarColor.getDeclaredField("RED").get(null);
					BarColor_since_1_9_R1_WHITE = BarColor.getDeclaredField("WHITE").get(null);
					BarColor_since_1_9_R1_YELLOW = BarColor.getDeclaredField("YELLOW").get(null);

					Class<?> BarStyle = NMSClass.get("BossBattle$BarStyle");
					BarStyle_since_1_9_R1_NOTCHED_6 = BarStyle.getDeclaredField("NOTCHED_6").get(null);
					BarStyle_since_1_9_R1_NOTCHED_10 = BarStyle.getDeclaredField("NOTCHED_10").get(null);
					BarStyle_since_1_9_R1_NOTCHED_12 = BarStyle.getDeclaredField("NOTCHED_12").get(null);
					BarStyle_since_1_9_R1_NOTCHED_20 = BarStyle.getDeclaredField("NOTCHED_20").get(null);
					BarStyle_since_1_9_R1_PROGRESS = BarStyle.getDeclaredField("PROGRESS").get(null);

					Class<?> PacketPlayOutBoss_Action = NMSClass.get("PacketPlayOutBoss$Action");
					PacketPlayOutBoss_Action_since_1_9_R1_ADD = PacketPlayOutBoss_Action.getDeclaredField("ADD").get(null);
					PacketPlayOutBoss_Action_since_1_9_R1_REMOVE = PacketPlayOutBoss_Action.getDeclaredField("REMOVE").get(null);
					PacketPlayOutBoss_Action_since_1_9_R1_UPDATE_NAME = PacketPlayOutBoss_Action.getDeclaredField("UPDATE_NAME").get(null);
					PacketPlayOutBoss_Action_since_1_9_R1_UPDATE_PCT = PacketPlayOutBoss_Action.getDeclaredField("UPDATE_PCT").get(null);
					PacketPlayOutBoss_Action_since_1_9_R1_UPDATE_PROPERTIES = PacketPlayOutBoss_Action.getDeclaredField("UPDATE_PROPERTIES").get(null);
					PacketPlayOutBoss_Action_since_1_9_R1_UPDATE_STYLE = PacketPlayOutBoss_Action.getDeclaredField("UPDATE_STYLE").get(null);
				}
				if (NMSClass.versionNumber >= 12) {
					Class<?> ChatMessageType = NMSClass.get("ChatMessageType");
					ChatMessageType_since_1_12_R1_CHAT = ChatMessageType.getDeclaredField("CHAT").get(null);
					ChatMessageType_since_1_12_R1_SYSTEM = ChatMessageType.getDeclaredField("SYSTEM").get(null);
					ChatMessageType_since_1_12_R1_GAME_INFO = ChatMessageType.getDeclaredField("GAME_INFO").get(null);
				}
				if (NMSClass.versionNumber >= 14) {
					Class<?> EntityPose = NMSClass.get("EntityPose");
					EntityPose_SLEEPING = EntityPose.getDeclaredField("SLEEPING").get(null);
				}
			}
		} catch (Exception e) {
			Shared.error("Failed to initialize EnumAPI class", e);
		}
	}
}