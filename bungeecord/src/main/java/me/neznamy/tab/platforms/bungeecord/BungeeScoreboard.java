package me.neznamy.tab.platforms.bungeecord;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;
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
    public void setDisplaySlot(@NotNull DisplaySlot slot, @NotNull String objective) {
        player.getPlayer().unsafe().sendPacket(new ScoreboardDisplay((byte)slot.ordinal(), objective));
    }

    @Override
    public void registerObjective0(@NotNull String objectiveName, @NotNull String title, boolean hearts) {
        player.getPlayer().unsafe().sendPacket(new ScoreboardObjective(objectiveName, jsonOrRaw(title, player.getVersion()), hearts ? ScoreboardObjective.HealthDisplay.HEARTS : ScoreboardObjective.HealthDisplay.INTEGER, (byte) 0));
    }

    @Override
    public void unregisterObjective0(@NotNull String objectiveName) {
        player.getPlayer().unsafe().sendPacket(new ScoreboardObjective(objectiveName, "", null, (byte) 1)); // Empty string to prevent kick on 1.7
    }

    @Override
    public void updateObjective0(@NotNull String objectiveName, @NotNull String title, boolean hearts) {
        player.getPlayer().unsafe().sendPacket(new ScoreboardObjective(objectiveName, jsonOrRaw(title, player.getVersion()), hearts ? ScoreboardObjective.HealthDisplay.HEARTS : ScoreboardObjective.HealthDisplay.INTEGER, (byte) 2));
    }

    @Override
    public void registerTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix, @NotNull NameVisibility visibility, @NotNull CollisionRule collision, @NotNull Collection<String> players, int options) {
        int color = 0;
        if (player.getVersion().getMinorVersion() >= 13) {
            color = EnumChatFormat.lastColorsOf(prefix).ordinal();
        }
        player.getPlayer().unsafe().sendPacket(new Team(name, (byte) 0, jsonOrRaw(name, player.getVersion()),
                jsonOrRaw(prefix, player.getVersion()), jsonOrRaw(suffix, player.getVersion()),
                visibility.toString(), collision.toString(), color, (byte)options, players.toArray(new String[0])));
    }

    @Override
    public void unregisterTeam0(@NotNull String name) {
        player.getPlayer().unsafe().sendPacket(new Team(name));
    }

    @Override
    public void updateTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix, @NotNull NameVisibility visibility, @NotNull CollisionRule collision, int options) {
        int color = 0;
        if (player.getVersion().getMinorVersion() >= 13) {
            color = EnumChatFormat.lastColorsOf(prefix).ordinal();
        }
        player.getPlayer().unsafe().sendPacket(new Team(name, (byte) 2, jsonOrRaw(name, player.getVersion()),
                jsonOrRaw(prefix, player.getVersion()), jsonOrRaw(suffix, player.getVersion()),
                visibility.toString(), collision.toString(), color, (byte)options, null));
    }

    @Override
    public void setScore0(@NotNull String objective, @NotNull String playerName, int score) {
        player.getPlayer().unsafe().sendPacket(new ScoreboardScore(playerName, (byte) 0, objective, score));
    }

    @Override
    public void removeScore0(@NotNull String objective, @NotNull String playerName) {
        player.getPlayer().unsafe().sendPacket(new ScoreboardScore(playerName, (byte) 1, objective, 0));
    }

    /**
     * If {@code clientVersion} is &gt;= 1.13, creates a component from given text and returns
     * it as a serialized component, which BungeeCord uses.
     * <p>
     * If {@code clientVersion} is &lt; 1.12, the text is returned
     *
     * @param   text
     *          Text to convert
     * @param   clientVersion
     *          Version of player to convert text for
     * @return  serialized component for 1.13+ clients, cut string for 1.12-
     */
    private String jsonOrRaw(@NotNull String text, @NotNull ProtocolVersion clientVersion) {
        if (clientVersion.getMinorVersion() >= 13) {
            return IChatBaseComponent.optimizedComponent(text).toString(clientVersion);
        } else {
            return text;
        }
    }

}
