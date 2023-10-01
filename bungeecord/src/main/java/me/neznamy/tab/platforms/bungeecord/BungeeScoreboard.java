package me.neznamy.tab.platforms.bungeecord;

import lombok.SneakyThrows;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardScore;
import net.md_5.bungee.protocol.packet.Team;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;

/**
 * Scoreboard handler for BungeeCord. Because it does not offer
 * any Scoreboard API and the scoreboard class it has is just a
 * downstream tracker, we need to use packets.
 */
public class BungeeScoreboard extends Scoreboard<BungeeTabPlayer> {

    /** Constructor to support both <1.20.2 and 1.20.2+ builds */
    @SuppressWarnings("unchecked")
    private static final Constructor<ScoreboardDisplay> newScoreboardDisplay = (Constructor<ScoreboardDisplay>)
            Arrays.stream(ScoreboardDisplay.class.getConstructors()).filter(c -> c.getParameterCount() == 2).findAny().orElse(null);

    public BungeeScoreboard(@NotNull BungeeTabPlayer player) {
        super(player);
    }

    @Override
    @SneakyThrows
    public void setDisplaySlot(@NotNull DisplaySlot slot, @NotNull String objective) {
        player.sendPacket(newScoreboardDisplay.newInstance((byte)slot.ordinal(), objective));
    }

    @Override
    public void registerObjective0(@NotNull String objectiveName, @NotNull String title, @NotNull HealthDisplay display) {
        player.sendPacket(new ScoreboardObjective(
                objectiveName,
                jsonOrRaw(title),
                ScoreboardObjective.HealthDisplay.valueOf(display.name()),
                (byte) 0
        ));
    }

    @Override
    public void unregisterObjective0(@NotNull String objectiveName) {
        player.sendPacket(new ScoreboardObjective(
                objectiveName,
                "", // Empty string to prevent kick on 1.7
                null,
                (byte) 1
        ));
    }

    @Override
    public void updateObjective0(@NotNull String objectiveName, @NotNull String title, @NotNull HealthDisplay display) {
        player.sendPacket(new ScoreboardObjective(
                objectiveName,
                jsonOrRaw(title),
                ScoreboardObjective.HealthDisplay.valueOf(display.name()),
                (byte) 2
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
        player.sendPacket(new Team(name, (byte) 0, jsonOrRaw(name), jsonOrRaw(prefix), jsonOrRaw(suffix),
                visibility.toString(), collision.toString(), color, (byte)options, players.toArray(new String[0])));
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
        player.sendPacket(new Team(name, (byte) 2, jsonOrRaw(name), jsonOrRaw(prefix),
                jsonOrRaw(suffix), visibility.toString(), collision.toString(), color, (byte)options, null));
    }

    @Override
    public void setScore0(@NotNull String objective, @NotNull String playerName, int score) {
        player.sendPacket(new ScoreboardScore(playerName, (byte) 0, objective, score));
    }

    @Override
    public void removeScore0(@NotNull String objective, @NotNull String playerName) {
        player.sendPacket(new ScoreboardScore(playerName, (byte) 1, objective, 0));
    }

    /**
     * If player's version is 1.13+, creates a component from given text and returns
     * it as a serialized component, which BungeeCord uses.
     * <p>
     * If player's version is 1.12-, the text is returned
     *
     * @param   text
     *          Text to convert
     * @return  serialized component for 1.13+ clients, cut string for 1.12-
     */
    @NotNull
    private String jsonOrRaw(@NotNull String text) {
        if (player.getVersion().getMinorVersion() >= 13) {
            return IChatBaseComponent.optimizedComponent(text).toString(player.getVersion());
        } else {
            return text;
        }
    }
}
