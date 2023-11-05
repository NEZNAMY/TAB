package me.neznamy.tab.platforms.bungeecord;

import lombok.SneakyThrows;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.platform.Scoreboard;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardScore;
import net.md_5.bungee.protocol.packet.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Scoreboard handler for BungeeCord. Because it does not offer
 * any Scoreboard API and the scoreboard class it has is just a
 * downstream tracker, we need to use packets.
 */
public class BungeeScoreboard extends Scoreboard<BungeeTabPlayer> {

    public BungeeScoreboard(@NotNull BungeeTabPlayer player) {
        super(player);
    }

    @Override
    @SneakyThrows
    public void setDisplaySlot(@NotNull DisplaySlot slot, @NotNull String objective) {
        player.sendPacket(BungeeMultiVersion.newScoreboardDisplay(slot.ordinal(), objective));
    }

    @Override
    public void registerObjective0(@NotNull String objectiveName, @NotNull String title, @NotNull HealthDisplay display) {
        player.sendPacket(BungeeMultiVersion.newScoreboardObjective(
                objectiveName,
                title,
                ScoreboardObjective.HealthDisplay.valueOf(display.name()),
                ObjectiveAction.REGISTER,
                player.getVersion()
        ));
    }

    @Override
    public void unregisterObjective0(@NotNull String objectiveName) {
        player.sendPacket(BungeeMultiVersion.newScoreboardObjective(
                objectiveName,
                "", // Empty value instead of null to prevent NPE kick on 1.7
                null,
                ObjectiveAction.UNREGISTER,
                player.getVersion()
        ));
    }

    @Override
    public void updateObjective0(@NotNull String objectiveName, @NotNull String title, @NotNull HealthDisplay display) {
        player.sendPacket(BungeeMultiVersion.newScoreboardObjective(
                objectiveName,
                title,
                ScoreboardObjective.HealthDisplay.valueOf(display.name()),
                ObjectiveAction.UPDATE,
                player.getVersion()
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
        player.sendPacket(BungeeMultiVersion.newTeam(
                name,
                TeamAction.CREATE,
                prefix,
                suffix,
                visibility.toString(),
                collision.toString(),
                color,
                (byte)options,
                players.toArray(new String[0]),
                player.getVersion()
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
        player.sendPacket(BungeeMultiVersion.newTeam(
                name,
                TeamAction.UPDATE,
                prefix,
                suffix,
                visibility.toString(),
                collision.toString(),
                color,
                (byte)options,
                null,
                player.getVersion()
        ));
    }

    @Override
    public void setScore0(@NotNull String objective, @NotNull String playerName, int score) {
        player.sendPacket(new ScoreboardScore(playerName, (byte) ScoreAction.CHANGE.ordinal(), objective, score));
    }

    @Override
    public void removeScore0(@NotNull String objective, @NotNull String playerName) {
        player.sendPacket(new ScoreboardScore(playerName, (byte) ScoreAction.REMOVE.ordinal(), objective, 0));
    }
}
