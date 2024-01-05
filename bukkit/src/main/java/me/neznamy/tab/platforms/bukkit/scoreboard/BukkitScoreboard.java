package me.neznamy.tab.platforms.bukkit.scoreboard;

import lombok.Getter;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

/**
 * Scoreboard implementation using Bukkit API (1.5.2+). It has several issues:
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
 *      making per-player view of teams, especially sidebar not working.
 */
// Throw the NPE if something is not as expected, parent class should ensure it anyway
@SuppressWarnings({"deprecation", "DataFlowIssue"})
public class BukkitScoreboard extends Scoreboard<BukkitTabPlayer> {

    /** Version in which team colors were added */
    private static final int TEAM_COLOR_VERSION = 13;

    /** Version in which render type was added into the API */
    private static final int RENDER_TYPE_VERSION = 14;

    /** Pointless limit on 1.13+ servers introduced by Bukkit to limit us */
    private static final int PREFIX_SUFFIX_LIMIT_MODERN = 64;

    /** Pointless limit on 1.13+ servers introduced by Bukkit to limit us */
    private static final int TITLE_LIMIT_MODERN = 128;

    /** Flag tracking whether this implementation is available for use */
    @Getter
    private static final boolean available;

    /** Array of display slots, because the Bukkit order does not match network ordinals */
    private static org.bukkit.scoreboard.DisplaySlot[] slots;

    /** Server's minor version */
    private final int serverMinorVersion = BukkitReflection.getMinorVersion();

    protected org.bukkit.scoreboard.Scoreboard scoreboard;

    static {
        available = ReflectionUtils.methodExists(Bukkit.class, "getScoreboardManager");
        if (available) {
            slots = new org.bukkit.scoreboard.DisplaySlot[]{
                    org.bukkit.scoreboard.DisplaySlot.PLAYER_LIST,
                    org.bukkit.scoreboard.DisplaySlot.SIDEBAR,
                    org.bukkit.scoreboard.DisplaySlot.BELOW_NAME
            };
        }
    }

    /**
     * Constructs new instance with given player and puts them into new scoreboard.
     *
     * @param   player
     *          Player this scoreboard will belong to
     */
    public BukkitScoreboard(@NotNull BukkitTabPlayer player) {
        super(player);

        // Put player into a different scoreboard for per-player view
        runSync(() -> {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            player.getPlayer().setScoreboard(scoreboard);
        });
    }

    @Override
    public void setDisplaySlot0(int slot, @NotNull String objective) {
        checkPlayerScoreboard();
        scoreboard.getObjective(objective).setDisplaySlot(slots[slot]);
    }

    @Override
    public void setScore0(@NotNull String objective, @NotNull String scoreHolder, int score,
                          @Nullable IChatBaseComponent displayName, @Nullable IChatBaseComponent numberFormat) {
        checkPlayerScoreboard();
        if (serverMinorVersion >= 7 && TAB.getInstance().getServerVersion().getNetworkId() >= ProtocolVersion.V1_7_8.getNetworkId()) {
            scoreboard.getObjective(objective).getScore(scoreHolder).setScore(score);
        } else {
            scoreboard.getObjective(objective).getScore(Bukkit.getOfflinePlayer(scoreHolder)).setScore(score);
        }
    }

    @Override
    public void removeScore0(@NotNull String objective, @NotNull String scoreHolder) {
        checkPlayerScoreboard();
        if (serverMinorVersion >= 7 && TAB.getInstance().getServerVersion().getNetworkId() >= ProtocolVersion.V1_7_8.getNetworkId()) {
            scoreboard.resetScores(scoreHolder);
        } else {
            scoreboard.resetScores(Bukkit.getOfflinePlayer(scoreHolder));
        }
    }

    @Override
    public void registerObjective0(@NotNull String objectiveName, @NotNull String title, int display,
                                   @Nullable IChatBaseComponent numberFormat) {
        checkPlayerScoreboard();
        newObjective(objectiveName, "dummy", title, display);
    }

    @Override
    public void unregisterObjective0(@NotNull String objectiveName) {
        checkPlayerScoreboard();
        scoreboard.getObjective(objectiveName).unregister();
    }

    @Override
    public void updateObjective0(@NotNull String objectiveName, @NotNull String title, int display,
                                 @Nullable IChatBaseComponent numberFormat) {
        checkPlayerScoreboard();
        Objective obj = scoreboard.getObjective(objectiveName);
        setDisplayName(obj, title);
        if (serverMinorVersion >= RENDER_TYPE_VERSION) obj.setRenderType(RenderType.values()[display]);
    }

