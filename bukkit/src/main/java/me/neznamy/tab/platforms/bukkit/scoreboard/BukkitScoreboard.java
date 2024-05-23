package me.neznamy.tab.platforms.bukkit.scoreboard;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Team.OptionStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
public class BukkitScoreboard extends SafeScoreboard<BukkitTabPlayer> {

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
    public BukkitScoreboard(@NonNull BukkitTabPlayer player) {
        super(player);

        // Put player into a different scoreboard for per-player view
        runSync(() -> {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            player.getPlayer().setScoreboard(scoreboard);
        });
    }

    @Override
    public void registerObjective(@NonNull Objective objective) {
        checkPlayerScoreboard();
        org.bukkit.scoreboard. Objective obj = newObjective(objective.getName(), "dummy", objective.getTitle(), objective.getHealthDisplay());
        setObjectiveNumberFormat(obj, objective.getNumberFormat());
        obj.setDisplaySlot(slots[objective.getDisplaySlot().ordinal()]);
    }

    @Override
    public void unregisterObjective(@NonNull Objective objective) {
        checkPlayerScoreboard();
        scoreboard.getObjective(objective.getName()).unregister();
    }

    @Override
    public void updateObjective(@NonNull Objective objective) {
        checkPlayerScoreboard();
        org.bukkit.scoreboard.Objective obj = scoreboard.getObjective(objective.getName());
        setDisplayName(obj, objective.getTitle());
        if (serverMinorVersion >= RENDER_TYPE_VERSION) obj.setRenderType(RenderType.values()[objective.getHealthDisplay().ordinal()]);
        setObjectiveNumberFormat(obj, objective.getNumberFormat());
    }

    @Override
    public void setScore(@NonNull Score score) {
        checkPlayerScoreboard();
        org.bukkit.scoreboard.Score s;
        if (serverMinorVersion >= 7 && player.getPlatform().getServerVersion().getNetworkId() >= ProtocolVersion.V1_7_8.getNetworkId()) {
            s = scoreboard.getObjective(score.getObjective()).getScore(score.getHolder());
        } else {
            s = scoreboard.getObjective(score.getObjective()).getScore(Bukkit.getOfflinePlayer(score.getHolder()));
        }
        s.setScore(score.getValue());
        setScoreDisplayName(s, score.getDisplayName());
        setScoreNumberFormat(s, score.getNumberFormat());
    }

    @Override
    public void removeScore(@NonNull Score score) {
        checkPlayerScoreboard();
        if (serverMinorVersion >= 7 && player.getPlatform().getServerVersion().getNetworkId() >= ProtocolVersion.V1_7_8.getNetworkId()) {
            scoreboard.resetScores(score.getHolder());
        } else {
            scoreboard.resetScores(Bukkit.getOfflinePlayer(score.getHolder()));
        }
    }

    @Override
    public void registerTeam(@NonNull Team team) {
        checkPlayerScoreboard();
        org.bukkit.scoreboard.Team t = scoreboard.registerNewTeam(team.getName());
        setPrefix(t, team.getPrefix());
        setSuffix(t, team.getSuffix());
        if (serverMinorVersion >= 8)
            t.setNameTagVisibility(NameTagVisibility.valueOf(team.getVisibility().name()));
        if (serverMinorVersion >= 9)
            t.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, OptionStatus.values()[team.getCollision().ordinal()]);
        if (serverMinorVersion >= TEAM_COLOR_VERSION)
            t.setColor(ChatColor.valueOf(team.getColor().name()));
        if (serverMinorVersion >= 7 && player.getPlatform().getServerVersion().getNetworkId() >= ProtocolVersion.V1_7_8.getNetworkId()) {
            team.getPlayers().forEach(t::addEntry);
        } else {
            team.getPlayers().forEach(p -> t.addPlayer(Bukkit.getOfflinePlayer(p)));
        }
        t.setAllowFriendlyFire((team.getOptions() & 0x01) != 0);
        t.setCanSeeFriendlyInvisibles((team.getOptions() & 0x02) != 0);
    }

    @Override
    public void unregisterTeam(@NonNull Team team) {
        checkPlayerScoreboard();
        scoreboard.getTeam(team.getName()).unregister();
    }

    @Override
    public void updateTeam(@NonNull Team team) {
        checkPlayerScoreboard();
        org.bukkit.scoreboard.Team t = scoreboard.getTeam(team.getName());
        setPrefix(t, team.getPrefix());
        setSuffix(t, team.getSuffix());
        if (serverMinorVersion >= 8)
            t.setNameTagVisibility(NameTagVisibility.valueOf(team.getVisibility().name()));
        if (serverMinorVersion >= 9)
            t.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, OptionStatus.values()[team.getCollision().ordinal()]);
        if (serverMinorVersion >= TEAM_COLOR_VERSION)
            t.setColor(ChatColor.valueOf(team.getColor().name()));
        t.setAllowFriendlyFire((team.getOptions() & 0x01) != 0);
        t.setCanSeeFriendlyInvisibles((team.getOptions() & 0x02) != 0);
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
     * @return  Created objective
     */
    public org.bukkit.scoreboard.Objective newObjective(@NonNull String objectiveName, @NonNull String criteria, @NonNull String title, @NonNull HealthDisplay display) {
        if (serverMinorVersion >= RENDER_TYPE_VERSION) {
            return scoreboard.registerNewObjective(
                    objectiveName,
                    criteria,
                    transform(title, TITLE_LIMIT_MODERN, Limitations.SCOREBOARD_TITLE_PRE_1_13),
                    RenderType.values()[display.ordinal()]
            );
        } else {
            org.bukkit.scoreboard.Objective obj = scoreboard.registerNewObjective(objectiveName, display == HealthDisplay.HEARTS ? "health" : "dummy");
            setDisplayName(obj, title);
            return obj;
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
    public void setDisplayName(@NonNull org.bukkit.scoreboard.Objective objective, @NonNull String displayName) {
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
    public void setPrefix(@NonNull org.bukkit.scoreboard.Team team, @NonNull String prefix) {
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
    public void setSuffix(@NonNull org.bukkit.scoreboard.Team team, @NonNull String suffix) {
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
    @NonNull
    private String transform(@NonNull String text, int maxLengthModern, int maxLengthLegacy) {
        String transformed = player.getPlatform().toBukkitFormat(TabComponent.optimized(text), player.getVersion().supportsRGB());
        if (player.getPlatform().getServerVersion().supportsRGB() && maxLengthModern < TITLE_LIMIT_MODERN) { // Scoreboard title is not stripping colors
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
     * Sets display name of score to specified value.
     *
     * @param   s
     *          Score to set display name of
     * @param   displayName
     *          Display name to use
     */
    public void setScoreDisplayName(@NotNull org.bukkit.scoreboard.Score s, @Nullable TabComponent displayName) {
        // Not available on Bukkit
    }

    /**
     * Sets number format of score to specified value.
     *
     * @param   s
     *          Score to set number format of
     * @param   numberFormat
     *          Number format to use
     */
    public void setScoreNumberFormat(@NotNull org.bukkit.scoreboard.Score s, @Nullable TabComponent numberFormat) {
        // Not available on Bukkit
    }

    /**
     * Sets default number format of object to specified value.
     *
     * @param   o
     *          Objective to set default number format of
     * @param   numberFormat
     *          Number format to use
     */
    public void setObjectiveNumberFormat(@NotNull org.bukkit.scoreboard.Objective o, @Nullable TabComponent numberFormat) {
        // Not available on Bukkit
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
    private void runSync(@NonNull Runnable task) {
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