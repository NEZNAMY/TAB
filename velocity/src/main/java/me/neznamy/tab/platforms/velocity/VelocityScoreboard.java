package me.neznamy.tab.platforms.velocity;

import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.proxy.message.outgoing.SetDisplayObjective;
import me.neznamy.tab.shared.proxy.message.outgoing.SetObjective;
import me.neznamy.tab.shared.proxy.message.outgoing.SetScore;
import me.neznamy.tab.shared.proxy.message.outgoing.SetScoreboardTeam;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Scoreboard handler for Velocity. Because it does not have
 * any scoreboard API, we need to use bridge to send the packets.
 */
public class VelocityScoreboard extends Scoreboard<VelocityTabPlayer> {

    public VelocityScoreboard(@NotNull VelocityTabPlayer player) {
        super(player);
    }

    @Override
    public void setDisplaySlot(int slot, @NotNull String objective) {
        player.sendPluginMessage(new SetDisplayObjective(slot, objective));
    }

    @Override
    public void registerObjective0(@NotNull String objectiveName, @NotNull String title, int display,
                                   @Nullable IChatBaseComponent numberFormat) {
        player.sendPluginMessage(new SetObjective(objectiveName, ObjectiveAction.REGISTER, title, display,
                numberFormat == null ? null : numberFormat.toString(player.getVersion())));
    }

    @Override
    public void unregisterObjective0(@NotNull String objectiveName) {
        player.sendPluginMessage(new SetObjective(objectiveName));
    }

    @Override
    public void updateObjective0(@NotNull String objectiveName, @NotNull String title, int display,
                                 @Nullable IChatBaseComponent numberFormat) {
        player.sendPluginMessage(new SetObjective(objectiveName, ObjectiveAction.UPDATE, title, display,
                numberFormat == null ? null : numberFormat.toString(player.getVersion())));
    }

    @Override
    public void registerTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                              @NotNull NameVisibility visibility, @NotNull CollisionRule collision,
                              @NotNull Collection<String> players, int options) {
        player.sendPluginMessage(new SetScoreboardTeam(name, TeamAction.CREATE, prefix, suffix, options,
                visibility.toString(), collision.toString(), EnumChatFormat.lastColorsOf(prefix).ordinal(), players));
    }

    @Override
    public void unregisterTeam0(@NotNull String name) {
        player.sendPluginMessage(new SetScoreboardTeam(name));
    }

    @Override
    public void updateTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                            @NotNull NameVisibility visibility, @NotNull CollisionRule collision, int options) {
        player.sendPluginMessage(new SetScoreboardTeam(name, TeamAction.UPDATE, prefix, suffix, options,
                visibility.toString(), collision.toString(), EnumChatFormat.lastColorsOf(prefix).ordinal(), null));
    }

    @Override
    public void setScore0(@NotNull String objective, @NotNull String scoreHolder, int score,
                          @Nullable IChatBaseComponent displayName, @Nullable IChatBaseComponent numberFormat) {
        player.sendPluginMessage(new SetScore(
                objective, ScoreAction.CHANGE, scoreHolder, score,
                displayName == null ? null : displayName.toString(player.getVersion()),
                numberFormat == null ? null : numberFormat.toString(player.getVersion())
        ));
    }

    @Override
    public void removeScore0(@NotNull String objective, @NotNull String scoreHolder) {
        player.sendPluginMessage(new SetScore(objective, scoreHolder));
    }
}
