package me.neznamy.tab.shared.features.redis.feature;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.redis.message.RedisMessage;
import me.neznamy.tab.shared.platform.Scoreboard.NameVisibility;
import me.neznamy.tab.shared.platform.Scoreboard.CollisionRule;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class RedisTeams extends RedisFeature {

    private final RedisSupport redisSupport;
    private final NameTag nameTags;
    private final Map<RedisPlayer, String> teamNames = new WeakHashMap<>();
    private final Map<RedisPlayer, String> prefixes = new WeakHashMap<>();
    private final Map<RedisPlayer, String> suffixes = new WeakHashMap<>();
    private final Map<RedisPlayer, NameVisibility> nameVisibilities = new WeakHashMap<>();

    public RedisTeams(@NotNull RedisSupport redisSupport, @NotNull NameTag nameTags) {
        this.redisSupport = redisSupport;
        this.nameTags = nameTags;
        redisSupport.registerMessage("teams", Update.class, Update::new);
    }

    @Override
    public void onJoin(@NotNull TabPlayer player) {
        for (RedisPlayer redis : redisSupport.getRedisPlayers().values()) {
            player.getScoreboard().registerTeam(teamNames.get(redis), prefixes.get(redis), suffixes.get(redis),
                        nameVisibilities.get(redis), CollisionRule.ALWAYS,
                        Collections.singletonList(redis.getNickname()), 2, EnumChatFormat.lastColorsOf(prefixes.get(redis)));
        }
    }

    @Override
    public void onJoin(@NotNull RedisPlayer player) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            viewer.getScoreboard().registerTeam(teamNames.get(player), prefixes.get(player), suffixes.get(player),
                    nameVisibilities.get(player), CollisionRule.ALWAYS,
                    Collections.singletonList(player.getNickname()), 2, EnumChatFormat.lastColorsOf(prefixes.get(player)));
        }
    }

    @Override
    public void onQuit(@NotNull RedisPlayer player) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            viewer.getScoreboard().unregisterTeam(teamNames.get(player));
        }
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out, @NotNull TabPlayer player) {
        out.writeUTF(nameTags.getSorting().getShortTeamName(player));
        out.writeUTF(player.getProperty(TabConstants.Property.TAGPREFIX).get());
        out.writeUTF(player.getProperty(TabConstants.Property.TAGSUFFIX).get());
        out.writeUTF((nameTags.getTeamVisibility(player, player) ? NameVisibility.ALWAYS : NameVisibility.NEVER).toString());
    }

    @Override
    public void read(@NotNull ByteArrayDataInput in, @NotNull RedisPlayer player) {
        String teamName = in.readUTF();
        teamName = checkTeamName(player, teamName.substring(0, teamName.length()-1), 65);
        teamNames.put(player, teamName);
        prefixes.put(player, in.readUTF());
        suffixes.put(player, in.readUTF());
        nameVisibilities.put(player, NameVisibility.getByName(in.readUTF()));
    }

    @Override
    public void onLoginPacket(@NotNull TabPlayer player) {
        onJoin(player);
    }

    private @NotNull String checkTeamName(@NotNull RedisPlayer player, @NotNull String currentName15, int id) {
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
        private NameVisibility nameVisibility;

        @Override
        public void write(@NotNull ByteArrayDataOutput out) {
            writeUUID(out, playerId);
            out.writeUTF(teamName);
            out.writeUTF(prefix);
            out.writeUTF(suffix);
            out.writeUTF(nameVisibility.toString());
        }

        @Override
        public void read(@NotNull ByteArrayDataInput in) {
            playerId = readUUID(in);
            teamName = in.readUTF();
            prefix = in.readUTF();
            suffix = in.readUTF();
            nameVisibility = NameVisibility.getByName(in.readUTF());
        }

        @Override
        public void process(@NotNull RedisSupport redisSupport) {
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
                            CollisionRule.ALWAYS, Collections.singletonList(target.getNickname()), 2, EnumChatFormat.lastColorsOf(prefix));
                }
            } else {
                for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                    viewer.getScoreboard().updateTeam(oldTeamName, prefix, suffix, nameVisibility,
                            CollisionRule.ALWAYS, 2, EnumChatFormat.lastColorsOf(prefix));
                }
            }
        }
    }
}
