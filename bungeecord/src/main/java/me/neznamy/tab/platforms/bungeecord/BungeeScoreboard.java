package me.neznamy.tab.platforms.bungeecord;

import lombok.NonNull;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.shared.TabScoreboard;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardScore;
import net.md_5.bungee.protocol.packet.Team;

import java.util.Collection;

/**
 * Scoreboard handler for BungeeCord. Because it does not offer
 * any Scoreboard API and the scoreboard class it has is just a
 * downstream tracker, we need to use packets.
 */
public class BungeeScoreboard extends TabScoreboard {

    public BungeeScoreboard(TabPlayer player) {
        super(player);
    }

    @Override
    public void setDisplaySlot(DisplaySlot slot, @NonNull String objective) {
        player.sendPacket(new ScoreboardDisplay((byte)slot.ordinal(), objective));
    }

    @Override
    public void registerObjective0(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        player.sendPacket(new ScoreboardObjective(objectiveName, jsonOrRaw(title, player.getVersion()), hearts ? ScoreboardObjective.HealthDisplay.HEARTS : ScoreboardObjective.HealthDisplay.INTEGER, (byte) 0));
    }

    @Override
    public void unregisterObjective0(@NonNull String objectiveName) {
        player.sendPacket(new ScoreboardObjective(objectiveName, "", null, (byte) 1));
    }

    @Override
    public void updateObjective0(@NonNull String objectiveName, @NonNull String title, boolean hearts) {
        player.sendPacket(new ScoreboardObjective(objectiveName, jsonOrRaw(title, player.getVersion()), hearts ? ScoreboardObjective.HealthDisplay.HEARTS : ScoreboardObjective.HealthDisplay.INTEGER, (byte) 2));
    }

    @Override
    public void registerTeam0(@NonNull String name, String prefix, String suffix, String visibility, String collision, Collection<String> players, int options) {
        int color = 0;
        if (player.getVersion().getMinorVersion() >= 13) {
            color = EnumChatFormat.lastColorsOf(prefix).ordinal();
        }
        player.sendPacket(new Team(name, (byte) 0, jsonOrRaw(name, player.getVersion()),
                jsonOrRaw(prefix, player.getVersion()), jsonOrRaw(suffix, player.getVersion()),
                visibility, collision, color, (byte)options, players.toArray(new String[0])));
    }

    @Override
    public void unregisterTeam0(@NonNull String name) {
        player.sendPacket(new Team(name));
    }

    @Override
    public void updateTeam0(@NonNull String name, String prefix, String suffix, String visibility, String collision, int options) {
        int color = 0;
        if (player.getVersion().getMinorVersion() >= 13) {
            color = EnumChatFormat.lastColorsOf(prefix).ordinal();
        }
        player.sendPacket(new Team(name, (byte) 2, jsonOrRaw(name, player.getVersion()),
                jsonOrRaw(prefix, player.getVersion()), jsonOrRaw(suffix, player.getVersion()),
                visibility, collision, color, (byte)options, null));
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
    private String jsonOrRaw(String text, ProtocolVersion clientVersion) {
        if (text == null) return null;
        if (clientVersion.getMinorVersion() >= 13) {
            return IChatBaseComponent.optimizedComponent(text).toString(clientVersion);
        } else {
            return text;
        }
    }

    @Override
    public void setScore0(@NonNull String objective, @NonNull String playerName, int score) {
        player.sendPacket(new ScoreboardScore(playerName, (byte) 0, objective, score));
    }

    @Override
    public void removeScore0(@NonNull String objective, @NonNull String playerName) {
        player.sendPacket(new ScoreboardScore(playerName, (byte) 1, objective, 0));
    }
}
