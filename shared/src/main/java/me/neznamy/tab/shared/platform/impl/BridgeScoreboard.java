package me.neznamy.tab.shared.platform.impl;

import lombok.NonNull;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import me.neznamy.tab.shared.proxy.message.outgoing.SetDisplayObjective;
import me.neznamy.tab.shared.proxy.message.outgoing.SetObjective;
import me.neznamy.tab.shared.proxy.message.outgoing.SetScore;
import me.neznamy.tab.shared.proxy.message.outgoing.SetScoreboardTeam;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Scoreboard handler using bridge to encode the packets.
 */
public class BridgeScoreboard extends Scoreboard<ProxyTabPlayer> {

    /**
     * Constructs new instance.
     *
     * @param   player
     *          Player this scoreboard belongs to
     */
    public BridgeScoreboard(@NonNull ProxyTabPlayer player) {
        super(player);
    }

    @Override
    public void setDisplaySlot0(int slot, @NonNull String objective) {
        player.sendPluginMessage(new SetDisplayObjective(slot, objective));
    }

    @Override
    public void registerObjective0(@NonNull String objectiveName, @NonNull String title, int display,
                                   @Nullable TabComponent numberFormat) {
        player.sendPluginMessage(new SetObjective(objectiveName, ObjectiveAction.REGISTER, title, display,
                numberFormat == null ? null : numberFormat.toString(player.getVersion())));
    }

    @Override
    public void unregisterObjective0(@NonNull String objectiveName) {
        player.sendPluginMessage(new SetObjective(objectiveName));
    }

    @Override
    public void updateObjective0(@NonNull String objectiveName, @NonNull String title, int display,
                                 @Nullable TabComponent numberFormat) {
        player.sendPluginMessage(new SetObjective(objectiveName, ObjectiveAction.UPDATE, title, display,
                numberFormat == null ? null : numberFormat.toString(player.getVersion())));
    }

    @Override
    public void registerTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix,
                              @NonNull NameVisibility visibility, @NonNull CollisionRule collision,
                              @NonNull Collection<String> players, int options, @NonNull EnumChatFormat color) {
        player.sendPluginMessage(new SetScoreboardTeam(name, TeamAction.CREATE, prefix, suffix, options,
                visibility.toString(), collision.toString(), color.ordinal(), players));
    }

    @Override
    public void unregisterTeam0(@NonNull String name) {
        player.sendPluginMessage(new SetScoreboardTeam(name));
    }

    @Override
    public void updateTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix,
                            @NonNull NameVisibility visibility, @NonNull CollisionRule collision,
                            int options, @NonNull EnumChatFormat color) {
        player.sendPluginMessage(new SetScoreboardTeam(name, TeamAction.UPDATE, prefix, suffix, options,
                visibility.toString(), collision.toString(), color.ordinal(), null));
    }

    @Override
    public void setScore0(@NonNull String objective, @NonNull String scoreHolder, int score,
                          @Nullable TabComponent displayName, @Nullable TabComponent numberFormat) {
        player.sendPluginMessage(new SetScore(
                objective, ScoreAction.CHANGE, scoreHolder, score,
                displayName == null ? null : displayName.toString(player.getVersion()),
                numberFormat == null ? null : numberFormat.toString(player.getVersion())
        ));
    }

    @Override
    public void removeScore0(@NonNull String objective, @NonNull String scoreHolder) {
        player.sendPluginMessage(new SetScore(objective, scoreHolder));
    }
}
