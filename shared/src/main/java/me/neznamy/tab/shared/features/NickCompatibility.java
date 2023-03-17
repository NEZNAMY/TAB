package me.neznamy.tab.shared.features;

import java.util.Collections;
import java.util.Objects;

import lombok.Getter;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;

public class NickCompatibility extends TabFeature {

    @Getter private final String featureName = "Nick compatibility";
    private final NameTag nameTags = (NameTag) TAB.getInstance().getTeamManager();
    private final BelowName belowname = (BelowName) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BELOW_NAME);
    private final YellowNumber yellownumber = (YellowNumber) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.YELLOW_NUMBER);
    private final RedisSupport redis = (RedisSupport) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);

    @Override
    public void onPlayerInfo(TabPlayer receiver, PacketPlayOutPlayerInfo packet) {
        if (!packet.getActions().contains(EnumPlayerInfoAction.ADD_PLAYER)) return;
        for (PlayerInfoData data : packet.getEntries()) {
            TabPlayer packetPlayer = TAB.getInstance().getPlayerByTabListUUID(data.getUniqueId());
            if (packetPlayer != null && packetPlayer != receiver && !packetPlayer.getNickname().equals(data.getName())) {
                ((ITabPlayer)packetPlayer).setNickname(data.getName());
                TAB.getInstance().debug("Processing name change of player " + packetPlayer.getName() + " to " + data.getName());
                processNameChange(packetPlayer);
            }
            if (redis != null) {
                RedisPlayer redisPlayer = redis.getRedisPlayers().get(data.getUniqueId().toString());
                if (redisPlayer == null) continue;
                if (!redisPlayer.getNickname().equals(data.getName())) {
                    redisPlayer.setNickname(data.getName());
                    TAB.getInstance().debug("Processing name change of redis player " + redisPlayer.getName() + " to " + data.getName());
                    processNameChange(redisPlayer);
                }
            }
        }
    }

    private void processNameChange(TabPlayer player) {
        TAB.getInstance().getCPUManager().runMeasuredTask(this, TabConstants.CpuUsageCategory.PACKET_PLAYER_INFO, () -> {

            if (nameTags != null && !nameTags.hasTeamHandlingPaused(player)) {
                for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                    viewer.getScoreboard().unregisterTeam(nameTags.getSorting().getShortTeamName(player));
                    String replacedPrefix = player.getProperty(TabConstants.Property.TAGPREFIX).getFormat(viewer);
                    String replacedSuffix = player.getProperty(TabConstants.Property.TAGSUFFIX).getFormat(viewer);
                    viewer.getScoreboard().registerTeam(nameTags.getSorting().getShortTeamName(player), replacedPrefix, replacedSuffix, nameTags.translate(nameTags.getTeamVisibility(player, viewer)),
                            nameTags.translate(nameTags.getCollisionManager().getCollision(player)), Collections.singletonList(player.getNickname()), nameTags.getTeamOptions());
                }
            }
            if (belowname != null) {
                int value = belowname.getValue(player);
                for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                    if (all.getWorld().equals(player.getWorld()) && Objects.equals(all.getServer(), player.getServer())) {
                        all.getScoreboard().setScore(BelowName.OBJECTIVE_NAME, player.getNickname(), value);
                    }
                }
            }
            if (yellownumber != null) {
                int value = yellownumber.getValue(player);
                for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                    all.getScoreboard().setScore(YellowNumber.OBJECTIVE_NAME, player.getNickname(), value);
                }
            }
        });
    }

    private void processNameChange(RedisPlayer player) {
        TAB.getInstance().getCPUManager().runMeasuredTask(this, TabConstants.CpuUsageCategory.PACKET_PLAYER_INFO, () -> {

            if (nameTags != null) {
                for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                    viewer.getScoreboard().unregisterTeam(player.getTeamName());
                    viewer.getScoreboard().registerTeam(player.getTeamName(), player.getTagPrefix(), player.getTagSuffix(),
                            player.isNameVisibility() ? "always" : "never", "always", Collections.singletonList(player.getNickname()), 2);
                }
            }
            if (belowname != null) {
                for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                    if (Objects.equals(all.getServer(), player.getServer())) {
                        all.getScoreboard().setScore(BelowName.OBJECTIVE_NAME, player.getNickname(), player.getBelowName());
                    }
                }
            }
            if (yellownumber != null) {
                for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                    all.getScoreboard().setScore(YellowNumber.OBJECTIVE_NAME, player.getNickname(), player.getYellowNumber());
                }
            }
        });
    }
}