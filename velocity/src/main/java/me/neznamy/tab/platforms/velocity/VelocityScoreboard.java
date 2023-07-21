package me.neznamy.tab.platforms.velocity;

import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Scoreboard handler for Velocity. Because it does not have
 * any scoreboard API, we need to use bridge to send the packets.
 */
public class VelocityScoreboard extends Scoreboard<VelocityTabPlayer> {

    public VelocityScoreboard(@NotNull VelocityTabPlayer player) {
        super(player);
    }

    @Override
    public void setDisplaySlot(@NotNull DisplaySlot slot, @NotNull String objective) {
        player.sendPluginMessage("PacketPlayOutScoreboardDisplayObjective", slot.ordinal(), objective);
    }

    @Override
    public void registerObjective0(@NotNull String objectiveName, @NotNull String title, boolean hearts) {
        player.sendPluginMessage("PacketPlayOutScoreboardObjective", objectiveName, 0,
                title, IChatBaseComponent.optimizedComponent(title).toString(player.getVersion()), hearts ? 1 : 0);
    }

    @Override
    public void unregisterObjective0(@NotNull String objectiveName) {
        player.sendPluginMessage("PacketPlayOutScoreboardObjective", objectiveName, 1);
    }

    @Override
    public void updateObjective0(@NotNull String objectiveName, @NotNull String title, boolean hearts) {
        player.sendPluginMessage("PacketPlayOutScoreboardObjective", objectiveName, 2,
                title, IChatBaseComponent.optimizedComponent(title).toString(player.getVersion()), hearts ? 1 : 0);
    }

    @Override
    public void registerTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix, @NotNull NameVisibility visibility, @NotNull CollisionRule collision, @NotNull Collection<String> players, int options) {
        List<Object> args = new ArrayList<>();
        args.add("PacketPlayOutScoreboardTeam");
        args.add(name);
        args.add(0);
        args.add(players.size());
        args.addAll(players);
        args.add(prefix);
        args.add(IChatBaseComponent.optimizedComponent(prefix).toString(player.getVersion()));
        args.add(suffix);
        args.add(IChatBaseComponent.optimizedComponent(suffix).toString(player.getVersion()));
        args.add(options);
        args.add(visibility.toString());
        args.add(collision.toString());
        args.add(EnumChatFormat.lastColorsOf(prefix).ordinal());
        player.sendPluginMessage(args.toArray());
    }

    @Override
    public void unregisterTeam0(@NotNull String name) {
        player.sendPluginMessage("PacketPlayOutScoreboardTeam", name, 1, 0);
    }

    @Override
    public void updateTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix, @NotNull NameVisibility visibility, @NotNull CollisionRule collision, int options) {
        List<Object> args = new ArrayList<>();
        args.add("PacketPlayOutScoreboardTeam");
        args.add(name);
        args.add(2);
        args.add(0);
        args.add(prefix);
        args.add(IChatBaseComponent.optimizedComponent(prefix).toString(player.getVersion()));
        args.add(suffix);
        args.add(IChatBaseComponent.optimizedComponent(suffix).toString(player.getVersion()));
        args.add(options);
        args.add(visibility.toString());
        args.add(collision.toString());
        args.add(EnumChatFormat.lastColorsOf(prefix).ordinal());
        player.sendPluginMessage(args.toArray());
    }

    @Override
    public void setScore0(@NotNull String objective, @NotNull String playerName, int score) {
        player.sendPluginMessage("PacketPlayOutScoreboardScore", objective, 0, playerName, score);
    }

    @Override
    public void removeScore0(@NotNull String objective, @NotNull String playerName) {
        player.sendPluginMessage("PacketPlayOutScoreboardScore", objective, 1, playerName, 0);
    }
}
