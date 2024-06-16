package me.neznamy.tab.shared.features.redis;

import lombok.*;
import me.neznamy.tab.shared.TabConstants.Permission;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Class holding information about a player connected to another proxy.
 */
@Getter
@Setter
public class RedisPlayer {

    /** Tablist UUID of the player */
    @NotNull
    private final UUID uniqueId;

    /** Player's real name */
    @NotNull
    private final String name;

    /** Player's name as seen in game profile */
    @NotNull
    private String nickname;

    /** Name of server the player is connected to */
    @NotNull
    public String server;

    /** Whether player is vanished or not */
    private boolean vanished;

    /** Whether player is staff or not */
    private final boolean staff;

    /** Belowname number for 1.20.2- */
    private int belowNameNumber;

    /** Belowname NumberFormat for 1.20.3+ */
    private TabComponent belowNameFancy;

    /** Player's skin for global playerlist */
    private TabList.Skin skin;

    /** Tablist display name */
    private TabComponent tabFormat;

    /** Scoreboard team name */
    private String teamName;

    /** Nametag prefix */
    private String tagPrefix;

    /** Nametag suffix */
    private String tagSuffix;

    /** Nametag visibility rule */
    private Scoreboard.NameVisibility nameVisibility;

    /** Playerlist objective number for 1.20.2- */
    private int playerlistNumber;

    /** Playerlist objective NumberFormat for 1.20.3+ */
    private TabComponent playerlistFancy;

    /**
     * Constructs new instance with given parameters.
     *
     * @param   uniqueId
     *          Player's tablist UUID
     * @param   name
     *          Player's real name
     * @param   nickname
     *          Player's nickname in game profile
     * @param   server
     *          Player's server
     * @param   vanished
     *          Whether player is vanished or not
     * @param   staff
     *          Whether player has {@link Permission#STAFF} permission or not
     */
    public RedisPlayer(@NotNull UUID uniqueId, @NotNull String name, @NotNull String nickname, @NotNull String server, boolean vanished, boolean staff) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.nickname = nickname;
        this.server = server;
        this.vanished = vanished;
        this.staff = staff;
    }
}