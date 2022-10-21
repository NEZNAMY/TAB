package me.neznamy.tab.shared.features;

import java.util.Collections;
import java.util.Objects;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore.Action;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;

public class NickCompatibility extends TabFeature {

    private final NameTag nameTags = (NameTag) TAB.getInstance().getTeamManager();
    private final BelowName belowname = (BelowName) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BELOW_NAME);
    private final YellowNumber yellownumber = (YellowNumber) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.YELLOW_NUMBER);
    private RedisSupport redis;

    public NickCompatibility() {
        super("Nick compatibility", null);
        TAB.getInstance().debug("Loaded NickCompatibility feature");
    }

    @Override
    public void load() {
        // redis feature is instantiated after nick compatibility
        redis = (RedisSupport) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
    }

    @Override
    public void onPlayerInfo(TabPlayer receiver, PacketPlayOutPlayerInfo packet) {
        if (packet.getAction() != EnumPlayerInfoAction.ADD_PLAYER) return;
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
                if (!redisPlayer.getNickName().equals(data.getName())) {
                    redisPlayer.setNickName(data.getName());
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
                    viewer.sendCustomPacket(new PacketPlayOutScoreboardTeam(nameTags.getSorting().getShortTeamName(player)), this);
                    String replacedPrefix = player.getProperty(TabConstants.Property.TAGPREFIX).getFormat(viewer);
                    String replacedSuffix = player.getProperty(TabConstants.Property.TAGSUFFIX).getFormat(viewer);
                    viewer.sendCustomPacket(new PacketPlayOutScoreboardTeam(nameTags.getSorting().getShortTeamName(player), replacedPrefix, replacedSuffix, nameTags.translate(nameTags.getTeamVisibility(player, viewer)),
                            nameTags.translate(nameTags.getCollisionManager().getCollision(player)), Collections.singletonList(player.getNickname()), nameTags.getTeamOptions()), this);
                }
            }
            if (belowname != null) {
                int value = belowname.getValue(player);
                for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                    if (all.getWorld().equals(player.getWorld()) && Objects.equals(all.getServer(), player.getServer()))
                        all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, BelowName.OBJECTIVE_NAME, player.getNickname(), value), this);
                }
            }
            if (yellownumber != null) {
                int value = yellownumber.getValue(player);
                for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                    all.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, YellowNumber.OBJECTIVE_NAME, player.getNickname(), value), this);
                }
            }
        });
    }

    private void processNameChange(RedisPlayer player) {
        TAB.getInstance().getCPUManager().runMeasuredTask(this, TabConstants.CpuUsageCategory.PACKET_PLAYER_INFO, () -> {

            if (nameTags != null) {
                PacketPlayOutScoreboardTeam unregister = player.getUnregisterTeamPacket();
                PacketPlayOutScoreboardTeam register = player.getRegisterTeamPacket();
                for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                    viewer.sendCustomPacket(unregister, this);
                    viewer.sendCustomPacket(register, this);
                }
            }
            if (belowname != null) {
                PacketPlayOutScoreboardScore packet = player.getBelowNameUpdatePacket();
                for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                    if (Objects.equals(all.getServer(), player.getServer()))
                        all.sendCustomPacket(packet, this);
                }
            }
            if (yellownumber != null) {
                PacketPlayOutScoreboardScore packet = player.getYellowNumberUpdatePacket();
                for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                    all.sendCustomPacket(packet, this);
                }
            }
        });
    }
}