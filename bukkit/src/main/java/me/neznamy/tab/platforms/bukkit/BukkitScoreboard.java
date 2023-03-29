package me.neznamy.tab.platforms.bukkit;

import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutScoreboardDisplayObjectiveStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutScoreboardObjectiveStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutScoreboardScoreStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutScoreboardTeamStorage;
import me.neznamy.tab.shared.TabScoreboard;

import java.util.Collection;

/**
 * Scoreboard implementation for Bukkit, which uses packets
 * to send scoreboards. While md_5 keeps saying that you don't
 * need packets for scoreboards in every single spigot update,
 * this is not true. It may seem that way at first, but after
 * investigating, fatal problems are found:<p>
 * #1 - Limitations on legacy versions are forced in the API.
 *      While this may not seem like a problem, it enforces those
 *      limits even for 1.13+ players (if using ViaVersion).<p>
 * #2 - Modern versions no longer have any limits, but md_5
 *      decided to add some random limits for absolutely no reason
 *      at all. Scoreboard title received a random 128 characters
 *      limit including color codes. Together with the almighty bukkit
 *      RGB format using 14 characters for 1 color code, this makes
 *      gradients just impossible to use. Team prefix/suffix also
 *      received a 64 characters limit (excluding color codes at least),
 *      however that might not be enough for displaying a line of text
 *      in sidebar, which would require splitting the text into prefix
 *      and suffix, which is just begging for bugs to be introduced.<p>
 * #3 - Other plugins can decide to put players into their own
 *      scoreboard, automatically destroying all visuals made by the
 *      plugin. They might also put all players into the same scoreboard,
 *      making per-player view of teams, especially sidebar not working.<p>
 * #4 - API made scoreboards get saved into the world folder and then
 *      resent on join with everything that was ever registered to it,
 *      which is very poor performance-wise, especially since it does
 *      that in the main server thread. It might also send conflicting
 *      information, such as putting a player into team they were on
 *      previously, but are not supposed to be anymore. Removing the plugin
 *      would cause all visuals to still be present on the server,
 *      requiring owners to properly clear scoreboards, which is not something
 *      everyone knows how to do.
 */
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
        player.sendPacket(PacketPlayOutScoreboardObjectiveStorage.buildSilent(0, objectiveName, title, hearts, player.getVersion()));
    }

    @Override
    public void unregisterObjective0(@NonNull String objectiveName) {
        player.sendPacket(PacketPlayOutScoreboardObjectiveStorage.buildSilent(1, objectiveName, "", false, player.getVersion()));
    }

    @Override
    public void updateObjective0(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        player.sendPacket(PacketPlayOutScoreboardObjectiveStorage.buildSilent(2, objectiveName, title, hearts, player.getVersion()));
    }

    @Override
    public void registerTeam0(@NonNull String name, String prefix, String suffix, String visibility, String collision, Collection<String> players, int options) {
        player.sendPacket(PacketPlayOutScoreboardTeamStorage.register(name, prefix, suffix, visibility, collision, players, options, player.getVersion()));
    }

    @Override
    public void unregisterTeam0(@NonNull String name) {
        player.sendPacket(PacketPlayOutScoreboardTeamStorage.unregister(name));
    }

    @Override
    public void updateTeam0(@NonNull String name, String prefix, String suffix, String visibility, String collision, int options) {
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
