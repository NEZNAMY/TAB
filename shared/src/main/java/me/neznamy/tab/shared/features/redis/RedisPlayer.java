package me.neznamy.tab.shared.features.redis;

import java.util.Collections;
import java.util.UUID;

import me.neznamy.tab.api.protocol.Skin;
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
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.features.BelowName;
import me.neznamy.tab.shared.features.YellowNumber;

public class RedisPlayer {

    private RedisSupport redis;
    private boolean disabledPlayerList;
    private boolean disabledNameTags;

    private UUID uniqueId;
    private String name;
    private String nickname;
    private String server;
    private String tabFormat;
    private String teamName;
    private boolean vanished;
    private Skin skin;
    private String tagPrefix;
    private String tagSuffix;
    private boolean nameVisibility;
    private String belowName;
    private String yellowNumber;
    private boolean staff;

    private RedisPlayer() {
    }

    public static RedisPlayer fromJson(RedisSupport redis, JSONObject json) {
        RedisPlayer player = new RedisPlayer();
        player.redis = redis;
        player.uniqueId = UUID.fromString((String) json.get("UUID"));
        player.name = (String) json.get("name");
        player.nickname = player.name;
        player.server = (String) json.get("server");
        player.tabFormat = (String) json.get("tabformat");
        String team = (String) json.get("teamname");
        player.teamName = checkTeamName(redis, team.substring(0, team.length()-1), 65);
        player.vanished = (boolean) json.get("vanished");
        String skinValue = (String) json.get("skin-value");
        if (skinValue != null) {
            player.skin = new Skin(skinValue, (String) json.get("skin-signature"));
        }
        player.tagPrefix = (String) json.get("tagprefix");
        player.tagSuffix = (String) json.get("tagsuffix");
        player.nameVisibility = (boolean) json.get("namevisibility");
        player.belowName = (String) json.get("belowname");
        player.yellowNumber = (String) json.get("yellow-number");
        player.staff = (boolean) json.get("staff");
        player.disabledPlayerList = redis.getPlayerList() == null || redis.getPlayerList().isDisabled(player.server, null);
        player.disabledNameTags = redis.getNameTags() == null || redis.getNameTags().isDisabled(player.server, null);
        return player;
    }

    private static String checkTeamName(RedisSupport redis, String currentName15, int id) {
        String potentialTeamName = currentName15 + (char)id;
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (redis.getSorting() != null && redis.getSorting().getShortTeamName(all).equals(potentialTeamName)) {
                return checkTeamName(redis, currentName15, id+1);
            }
        }
        for (RedisPlayer all : redis.getRedisPlayers().values()) {
            if (all.getTeamName() != null && all.getTeamName().equals(potentialTeamName)) {
                return checkTeamName(redis, currentName15, id+1);
            }
        }
        return potentialTeamName;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject toJson(RedisSupport redis, TabPlayer p) {
        JSONObject json = new JSONObject();
        json.put("action", "join");
        json.put("UUID", p.getTablistUUID().toString());
        json.put("name", p.getName());
        json.put("server", p.getServer());
        if (redis.getPlayerList() != null) {
            json.put("tabformat", p.getProperty(TabConstants.Property.TABPREFIX).get() + p.getProperty(TabConstants.Property.CUSTOMTABNAME).get() + p.getProperty(TabConstants.Property.TABSUFFIX).get());
        }
        if (p.getProperty(TabConstants.Property.TAGPREFIX) != null) {
            json.put(TabConstants.Property.TAGPREFIX, p.getProperty(TabConstants.Property.TAGPREFIX).get());
            json.put(TabConstants.Property.TAGSUFFIX, p.getProperty(TabConstants.Property.TAGSUFFIX).get());
            json.put("namevisibility", redis.getNameTags().getTeamVisibility(p, p));
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
        json.put("teamname", redis.getSorting() == null ? null : redis.getSorting().getShortTeamName(p));
        json.put("vanished", p.isVanished());
        json.put("staff", p.hasPermission(TabConstants.Permission.STAFF));
        if (p.getSkin() != null) {
            json.put("skin-value", p.getSkin().getValue());
            json.put("skin-signature", p.getSkin().getSignature());
        }
        return json;
    }

    public PacketPlayOutPlayerInfo getAddPacket() {
        return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, new PlayerInfoData(nickname, uniqueId, skin, 0, EnumGamemode.SURVIVAL,
                disabledPlayerList ? null : IChatBaseComponent.optimizedComponent(tabFormat), null));
    }

