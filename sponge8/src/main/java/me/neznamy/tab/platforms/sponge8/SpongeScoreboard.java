package me.neznamy.tab.platforms.sponge8;

import lombok.NonNull;
import me.neznamy.tab.api.DisplaySlot;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.util.ComponentCache;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabScoreboard;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.Collection;
import java.util.Locale;

public class SpongeScoreboard extends TabScoreboard {

    private static final Scoreboard dummyScoreboard = new Scoreboard();

    private static final ComponentCache<IChatBaseComponent, Component> componentCache = new ComponentCache<>(10000,
            (component, clientVersion) -> net.minecraft.network.chat.Component.Serializer.fromJson(component.toString(clientVersion)));

    public SpongeScoreboard(TabPlayer player) {
        super(player);
    }

    @Override
    public void setDisplaySlot(DisplaySlot slot, @NonNull String objective) {
        player.sendPacket(new ClientboundSetDisplayObjectivePacket(slot.ordinal(),
                new Objective(new Scoreboard(), objective, null, TextComponent.EMPTY, null)));
    }

    @Override
    public void registerObjective0(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        String displayName = player.getVersion().getMinorVersion() < 13 ? TAB.getInstance().getPlatform().getPacketBuilder().cutTo(title, 32) : title;
        player.sendPacket(new ClientboundSetObjectivePacket(
                new Objective(
                        dummyScoreboard,
                        objectiveName,
                        null,
                        componentCache.get(IChatBaseComponent.optimizedComponent(displayName), player.getVersion()),
                        hearts ? ObjectiveCriteria.RenderType.HEARTS : ObjectiveCriteria.RenderType.INTEGER
                ), 0
        ));
    }

    @Override
    public void unregisterObjective0(@NonNull String objectiveName) {
        player.sendPacket(new ClientboundSetObjectivePacket(new Objective(dummyScoreboard, objectiveName, null, null, null), 1));
    }

    @Override
    public void updateObjective0(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        String displayName = player.getVersion().getMinorVersion() < 13 ? TAB.getInstance().getPlatform().getPacketBuilder().cutTo(title, 32) : title;
        player.sendPacket(new ClientboundSetObjectivePacket(
                new Objective(
                        dummyScoreboard,
                        objectiveName,
                        null,
                        componentCache.get(IChatBaseComponent.optimizedComponent(displayName), player.getVersion()),
                        hearts ? ObjectiveCriteria.RenderType.HEARTS : ObjectiveCriteria.RenderType.INTEGER
                ), 2
        ));
    }

    @Override
    public void registerTeam0(@NonNull String name, String prefix, String suffix, String visibility, String collision, Collection<String> players, int options) {
        PlayerTeam team = new PlayerTeam(dummyScoreboard, name);
        team.setAllowFriendlyFire((options & 0x01) > 0);
        team.setSeeFriendlyInvisibles((options & 0x02) > 0);
        team.setColor(ChatFormatting.valueOf(EnumChatFormat.lastColorsOf(prefix).name()));
        String finalPrefix = prefix;
        String finalSuffix = suffix;
        if (player.getVersion().getMinorVersion() < 13) {
            finalPrefix = TAB.getInstance().getPlatform().getPacketBuilder().cutTo(finalPrefix, 16);
            finalSuffix = TAB.getInstance().getPlatform().getPacketBuilder().cutTo(finalSuffix, 16);
        }
        if (collision != null)
            team.setCollisionRule(Team.CollisionRule.valueOf(collision.toUpperCase(Locale.US)));
        if (visibility != null)
            team.setNameTagVisibility(Team.Visibility.valueOf(visibility.toUpperCase(Locale.US)));
        if (finalPrefix != null)
            team.setPlayerPrefix(componentCache.get(IChatBaseComponent.optimizedComponent(finalPrefix), player.getVersion()));
        if (finalSuffix != null)
            team.setPlayerSuffix(componentCache.get(IChatBaseComponent.optimizedComponent(finalSuffix), player.getVersion()));
        team.getPlayers().addAll(players);
        player.sendPacket(new ClientboundSetPlayerTeamPacket(team, 0));
    }

    @Override
    public void unregisterTeam0(@NonNull String name) {
        player.sendPacket(new ClientboundSetPlayerTeamPacket(new PlayerTeam(dummyScoreboard, name), 1));
    }

    @Override
    public void updateTeam0(@NonNull String name, String prefix, String suffix, String visibility, String collision, int options) {
        PlayerTeam team = new PlayerTeam(dummyScoreboard, name);
        team.setAllowFriendlyFire((options & 0x01) > 0);
        team.setSeeFriendlyInvisibles((options & 0x02) > 0);
        team.setColor(ChatFormatting.valueOf(EnumChatFormat.lastColorsOf(prefix).name()));
        String finalPrefix = prefix;
        String finalSuffix = suffix;
        if (player.getVersion().getMinorVersion() < 13) {
            finalPrefix = TAB.getInstance().getPlatform().getPacketBuilder().cutTo(finalPrefix, 16);
            finalSuffix = TAB.getInstance().getPlatform().getPacketBuilder().cutTo(finalSuffix, 16);
        }
        if (collision != null)
            team.setCollisionRule(Team.CollisionRule.valueOf(collision.toUpperCase(Locale.US)));
        if (visibility != null)
            team.setNameTagVisibility(Team.Visibility.valueOf(visibility.toUpperCase(Locale.US)));
        if (finalPrefix != null)
            team.setPlayerPrefix(componentCache.get(IChatBaseComponent.optimizedComponent(finalPrefix), player.getVersion()));
        if (finalSuffix != null)
            team.setPlayerSuffix(componentCache.get(IChatBaseComponent.optimizedComponent(finalSuffix), player.getVersion()));
        player.sendPacket(new ClientboundSetPlayerTeamPacket(team, 2));
    }

    @Override
    public void setScore0(@NonNull String objective, @NonNull String playerName, int score) {
        player.sendPacket(new ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, objective, playerName, score));
    }

    @Override
    public void removeScore0(@NonNull String objective, @NonNull String playerName) {
        player.sendPacket(new ClientboundSetScorePacket(ServerScoreboard.Method.REMOVE, objective, playerName, 0));
    }
}
