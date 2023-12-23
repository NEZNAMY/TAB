package me.neznamy.tab.platforms.bungeecord;

import com.google.common.collect.Lists;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.redis.feature.RedisTeams;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.protocol.Either;
import net.md_5.bungee.protocol.NumberFormat;
import net.md_5.bungee.protocol.packet.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Scoreboard handler for BungeeCord. Because it does not offer
 * any Scoreboard API and the scoreboard class it has is just a
 * downstream tracker, we need to use packets.
 */
public class BungeeScoreboard extends Scoreboard<BungeeTabPlayer> {

    /**
     * Constructs new instance with given parameter
     *
     * @param   player
     *          Player this scoreboard will belong to
     */
    public BungeeScoreboard(@NotNull BungeeTabPlayer player) {
        super(player);
    }

    @Override
    public void setDisplaySlot0(int slot, @NotNull String objective) {
        player.sendPacket(new ScoreboardDisplay(slot, objective));
    }

    @Override
    public void registerObjective0(@NotNull String objectiveName, @NotNull String title, int display,
                                   @Nullable IChatBaseComponent numberFormat) {
        player.sendPacket(new ScoreboardObjective(
                objectiveName,
                either(title),
                ScoreboardObjective.HealthDisplay.values()[display],
                (byte) ObjectiveAction.REGISTER,
                numberFormat == null ? null : new NumberFormat(NumberFormat.Type.FIXED,
                        player.getPlatform().toComponent(numberFormat, player.getVersion()))
        ));
    }

    @Override
    public void unregisterObjective0(@NotNull String objectiveName) {
        player.sendPacket(new ScoreboardObjective(
                objectiveName,
                either(""), // Empty value instead of null to prevent NPE kick on 1.7
                null,
                (byte) ObjectiveAction.UNREGISTER,
                null
        ));
    }

    @Override
    public void updateObjective0(@NotNull String objectiveName, @NotNull String title, int display,
                                 @Nullable IChatBaseComponent numberFormat) {
        player.sendPacket(new ScoreboardObjective(
                objectiveName,
                either(title),
                ScoreboardObjective.HealthDisplay.values()[display],
                (byte) ObjectiveAction.UPDATE,
                numberFormat == null ? null : new NumberFormat(NumberFormat.Type.FIXED,
                        player.getPlatform().toComponent(numberFormat, player.getVersion()))
        ));
    }

    @Override
    public void registerTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                              @NotNull NameVisibility visibility, @NotNull CollisionRule collision,
                              @NotNull Collection<String> players, int options) {
        int color = 0;
        if (player.getVersion().getMinorVersion() >= 13) {
            color = EnumChatFormat.lastColorsOf(prefix).ordinal();
        }
        player.sendPacket(new Team(
                name,
                (byte) TeamAction.CREATE,
                either(name),
                either(prefix),
                either(suffix),
                visibility.toString(),
                collision.toString(),
                color,
                (byte)options,
                players.toArray(new String[0])
        ));
    }

    @Override
    public void unregisterTeam0(@NotNull String name) {
        player.sendPacket(new Team(name));
    }

    @Override
    public void updateTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                            @NotNull NameVisibility visibility, @NotNull CollisionRule collision, int options) {
        int color = 0;
        if (player.getVersion().getMinorVersion() >= 13) {
            color = EnumChatFormat.lastColorsOf(prefix).ordinal();
        }
        player.sendPacket(new Team(
                name,
                (byte) TeamAction.UPDATE,
                either(name),
                either(prefix),
                either(suffix),
                visibility.toString(),
                collision.toString(),
                color,
                (byte)options,
                null
        ));
    }

    @Override
    public void setScore0(@NotNull String objective, @NotNull String scoreHolder, int score,
                          @Nullable IChatBaseComponent displayName, @Nullable IChatBaseComponent numberFormat) {
        player.sendPacket(new ScoreboardScore(
                scoreHolder,
                (byte) ScoreAction.CHANGE,
                objective,
                score,
                displayName == null ? null : player.getPlatform().toComponent(displayName, player.getVersion()),
                numberFormat == null ? null : new NumberFormat(NumberFormat.Type.FIXED,
                        player.getPlatform().toComponent(numberFormat, player.getVersion()))
        ));
    }

    @Override
    public void removeScore0(@NotNull String objective, @NotNull String scoreHolder) {
        if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId()) {
            player.sendPacket(new ScoreboardScoreReset(scoreHolder, objective));
        } else {
            player.sendPacket(new ScoreboardScore(scoreHolder, (byte) ScoreAction.REMOVE, objective, 0, null, null));
        }
    }

    private Either<String, BaseComponent> either(@NotNull String text) {
        if (player.getVersion().getMinorVersion() >= 13) {
            return Either.right(player.getPlatform().toComponent(IChatBaseComponent.optimizedComponent(text), player.getVersion()));
        } else {
            return Either.left(text);
        }
    }

    @Override
    public boolean isTeamPacket(@NotNull Object packet) {
        return packet instanceof Team;
    }

    @Override
    public void onTeamPacket(@NotNull Object team) {
        if (TAB.getInstance().getNameTagManager() == null) return;
        Team packet = (Team) team;
        if (packet.getMode() == 1 || packet.getMode() == 2 || packet.getMode() == 4) return;
        Collection<String> col = Lists.newArrayList(packet.getPlayers());
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            Sorting sorting = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SORTING);
            String expectedTeam = sorting.getShortTeamName(p);
            if (expectedTeam != null && (col.contains(p.getNickname()) || col.contains(p.getName())) &&
                    !((NameTag)TAB.getInstance().getNameTagManager()).getDisableChecker().isDisabledPlayer(p) &&
                    !TAB.getInstance().getNameTagManager().hasTeamHandlingPaused(p) && !packet.getName().equals(expectedTeam)) {
                logTeamOverride(packet.getName(), p.getName(), expectedTeam);
                col.remove(p.getNickname());
                col.remove(p.getName());
            }
        }
        RedisSupport redis = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
        if (redis != null) {
            RedisTeams teams = redis.getRedisTeams();
            if (teams != null) {
                for (RedisPlayer p : redis.getRedisPlayers().values()) {
                    if (col.contains(p.getNickname()) && !packet.getName().equals(teams.getTeamNames().get(p))) {
                        logTeamOverride(packet.getName(), p.getNickname(), teams.getTeamNames().get(p));
                        col.remove(p.getNickname());
                    }
                }
            }
        }
        packet.setPlayers(col.toArray(new String[0]));
    }
}