    public PacketPlayOutPlayerInfo getUpdatePacket() {
        return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PlayerInfoData(uniqueId, disabledPlayerList ? null : IChatBaseComponent.optimizedComponent(tabFormat)));
    }

    public PacketPlayOutPlayerInfo getRemovePacket() {
        PlayerInfoData data = new PlayerInfoData(uniqueId);
        data.setName(nickname); //making null check not kill own packets
        return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, data);
    }

    public PacketPlayOutScoreboardTeam getRegisterTeamPacket() {
        if (disabledNameTags) return null;
        return new PacketPlayOutScoreboardTeam(teamName, tagPrefix, tagSuffix, nameVisibility ? "always" : "never", "always", Collections.singletonList(nickname), 2);
    }

    public PacketPlayOutScoreboardTeam getUpdateTeamPacket() {
        if (disabledNameTags) return null;
        return new PacketPlayOutScoreboardTeam(teamName, tagPrefix, tagSuffix, nameVisibility ? "always" : "never", "always", 2);
    }

    public PacketPlayOutScoreboardTeam getUnregisterTeamPacket() {
        if (disabledNameTags) return null;
        return new PacketPlayOutScoreboardTeam(teamName);
    }

    public PacketPlayOutScoreboardScore getBelowNameUpdatePacket() {
        if (belowName == null) return null;
        return new PacketPlayOutScoreboardScore(Action.CHANGE, BelowName.OBJECTIVE_NAME, nickname, TAB.getInstance().getErrorManager().parseInteger(belowName, 0));
    }

    public PacketPlayOutScoreboardScore getYellowNumberUpdatePacket() {
        if (yellowNumber == null) return null;
        return new PacketPlayOutScoreboardScore(Action.CHANGE, YellowNumber.OBJECTIVE_NAME, nickname, TAB.getInstance().getErrorManager().parseInteger(yellowNumber, 0));
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
        if (redis.getPlayerList() != null) {
            if (disabledPlayerList) {
                if (!redis.getPlayerList().isDisabled(server, null)) {
                    disabledPlayerList = false;
                    for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                        if (all.getVersion().getMinorVersion() < 8) continue;
                        all.sendCustomPacket(getUpdatePacket(), redis);
                    }
                }
            } else {
                if (redis.getPlayerList().isDisabled(server, null)) {
                    disabledPlayerList = true;
                    for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                        if (all.getVersion().getMinorVersion() < 8) continue;
                        all.sendCustomPacket(getUpdatePacket(), redis);
                    }
                }
            }
        }
        if (redis.getNameTags() != null) {
            if (disabledNameTags) {
                if (!redis.getNameTags().isDisabled(server, null)) {
                    disabledNameTags = false;
                    for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                        all.sendCustomPacket(getRegisterTeamPacket(), redis);
                    }
                }
            } else {
                if (redis.getNameTags().isDisabled(server, null)) {
                    disabledNameTags = true;
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

    public String getNickName() {
        return nickname;
    }

    public void setNickName(String nickname) {
        this.nickname = nickname;
    }

    public String getTabFormat() {
        return tabFormat;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTabFormat(String format) {
        this.tabFormat = format;
    }

    public void setTagPrefix(String tagprefix) {
        this.tagPrefix = tagprefix;
    }

    public void setTagSuffix(String tagsuffix) {
        this.tagSuffix = tagsuffix;
    }

    public void setBelowName(String belowname) {
        this.belowName = belowname;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public void setYellowNumber(String yellowNumber) {
        this.yellowNumber = yellowNumber;
    }

    public boolean isStaff() {
        return staff;
    }

    public boolean hasDisabledPlayerlist() {
        return disabledPlayerList;
    }

    public void setTeamName(String teamName) {
        this.teamName = checkTeamName(redis, teamName.substring(0, teamName.length()-1), 65);
    }
}