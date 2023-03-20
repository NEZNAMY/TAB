package me.neznamy.tab.shared.features;

import lombok.Getter;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.feature.EntryAddListener;
import me.neznamy.tab.api.feature.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public class NickCompatibility extends TabFeature implements EntryAddListener {

    @Getter private final String featureName = "Nick compatibility";
    private final NameTag nameTags = (NameTag) TAB.getInstance().getTeamManager();
    private final BelowName belowname = (BelowName) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BELOW_NAME);
    private final YellowNumber yellownumber = (YellowNumber) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.YELLOW_NUMBER);
    private final RedisSupport redis = (RedisSupport) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);

    @Override
    public void onEntryAdd(TabPlayer packetReceiver, UUID id, String name) {
        TabPlayer packetPlayer = TAB.getInstance().getPlayerByTabListUUID(id);
        if (packetPlayer != null && packetPlayer != packetReceiver && !packetPlayer.getNickname().equals(name)) {
            ((ITabPlayer)packetPlayer).setNickname(name);
            TAB.getInstance().debug("Processing name change of player " + packetPlayer.getName() + " to " + name);
            processNameChange(packetPlayer);
        }
        if (redis != null) {
            RedisPlayer redisPlayer = redis.getRedisPlayers().get(id.toString());
            if (redisPlayer == null) return;
            if (!redisPlayer.getNickname().equals(name)) {
                redisPlayer.setNickname(name);
                TAB.getInstance().debug("Processing name change of redis player " + redisPlayer.getName() + " to " + name);
                processNameChange(redisPlayer);
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