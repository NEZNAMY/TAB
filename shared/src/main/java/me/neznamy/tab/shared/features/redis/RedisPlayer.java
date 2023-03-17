package me.neznamy.tab.shared.features.redis;

import lombok.Getter;
import lombok.Setter;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.api.protocol.Skin;
import me.neznamy.tab.shared.TAB;
import org.json.simple.JSONObject;

import java.util.Collections;
import java.util.UUID;

public class RedisPlayer {

    private RedisSupport redis;
    @Getter private boolean disabledPlayerList;
    private boolean disabledNameTags;

    @Getter private UUID uniqueId;
    @Getter private String name;
    @Getter @Setter private String nickname;
    @Getter private String server;
    @Getter @Setter private String tabFormat;
    @Getter private String teamName;
    @Getter private boolean vanished;
    private Skin skin;
    @Setter @Getter private String tagPrefix;
    @Setter @Getter private String tagSuffix;
    @Getter private boolean nameVisibility;
    @Setter private String belowName;
    @Setter private String yellowNumber;
    @Getter private boolean staff;

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
        json.put("UUID", p.getTablistId().toString());
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
        return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, new PlayerInfoData(nickname, uniqueId, skin, true, 0, EnumGamemode.SURVIVAL,
                disabledPlayerList ? null : IChatBaseComponent.optimizedComponent(tabFormat), null, null));
    }

    public PacketPlayOutPlayerInfo getUpdatePacket() {
        return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, new PlayerInfoData(uniqueId, disabledPlayerList ? null : IChatBaseComponent.optimizedComponent(tabFormat)));
    }

    public PacketPlayOutPlayerInfo getRemovePacket() {
        PlayerInfoData data = new PlayerInfoData(uniqueId);
        data.setName(nickname); //making null check not kill own packets
        return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, data);
    }

    public int getBelowName() {
        return TAB.getInstance().getErrorManager().parseInteger(belowName, 0);
    }

    public int getYellowNumber() {
        return TAB.getInstance().getErrorManager().parseInteger(yellowNumber, 0);
    }

    public void setServer(String server) {
        this.server = server;
        if (redis.getPlayerList() != null) {
            if (disabledPlayerList) {
                if (!redis.getPlayerList().isDisabled(server, null)) {
                    disabledPlayerList = false;
                    for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                        if (all.getVersion().getMinorVersion() < 8) continue;
                        all.sendCustomPacket(getUpdatePacket());
                    }
                }
            } else {
                if (redis.getPlayerList().isDisabled(server, null)) {
                    disabledPlayerList = true;
                    for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                        if (all.getVersion().getMinorVersion() < 8) continue;
                        all.sendCustomPacket(getUpdatePacket());
                    }
                }
            }
        }
        if (redis.getNameTags() != null) {
            if (disabledNameTags) {
                if (!redis.getNameTags().isDisabled(server, null)) {
                    disabledNameTags = false;
                    for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                        all.getScoreboard().registerTeam(teamName, tagPrefix, tagSuffix, nameVisibility ? "always" : "never",
                                "always", Collections.singletonList(nickname), 2);
                    }
                }
            } else {
                if (redis.getNameTags().isDisabled(server, null)) {
                    disabledNameTags = true;
                    for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                        all.getScoreboard().unregisterTeam(teamName);
                    }
                }
            }
        }
    }

    public void setTeamName(String teamName) {
        this.teamName = checkTeamName(redis, teamName.substring(0, teamName.length()-1), 65);
    }
}