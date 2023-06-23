package me.neznamy.tab.platforms.bukkit.scoreboard;

import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Scoreboard handler using Paper API, which got added
 * in early 1.16.5 builds. Unlike the Bukkit API, the new
 * methods using adventure components do not have any
 * pointless artificial limits added in.
 * <p>
 * However, it still inherits the issue with compatibility
 * with other plugins that create scoreboards.
 * <p>
 * Currently, this class is never used. In the future, it might
 * be used as a fallback solution for unsupported versions if NMS
 * changes and fields fail to load. Or as a complete replacement
 * if compatibility with other plugins turns out not to be a problem.
 */
// Throw the NPE if something is not as expected, parent class should ensure it anyway
// Use Bukkit setColor, the adventure one does not support magic codes while they are permitted
@SuppressWarnings({"ConstantConditions", "deprecation"})
public class PaperScoreboard extends Scoreboard<BukkitTabPlayer> {

    private final org.bukkit.scoreboard.Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

    public PaperScoreboard(BukkitTabPlayer player) {
        super(player);

        // Put player into a different scoreboard for per-player view
        player.getPlayer().setScoreboard(scoreboard);
    }

    @Override
    public void setDisplaySlot(@NotNull DisplaySlot slot, @NotNull String objective) {
        checkPlayerScoreboard();
        scoreboard.getObjective(objective).setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.valueOf(slot.name()));
    }

    @Override
    public void setScore0(@NotNull String objective, @NotNull String playerName, int score) {
        checkPlayerScoreboard();
        scoreboard.getObjective(objective).getScore(playerName).setScore(score);
    }

    @Override
    public void removeScore0(@NotNull String objective, @NotNull String playerName) {
        checkPlayerScoreboard();
        scoreboard.resetScores(playerName);
    }

    @Override
    public void registerObjective0(@NotNull String objectiveName, @NotNull String title, boolean hearts) {
        checkPlayerScoreboard();
        scoreboard.registerNewObjective(objectiveName, "dummy", toAdventure(title), hearts ? RenderType.HEARTS : RenderType.INTEGER);
    }

    @Override
    public void unregisterObjective0(@NotNull String objectiveName) {
        checkPlayerScoreboard();
        scoreboard.getObjective(objectiveName).unregister();
    }

    @Override
    public void updateObjective0(@NotNull String objectiveName, @NotNull String title, boolean hearts) {
        checkPlayerScoreboard();
        Objective obj = scoreboard.getObjective(objectiveName);
        obj.setRenderType(hearts ? RenderType.HEARTS : RenderType.INTEGER);
        obj.displayName(toAdventure(title));
    }

    @Override
    public void registerTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                              @NotNull NameVisibility visibility, @NotNull CollisionRule collision,
                              @NotNull Collection<String> players, int options) {
        checkPlayerScoreboard();
        Team team = scoreboard.registerNewTeam(name);
        team.prefix(toAdventure(prefix));
        team.suffix(toAdventure(suffix));
        team.setColor(ChatColor.valueOf(EnumChatFormat.lastColorsOf(prefix).name()));
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.values()[visibility.ordinal()]);
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.values()[collision.ordinal()]);
        players.forEach(team::addEntry);
        team.setAllowFriendlyFire((options & 0x01) != 0);
        team.setCanSeeFriendlyInvisibles((options & 0x02) != 0);
    }

    @Override
    public void unregisterTeam0(@NotNull String name) {
        checkPlayerScoreboard();
        scoreboard.getTeam(name).unregister();
    }

    @Override
    public void updateTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                            @NotNull NameVisibility visibility, @NotNull CollisionRule collision, int options) {
        checkPlayerScoreboard();
        Team team = scoreboard.getTeam(name);
        team.prefix(toAdventure(prefix));
        team.suffix(toAdventure(suffix));
        team.setColor(ChatColor.valueOf(EnumChatFormat.lastColorsOf(prefix).name()));
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.values()[visibility.ordinal()]);
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.values()[collision.ordinal()]);
        team.setAllowFriendlyFire((options & 0x01) != 0);
        team.setCanSeeFriendlyInvisibles((options & 0x02) != 0);
    }

    /**
     * Converts raw text into adventure component
     *
     * @param   text
     *          Text to convert
     * @return  Converted component
     */
    private @NotNull Component toAdventure(@NotNull String text) {
        return IChatBaseComponent.optimizedComponent(text).toAdventureComponent(player.getVersion());
    }

    /**
     * Makes sure player is in the correct scoreboard.
     * If not, puts the player into correct scoreboard and prints
     * a warning.
     * The only possible reason for this to happen is another plugin
     * putting a player into a different scoreboard.
     * Sadly there is no efficient solution to this, which md_5 fails
     * to understand and keeps saying you don't need packets for scoreboards.
     */
    private void checkPlayerScoreboard() {
        if (player.getPlayer().getScoreboard() != scoreboard) {
            player.getPlayer().setScoreboard(scoreboard);
            TAB.getInstance().getErrorManager().printError("Player " + player.getName() + " was in a different scoreboard " +
                    "than expected. This means another plugin changed player's scoreboard.");
        }
    }
}
