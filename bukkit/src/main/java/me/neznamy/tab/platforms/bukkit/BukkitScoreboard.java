package me.neznamy.tab.platforms.bukkit;

import lombok.NonNull;
import me.neznamy.tab.api.DisplaySlot;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutScoreboardDisplayObjectiveStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutScoreboardObjectiveStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutScoreboardScoreStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutScoreboardTeamStorage;
import me.neznamy.tab.shared.TabScoreboard;

import java.util.Collection;

public class BukkitScoreboard extends TabScoreboard {

    public BukkitScoreboard(TabPlayer player) {
        super(player);
    }

    @Override
    public void setDisplaySlot(DisplaySlot slot, @NonNull String objective) {
        player.sendPacket(PacketPlayOutScoreboardDisplayObjectiveStorage.buildSilent(slot.ordinal(), objective));
    }

    @Override
    public void registerObjective0(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        if (player.getVersion().getMinorVersion() < 13) objectiveName = cutTo(objectiveName, 32);
        player.sendPacket(PacketPlayOutScoreboardObjectiveStorage.buildSilent(0, objectiveName, title, hearts, player.getVersion()));
    }

    @Override
    public void unregisterObjective0(@NonNull String objectiveName) {
        player.sendPacket(PacketPlayOutScoreboardObjectiveStorage.buildSilent(1, objectiveName, "", false, player.getVersion()));
    }

    @Override
    public void updateObjective0(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        if (player.getVersion().getMinorVersion() < 13) objectiveName = cutTo(objectiveName, 32);
        player.sendPacket(PacketPlayOutScoreboardObjectiveStorage.buildSilent(2, objectiveName, title, hearts, player.getVersion()));
    }

    @Override
    public void registerTeam0(@NonNull String name, String prefix, String suffix, String visibility, String collision, Collection<String> players, int options) {
        if (player.getVersion().getMinorVersion() < 13) {
            prefix = cutTo(prefix, 16);
            suffix = cutTo(suffix, 16);
        }
        player.sendPacket(PacketPlayOutScoreboardTeamStorage.register(name, prefix, suffix, visibility, collision, players, options, player.getVersion()));
    }

    @Override
    public void unregisterTeam0(@NonNull String name) {
        player.sendPacket(PacketPlayOutScoreboardTeamStorage.unregister(name));
    }

    @Override
    public void updateTeam0(@NonNull String name, String prefix, String suffix, String visibility, String collision, int options) {
        if (player.getVersion().getMinorVersion() < 13) {
            prefix = cutTo(prefix, 16);
            suffix = cutTo(suffix, 16);
        }
        player.sendPacket(PacketPlayOutScoreboardTeamStorage.update(name, prefix, suffix, visibility, collision, options, player.getVersion()));
    }

    @Override
    public void setScore0(@NonNull String objective, @NonNull String playerName, int score) {
        player.sendPacket(PacketPlayOutScoreboardScoreStorage.change(objective, playerName, score));
    }

    @Override
    public void removeScore0(@NonNull String objective, @NonNull String playerName) {
        player.sendPacket(PacketPlayOutScoreboardScoreStorage.remove(objective, playerName));
    }
}