    @Override
    public void registerTeam0(@NotNull String name, @NotNull String prefix, @NotNull String suffix,
                              @NotNull NameVisibility visibility, @NotNull CollisionRule collision,
                              @NotNull Collection<String> players, int options, @NotNull EnumChatFormat color) {
        checkPlayerScoreboard();
        Team team = scoreboard.registerNewTeam(name);
        setPrefix(team, prefix);
        setSuffix(team, suffix);
        if (serverMinorVersion >= 8)
            team.setNameTagVisibility(NameTagVisibility.valueOf(visibility.name()));
        if (serverMinorVersion >= 9)
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.values()[collision.ordinal()]);
        if (serverMinorVersion >= TEAM_COLOR_VERSION)
            team.setColor(ChatColor.valueOf(color.name()));
        if (serverMinorVersion >= 7 && TAB.getInstance().getServerVersion().getNetworkId() >= ProtocolVersion.V1_7_8.getNetworkId()) {
            players.forEach(team::addEntry);
        } else {
            players.forEach(p -> team.addPlayer(Bukkit.getOfflinePlayer(p)));
        }
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
                            @NotNull NameVisibility visibility, @NotNull CollisionRule collision,
                            int options, @NotNull EnumChatFormat color) {
        checkPlayerScoreboard();
        Team team = scoreboard.getTeam(name);
        setPrefix(team, prefix);
        setSuffix(team, suffix);
        if (serverMinorVersion >= 8)
            team.setNameTagVisibility(NameTagVisibility.valueOf(visibility.name()));
        if (serverMinorVersion >= 9)
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.values()[collision.ordinal()]);
        if (serverMinorVersion >= TEAM_COLOR_VERSION)
            team.setColor(ChatColor.valueOf(color.name()));
        team.setAllowFriendlyFire((options & 0x01) != 0);
        team.setCanSeeFriendlyInvisibles((options & 0x02) != 0);
    }

    /**
     * Creates new Scoreboard objective with given parameters.
     *
     * @param   objectiveName
     *          Objective name, max 16 characters
     * @param   criteria
     *          Objective criteria
     * @param   title
     *          Objective title
     * @param   display
     *          Score display type
     */
    public void newObjective(String objectiveName, String criteria, String title, int display) {
        if (serverMinorVersion >= RENDER_TYPE_VERSION) {
            scoreboard.registerNewObjective(
                    objectiveName,
                    criteria,
                    transform(title, TITLE_LIMIT_MODERN, Limitations.SCOREBOARD_TITLE_PRE_1_13),
                    RenderType.values()[display]
            );
        } else {
            setDisplayName(scoreboard.registerNewObjective(objectiveName, display == HealthDisplay.HEARTS ? "health" : "dummy"), title);
        }
    }

    /**
     * Changes objective's display name.
     *
     * @param   objective
     *          Objective to change display name of
     * @param   displayName
     *          New display name
     */
    public void setDisplayName(@NotNull Objective objective, @NotNull String displayName) {
        objective.setDisplayName(transform(displayName, TITLE_LIMIT_MODERN, Limitations.SCOREBOARD_TITLE_PRE_1_13));
    }

    /**
     * Changes prefix of a team.
     *
     * @param   team
     *          Team to change prefix of
     * @param   prefix
     *          Prefix to change to
     */
    public void setPrefix(@NotNull Team team, @NotNull String prefix) {
        team.setPrefix(transform(prefix, PREFIX_SUFFIX_LIMIT_MODERN, Limitations.TEAM_PREFIX_SUFFIX_PRE_1_13));
    }

    /**
     * Changes suffix of a team.
     *
     * @param   team
     *          Team to change suffix of
     * @param   suffix
     *          Suffix to change to
     */
    public void setSuffix(@NotNull Team team, @NotNull String suffix) {
        team.setSuffix(transform(suffix, PREFIX_SUFFIX_LIMIT_MODERN, Limitations.TEAM_PREFIX_SUFFIX_PRE_1_13));
    }

    /**
     * Transforms text into format using bukkit RGB syntax if server version is 1.16+
     *
     * @param   text
     *          Text to transform
     * @param   maxLengthModern
     *          Maximum text length defined by bukkit API on 1.13+
     * @param   maxLengthLegacy
     *          Maximum text length defined by bukkit API on 1.12-
     * @return  Converted text
     */
    @NotNull
    private String transform(@NotNull String text, int maxLengthModern, int maxLengthLegacy) {
        String transformed = BukkitUtils.toBukkitFormat(IChatBaseComponent.optimizedComponent(text), player.getVersion().supportsRGB());
        if (TAB.getInstance().getServerVersion().supportsRGB() && maxLengthModern < TITLE_LIMIT_MODERN) { // Scoreboard title is not stripping colors
            while (ChatColor.stripColor(transformed).length() > maxLengthModern)
                transformed = transformed.substring(0, transformed.length()-1);
        } else if (serverMinorVersion >= 13) {
            if (transformed.length() > maxLengthModern)
                transformed = transformed.substring(0, maxLengthModern);
        } else {
            if (transformed.length() > maxLengthLegacy)
                transformed = transformed.substring(0, maxLengthLegacy);
        }
        return transformed;
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
            TAB.getInstance().getErrorManager().printError("Player " + player.getName() + " was in a different scoreboard " +
                    "than expected. This means another plugin changed player's scoreboard.");
            runSync(() -> player.getPlayer().setScoreboard(scoreboard));
        }
    }

    /**
     * Runs the task if in main thread. If not, submits the task and waits for the result.
     *
     * @param   task
     *          Task to run
     */
    @SneakyThrows
    private void runSync(@NotNull Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            // Server thread (plugin reload)
            task.run();
        } else {
            // Plugin thread (player join)
            CountDownLatch c = new CountDownLatch(1);
            player.getPlatform().runSync(player.getPlayer(), () -> {
                task.run();
                c.countDown();
            });
            c.await();
        }
    }
}
