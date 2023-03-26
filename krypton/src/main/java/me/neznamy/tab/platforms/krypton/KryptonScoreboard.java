package me.neznamy.tab.platforms.krypton;

import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.shared.TabScoreboard;
import net.kyori.adventure.text.Component;
import org.kryptonmc.krypton.packet.out.play.PacketOutDisplayObjective;
import org.kryptonmc.krypton.packet.out.play.PacketOutUpdateObjectives;
import org.kryptonmc.krypton.packet.out.play.PacketOutUpdateScore;
import org.kryptonmc.krypton.packet.out.play.PacketOutUpdateTeams;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class KryptonScoreboard extends TabScoreboard {

    public KryptonScoreboard(@NonNull TabPlayer player) {
        super(player);
    }

    @Override
    public void setDisplaySlot(@NonNull DisplaySlot slot, @NonNull String objective) {
        player.sendPacket(new PacketOutDisplayObjective(slot.ordinal(), objective));
    }

    @Override
    public void registerObjective0(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        player.sendPacket(new PacketOutUpdateObjectives(objectiveName, (byte)0, Main.toComponent(title, player.getVersion()),
                hearts ? 1 : 0));
    }

    @Override
    public void unregisterObjective0(@NonNull String objectiveName) {
        player.sendPacket(new PacketOutUpdateObjectives(objectiveName, (byte)1, Component.empty(), -1));
    }

    @Override
    public void updateObjective0(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        player.sendPacket(new PacketOutUpdateObjectives(objectiveName, (byte)2, Main.toComponent(title, player.getVersion()),
                hearts ? 1 : 0));
    }

    @Override
    public void registerTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix, @NonNull String visibility, @NonNull String collision, @NonNull Collection<String> players, int options) {
        player.sendPacket(
                new PacketOutUpdateTeams(name, PacketOutUpdateTeams.Action.CREATE,
                        createParameters(name, prefix, suffix, visibility, collision, options), players.stream().map(Component::text).collect(Collectors.toList()))
        );
    }

    @Override
    public void unregisterTeam0(@NonNull String name) {
        player.sendPacket(new PacketOutUpdateTeams(name, PacketOutUpdateTeams.Action.REMOVE, null, Collections.emptyList()));
    }

    @Override
    public void updateTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix, @NonNull String visibility, @NonNull String collision, int options) {
        player.sendPacket(new PacketOutUpdateTeams(name, PacketOutUpdateTeams.Action.UPDATE_INFO, createParameters(name, prefix, suffix, visibility, collision, options), Collections.emptyList()));
    }

    private PacketOutUpdateTeams.Parameters createParameters(String name, String prefix, String suffix, String visibility, String collision, int options) {
        return new PacketOutUpdateTeams.Parameters(Component.text(name), (byte)options, visibility, collision,
                EnumChatFormat.lastColorsOf(prefix).ordinal(), Main.toComponent(prefix, player.getVersion()),
                Main.toComponent(suffix, player.getVersion()));
    }

    @Override
    public void setScore0(@NonNull String objective, @NonNull String playerName, int score) {
        player.sendPacket(new PacketOutUpdateScore(playerName, 0, objective, score));
    }

    @Override
    public void removeScore0(@NonNull String objective, @NonNull String playerName) {
        player.sendPacket(new PacketOutUpdateScore(playerName, 1, objective, 0));
    }
}