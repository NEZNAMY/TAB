package me.neznamy.tab.platforms.bungeecord.redisbungee;

import java.util.Arrays;
import java.util.UUID;

import org.json.simple.JSONObject;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore.Action;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.BelowName;
import me.neznamy.tab.shared.features.YellowNumber;
import me.neznamy.tab.shared.features.nametags.NameTag;

public class RedisPlayer {

	private RedisBungeeSupport redis;
	private boolean disabledPlayerlist;
	private boolean disabledNametags;

	private UUID uniqueId;
	private String name;
	private String server;
	private String tabformat;
	private String teamName;
	private boolean vanished;
	private String[][] skin;
	private String tagprefix;
	private String tagsuffix;
	private boolean namevisibility;
	private String belowname;
	private String yellowNumber;
	private boolean staff;

	private RedisPlayer() {
	}

	public static RedisPlayer fromJson(RedisBungeeSupport redis, JSONObject json) {
		RedisPlayer player = new RedisPlayer();
		player.redis = redis;
		player.uniqueId = UUID.fromString((String) json.get("UUID"));
		player.name = (String) json.get("name");
		player.server = (String) json.get("server");
		player.tabformat = (String) json.get("tabformat");
		player.teamName = (String) json.get("teamname");
		player.vanished = (boolean) json.get("vanished");
		String skinValue = (String) json.get("skin-value");
		if (skinValue != null) {
			player.skin = new String[1][3];
			player.skin[0][0] = "textures";
			player.skin[0][1] = skinValue;
			player.skin[0][2] = (String) json.get("skin-signature");
		} else {
			player.skin = new String[0][0];
		}
		player.tagprefix = (String) json.get("tagprefix");
		player.tagsuffix = (String) json.get("tagsuffix");
		player.namevisibility = (boolean) json.get("namevisibility");
		player.belowname = (String) json.get("belowname");
		player.yellowNumber = (String) json.get("yellow-number");
		player.staff = (boolean) json.get("staff");
		player.disabledPlayerlist = redis.getPlayerlist() == null || redis.getPlayerlist().isDisabled(player.server, null);
		player.disabledNametags = redis.getNametags() == null || redis.getNametags().isDisabled(player.server, null);
		return player;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject toJson(RedisBungeeSupport redis, TabPlayer p) {
		JSONObject json = new JSONObject();
		json.put("action", "join");
		json.put("UUID", p.getTablistUUID().toString());
		json.put("name", p.getName());
		json.put("server", p.getServer());
		if (redis.getPlayerlist() != null) {
			json.put("tabformat", p.getProperty(TabConstants.Property.TABPREFIX).get() + p.getProperty(TabConstants.Property.CUSTOMTABNAME).get() + p.getProperty(TabConstants.Property.TABSUFFIX).get());
		}
		if (p.getProperty(TabConstants.Property.TAGPREFIX) != null) {
			json.put(TabConstants.Property.TAGPREFIX, p.getProperty(TabConstants.Property.TAGPREFIX).get());
			json.put(TabConstants.Property.TAGSUFFIX, p.getProperty(TabConstants.Property.TAGSUFFIX).get());
			json.put("namevisibility", ((NameTag)TAB.getInstance().getFeatureManager().getFeature("nametag16")).getTeamVisibility(p, p));
		} else {
			json.put(TabConstants.Property.TAGPREFIX, "");
			json.put(TabConstants.Property.TAGSUFFIX, "");
			json.put("namevisibility", true);
		}
		if (p.getProperty(TabConstants.Property.BELOWNAME_NUMBER) != null) {
			json.put("belowname", p.getProperty(TabConstants.Property.BELOWNAME_NUMBER).get());
		}
		if (p.getProperty(TabConstants.Property.YELLOW_NUMBER) != null) {
			json.put("yellow-number", p.getProperty(TabConstants.Property.YELLOW_NUMBER).get());
		}
		json.put("teamname", p.getTeamName());
		json.put("vanished", p.isVanished());
		json.put("staff", p.hasPermission(TabConstants.Permission.STAFF));
		String[][] skin = (String[][]) p.getSkin();
		if (skin.length > 0) {
			json.put("skin-value", skin[0][1]);
			json.put("skin-signature", skin[0][2]);
		}
		return json;
	}

	public PacketPlayOutPlayerInfo getAddPacket() {
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, new PlayerInfoData(name, uniqueId, skin, 0, EnumGamemode.SURVIVAL, 
				disabledPlayerlist ? null : IChatBaseComponent.optimizedComponent(tabformat)));
	}

