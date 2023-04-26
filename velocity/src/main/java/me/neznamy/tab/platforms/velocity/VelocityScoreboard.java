package me.neznamy.tab.platforms.velocity;

import lombok.NonNull;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.PlatformScoreboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Scoreboard handler for Velocity. Because it does not have
 * any scoreboard API, we need to use bridge to send the packets.
 */
public class VelocityScoreboard extends PlatformScoreboard<VelocityTabPlayer> {

    public VelocityScoreboard(VelocityTabPlayer player) {
        super(player);
    }

    @Override
    public void setDisplaySlot(@NonNull DisplaySlot slot, @NonNull String objective) {
        player.sendPluginMessage("PacketPlayOutScoreboardDisplayObjective", slot.ordinal(), objective);
    }

    @Override
    public void registerObjective0(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        player.sendPluginMessage("PacketPlayOutScoreboardObjective", objectiveName, 0,
                title, IChatBaseComponent.optimizedComponent(title).toString(player.getVersion()), hearts ? 1 : 0);
    }

    @Override
    public void unregisterObjective0(@NonNull String objectiveName) {
        player.sendPluginMessage("PacketPlayOutScoreboardObjective", objectiveName, 1);
    }

    @Override
    public void updateObjective0(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        player.sendPluginMessage("PacketPlayOutScoreboardObjective", objectiveName, 2,
                title, IChatBaseComponent.optimizedComponent(title).toString(player.getVersion()), hearts ? 1 : 0);
    }

    @Override
    public void registerTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix, @NonNull String visibility, @NonNull String collision, @NonNull Collection<String> players, int options) {
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
        args.add(visibility);
        args.add(collision);
        args.add(EnumChatFormat.lastColorsOf(prefix).ordinal());
        player.sendPluginMessage(args.toArray());
    }

    @Override
    public void unregisterTeam0(@NonNull String name) {
        player.sendPluginMessage("PacketPlayOutScoreboardTeam", name, 1, 0);
    }

    @Override
    public void updateTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix, @NonNull String visibility, @NonNull String collision, int options) {
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
        args.add(visibility);
        args.add(collision);
        args.add(EnumChatFormat.lastColorsOf(prefix).ordinal());
        player.sendPluginMessage(args.toArray());
    }

    @Override
    public void setScore0(@NonNull String objective, @NonNull String playerName, int score) {
        player.sendPluginMessage("PacketPlayOutScoreboardScore", objective, 0, playerName, score);
    }

    @Override
    public void removeScore0(@NonNull String objective, @NonNull String playerName) {
        player.sendPluginMessage("PacketPlayOutScoreboardScore", objective, 1, playerName, 0);
    }
}
