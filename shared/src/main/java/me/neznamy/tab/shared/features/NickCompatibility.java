package me.neznamy.tab.shared.features;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.redis.feature.RedisBelowName;
import me.neznamy.tab.shared.features.redis.feature.RedisTeams;
import me.neznamy.tab.shared.features.redis.feature.RedisYellowNumber;
import me.neznamy.tab.shared.features.types.EntryAddListener;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * This feature attempts to provide compatibility with nick/disguise plugins by
 * listening to player add packet and see if nickname is different. If it is, player
 * is considered nicked and all name-bound features will use this new nickname.
 */
public class NickCompatibility extends TabFeature implements EntryAddListener {

    @Getter private final String featureName = "Nick compatibility";

    @Nullable private final NameTag nameTags = (NameTag)TAB.getInstance().getNameTagManager();
    @Nullable private final BelowName belowname = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.BELOW_NAME);
    @Nullable private final YellowNumber yellownumber = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.YELLOW_NUMBER);
    @Nullable private final RedisSupport redis = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
    @Nullable private final RedisTeams redisTeams = redis == null ? null : redis.getRedisTeams();
    @Nullable private final RedisYellowNumber redisYellowNumber = redis == null ? null : redis.getRedisYellowNumber();
    @Nullable private final RedisBelowName redisBelowName = redis == null ? null : redis.getRedisBelowName();

    public synchronized void onEntryAdd(TabPlayer packetReceiver, UUID id, String name) {
        TabPlayer packetPlayer = TAB.getInstance().getPlayerByTabListUUID(id);
        // Using "packetPlayer == packetReceiver" for now, as this should technically not matter, but it does
        // A nick plugin author said the nickname will be different for other players but same for nicking player,
        //      but the plugin does not even do that and changes it for everyone.
        // Another plugin changes name only for nicked player, for others it changes it in pipeline, which is
        //      injected after TAB, so this cannot be read properly and would false trigger un-nick.
        // For some very specific complicated plugins this may need to be different, either changing == to !=
        //      or completely removing the check. However, only this option worked for both nick plugins I tested.
        if (packetPlayer != null && packetPlayer == packetReceiver && !packetPlayer.getNickname().equals(name)) {
            packetPlayer.setNickname(name);
            TAB.getInstance().debug("Processing name change of player " + packetPlayer.getName() + " to " + name);
            processNameChange(packetPlayer);
        }
        if (redis != null) {
            RedisPlayer redisPlayer = redis.getRedisPlayers().get(id);
            if (redisPlayer == null) return;
            if (!redisPlayer.getNickname().equals(name)) {
                redisPlayer.setNickname(name);
                TAB.getInstance().debug("Processing name change of redis player " + redisPlayer.getName() + " to " + name);
                processNameChange(redisPlayer);
            }
        }
    }

    private void processNameChange(TabPlayer player) {
        TAB.getInstance().getCPUManager().runMeasuredTask(getFeatureName(), TabConstants.CpuUsageCategory.PACKET_PLAYER_INFO, () -> {
            if (nameTags != null && !nameTags.hasTeamHandlingPaused(player))
                for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                    viewer.getScoreboard().unregisterTeam(nameTags.getSorting().getShortTeamName(player));
                    viewer.getScoreboard().registerTeam(
                            nameTags.getSorting().getShortTeamName(player),
                            player.getProperty(TabConstants.Property.TAGPREFIX).getFormat(viewer),
                            player.getProperty(TabConstants.Property.TAGSUFFIX).getFormat(viewer),
                            nameTags.getTeamVisibility(player, viewer) ? Scoreboard.NameVisibility.ALWAYS : Scoreboard.NameVisibility.NEVER,
                            nameTags.getCollisionManager().getCollision(player) ? Scoreboard.CollisionRule.ALWAYS : Scoreboard.CollisionRule.NEVER,
                            Collections.singletonList(player.getNickname()),
                            nameTags.getTeamOptions()
                    );
                }
            if (belowname != null) {
                int value = belowname.getValue(player);
                for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                    if (all.getWorld().equals(player.getWorld()) && Objects.equals(all.getServer(), player.getServer()))
                        all.getScoreboard().setScore(BelowName.OBJECTIVE_NAME, player.getNickname(), value);
                }
            }
            if (yellownumber != null) {
                int value = yellownumber.getValue(player);
                for (TabPlayer all : TAB.getInstance().getOnlinePlayers())
                    all.getScoreboard().setScore(YellowNumber.OBJECTIVE_NAME, player.getNickname(), value);
            }
        });
    }

    private void processNameChange(RedisPlayer player) {
        TAB.getInstance().getCPUManager().runMeasuredTask(getFeatureName(), TabConstants.CpuUsageCategory.PACKET_PLAYER_INFO, () -> {
            if (redisTeams != null) {
                String teamName = redisTeams.getTeamNames().get(player);
                for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                    viewer.getScoreboard().unregisterTeam(teamName);
                    viewer.getScoreboard().registerTeam(
                            teamName,
                            redisTeams.getPrefixes().get(player),
                            redisTeams.getSuffixes().get(player),
                            redisTeams.getNameVisibilities().get(player),
                            Scoreboard.CollisionRule.ALWAYS,
                            Collections.singletonList(player.getNickname()),
                            redisTeams.getNameTags().getTeamOptions()
                    );
                }
            }
            if (redisBelowName != null) {
                for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                    if (Objects.equals(all.getServer(), player.getServer()))
                        all.getScoreboard().setScore(
                                BelowName.OBJECTIVE_NAME,
                                player.getNickname(),
                                redisBelowName.getValues().get(player)
                        );
                }
            }
            if (redisYellowNumber != null) {
                for (TabPlayer all : TAB.getInstance().getOnlinePlayers())
                    all.getScoreboard().setScore(
                            YellowNumber.OBJECTIVE_NAME,
                            player.getNickname(),
                            redisYellowNumber.getValues().get(player)
                    );
            }
        });
    }
}