	public PacketPlayOutPlayerInfo getUpdatePacket() {
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PlayerInfoData(uniqueId, disabledPlayerlist ? null : IChatBaseComponent.optimizedComponent(tabformat)));
	}

	public PacketPlayOutPlayerInfo getRemovePacket() {
		PlayerInfoData data = new PlayerInfoData(uniqueId);
		data.setName(name); //making null check not kill own packets
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, data);
	}

	public PacketPlayOutScoreboardTeam getRegisterTeamPacket() {
		if (disabledNametags) return null;
		return new PacketPlayOutScoreboardTeam(teamName, tagprefix, tagsuffix, namevisibility ? "always" : "never", "always", Arrays.asList(name), 0);
	}

	public PacketPlayOutScoreboardTeam getUpdateTeamPacket() {
		if (disabledNametags) return null;
		return new PacketPlayOutScoreboardTeam(teamName, tagprefix, tagsuffix, namevisibility ? "always" : "never", "always", 0);
	}

	public PacketPlayOutScoreboardTeam getUnregisterTeamPacket() {
		if (disabledNametags) return null;
		return new PacketPlayOutScoreboardTeam(teamName);
	}

	public PacketPlayOutScoreboardScore getBelowNameUpdatePacket() {
		if (belowname == null) return null;
		return new PacketPlayOutScoreboardScore(Action.CHANGE, BelowName.OBJECTIVE_NAME, name, TAB.getInstance().getErrorManager().parseInteger(belowname, 0, "belowname number"));
	}

	public PacketPlayOutScoreboardScore getYellowNumberUpdatePacket() {
		if (yellowNumber == null) return null;
		return new PacketPlayOutScoreboardScore(Action.CHANGE, YellowNumber.OBJECTIVE_NAME, name, TAB.getInstance().getErrorManager().parseInteger(yellowNumber, 0, "yellow number"));
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
		if (redis.getPlayerlist() != null) {
			if (disabledPlayerlist) {
				if (!redis.getPlayerlist().isDisabled(server, null)) {
					disabledPlayerlist = false;
					for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
						if (all.getVersion().getMinorVersion() < 8) continue;
						all.sendCustomPacket(getUpdatePacket(), redis);
					}
				}
			} else {
				if (redis.getPlayerlist().isDisabled(server, null)) {
					disabledPlayerlist = true;
					for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
						if (all.getVersion().getMinorVersion() < 8) continue;
						all.sendCustomPacket(getUpdatePacket(), redis);
					}
				}
			}
		}
		if (redis.getNametags() != null) {
			if (disabledNametags) {
				if (!redis.getNametags().isDisabled(server, null)) {
					disabledNametags = false;
					for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
						all.sendCustomPacket(getRegisterTeamPacket(), redis);
					}
				}
			} else {
				if (redis.getNametags().isDisabled(server, null)) {
					disabledNametags = true;
					for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
						all.sendCustomPacket(getUnregisterTeamPacket(), redis);
					}
				}
			}
		}
	}

	public boolean isVanished() {
		return vanished;
	}

	public String getName() {
		return name;
	}

	public String getTabFormat() {
		return tabformat;
	}

	public String getTeamName() {
		return teamName;
	}

	public void setTabFormat(String format) {
		this.tabformat = format;
	}

	public void setTagPrefix(String tagprefix) {
		this.tagprefix = tagprefix;
	}

	public void setTagSuffix(String tagsuffix) {
		this.tagsuffix = tagsuffix;
	}

	public void setBelowName(String belowname) {
		this.belowname = belowname;
	}

	public UUID getUniqueId() {
		return uniqueId;
	}

	public String getYellowNumber() {
		return yellowNumber;
	}

	public void setYellowNumber(String yellowNumber) {
		this.yellowNumber = yellowNumber;
	}

	public boolean isStaff() {
		return staff;
	}

	public boolean hasDisabledPlayerlist() {
		return disabledPlayerlist;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}
}