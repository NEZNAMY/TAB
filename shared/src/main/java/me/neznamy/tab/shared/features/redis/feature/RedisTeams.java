package me.neznamy.tab.shared.features.redis.feature;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.redis.message.RedisMessage;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RedisTeams extends RedisFeature {

    private final RedisSupport redisSupport;
    private final NameTag nameTags;
    @Getter private final Map<RedisPlayer, String> teamNames = new WeakHashMap<>();
    @Getter private final Map<RedisPlayer, String> prefixes = new WeakHashMap<>();
    @Getter private final Map<RedisPlayer, String> suffixes = new WeakHashMap<>();
    @Getter private final Map<RedisPlayer, String> nameVisibilities = new WeakHashMap<>();
    @Getter private final Set<RedisPlayer> disabledNameTags = Collections.newSetFromMap(new WeakHashMap<>());

    public RedisTeams(@NonNull RedisSupport redisSupport, @NonNull NameTag nameTags) {
        this.redisSupport = redisSupport;
        this.nameTags = nameTags;
        redisSupport.registerMessage("teams", Update.class, Update::new);
    }

    @Override
    public void onJoin(@NonNull TabPlayer player) {
        for (RedisPlayer redis : redisSupport.getRedisPlayers().values()) {
            if (!disabledNameTags.contains(redis)) {
                player.getScoreboard().registerTeam(teamNames.get(redis), prefixes.get(redis), suffixes.get(redis),
                        nameVisibilities.get(redis), "always",
                        Collections.singletonList(redis.getNickname()), 2);
            }
        }
    }

    @Override
    public void onJoin(@NonNull RedisPlayer player) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            viewer.getScoreboard().registerTeam(teamNames.get(player), prefixes.get(player), suffixes.get(player),
                    nameVisibilities.get(player), "always",
                    Collections.singletonList(player.getNickname()), 2);
        }
    }

    @Override
    public void onServerSwitch(@NonNull TabPlayer player) {
        onJoin(player);
    }

    @Override
    public void onServerSwitch(@NonNull RedisPlayer player) {
        if (disabledNameTags.contains(player)) {
            if (!nameTags.isDisabled(player.getServer(), null)) {
                disabledNameTags.remove(player);
                for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                    all.getScoreboard().registerTeam(teamNames.get(player), prefixes.get(player), suffixes.get(player),
                            nameVisibilities.get(player),
                            "always", Collections.singletonList(player.getNickname()), 2);
                }
            }
        } else {
            if (nameTags.isDisabled(player.getServer(), null)) {
                disabledNameTags.add(player);
                for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                    all.getScoreboard().unregisterTeam(teamNames.get(player));
                }
            }
        }
    }

    @Override
    public void onQuit(@NonNull RedisPlayer player) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            viewer.getScoreboard().unregisterTeam(teamNames.get(player));
        }
    }

    @Override
    public void write(@NonNull ByteArrayDataOutput out, @NonNull TabPlayer player) {
        out.writeUTF(nameTags.getSorting().getShortTeamName(player));
        out.writeUTF(player.getProperty(TabConstants.Property.TAGPREFIX).get());
        out.writeUTF(player.getProperty(TabConstants.Property.TAGSUFFIX).get());
        out.writeUTF(nameTags.getTeamVisibility(player, player) ? "always" : "never");
    }

    @Override
    public void read(@NonNull ByteArrayDataInput in, @NonNull RedisPlayer player) {
        String teamName = in.readUTF();
        teamName = checkTeamName(player, teamName.substring(0, teamName.length()-1), 65);
        teamNames.put(player, teamName);
        prefixes.put(player, in.readUTF());
        suffixes.put(player, in.readUTF());
        nameVisibilities.put(player, in.readUTF());
    }

    private @NotNull String checkTeamName(@NonNull RedisPlayer player, @NonNull String currentName15, int id) {
        String potentialTeamName = currentName15 + (char)id;
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (nameTags.getSorting().getShortTeamName(all).equals(potentialTeamName)) {
                return checkTeamName(player, currentName15, id+1);
            }
        }
        for (RedisPlayer all : redisSupport.getRedisPlayers().values()) {
            if (all == player) continue;
            if (teamNames.containsKey(all) && teamNames.get(all).equals(potentialTeamName)) {
                return checkTeamName(player, currentName15, id+1);
            }
        }
        return potentialTeamName;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public class Update extends RedisMessage {

        private UUID playerId;
        private String teamName;
        private String prefix;
        private String suffix;
        private String nameVisibility;

        @Override
        public void write(@NonNull ByteArrayDataOutput out) {
            writeUUID(out, playerId);
            out.writeUTF(teamName);
            out.writeUTF(prefix);
            out.writeUTF(suffix);
            out.writeUTF(nameVisibility);
        }

        @Override
        public void read(@NonNull ByteArrayDataInput in) {
            playerId = readUUID(in);
            teamName = in.readUTF();
            prefix = in.readUTF();
            suffix = in.readUTF();
            nameVisibility = in.readUTF();
        }

        @Override
        public void process(@NonNull RedisSupport redisSupport) {
            RedisPlayer target = redisSupport.getRedisPlayers().get(playerId);
            if (target == null) return; // Print warn?
            String oldTeamName = teamNames.get(target);
            String newTeamName = checkTeamName(target, teamName.substring(0, teamName.length()-1), 65);
            teamNames.put(target, newTeamName);
            prefixes.put(target, prefix);
            suffixes.put(target, suffix);
            if (!oldTeamName.equals(newTeamName)) {
                for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                    viewer.getScoreboard().unregisterTeam(oldTeamName);
                    viewer.getScoreboard().registerTeam(newTeamName, prefix, suffix, nameVisibility,
                            "always", Collections.singletonList(target.getNickname()), 2);
                }
            } else {
                for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                    viewer.getScoreboard().updateTeam(oldTeamName, prefix, suffix, nameVisibility,
                            "always", 2);
                }
            }
        }
    }
}
