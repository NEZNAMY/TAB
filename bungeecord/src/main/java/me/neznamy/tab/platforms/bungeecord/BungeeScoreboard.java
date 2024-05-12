package me.neznamy.tab.platforms.bungeecord;

import com.google.common.collect.Lists;
import lombok.NonNull;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.protocol.Either;
import net.md_5.bungee.protocol.NumberFormat;
import net.md_5.bungee.protocol.packet.*;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Scoreboard handler for BungeeCord. Because it does not offer
 * any Scoreboard API and the scoreboard class it has is just a
 * downstream tracker, we need to use packets.
 */
public class BungeeScoreboard extends Scoreboard<BungeeTabPlayer, BaseComponent> {

    /** Version with a minor team recode */
    private final int TEAM_REWORK_VERSION = 13;

    /**
     * Constructs new instance with given parameter
     *
     * @param   player
     *          Player this scoreboard will belong to
     */
    public BungeeScoreboard(@NonNull BungeeTabPlayer player) {
        super(player);
    }

    @Override
    public void setDisplaySlot0(int slot, @NonNull String objective) {
        player.sendPacket(new ScoreboardDisplay(slot, objective));
    }

    @Override
    public void registerObjective0(@NonNull String objectiveName, @NonNull String title, int display,
                                   @Nullable BaseComponent numberFormat) {
        player.sendPacket(new ScoreboardObjective(
                objectiveName,
                either(title),
                ScoreboardObjective.HealthDisplay.values()[display],
                (byte) ObjectiveAction.REGISTER,
                numberFormat == null ? null : new NumberFormat(NumberFormat.Type.FIXED, numberFormat)
        ));
    }

    @Override
    public void unregisterObjective0(@NonNull String objectiveName) {
        player.sendPacket(new ScoreboardObjective(
                objectiveName,
                either(""), // Empty value instead of null to prevent NPE kick on 1.7
                null,
                (byte) ObjectiveAction.UNREGISTER,
                null
        ));
    }

    @Override
    public void updateObjective0(@NonNull String objectiveName, @NonNull String title, int display,
                                 @Nullable BaseComponent numberFormat) {
        player.sendPacket(new ScoreboardObjective(
                objectiveName,
                either(title),
                ScoreboardObjective.HealthDisplay.values()[display],
                (byte) ObjectiveAction.UPDATE,
                numberFormat == null ? null : new NumberFormat(NumberFormat.Type.FIXED, numberFormat)
        ));
    }

    @Override
    public void registerTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix,
                              @NonNull NameVisibility visibility, @NonNull CollisionRule collision,
                              @NonNull Collection<String> players, int options, @NonNull EnumChatFormat color) {
        player.sendPacket(new Team(
                name,
                (byte) TeamAction.CREATE,
                either(name),
                either(prefix),
                either(suffix),
                visibility.toString(),
                collision.toString(),
                player.getVersion().getMinorVersion() >= TEAM_REWORK_VERSION ? color.ordinal() : 0,
                (byte)options,
                players.toArray(new String[0])
        ));
    }

    @Override
    public void unregisterTeam0(@NonNull String name) {
        player.sendPacket(new Team(name));
    }

    @Override
    public void updateTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix,
                            @NonNull NameVisibility visibility, @NonNull CollisionRule collision,
                            int options, @NonNull EnumChatFormat color) {
        player.sendPacket(new Team(
                name,
                (byte) TeamAction.UPDATE,
                either(name),
                either(prefix),
                either(suffix),
                visibility.toString(),
                collision.toString(),
                player.getVersion().getMinorVersion() >= TEAM_REWORK_VERSION ? color.ordinal() : 0,
                (byte)options,
                null
        ));
    }

    @Override
    public void setScore0(@NonNull String objective, @NonNull String scoreHolder, int score,
                          @Nullable BaseComponent displayName, @Nullable BaseComponent numberFormat) {
        player.sendPacket(new ScoreboardScore(
                scoreHolder,
                (byte) ScoreAction.CHANGE,
                objective,
                score,
                displayName,
                numberFormat == null ? null : new NumberFormat(NumberFormat.Type.FIXED, numberFormat)
        ));
    }

    @Override
    public void removeScore0(@NonNull String objective, @NonNull String scoreHolder) {
        if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId()) {
            player.sendPacket(new ScoreboardScoreReset(scoreHolder, objective));
        } else {
            player.sendPacket(new ScoreboardScore(scoreHolder, (byte) ScoreAction.REMOVE, objective, 0, null, null));
        }
    }

    private Either<String, BaseComponent> either(@NonNull String text) {
        if (player.getVersion().getMinorVersion() >= TEAM_REWORK_VERSION) {
            return Either.right(TabComponent.optimized(text).convert(player.getVersion()));
        } else {
            return Either.left(text);
        }
    }

    @Override
    public void onPacketSend(@NonNull Object packet) {
        if (isAntiOverrideScoreboard()) {
            if (packet instanceof ScoreboardDisplay) {
                ScoreboardDisplay display = (ScoreboardDisplay) packet;
                TAB.getInstance().getFeatureManager().onDisplayObjective(player, display.getPosition(), display.getName());
            }
            if (packet instanceof ScoreboardObjective) {
                ScoreboardObjective objective = (ScoreboardObjective) packet;
                TAB.getInstance().getFeatureManager().onObjective(player, objective.getAction(), objective.getName());
            }
        }
        if (isAntiOverrideTeams() && packet instanceof Team) {
            Team team = (Team) packet;
            if (team.getMode() == TeamAction.REMOVE || team.getMode() == TeamAction.UPDATE) return;
            team.setPlayers(onTeamPacket(team.getMode(), team.getName(), Lists.newArrayList(team.getPlayers())).toArray(new String[0]));
        }
    }
}
